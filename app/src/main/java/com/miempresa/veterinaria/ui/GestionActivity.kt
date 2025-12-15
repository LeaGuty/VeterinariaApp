package com.miempresa.veterinaria.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Importante para obtener el ViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.miempresa.veterinaria.viewmodel.MainViewModel

class GestionActivity : ComponentActivity() {

    // Aquí inicializamos el ViewModel que conecta datos y vista
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Llamamos a nuestra pantalla de gestión pasando el ViewModel y ajustando el padding
                    Box(modifier = Modifier.padding(innerPadding)) {
                        PantallaGestion(viewModel = viewModel)
                    }
                }
            }
        }
    }
}