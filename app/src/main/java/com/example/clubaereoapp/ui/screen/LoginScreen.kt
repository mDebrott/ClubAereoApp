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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clubaereoapp.ui.components.CapebeLogo
import com.example.clubaereoapp.ui.components.Footer
import com.example.clubaereoapp.ui.components.Header
import com.example.clubaereoapp.ui.viewModel.LoginViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current

    Scaffold(
        topBar = { Header(title = "Iniciar Sesión · CAPEBE") },
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
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CapebeLogo(size = 110.dp, showSubtitle = true)

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Acceso al Sistema",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF12304A)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Administrador: @capebe.cl\nSocio: Correo registrado",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.onEmailChanged(it) },
                            label = { Text("Correo Electrónico") },
                            placeholder = { Text("admin@capebe.cl o usuario@domain.com") },
                            singleLine = true,
                            leadingIcon = {
                                val isEmailAdmin = state.email.trim().lowercase().endsWith("@capebe.cl")
                                Icon(
                                    imageVector = if (isEmailAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isEmailAdmin) Color(0xFF0B7BB0) else Color.Gray
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (state.email.trim().lowercase().endsWith("@capebe.cl")) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✓ Correo identificado como Administrador CAPEBE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF0B7BB0),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.password,
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            label = { Text("Contraseña") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        viewModel.errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = it, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Botón de ingresar
                        Button(
                            onClick = {
                                viewModel.onLoginClick(
                                    context = context,
                                    onError = { vibrar(context, 500L) },
                                    onSuccess = { role ->
                                        val welcomeMsg = if (role == "ADMIN") {
                                            "Bienvenido, Administrador CAPEBE"
                                        } else {
                                            "Bienvenido, Socio CAPEBE"
                                        }
                                        Toast.makeText(context, welcomeMsg, Toast.LENGTH_LONG).show()
                                        onLoginSuccess()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF12304A)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "INGRESAR",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Botón secundario para ir a registro
                        OutlinedButton(
                            onClick = { onNavigateToRegister() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Crear Cuenta (Registro)", color = Color(0xFF12304A))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Accesos rápidos para probar el sistema
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    viewModel.onEmailChanged("admin@capebe.cl")
                                    viewModel.onPasswordChanged("admin123")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2F3FC)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color(0xFF0B7BB0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Demo Admin", color = Color(0xFF0B7BB0), fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.onEmailChanged("socio@gmail.com")
                                    viewModel.onPasswordChanged("socio123")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F4F7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF12304A),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Demo Socio", color = Color(0xFF12304A), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
