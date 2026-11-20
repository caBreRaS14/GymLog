package co.fexe.gymlog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import co.fexe.gymlog.data.Registro

@Composable
fun InicioScreen(
    s: UiState, vm: RegistroViewModel,
    onEditar: (Registro) -> Unit, onBorrar: (Registro) -> Unit
) {
    LazyColumn(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Cabecera("Tu progreso") }
        item { Tablero(s) }
        item { MetaSemanal(s, vm) }
        if (s.registros.isEmpty()) item { Vacio("Aún no has registrado nada", "Toca «Registrar» y anota tu primer ejercicio.") }
        else {
            item { Etiqueta("ÚLTIMOS REGISTROS", Modifier.padding(top = 8.dp)) }
            items(s.registros.take(3), key = { it.id }) { r ->
                TarjetaRegistro(r, onEditar = { onEditar(r) }, onBorrar = { onBorrar(r) })
            }
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun Tablero(s: UiState) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Etiqueta("CARGA TOTAL DE LA SEMANA")
            Row(verticalAlignment = Alignment.Bottom) {
                Text(kg(s.volumenSemana).removeSuffix(" kg"), style = MaterialTheme.typography.headlineLarge, color = Lima)
                Text(" kg", Modifier.padding(bottom = 8.dp), color = TextoSuave)
            }
            Text(
                "Es la suma de series × repeticiones × peso de todo lo que entrenaste en los últimos 7 días.",
                style = MaterialTheme.typography.bodyMedium, color = TextoSuave
            )
            Spacer(Modifier.height(12.dp))
            Comparacion(s.volumenSemana, s.volumenAnterior)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniDato("Ejercicios", s.entrenosSemana.toString(), Modifier.weight(1f))
                MiniDato("Series", s.seriesSemana.toString(), Modifier.weight(1f))
                MiniDato("Días activos", s.diasSemana.toString(), Modifier.weight(1f))
            }
            if (s.volumenPorGrupo.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                Etiqueta("¿QUÉ MÚSCULOS TRABAJASTE?")
                Spacer(Modifier.height(6.dp))
                val max = s.volumenPorGrupo.values.max().coerceAtLeast(1.0)
                val total = s.volumenPorGrupo.values.sum().coerceAtLeast(1.0)
                s.volumenPorGrupo.entries.sortedByDescending { it.value }.forEach { (g, v) ->
                    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(g, Modifier.width(72.dp), style = MaterialTheme.typography.bodyMedium)
                        LinearProgressIndicator(
                            progress = { (v / max).toFloat() },
                            modifier = Modifier.weight(1f).height(8.dp).clip(CircleShape),
                            color = colorGrupo(g), trackColor = SuperficieAlta
                        )
                        Text("${(v / total * 100).toInt()}%", Modifier.width(48.dp).padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
                    }
                }
            }
        }
    }
}

@Composable
private fun Comparacion(actual: Double, anterior: Double) {
    val (texto, color) = when {
        anterior <= 0 -> "Aún no hay una semana anterior para comparar" to TextoSuave
        else -> {
            val pct = ((actual - anterior) / anterior * 100).toInt()
            when {
                pct > 0 -> "▲ $pct% más que la semana pasada" to Lima
                pct < 0 -> "▼ ${-pct}% menos que la semana pasada" to colorGrupo("Pecho")
                else -> "Igual que la semana pasada" to TextoSuave
            }
        }
    }
    Box(Modifier.clip(RoundedCornerShape(10.dp)).background(SuperficieAlta).padding(horizontal = 12.dp, vertical = 6.dp)) {
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun MiniDato(titulo: String, valor: String, modifier: Modifier) {
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).background(SuperficieAlta).padding(10.dp)
    ) {
        Text(valor, style = MaterialTheme.typography.titleLarge)
        Text(titulo, style = MaterialTheme.typography.bodySmall, color = TextoSuave, maxLines = 1)
    }
}
@Composable
private fun MetaSemanal(s: UiState, vm: RegistroViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, null, tint = Lima)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (s.racha == 1) "Racha: 1 día" else "Racha: ${s.racha} días",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(16.dp))
            Etiqueta("META SEMANAL")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${s.diasSemana} de ${s.metaSemanal} días entrenados", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (s.diasSemana.toFloat() / s.metaSemanal).coerceAtMost(1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = Lima, trackColor = SuperficieAlta
                    )
                    if (s.diasSemana >= s.metaSemanal)
                        Text("¡Meta cumplida!", Modifier.padding(top = 6.dp), color = Lima)
                }
                IconButton(onClick = { vm.cambiarMeta(-1) }) { Icon(Icons.Default.Remove, "Bajar la meta semanal") }
                IconButton(onClick = { vm.cambiarMeta(1) }) { Icon(Icons.Default.Add, "Subir la meta semanal") }
            }
        }
    }
}