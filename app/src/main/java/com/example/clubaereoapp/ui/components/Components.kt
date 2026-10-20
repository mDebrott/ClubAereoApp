package com.example.clubaereoapp.ui.components
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(title: String) {
    TopAppBar(title = { Text(title) })
}

@Composable
fun Footer() {
    Text(
        text = "Club Aéreo App © 2026",
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

