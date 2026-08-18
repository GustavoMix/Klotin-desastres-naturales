package com.gustavomix.desastres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.gustavomix.desastres.ui.EventosViewModel
import com.gustavomix.desastres.ui.Navegacion
import com.gustavomix.desastres.ui.TemaDesastres

class MainActivity : ComponentActivity() {

    private val viewModel: EventosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TemaDesastres {
                Navegacion(viewModel = viewModel)
            }
        }
    }
}
