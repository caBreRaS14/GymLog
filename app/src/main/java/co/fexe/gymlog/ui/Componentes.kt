package co.fexe.gymlog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.fexe.gymlog.data.Registro
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val loc = Locale("es", "CO")
fun kg(v: Double): String = String.format(loc, "%,.1f", v).removeSuffix(",0") + " kg"
fun fechaCorta(ms: Long): String = SimpleDateFormat("dd MMM", loc).format(Date(ms))
fun dia(ms: Long): String = SimpleDateFormat("dd MMM · HH:mm", loc).format(Date(ms))

@Composable
fun Cabecera(titulo: String, sub: String? = null) {
    Column(Modifier.padding(top = 12.dp, bottom = 4.dp)) {
        Text("GYMLOG", style = MaterialTheme.typography.labelLarge, color = Lima)
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        if (sub != null) Text(sub, style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
    }
}

@Composable
fun Etiqueta(texto: String, modifier: Modifier = Modifier) =
    Text(texto, style = MaterialTheme.typography.labelLarge, color = TextoSuave, modifier = modifier)

@Composable
fun TarjetaRegistro(r: Registro, onEditar: () -> Unit, onBorrar: () -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Superficie),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(colorGrupo(r.grupo)))
            Column(Modifier.weight(1f).padding(start = 14.dp, top = 12.dp, bottom = 12.dp)) {
                Text(r.ejercicio, style = MaterialTheme.typography.titleMedium)
                Text("${r.grupo} · ${dia(r.fecha)}", style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(SuperficieAlta).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("${r.series} × ${r.repeticiones}", fontWeight = FontWeight.Bold)
                }
            }
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(top = 10.dp, end = 4.dp, bottom = 4.dp)) {
                Text(if (r.pesoKg == 0.0) "Peso corporal" else kg(r.pesoKg),
                    Modifier.padding(end = 10.dp), style = MaterialTheme.typography.titleMedium,
                    color = colorGrupo(r.grupo))
                Row {
                    IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, "Editar ${r.ejercicio}", tint = TextoSuave) }
                    IconButton(onClick = onBorrar) { Icon(Icons.Default.Delete, "Eliminar ${r.ejercicio}", tint = TextoSuave) }
                }
            }
        }
    }
}

@Composable
fun Vacio(titulo: String, detalle: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.FitnessCenter, null, Modifier.size(56.dp), tint = Lima)
        Spacer(Modifier.height(12.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(detalle, color = TextoSuave)
    }
}

@Composable
fun ConfirmarBorrado(r: Registro, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        containerColor = Superficie,
        onDismissRequest = onCancelar,
        title = { Text("¿Eliminar registro?") },
        text = { Text("Se borrará «${r.ejercicio}» (${r.series}×${r.repeticiones}). Esta acción no se puede deshacer.") },
        confirmButton = { Button(onClick = onConfirmar) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar", color = TextoSuave) } }
    )
}

@Composable
fun FormRegistro(r: Registro?, error: String?, vm: RegistroViewModel, onCerrar: () -> Unit) {
    var ej by rememberSaveable { mutableStateOf(r?.ejercicio ?: "") }
    var grupo by rememberSaveable { mutableStateOf(r?.grupo ?: GRUPOS[0]) }
    var series by rememberSaveable { mutableStateOf(r?.series?.toString() ?: "") }
    var reps by rememberSaveable { mutableStateOf(r?.repeticiones?.toString() ?: "") }
    var peso by rememberSaveable { mutableStateOf(r?.pesoKg?.toString() ?: "") }
    AlertDialog(
        containerColor = Superficie,
        shape = RoundedCornerShape(24.dp),
        onDismissRequest = onCerrar,
        title = { Text(if (r == null) "Nuevo ejercicio" else "Editar ejercicio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(ej, { ej = it }, label = { Text("Ejercicio (ej. Press banca)") }, singleLine = true)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GRUPOS.chunked(3).forEach { fila ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            fila.forEach { g ->
                                FilterChip(
                                    selected = grupo == g, onClick = { grupo = g },
                                    label = { Text(g, style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorGrupo(g),
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                                )
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(series, { series = it }, Modifier.weight(1f), label = { Text("Series") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(reps, { reps = it }, Modifier.weight(1f), label = { Text("Reps") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                OutlinedTextField(peso, { peso = it }, label = { Text("Peso (kg)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            Button(onClick = { vm.guardar(r?.id ?: 0L, ej, grupo, series, reps, peso, onCerrar) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar", color = TextoSuave) } }
    )
}