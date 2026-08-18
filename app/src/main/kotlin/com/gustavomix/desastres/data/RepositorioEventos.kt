package com.gustavomix.desastres.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

private const val URL_FEED =
    "https://raw.githubusercontent.com/GustavoMix/cron-desastres-naturales/" +
        "claude/scraper-cron-j5z2ny/datos/recientes.json"

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
        fechaEvento = obj.optStringOrNull("fecha_evento"),
        magnitud = if (obj.has("magnitud") && !obj.isNull("magnitud")) obj.optDouble("magnitud") else null,
        unidadMagnitud = obj.optStringOrNull("unidad_magnitud"),
        nivelAlerta = obj.optStringOrNull("nivel_alerta"),
        url = obj.optStringOrNull("url"),
        latitud = if (obj.has("latitud") && !obj.isNull("latitud")) obj.optDouble("latitud") else null,
        longitud = if (obj.has("longitud") && !obj.isNull("longitud")) obj.optDouble("longitud") else null,
    )
}

private fun JSONObject.optStringOrNull(clave: String): String? =
    if (has(clave) && !isNull(clave)) getString(clave).ifBlank { null } else null
