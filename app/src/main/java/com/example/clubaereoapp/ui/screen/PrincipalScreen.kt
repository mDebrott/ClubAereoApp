package com.example.clubaereoapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubaereoapp.R
import com.example.clubaereoapp.ui.components.Footer
import com.example.clubaereoapp.ui.components.Header

//Ya hora de ponerse a trabajar en esta pantalla, no tengo idea que diseño hacerle asi que
//improvisare con alguna imagen que encuentre

@Composable
fun PrincipalScreen(
    onNavigateToLogin: () -> Unit
){
    val context = LocalContext.current
    Scaffold(
        topBar = { Header(title = "Club Aereo App") },
        bottomBar = { Footer() }
    ){
        innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0B7BB0)),
                contentAlignment = Alignment.Center
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logoavion),
                    contentDescription = "logo Remplazable de avion",
                    modifier = Modifier.size(220.dp)

                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Bienvenido, Socio", Toast.LENGTH_SHORT).show()
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF85E0F8),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "INGRESAR",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

        }

    }
}