package com.gustavomix.desastres.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun Navegacion(viewModel: EventosViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val rutaActual = backStackEntry?.destination

            NavigationBar {
                destinosBarraInferior.forEach { destino ->
                    NavigationBarItem(
                        selected = rutaActual?.hierarchy?.any { it.route == destino.ruta } == true,
                        onClick = {
                            navController.navigate(destino.ruta) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(destino.icono, contentDescription = destino.etiqueta) },
                        label = { Text(destino.etiqueta) },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Ruta.Reportar.ruta) }) {
                Icon(Icons.Filled.Add, contentDescription = "Reportar incidente")
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Ruta.Inicio.ruta,
            modifier = Modifier.padding(padding),
        ) {
            composable(Ruta.Inicio.ruta) {
                PantallaInicio(
                    viewModel = viewModel,
                    alVerAlerta = { id -> navController.navigate(Ruta.AlertaDetalle.con(id)) },
                    alVerTodasLasAlertas = { navController.navigate(Ruta.Alertas.ruta) },
                )
            }
            composable(Ruta.Alertas.ruta) {
                PantallaAlertas(
                    viewModel = viewModel,
                    alVerAlerta = { id -> navController.navigate(Ruta.AlertaDetalle.con(id)) },
                )
            }
            composable(Ruta.Mapa.ruta) {
                PantallaMapa(viewModel = viewModel)
            }
            composable(Ruta.Noticias.ruta) {
                PantallaNoticias(
                    viewModel = viewModel,
                    alVerAlerta = { id -> navController.navigate(Ruta.AlertaDetalle.con(id)) },
                )
            }
            composable(Ruta.Recursos.ruta) {
                PantallaRecursos()
            }
            composable(Ruta.Reportar.ruta) {
                PantallaReportar(alGuardar = { navController.popBackStack() })
            }
            composable(Ruta.Mas.ruta) { entrada ->
                PantallaMas(senialRecarga = entrada.id)
            }
            composable(
                route = Ruta.AlertaDetalle.ruta,
                arguments = listOf(navArgument("id") {}),
            ) { entrada ->
                val id = entrada.arguments?.getString("id")
                PantallaAlertaDetalle(
                    evento = id?.let { viewModel.obtenerPorId(it) },
                    alVolver = { navController.popBackStack() },
                )
            }
        }
    }
}
