package com.gustavomix.desastres

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.gustavomix.desastres.ui.EventosViewModel
import com.gustavomix.desastres.ui.PantallaEventos

class MainActivity : ComponentActivity() {

    private val viewModel: EventosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold { padding ->
                    PantallaEventos(modifier = Modifier.padding(padding), viewModel = viewModel)
                }
            }
        }
    }
}
