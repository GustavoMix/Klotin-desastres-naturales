package com.gustavomix.desastres.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.RepositorioEventos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EstadoEventos {
    data object Cargando : EstadoEventos
    data class Error(val mensaje: String) : EstadoEventos
    data class Listo(val generado: String, val eventos: List<Evento>) : EstadoEventos
}

class EventosViewModel(
    private val repositorio: RepositorioEventos = RepositorioEventos(),
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoEventos>(EstadoEventos.Cargando)
    val estado: StateFlow<EstadoEventos> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        _estado.value = EstadoEventos.Cargando
        viewModelScope.launch {
            _estado.value = try {
                val feed = repositorio.obtenerFeed()
                // Del más nuevo al más viejo: las fechas ISO se ordenan igual como texto.
                val ordenados = feed.eventos.sortedByDescending { it.fechaEvento ?: "" }
                EstadoEventos.Listo(feed.generado, ordenados)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                EstadoEventos.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun obtenerPorId(id: String): Evento? =
        (estado.value as? EstadoEventos.Listo)?.eventos?.find { it.id == id }
}
