package co.fexe.gymlog.ui

import co.fexe.gymlog.data.Registro
import java.util.Calendar

/** Medianoche del día al que pertenece [ms]. */
fun inicioDia(ms: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ms
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private fun diaAnterior(dia: Long): Long = Calendar.getInstance().apply {
    timeInMillis = dia; add(Calendar.DAY_OF_YEAR, -1)
}.timeInMillis

/** Días consecutivos con al menos un registro, contando desde hoy (o ayer si hoy aún no entrenó). */
fun racha(lista: List<Registro>, ahora: Long = System.currentTimeMillis()): Int {
    val dias = lista.map { inicioDia(it.fecha) }.toSet()
    var d = inicioDia(ahora)
    if (d !in dias) d = diaAnterior(d)
    var n = 0
    while (d in dias) { n++; d = diaAnterior(d) }
    return n
}

/** 1RM estimado con la fórmula de Epley. Null si no hay peso externo. */
fun unRmEstimado(r: Registro): Double? =
    if (r.pesoKg > 0) r.pesoKg * (1 + r.repeticiones / 30.0) else null