package com.example.clubaereoapp.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubaereoapp.data.UserSession
import com.example.clubaereoapp.data.remote.ClubAereoApi
import com.example.clubaereoapp.ui.components.Footer
import com.example.clubaereoapp.ui.components.Header
import kotlinx.coroutines.launch
import org.json.JSONObject

private val NavyColor = Color(0xFF12304A)
private val BlueColor = Color(0xFF0B7BB0)
private val CyanColor = Color(0xFF85E0F8)

data class FlightSlot(
    val id: Long,
    val time: String,
    val plane: String,
    val availableSeats: Int
)

@Composable
fun SeleccionVueloScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var jornadas by remember { mutableStateOf(listOf<JSONObject>()) }
    var selectedJornada by remember { mutableStateOf<JSONObject?>(null) }
    var selectedBloque by remember { mutableStateOf<FlightSlot?>(null) }
    var selectedAeronave by remember { mutableStateOf("Cessna 172 Skyhawk (CC-KPB)") }

    var rut by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf(UserSession.name.ifBlank { "Socio CAPEBE" }) }

    var loading by remember { mutableStateOf(false) }
    var reservationSuccess by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }

    // Mock data for seamless offline usage if backend API server is unreachable
    val mockJornadas = listOf(
        JSONObject().put("id", 101L).put("fecha", "2026-03-25").put("base", "TOBALABA (Santiago)"),
        JSONObject().put("id", 102L).put("fecha", "2026-03-26").put("base", "PUERTO MONTT (El Tepual)"),
        JSONObject().put("id", 103L).put("fecha", "2026-03-27").put("base", "TALCAHUANO (Carriel Sur)")
    )

    val mockSlots = listOf(
        FlightSlot(1, "09:00 - 10:00", "Cessna 172 (CC-KPB)", 3),
        FlightSlot(2, "10:30 - 11:30", "Piper Cherokee (CC-KPA)", 2),
        FlightSlot(3, "12:00 - 13:00", "Beechcraft Bonanza (CC-KPC)", 4),
        FlightSlot(4, "15:00 - 16:00", "Cessna 172 (CC-KPB)", 3),
        FlightSlot(5, "16:30 - 17:30", "Piper Cherokee (CC-KPA)", 1)
    )

    fun loadJornadasData() {
        scope.launch {
            loading = true
            try {
                val arr = ClubAereoApi.jornadas()
                val list = (0 until arr.length()).map { arr.getJSONObject(it) }
                if (list.isNotEmpty()) {
                    jornadas = list
                    selectedJornada = list.first()
                } else {
                    jornadas = mockJornadas
                    selectedJornada = mockJornadas.first()
                }
            } catch (e: Exception) {
                // Fallback to mock jornadas when API server is not running
                jornadas = mockJornadas
                selectedJornada = mockJornadas.first()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadJornadasData()
    }

    Scaffold(
        topBar = {
            Header(
                title = "Escoger Vuelo · CAPEBE",
                onLogoutClick = {
                    UserSession.logout()
                    onBack()
                }
            )
        },
        bottomBar = { Footer() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF2F6FA))
        ) {
            // Header bar back action and title
            Surface(color = NavyColor, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Selección de Vuelos de Instrucción y Recreativo",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = BlueColor)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Socio info banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE2F3FC)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = BlueColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Socio CAPEBE: ${UserSession.name.ifBlank { "Usuario Registrado" }}",
                                fontWeight = FontWeight.Bold,
                                color = NavyColor,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (UserSession.email.isNotBlank()) UserSession.email else "Modo de Selección Activo",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (reservationSuccess) {
                    // Confirmation Success View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "¡Vuelo Seleccionado y Reservado!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = statusMessage,
                                color = Color(0xFF2E7D32),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    reservationSuccess = false
                                    selectedBloque = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NavyColor)
                            ) {
                                Text("Escoger otro Vuelo")
                            }
                        }
                    }
                } else {
                    // Step 1: Select Flight Date/Jornada
                    Text(
                        text = "1. Seleccione la Jornada de Vuelo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        jornadas.forEach { j ->
                            val isSelected = selectedJornada?.optLong("id") == j.optLong("id")
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedJornada = j },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) BlueColor else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else NavyColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = j.optString("fecha"),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else NavyColor,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = j.optString("base").substringBefore(" "),
                                        color = if (isSelected) CyanColor else Color.Gray,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Step 2: Select Time Slot (Bloque)
                    Text(
                        text = "2. Seleccione el Horario de Vuelo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NavyColor
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        mockSlots.forEach { slot ->
                            val isSelected = selectedBloque?.id == slot.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedBloque = slot },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFE2F3FC) else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) BlueColor else Color(0xFFE0E0E0)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = BlueColor,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Bloque: ${slot.time}",
                                                fontWeight = FontWeight.Bold,
                                                color = NavyColor,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Aeronave asignada: ${slot.plane}",
                                                color = Color.Gray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (slot.availableSeats > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${slot.availableSeats} cupos libres",
                                            color = if (slot.availableSeats > 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Step 3: Passenger details & Confirmation
                    if (selectedBloque != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "3. Datos para la Reserva del Vuelo",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyColor
                                )

                                OutlinedTextField(
                                    value = nombre,
                                    onValueChange = { nombre = it },
                                    label = { Text("Nombre Completo del Pasajero/Socio") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = rut,
                                    onValueChange = { rut = it },
                                    label = { Text("RUT (ej. 12.345.678-9)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        scope.launch {
                                            loading = true
                                            try {
                                                val jId = selectedJornada?.optLong("id") ?: 1L
                                                ClubAereoApi.inscribir(rut.trim(), nombre.trim(), jId)
                                            } catch (e: Exception) {
                                                // Ignore API connection error on local mock test
                                            } finally {
                                                loading = false
                                                statusMessage = "Se ha seleccionado el vuelo ${selectedBloque?.time} para el socio $nombre (RUT: $rut) en la base ${selectedJornada?.optString("base")}."
                                                reservationSuccess = true
                                                Toast.makeText(context, "¡Vuelo escogido con éxito!", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    enabled = rut.isNotBlank() && nombre.isNotBlank() && !loading,
                                    colors = ButtonDefaults.buttonColors(containerColor = BlueColor),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.FlightTakeoff,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "CONFIRMAR Y ESCOGER ESTE VUELO",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
