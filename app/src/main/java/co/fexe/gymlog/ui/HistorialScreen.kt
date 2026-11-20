package co.fexe.gymlog.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.fexe.gymlog.data.Registro

@Composable
fun HistorialScreen(s: UiState, onEditar: (Registro) -> Unit, onBorrar: (Registro) -> Unit) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var grupo by rememberSaveable { mutableStateOf<String?>(null) }
    // El filtro solo se recalcula si cambian los datos, la búsqueda o el grupo.
    val lista = remember(s.registros, busqueda, grupo) {
        s.registros.filter {
            (grupo == null || it.grupo == grupo) && it.ejercicio.contains(busqueda.trim(), ignoreCase = true)
        }
    }
    LazyColumn(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Cabecera("Historial", "${s.registros.size} registros en total") }
        item {
            OutlinedTextField(
                busqueda, { busqueda = it }, Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Buscar ejercicio") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (busqueda.isNotEmpty())
                        IconButton(onClick = { busqueda = "" }) { Icon(Icons.Default.Clear, "Borrar búsqueda") }
                }
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item { FilterChip(grupo == null, { grupo = null }, label = { Text("Todos", style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false) }) }
                items(GRUPOS) { g ->
                    FilterChip(
                        selected = grupo == g, onClick = { grupo = if (grupo == g) null else g },
                        label = { Text(g, style = MaterialTheme.typography.bodyMedium, maxLines = 1, softWrap = false) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorGrupo(g),
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }
        }
        if (lista.isEmpty()) item {
            Vacio(
                if (s.registros.isEmpty()) "Aún no hay registros" else "Sin resultados",
                if (s.registros.isEmpty()) "Toca «Registrar» para empezar." else "Prueba con otro nombre o grupo."
            )
        }
        items(lista, key = { it.id }, contentType = { "registro" }) { r ->
            TarjetaRegistro(r, onEditar = { onEditar(r) }, onBorrar = { onBorrar(r) })
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}