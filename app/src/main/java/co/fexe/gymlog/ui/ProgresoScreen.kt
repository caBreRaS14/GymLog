package co.fexe.gymlog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.fexe.gymlog.data.Registro

@Composable
fun ProgresoScreen(s: UiState, onAbrir: (String) -> Unit) {
    // Agrupa por nombre sin distinguir mayúsculas
    val ejercicios = s.registros.groupBy { it.ejercicio.trim().lowercase() }
        .map { (_, regs) -> regs.maxBy { it.fecha }.ejercicio to regs }
        .sortedByDescending { it.second.size }
    LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Cabecera("Progreso", "Toca un ejercicio para ver su evolución") }
        if (ejercicios.isEmpty()) item { Vacio("Sin datos todavía", "Registra ejercicios para ver tu progreso.") }
        items(ejercicios, key = { it.first.lowercase() }) { (nombre, regs) ->
            val pr = regs.maxOf { it.pesoKg }
            val grupo = regs.first().grupo
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Superficie),
                modifier = Modifier.fillMaxWidth().clickable { onAbrir(nombre) }
            ) {
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(Modifier.width(6.dp).fillMaxHeight().background(colorGrupo(grupo)))
                    Column(Modifier.weight(1f).padding(14.dp)) {
                        Text(nombre, style = MaterialTheme.typography.titleMedium)
                        Text("$grupo · ${regs.size} ${if (regs.size == 1) "registro" else "registros"}",
                            style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
                    }
                    val serie = regs.sortedBy { it.fecha }
                        .map { if (regs.any { x -> x.pesoKg > 0 }) it.pesoKg else (it.series * it.repeticiones).toDouble() }
                    Grafica(serie, colorGrupo(grupo), Modifier.align(Alignment.CenterVertically).width(72.dp).height(40.dp), 6.dp)
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(14.dp)) {
                        Etiqueta("RÉCORD")
                        Text(if (pr == 0.0) "Peso corporal" else kg(pr),
                            style = MaterialTheme.typography.titleMedium, color = colorGrupo(grupo))
                    }
                    Icon(Icons.Default.ChevronRight, null, Modifier.align(Alignment.CenterVertically).padding(end = 8.dp), tint = TextoSuave)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun DetalleScreen(
    nombre: String, s: UiState, onVolver: () -> Unit,
    onEditar: (Registro) -> Unit, onBorrar: (Registro) -> Unit
) {
    val regs = s.registros.filter { it.ejercicio.trim().equals(nombre.trim(), ignoreCase = true) }
    val ordenados = regs.sortedBy { it.fecha }
    val grupo = regs.firstOrNull()?.grupo ?: GRUPOS[0]
    val color = colorGrupo(grupo)
    val usaPeso = regs.any { it.pesoKg > 0 }
    val valores = ordenados.map { if (usaPeso) it.pesoKg else (it.series * it.repeticiones).toDouble() }

    LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                Column {
                    Text(nombre, style = MaterialTheme.typography.titleLarge)
                    Text(grupo, color = color)
                }
            }
        }
        if (regs.isEmpty()) {
            item { Vacio("Sin registros", "Este ejercicio ya no tiene datos.") }
        } else {
            item {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Superficie),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp)) {
                        Etiqueta(if (usaPeso) "EVOLUCIÓN DEL PESO (KG)" else "EVOLUCIÓN (REPS TOTALES)")
                        Spacer(Modifier.height(8.dp))
                        Grafica(valores, color, Modifier.fillMaxWidth().height(160.dp))
                        if (ordenados.size >= 2) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(fechaCorta(ordenados.first().fecha), style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
                                Text(fechaCorta(ordenados.last().fecha), style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
                            }
                            val ini = valores.first()
                            val fin = valores.last()
                            val dif = fin - ini
                            fun fmt(v: Double) = if (usaPeso) kg(v) else "${v.toInt()} reps"
                            Spacer(Modifier.height(8.dp))
                            Text(
                                when {
                                    dif > 0 -> "Mejoraste de ${fmt(ini)} a ${fmt(fin)} (+${fmt(dif)})"
                                    dif < 0 -> "Bajó de ${fmt(ini)} a ${fmt(fin)} (${fmt(dif)})"
                                    else -> "Sin cambios: ${fmt(fin)}"
                                },
                                style = MaterialTheme.typography.titleMedium, color = color
                            )
                        } else Text("Registra este ejercicio más veces para ver la tendencia.",
                            style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
                    }
                }
            }
            item {
                val pr = regs.maxOf { it.pesoKg }
                val unRm = regs.mapNotNull { unRmEstimado(it) }.maxOrNull()
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Dato("Récord", if (pr == 0.0) "Peso corporal" else kg(pr), color, Modifier.weight(1f))
                    Dato("1RM estimado", unRm?.let { kg(it) } ?: "—", color, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Dato("Volumen total", kg(regs.sumOf { it.volumen }), color, Modifier.weight(1f))
                    Dato("Registros", regs.size.toString(), color, Modifier.weight(1f))
                }
            }
            item { Etiqueta("HISTORIAL DEL EJERCICIO", Modifier.padding(top = 8.dp)) }
            items(regs, key = { it.id }) { r ->
                TarjetaRegistro(r, onEditar = { onEditar(r) }, onBorrar = { onBorrar(r) })
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun Dato(titulo: String, valor: String, color: Color, modifier: Modifier) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Superficie), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Etiqueta(titulo.uppercase())
            Text(valor, style = MaterialTheme.typography.titleLarge, color = color)
        }
    }
}

@Composable
fun Grafica(puntos: List<Double>, color: Color, modifier: Modifier, margenDp: Dp = 14.dp) {
    Canvas(modifier) {
        if (puntos.isEmpty()) return@Canvas
        val min = puntos.min()
        val max = puntos.max()
        val margen = margenDp.toPx()
        val w = size.width - 2 * margen
        val h = size.height - 2 * margen
        fun x(i: Int): Float = if (puntos.size == 1) size.width / 2 else margen + w * i / (puntos.size - 1)
        fun y(v: Double): Float {
            val f = if (max == min) 0.5f else ((v - min) / (max - min)).toFloat()
            return margen + h * (1 - f)
        }
        // Línea base
        drawLine(SuperficieAlta, Offset(margen, size.height - margen), Offset(size.width - margen, size.height - margen), 2.dp.toPx())
        val path = Path()
        puntos.forEachIndexed { i, v -> if (i == 0) path.moveTo(x(i), y(v)) else path.lineTo(x(i), y(v)) }
        drawPath(path, color, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        puntos.forEachIndexed { i, v -> drawCircle(color, 6.dp.toPx(), Offset(x(i), y(v))) }
    }
}