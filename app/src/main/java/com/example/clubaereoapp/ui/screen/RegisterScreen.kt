package com.example.clubaereoapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.clubaereoapp.ui.components.Footer
import com.example.clubaereoapp.ui.components.Header

@Composable

fun RegisterScreen()
{
    Scaffold(
        topBar = { Header(title = "Registro de usuario") },
        bottomBar = { Footer()}
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding( 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Pantalla de registro", style = MaterialTheme.typography.headlineMedium)

        }


    }

}