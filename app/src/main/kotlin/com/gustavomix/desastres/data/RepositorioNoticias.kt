package com.gustavomix.desastres.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

private const val URL_NOTICIAS =
    "https://cdn.jsdelivr.net/gh/GustavoMix/cron-desastres-naturales@main/datos/noticias.json"

/**
 * Las noticias van en su propio archivo, aparte del feed de eventos, y se piden
 * recién cuando alguien va a mirarlas. Meterlas dentro del feed obligaría a
 * bajarlas siempre, aunque quien abre la app solo mire el mapa.
 */
class RepositorioNoticias(
    private val cliente: OkHttpClient = Red.cliente(),
) {
    suspend fun obtener(): FeedNoticias = withContext(Dispatchers.IO) {
        val peticion = Request.Builder().url(URL_NOTICIAS).build()
        cliente.newCall(peticion).execute().use { respuesta ->
            // 404 es un caso normal, no un error: el archivo aparece recién
            // después de la primera corrida del scraper que busque noticias.
            // Mostrar "falló la descarga" ahí sería mentir sobre lo que pasa.
            if (respuesta.code == 404) return@withContext FeedNoticias.VACIO
            if (!respuesta.isSuccessful) {
                throw IOException("HTTP ${respuesta.code} al pedir las noticias")
            }
            val cuerpo = respuesta.body?.string() ?: throw IOException("Respuesta vacía")
            parsear(cuerpo)
        }
    }

    private fun parsear(json: String): FeedNoticias {
        val raiz = JSONObject(json)
        val porEvento = raiz.optJSONObject("noticias") ?: return FeedNoticias(
            generado = raiz.optString("generado"),
            porEvento = emptyMap(),
        )

        val mapa = mutableMapOf<String, List<Noticia>>()
        for (clave in porEvento.keys()) {
            val arreglo = porEvento.optJSONArray(clave) ?: continue
            val notas = (0 until arreglo.length()).mapNotNull { indice ->
                arreglo.optJSONObject(indice)?.let(::parsearNoticia)
            }
            if (notas.isNotEmpty()) mapa[clave] = notas
        }

        return FeedNoticias(generado = raiz.optString("generado"), porEvento = mapa)
    }

    private fun parsearNoticia(obj: JSONObject): Noticia? {
        val titulo = obj.textoODefault("titulo") ?: return null
        val url = obj.textoODefault("url") ?: return null
        return Noticia(
            titulo = titulo,
            url = url,
            medio = obj.textoODefault("medio").orEmpty(),
            fecha = obj.textoODefault("fecha"),
            imagen = obj.textoODefault("imagen"),
            esVideo = obj.optBoolean("es_video", false),
        )
    }
}

private fun JSONObject.textoODefault(clave: String): String? =
    if (has(clave) && !isNull(clave)) optString(clave).trim().ifBlank { null } else null
