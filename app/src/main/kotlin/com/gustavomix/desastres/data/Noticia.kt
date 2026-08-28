package com.gustavomix.desastres.data

/** Una nota de un medio sobre un evento. */
data class Noticia(
    val titulo: String,
    val url: String,
    val medio: String,
    val fecha: String?,
    val imagen: String?,
    val esVideo: Boolean,
)

/**
 * Las noticias de todos los eventos que el scraper alcanzó a enriquecer.
 *
 * La clave es el `id_agrupado` del evento, no su `id`: GDACS republica un ciclón
 * por episodios y colgar las notas del episodio las fragmentaría entre veinte
 * registros del mismo fenómeno.
 */
data class FeedNoticias(
    val generado: String,
    val porEvento: Map<String, List<Noticia>>,
) {
    fun para(idAgrupado: String?): List<Noticia> =
        idAgrupado?.let { porEvento[it] }.orEmpty()

    /** Todas las notas juntas, de la más nueva a la más vieja. */
    fun todas(): List<Noticia> =
        porEvento.values.flatten().sortedByDescending { it.fecha ?: "" }

    companion object {
        val VACIO = FeedNoticias(generado = "", porEvento = emptyMap())
    }
}
