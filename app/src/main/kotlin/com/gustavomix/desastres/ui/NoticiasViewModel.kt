package com.gustavomix.desastres.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustavomix.desastres.data.FeedNoticias
import com.gustavomix.desastres.data.Noticia
import com.gustavomix.desastres.data.RepositorioNoticias
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EstadoNoticias {
    data object Ocioso : EstadoNoticias
    data object Cargando : EstadoNoticias
    data class Error(val mensaje: String) : EstadoNoticias
    data class Listo(val feed: FeedNoticias) : EstadoNoticias
}

/**
 * Las noticias se piden aparte del feed de eventos y **recién cuando alguien va
 * a mirarlas**: quien abre la app para ver el mapa no tiene por qué bajar los
 * artículos de cuarenta eventos.
 */
class NoticiasViewModel(
    private val repositorio: RepositorioNoticias = RepositorioNoticias(),
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoNoticias>(EstadoNoticias.Ocioso)
    val estado: StateFlow<EstadoNoticias> = _estado.asStateFlow()

    /**
     * Carga una sola vez por sesión. Se llama desde cada pantalla que muestre
     * noticias, así que tiene que ser idempotente: si ya cargó o está cargando,
     * no vuelve a salir a la red.
     */
    fun cargarSiHaceFalta() {
        if (_estado.value is EstadoNoticias.Listo || _estado.value is EstadoNoticias.Cargando) return
        recargar()
    }

    fun recargar() {
        _estado.value = EstadoNoticias.Cargando
        viewModelScope.launch {
            _estado.value = try {
                EstadoNoticias.Listo(repositorio.obtener())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                EstadoNoticias.Error(e.message ?: "Error desconocido")
            }
        }
    }

    /** Las notas de un evento. Vacío mientras no hayan cargado, que es lo correcto. */
    fun para(idAgrupado: String?): List<Noticia> =
        (estado.value as? EstadoNoticias.Listo)?.feed?.para(idAgrupado).orEmpty()
}
