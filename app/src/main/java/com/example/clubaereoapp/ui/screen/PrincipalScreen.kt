package com.example.clubaereoapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubaereoapp.data.UserSession
import com.example.clubaereoapp.ui.components.CapebeLogo
import com.example.clubaereoapp.ui.components.Footer
import com.example.clubaereoapp.ui.components.Header


@Composable
fun PrincipalScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToSeleccionVuelo: () -> Unit = {},
    onNavigateToOperaciones: () -> Unit = {}
) {
    val context = LocalContext.current
    // Estado local para forzar la recomposición si UserSession no usa mutableStateOf
    var isLoggedIn by remember { mutableStateOf(UserSession.isLoggedIn) }

    val handleLogout = {
        UserSession.logout()
        isLoggedIn = false
        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            Header(
                title = "Club Aéreo CAPEBE",
                onLogoutClick = handleLogout
            )
        },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0B7BB0)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                CapebeLogo(size = 150.dp, showSubtitle = true)

                Spacer(modifier = Modifier.height(20.dp))

                // Banner de usuario logueado
                if (isLoggedIn) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (UserSession.isAdmin) Color(0xFF12304A) else Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (UserSession.isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = if (UserSession.isAdmin) Color(0xFF85E0F8) else Color(0xFF0B7BB0),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (UserSession.isAdmin) "ADMINISTRADOR CAPEBE" else "SOCIO CLUB CAPEBE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (UserSession.isAdmin) Color.White else Color(0xFF12304A)
                                )
                                Text(
                                    text = UserSession.email,
                                    fontSize = 12.sp,
                                    color = if (UserSession.isAdmin) Color(0xFF85E0F8) else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Panel principal de opciones
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Menú Principal CAPEBE",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF12304A)
                        )

                        Button(
                            onClick = onNavigateToSeleccionVuelo,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B7BB0)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FlightTakeoff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "ESCOGER VUELO A REALIZAR",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!isLoggedIn || UserSession.isAdmin) {
                            Button(
                                onClick = onNavigateToOperaciones,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12304A)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = Color(0xFF85E0F8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "GESTIÓN DE OPERACIONES (ADMIN)",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        if (!isLoggedIn) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onNavigateToLogin,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF85E0F8)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Login,
                                            contentDescription = null,
                                            tint = Color(0xFF12304A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Ingresar",
                                            color = Color(0xFF12304A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = onNavigateToRegister,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = Color(0xFF12304A),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Registro",
                                            color = Color(0xFF12304A),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = handleLogout,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Cerrar Sesión", color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Componente API Externa: Clima Aeronáutico en Tiempo Real (IE 3.1.4)
                com.example.clubaereoapp.ui.components.ClimaAeronauticoCard()

                Spacer(modifier = Modifier.height(16.dp))

                // Componente de batería
                TarjetaBateria(context)
            }
        }
    }
}
