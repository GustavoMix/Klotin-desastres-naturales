package com.gustavomix.desastres.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gustavomix.desastres.data.RepositorioReportes
import com.gustavomix.desastres.data.etiquetaTipo
import kotlinx.coroutines.launch

private val TIPOS_REPORTE = listOf("sismo", "incendio", "inundacion", "derrumbe", "sequia", "otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReportar(modifier: Modifier = Modifier, alGuardar: () -> Unit = {}) {
    val contexto = LocalContext.current
    val repositorio = remember { RepositorioReportes(contexto) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var tipoExpandido by remember { mutableStateOf(false) }
    var tipoSeleccionado by remember { mutableStateOf(TIPOS_REPORTE.first()) }
    var ubicacion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                "Reportar incidente",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                "El reporte se guarda solo en este teléfono, todavía no se envía a nadie.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            ExposedDropdownMenuBox(expanded = tipoExpandido, onExpandedChange = { tipoExpandido = it }) {
                OutlinedTextField(
                    value = etiquetaTipo(tipoSeleccionado),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de incidente") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoExpandido) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                )
                ExposedDropdownMenu(expanded = tipoExpandido, onDismissRequest = { tipoExpandido = false }) {
                    TIPOS_REPORTE.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(etiquetaTipo(tipo)) },
                            onClick = {
                                tipoSeleccionado = tipo
                                tipoExpandido = false
                            },
                        )
                    }
                }
            }

            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación") },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )

            Button(
                onClick = {
                    if (ubicacion.isBlank() || descripcion.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Completá ubicación y descripción antes de guardar")
                        }
                        return@Button
                    }
                    repositorio.guardar(
                        tipo = tipoSeleccionado,
                        ubicacion = ubicacion.trim(),
                        descripcion = descripcion.trim(),
                    )
                    ubicacion = ""
                    descripcion = ""
                    tipoSeleccionado = TIPOS_REPORTE.first()
                    scope.launch {
                        snackbarHostState.showSnackbar("Reporte guardado en \"Más > Mis reportes\"")
                    }
                    alGuardar()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) {
                Text("Guardar reporte")
            }
        }
    }
}
