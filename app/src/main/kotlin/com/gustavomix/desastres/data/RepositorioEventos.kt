package com.gustavomix.desastres.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private const val URL_FEED =
    "https://cdn.jsdelivr.net/gh/GustavoMix/cron-desastres-naturales@main/datos/recientes.json"

class RepositorioEventos(
    private val cliente: OkHttpClient = Red.cliente(),
) {
    suspend fun obtenerFeed(): Feed = withContext(Dispatchers.IO) {
        val peticion = Request.Builder().url(URL_FEED).build()
        cliente.newCall(peticion).execute().use { respuesta ->
            if (!respuesta.isSuccessful) {
                throw IOException("HTTP ${respuesta.code} al pedir el feed")
            }
            val cuerpo = respuesta.body?.string() ?: throw IOException("Respuesta vacía")
            parsearFeed(cuerpo)
        }
    }

    private fun parsearFeed(json: String): Feed {
        val raiz = JSONObject(json)
        val eventos = raiz.getJSONArray("eventos")
        return Feed(
            generado = raiz.optString("generado"),
            total = raiz.optInt("total", eventos.length()),
            eventos = (0 until eventos.length()).map { parsearEvento(eventos.getJSONObject(it)) },
            media = parsearConfiguracionMedia(raiz.optJSONObject("media")),
        )
    }

    private fun parsearEvento(obj: JSONObject): Evento = Evento(
        id = obj.getString("id"),
        // Los feeds viejos no lo traen; ahí el evento es su propio grupo.
        idAgrupado = obj.optStringOrNull("id_agrupado") ?: obj.getString("id"),
        fuente = obj.optString("fuente"),
        tipo = obj.optString("tipo"),
        titulo = obj.optString("titulo"),
        lugar = obj.optStringOrNull("lugar"),
        pais = obj.optStringOrNull("pais"),
        paises = obj.optListaDeTextos("paises"),
        fechaEvento = obj.optStringOrNull("fecha_evento"),
        magnitud = obj.optDoubleOrNull("magnitud"),
        unidadMagnitud = obj.optStringOrNull("unidad_magnitud"),
        nivelAlerta = obj.optStringOrNull("nivel_alerta"),
        url = obj.optStringOrNull("url"),
        latitud = obj.optDoubleOrNull("latitud"),
        longitud = obj.optDoubleOrNull("longitud"),
        profundidadKm = obj.optDoubleOrNull("profundidad_km"),
        media = parsearMediaEvento(obj.optJSONObject("media")),
    )

    private fun parsearMediaEvento(obj: JSONObject?): MediaEvento? {
        if (obj == null) return null
        val recursos = obj.optJSONArray("recursos")?.let { arreglo ->
            (0 until arreglo.length()).mapNotNull { indice ->
                val recurso = arreglo.optJSONObject(indice) ?: return@mapNotNull null
                val url = recurso.optStringOrNull("url") ?: return@mapNotNull null
                ImagenFuente(url, recurso.optStringOrNull("titulo"))
            }
        }.orEmpty()

        val media = MediaEvento(
            icono = obj.optStringOrNull("icono"),
            mapa = obj.optStringOrNull("mapa"),
            recursos = recursos,
        )
        // Un media todo vacío es ruido: se prefiere null para que el resto del
        // código pregunte una sola cosa.
        return if (media.icono == null && media.mapa == null && recursos.isEmpty()) null else media
    }

    /**
     * El feed manda sobre las plantillas de imagen; cada campo que falte cae en el
     * valor que trae la app. Así el cron puede cambiar de capa satelital o de
     * proveedor sin esperar a que la gente actualice el APK, y una app vieja frente
     * a un feed nuevo (o al revés) sigue mostrando fotos.
     */
    private fun parsearConfiguracionMedia(obj: JSONObject?): ConfiguracionMedia {
        if (obj == null) return ConfiguracionMedia()
        val porDefecto = ConfiguracionMedia()
        val satelite = obj.optJSONObject("satelite")
        val videos = obj.optJSONObject("videos")

        val grados = satelite?.optJSONObject("grados_por_tipo")?.let { objeto ->
            objeto.keys().asSequence().mapNotNull { clave ->
                val valor = objeto.optDouble(clave)
                if (valor.isNaN()) null else clave to valor
            }.toMap()
        }.orEmpty().ifEmpty { porDefecto.gradosPorTipo }

        return ConfiguracionMedia(
            plantillaSatelite = satelite?.optStringOrNull("plantilla")
                ?: porDefecto.plantillaSatelite,
            capa = satelite?.optStringOrNull("capa") ?: porDefecto.capa,
            credito = satelite?.optStringOrNull("credito") ?: porDefecto.credito,
            gradosPorTipo = grados,
            gradosPorDefecto = satelite?.optDoubleOrNull("grados_por_defecto")
                ?: porDefecto.gradosPorDefecto,
            diasTimelapse = satelite?.optDoubleOrNull("dias_timelapse")?.toInt()
                ?: porDefecto.diasTimelapse,
            plantillaBusquedaVideos = videos?.optStringOrNull("plantilla_busqueda")
                ?: porDefecto.plantillaBusquedaVideos,
        )
    }
}

private fun JSONObject.optStringOrNull(clave: String): String? =
    if (has(clave) && !isNull(clave)) getString(clave).trim().ifBlank { null } else null

private fun JSONObject.optDoubleOrNull(clave: String): Double? =
    if (has(clave) && !isNull(clave)) optDouble(clave).takeIf { !it.isNaN() } else null

private fun JSONObject.optListaDeTextos(clave: String): List<String> {
    val arreglo: JSONArray = optJSONArray(clave) ?: return emptyList()
    return (0 until arreglo.length()).mapNotNull { arreglo.optString(it).trim().ifBlank { null } }
}
