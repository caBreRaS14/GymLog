package co.fexe.gymlog.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.fexe.gymlog.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

val GRUPOS = listOf("Pecho", "Espalda", "Pierna", "Hombro", "Brazo", "Core")
private const val SEMANA_MS = 7L * 24 * 60 * 60 * 1000

data class UiState(
    val registros: List<Registro> = emptyList(),
    val entrenosSemana: Int = 0,
    val volumenSemana: Double = 0.0,
    val volumenPorGrupo: Map<String, Double> = emptyMap(),
    val diasSemana: Int = 0,
    val seriesSemana: Int = 0,
    val volumenAnterior: Double = 0.0,
    val racha: Int = 0,
    val metaSemanal: Int = 4,
    val error: String? = null
)

class RegistroViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = RegistroRepository(AppDb.get(app).registroDao())
    private val prefs = app.getSharedPreferences("gymlog", Context.MODE_PRIVATE)
    private val error = MutableStateFlow<String?>(null)
    private val meta = MutableStateFlow(prefs.getInt("meta", 4))
    private var guardando = false

    init {
        // Carga los datos de ejemplo una sola vez (la marca queda en SharedPreferences)
        if (!prefs.getBoolean("semilla_v2", false)) {
            viewModelScope.launch {
                repo.reemplazarTodo(datosEjemplo())
                prefs.edit().putBoolean("semilla_v2", true).apply()
            }
        }
    }

    val state: StateFlow<UiState> = combine(repo.registros, error, meta) { lista, err, m ->
        val desde = System.currentTimeMillis() - SEMANA_MS
        val sem = lista.filter { it.fecha >= desde }
        val ant = lista.filter { it.fecha >= desde - SEMANA_MS && it.fecha < desde }
        UiState(
            registros = lista,
            entrenosSemana = sem.size,
            volumenSemana = sem.sumOf { it.volumen },
            volumenPorGrupo = sem.groupBy { it.grupo }.mapValues { e -> e.value.sumOf { it.volumen } },
            seriesSemana = sem.sumOf { it.series },
            volumenAnterior = ant.sumOf { it.volumen },
            diasSemana = sem.map { inicioDia(it.fecha) }.toSet().size,
            racha = racha(lista),
            metaSemanal = m,
            error = err
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    /** Valida y guarda; ignora pulsaciones repetidas mientras se guarda. */
    fun guardar(id: Long, ejercicio: String, grupo: String, seriesTxt: String,
                repsTxt: String, pesoTxt: String, onOk: () -> Unit) {
        val series = seriesTxt.trim().toIntOrNull()
        val reps = repsTxt.trim().toIntOrNull()
        val peso = pesoTxt.replace(',', '.').trim().toDoubleOrNull()
        val msg = when {
            ejercicio.isBlank() -> "Escribe el nombre del ejercicio"
            series == null || series !in 1..20 -> "Las series deben ser un número entre 1 y 20"
            reps == null || reps !in 1..100 -> "Las repeticiones deben ser un número entre 1 y 100"
            peso == null || peso < 0 || peso > 500 -> "El peso debe estar entre 0 y 500 kg (0 si es peso corporal)"
            else -> null
        }
        if (msg != null) { error.value = msg; return }
        if (guardando) return
        guardando = true
        viewModelScope.launch {
            // Al editar se conserva la fecha original del registro
            val fecha = repo.fechaDe(id) ?: System.currentTimeMillis()
            repo.guardar(Registro(id, ejercicio.trim(), grupo, series!!, reps!!, peso!!, fecha))
            error.value = null
            guardando = false
            onOk()
        }
    }

    fun borrar(r: Registro) = viewModelScope.launch { repo.borrar(r) }
    fun limpiarError() { error.value = null }
    fun cambiarMeta(delta: Int) {
        val nueva = (meta.value + delta).coerceIn(1, 7)
        meta.value = nueva
        prefs.edit().putInt("meta", nueva).apply()
    }
}