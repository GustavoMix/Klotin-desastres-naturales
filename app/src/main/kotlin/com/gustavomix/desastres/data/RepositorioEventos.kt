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
    private val cliente: OkHttpClient = OkHttpClient(),
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
        )
    }

    private fun parsearEvento(obj: JSONObject): Evento = Evento(
        id = obj.getString("id"),
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
    )
}

private fun JSONObject.optStringOrNull(clave: String): String? =
    if (has(clave) && !isNull(clave)) getString(clave).trim().ifBlank { null } else null

private fun JSONObject.optDoubleOrNull(clave: String): Double? =
    if (has(clave) && !isNull(clave)) optDouble(clave).takeIf { !it.isNaN() } else null

private fun JSONObject.optListaDeTextos(clave: String): List<String> {
    val arreglo: JSONArray = optJSONArray(clave) ?: return emptyList()
    return (0 until arreglo.length()).mapNotNull { arreglo.optString(it).trim().ifBlank { null } }
}
