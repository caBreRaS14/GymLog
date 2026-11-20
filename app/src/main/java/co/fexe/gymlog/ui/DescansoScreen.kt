package co.fexe.gymlog.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val PRESETS = listOf(30, 60, 90, 120, 180)

private fun mmss(seg: Int) = "%d:%02d".format(seg / 60, seg % 60)

@Composable
fun DescansoScreen() {
    var total by rememberSaveable { mutableIntStateOf(60) }
    var restante by rememberSaveable { mutableIntStateOf(60) }
    var corriendo by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(corriendo) {
        while (corriendo && restante > 0) {
            delay(1000)
            restante--
        }
        if (restante <= 0) corriendo = false
    }
    val terminado = restante == 0

    Column(Modifier.padding(horizontal = 16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth()) { Cabecera("Descanso", "Tiempo entre series") }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PRESETS.forEach { p ->
                FilterChip(
                    selected = total == p, onClick = { total = p; restante = p; corriendo = false },
                    label = { Text(if (p < 60) "${p}s" else mmss(p), style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false) }
                )
            }
        }
        Spacer(Modifier.height(40.dp))
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { restante.toFloat() / total },
                modifier = Modifier.size(240.dp), strokeWidth = 14.dp,
                color = if (terminado) colorGrupo("Core") else Lima, trackColor = SuperficieAlta
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(mmss(restante), style = MaterialTheme.typography.headlineLarge)
                Text(if (terminado) "¡A la siguiente serie!" else if (corriendo) "Descansando" else "Listo para empezar",
                    color = TextoSuave)
            }
        }
        Spacer(Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { restante = total; corriendo = false }, shape = RoundedCornerShape(16.dp)) {
                Icon(Icons.Default.Refresh, null); Spacer(Modifier.width(6.dp)); Text("Reiniciar")
            }
            Button(
                onClick = { if (terminado) restante = total; corriendo = !corriendo },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(if (corriendo) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(6.dp))
                Text(if (corriendo) "Pausar" else "Iniciar")
            }
        }
    }
}