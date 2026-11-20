package co.fexe.gymlog

import android.graphics.Color as AColor
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import co.fexe.gymlog.data.Registro
import co.fexe.gymlog.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AColor.TRANSPARENT)
        )
        setContent { GymTheme { Surface(color = Fondo) { App() } } }
    }
}

private data class Destino(val ruta: String, val titulo: String, val icono: ImageVector)

private val DESTINOS = listOf(
    Destino("inicio", "Inicio", Icons.Default.Home),
    Destino("historial", "Historial", Icons.Default.History),
    Destino("progreso", "Progreso", Icons.Default.TrendingUp),
    Destino("descanso", "Descanso", Icons.Default.Timer)
)

@Composable
fun App(vm: RegistroViewModel = viewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route ?: "inicio"

    var editando by rememberSaveable { mutableStateOf<Long?>(null) } // null=cerrado, 0=nuevo
    var porBorrar by remember { mutableStateOf<Registro?>(null) }
    val actual = s.registros.firstOrNull { it.id == editando }

    val onEditar: (Registro) -> Unit = { editando = it.id }
    val onBorrar: (Registro) -> Unit = { porBorrar = it }

    Scaffold(
        containerColor = Fondo,
        bottomBar = {
            NavigationBar(containerColor = Superficie) {
                DESTINOS.forEach { d ->
                    NavigationBarItem(
                        selected = ruta == d.ruta || (d.ruta == "progreso" && ruta.startsWith("detalle")),
                        onClick = {
                            nav.navigate(d.ruta) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(d.icono, null) },
                        label = { Text(d.titulo) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (ruta == "inicio" || ruta == "historial") {
                ExtendedFloatingActionButton(
                    onClick = { editando = 0L },
                    containerColor = Lima, contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Registrar", fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = "inicio", modifier = Modifier.padding(pad)) {
            composable("inicio") { InicioScreen(s, vm, onEditar, onBorrar) }
            composable("historial") { HistorialScreen(s, onEditar, onBorrar) }
            composable("progreso") { ProgresoScreen(s) { nombre -> nav.navigate("detalle/${Uri.encode(nombre)}") } }
            composable("descanso") { DescansoScreen() }
            composable("detalle/{nombre}", arguments = listOf(navArgument("nombre") { type = NavType.StringType })) { e ->
                DetalleScreen(e.arguments?.getString("nombre") ?: "", s, { nav.popBackStack() }, onEditar, onBorrar)
            }
        }
    }

    if (editando != null) FormRegistro(actual, s.error, vm, onCerrar = { editando = null; vm.limpiarError() })
    porBorrar?.let { r ->
        ConfirmarBorrado(r, onConfirmar = { vm.borrar(r); porBorrar = null }, onCancelar = { porBorrar = null })
    }
}