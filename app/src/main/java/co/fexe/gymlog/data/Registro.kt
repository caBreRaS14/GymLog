package co.fexe.gymlog.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "registros")
data class Registro(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ejercicio: String,
    val grupo: String,
    val series: Int,
    val repeticiones: Int,
    val pesoKg: Double,
    val fecha: Long
) {
    val volumen: Double get() = series * repeticiones * pesoKg
}

@Dao
interface RegistroDao {
    @Query("SELECT * FROM registros ORDER BY fecha DESC, id DESC")
    fun todos(): Flow<List<Registro>>
    @Query("SELECT fecha FROM registros WHERE id = :id")
    suspend fun fechaDe(id: Long): Long?
    @Query("DELETE FROM registros") suspend fun borrarTodo()
    @Insert suspend fun insertar(r: Registro)
    @Update suspend fun actualizar(r: Registro)
    @Delete suspend fun eliminar(r: Registro)
}

@Database(entities = [Registro::class], version = 2, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun registroDao(): RegistroDao
    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(ctx: android.content.Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "gymlog.db")
                .fallbackToDestructiveMigration()
                .build().also { inst = it }
        }
    }
}

/**
 * Historial de ejemplo: 8 semanas de entrenamiento con rutina Push / Pull / Legs
 * (5 días por semana, 2 de descanso) y progresión gradual de carga.
 */
fun datosEjemplo(): List<Registro> {
    class Ej(val nombre: String, val grupo: String, val series: Int, val reps: Int, val peso: Double)

    val push = listOf(
        Ej("Press banca", "Pecho", 4, 10, 60.0),
        Ej("Press inclinado con mancuernas", "Pecho", 3, 10, 22.0),
        Ej("Press militar", "Hombro", 3, 10, 35.0),
        Ej("Elevaciones laterales", "Hombro", 3, 12, 8.0),
        Ej("Extensión de tríceps", "Brazo", 3, 12, 25.0)
    )
    val pull = listOf(
        Ej("Dominadas", "Espalda", 4, 10, 0.0),
        Ej("Remo con barra", "Espalda", 4, 10, 50.0),
        Ej("Peso muerto", "Espalda", 3, 6, 100.0),
        Ej("Curl de bíceps", "Brazo", 3, 12, 16.0),
        Ej("Face pull", "Hombro", 3, 15, 20.0)
    )
    val legs = listOf(
        Ej("Sentadilla", "Pierna", 4, 8, 80.0),
        Ej("Prensa de pierna", "Pierna", 3, 12, 120.0),
        Ej("Peso muerto rumano", "Pierna", 3, 10, 60.0),
        Ej("Curl femoral", "Pierna", 3, 12, 35.0),
        Ej("Plancha con peso", "Core", 3, 15, 10.0),
        Ej("Elevación de piernas", "Core", 3, 15, 0.0)
    )
    val rutina = listOf(push to 18, pull to 19, legs to 7) // (ejercicios, hora del día)

    val dia = 24L * 60 * 60 * 1000
    val hoy = System.currentTimeMillis()
    val inicioHoy = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

    val lista = mutableListOf<Registro>()
    var turno = 0
    for (d in 55 downTo 0) {                       // d = días atrás
        if (d % 7 == 3 || d % 7 == 6) continue     // días de descanso
        val (ejercicios, hora) = rutina[turno % 3]
        turno++
        val semanasAtras = d / 7
        ejercicios.forEach { e ->
            val peso = if (e.peso == 0.0) 0.0 else {
                val paso = if (e.peso >= 30) 2.5 else 1.0
                Math.round(e.peso * (1 - 0.03 * semanasAtras) / paso) * paso
            }
            // Ejercicios con peso corporal progresan en repeticiones
            val reps = if (e.peso == 0.0) (e.reps - semanasAtras).coerceAtLeast(4) else e.reps
            val fecha = minOf(inicioHoy - d * dia + hora * 3_600_000L, hoy - 60_000L)
            lista += Registro(0, e.nombre, e.grupo, e.series, reps, peso, fecha)
        }
    }
    return lista
}