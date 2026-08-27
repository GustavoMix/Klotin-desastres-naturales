package com.gustavomix.desastres.avisos

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gustavomix.desastres.MainActivity
import com.gustavomix.desastres.R
import com.gustavomix.desastres.data.Evento
import com.gustavomix.desastres.data.Severidad
import com.gustavomix.desastres.data.explicacionSeveridad
import com.gustavomix.desastres.data.horaBolivia
import com.gustavomix.desastres.data.magnitudTexto
import com.gustavomix.desastres.data.severidadDe
import com.gustavomix.desastres.data.tituloEvento

const val EXTRA_EVENTO = "evento_id"

private const val CANAL_FUERTES = "eventos_fuertes"
private const val CANAL_RESTO = "eventos"

/**
 * Notificaciones de eventos nuevos.
 *
 * Dos canales a propósito: los eventos graves suenan y los demás llegan callados.
 * Con un solo canal, alguien a quien le molesta el ruido de un sismo lejano de
 * magnitud 4 no tiene más opción que apagar todo, incluida la alerta roja que sí
 * le importaba. Separados, Android le deja bajarle el volumen a uno solo, y esa
 * elección es del usuario, no de la app.
 */
object Avisos {

    fun crearCanales(contexto: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val gestor = contexto.getSystemService(NotificationManager::class.java) ?: return

        gestor.createNotificationChannel(
            NotificationChannel(
                CANAL_FUERTES,
                "Eventos fuertes",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Terremotos y alertas que pueden causar daños."
                enableVibration(true)
            },
        )
        gestor.createNotificationChannel(
            NotificationChannel(
                CANAL_RESTO,
                "Otros eventos",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Eventos moderados o leves. Llegan sin sonido."
            },
        )
    }

    /** ¿Se pueden mostrar? Desde Android 13 hace falta permiso explícito. */
    fun sePuedeNotificar(contexto: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return NotificationManagerCompat.from(contexto).areNotificationsEnabled()
        }
        return ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun publicar(contexto: Context, evento: Evento) {
        if (!sePuedeNotificar(contexto)) return

        val severidad = severidadDe(evento)
        val grave = severidad == Severidad.ROJA || severidad == Severidad.NARANJA

        val detalle = listOfNotNull(
            magnitudTexto(evento),
            horaBolivia(evento.fechaEvento),
        ).joinToString(" · ")

        val intent = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_EVENTO, evento.id)
        }
        val pendiente = android.app.PendingIntent.getActivity(
            contexto,
            evento.id.hashCode(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        val aviso = NotificationCompat.Builder(contexto, if (grave) CANAL_FUERTES else CANAL_RESTO)
            .setSmallIcon(R.drawable.ic_aviso)
            .setContentTitle(tituloEvento(evento))
            .setContentText(detalle.ifBlank { explicacionSeveridad(severidad) })
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$detalle\n${explicacionSeveridad(severidad)}".trim()),
            )
            .setPriority(if (grave) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendiente)
            .setAutoCancel(true)
            .build()

        // El id del evento como id de notificación: si el mismo evento se
        // reprocesa, reemplaza su aviso en vez de apilar copias.
        runCatching {
            NotificationManagerCompat.from(contexto).notify(evento.id.hashCode(), aviso)
        }
    }
}
