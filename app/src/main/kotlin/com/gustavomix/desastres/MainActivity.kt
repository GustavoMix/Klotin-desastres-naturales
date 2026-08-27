package com.gustavomix.desastres

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import com.gustavomix.desastres.avisos.EXTRA_EVENTO
import com.gustavomix.desastres.ui.EventosViewModel
import com.gustavomix.desastres.ui.Navegacion
import com.gustavomix.desastres.ui.TemaDesastres

class MainActivity : ComponentActivity() {

    private val viewModel: EventosViewModel by viewModels()

    /**
     * Evento que pidió abrir una notificación.
     *
     * Es estado y no un valor leído una sola vez porque la activity es
     * `singleTask`: si ya está abierta, tocar una notificación no la vuelve a
     * crear, entra por `onNewIntent`. Sin esto, el segundo toque no haría nada.
     */
    private val eventoPedido = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        eventoPedido.value = intent?.getStringExtra(EXTRA_EVENTO)

        setContent {
            TemaDesastres {
                Navegacion(
                    viewModel = viewModel,
                    eventoPedido = eventoPedido.value,
                    alAbrirEventoPedido = { eventoPedido.value = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        eventoPedido.value = intent.getStringExtra(EXTRA_EVENTO)
    }
}
