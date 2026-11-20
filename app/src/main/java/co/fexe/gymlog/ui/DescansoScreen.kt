package co.fexe.gymlog.ui

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
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
    val horizontal = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    @Composable
    fun Reloj(tamano: Dp, grosor: Dp) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { restante.toFloat() / total },
                modifier = Modifier.size(tamano), strokeWidth = grosor,
                color = if (terminado) colorGrupo("Core") else Lima, trackColor = SuperficieAlta
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(mmss(restante), style = MaterialTheme.typography.headlineLarge)
                Text(
                    if (terminado) "¡A la siguiente serie!" else if (corriendo) "Descansando" else "Listo para empezar",
                    color = TextoSuave, style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    @Composable
    fun Tiempos() {
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PRESETS.forEach { p ->
                FilterChip(
                    selected = total == p, onClick = { total = p; restante = p; corriendo = false },
                    label = { Text(if (p < 60) "${p}s" else mmss(p), style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false) }
                )
            }
        }
    }

    @Composable
    fun Botones() {
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

    if (horizontal) {
        // Horizontal: reloj a la izquierda, título y controles a la derecha
        Row(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Reloj(190.dp, 12.dp) }
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Cabecera("Descanso", "Tiempo entre series")
                Tiempos()
                Botones()
            }
        }
    } else {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.fillMaxWidth()) { Cabecera("Descanso", "Tiempo entre series") }
            Spacer(Modifier.height(24.dp))
            Tiempos()
            Spacer(Modifier.height(40.dp))
            Reloj(240.dp, 14.dp)
            Spacer(Modifier.height(40.dp))
            Botones()
            Spacer(Modifier.height(24.dp))
        }
    }
}
