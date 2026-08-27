package com.gustavomix.desastres.data

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP único de la app, con caché en disco.
 *
 * La caché no es un lujo: el scraper corre una vez por semana, así que el feed
 * es idéntico byte a byte entre corridas y el CDN contesta 304 casi siempre. Sin
 * caché en disco no hay ETag que valga y cada apertura de la app se baja el
 * archivo entero. En un teléfono con datos móviles esa diferencia se nota, y más
 * todavía cuando el chequeo de notificaciones corre solo cada seis horas.
 */
object Red {

    private const val TAMANIO_CACHE_BYTES = 24L * 1024 * 1024

    @Volatile
    private var instancia: OkHttpClient? = null

    fun iniciar(contexto: Context) {
        if (instancia != null) return
        synchronized(this) {
            if (instancia != null) return
            instancia = construir(contexto.applicationContext)
        }
    }

    /**
     * El cliente ya armado. Si por lo que sea nadie llamó a [iniciar] —un test,
     * un proceso secundario—, devuelve uno sin caché en vez de reventar: peor es
     * quedarse sin datos.
     */
    fun cliente(): OkHttpClient = instancia ?: OkHttpClient()

    private fun construir(contexto: Context): OkHttpClient = OkHttpClient.Builder()
        .cache(Cache(File(contexto.cacheDir, "http"), TAMANIO_CACHE_BYTES))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
}
