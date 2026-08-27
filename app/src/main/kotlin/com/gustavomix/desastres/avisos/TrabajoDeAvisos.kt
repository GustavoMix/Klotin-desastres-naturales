package com.gustavomix.desastres.avisos

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.PreferenciasAvisos
import com.gustavomix.desastres.data.RepositorioEventos
import com.gustavomix.desastres.data.mereceAviso
import com.gustavomix.desastres.data.severidadDe
import java.util.concurrent.TimeUnit

private const val TRABAJO = "chequeo-de-eventos"

/**
 * Cada cuánto se mira el feed.
 *
 * Seis horas y no quince minutos: **el scraper corre una vez por semana**, así
 * que consultar más seguido no encuentra nada nuevo y solo gasta batería y datos
 * de alguien. Seis horas ya es generoso contra esa cadencia, y deja margen para
 * cuando el cron pase a correr más seguido.
 */
private const val HORAS_ENTRE_CHEQUEOS = 6L

/** Cuántas notificaciones puede disparar un solo chequeo. */
private const val MAXIMO_POR_CHEQUEO = 3

class TrabajoDeAvisos(
    contexto: Context,
    parametros: WorkerParameters,
) : CoroutineWorker(contexto, parametros) {

    override suspend fun doWork(): Result {
        val preferencias = PreferenciasAvisos(applicationContext)
        val ajustes = preferencias.leer()
        if (!ajustes.activos) return Result.success()

        val feed = try {
            RepositorioEventos().obtenerFeed()
        } catch (e: Exception) {
            // Sin red o con el CDN caído: se reintenta en el próximo ciclo. No
            // tiene sentido insistir ya mismo contra un feed que cambia una vez
            // por semana.
            return Result.retry()
        }

        val yaAvisados = preferencias.yaAvisados()
        val nuevos = feed.eventos
            .filter { it.id !in yaAvisados }
            .filter { mereceAviso(it, ajustes) }
            // Lo más grave primero, y a igual gravedad lo más reciente: si hay
            // que recortar, que sobreviva lo que más importa.
            .sortedWith(
                compareBy<Evento> { severidadDe(it).ordinal }
                    .thenByDescending { it.fechaEvento ?: "" },
            )

        nuevos.take(MAXIMO_POR_CHEQUEO).forEach { Avisos.publicar(applicationContext, it) }

        // Se marca como visto **todo** el feed, no solo lo notificado. Si solo se
        // marcaran los tres avisados, los que quedaron fuera del tope volverían a
        // competir en cada chequeo y el cuarto evento no se anunciaría nunca. Y
        // marcar también lo que no llegó al umbral es lo que se quiere: si alguien
        // baja el umbral, no le caen de golpe dos semanas de eventos viejos.
        preferencias.recordarAvisados(feed.eventos.map { it.id })

        return Result.success()
    }

    companion object {
        fun programar(contexto: Context) {
            val trabajo = PeriodicWorkRequestBuilder<TrabajoDeAvisos>(
                HORAS_ENTRE_CHEQUEOS,
                TimeUnit.HOURS,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .build()

            WorkManager.getInstance(contexto).enqueueUniquePeriodicWork(
                TRABAJO,
                // KEEP y no UPDATE: reprogramar en cada arranque de la app
                // reiniciaría el intervalo, y alguien que abre la app seguido no
                // recibiría nunca un chequeo.
                ExistingPeriodicWorkPolicy.KEEP,
                trabajo,
            )
        }

        fun cancelar(contexto: Context) {
            WorkManager.getInstance(contexto).cancelUniqueWork(TRABAJO)
        }
    }
}
