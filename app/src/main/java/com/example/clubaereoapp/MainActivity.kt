package com.example.clubaereoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.clubaereoapp.ui.screen.Login
import com.example.clubaereoapp.ui.theme.ClubAereoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClubAereoAppTheme {
                Login(onNavigateToRegister = { })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    ClubAereoAppTheme {
        Login(onNavigateToRegister = { })
    }
}