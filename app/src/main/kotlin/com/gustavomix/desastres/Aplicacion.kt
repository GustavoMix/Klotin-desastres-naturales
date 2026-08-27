package com.gustavomix.desastres

import android.app.Application
import com.gustavomix.desastres.avisos.Avisos
import com.gustavomix.desastres.data.Red

class Aplicacion : Application() {

    override fun onCreate() {
        super.onCreate()
        // Los dos corren también cuando el proceso lo despierta WorkManager sin
        // que haya ninguna pantalla abierta: el chequeo de eventos necesita el
        // cliente HTTP con caché y los canales ya creados.
        Red.iniciar(this)
        Avisos.crearCanales(this)
    }
}
