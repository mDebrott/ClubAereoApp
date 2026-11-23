package com.example.clubaereoapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clubaereoapp.data.remote.ClubAereoApi
import kotlinx.coroutines.launch
import org.json.JSONObject

private val OperationsNavy = Color(0xFF12304A)
private val OperationsBlue = Color(0xFF1F84B8)
private val OperationsBackground = Color(0xFFF2F6FA)

@Composable
fun JornadasScreen(onBack: () -> Unit, onSelect: (Long, String, String) -> Unit) {
    val scope = rememberCoroutineScope()
    var jornadas by remember { mutableStateOf(listOf<JSONObject>()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var base by remember { mutableStateOf("TOBALABA") }

    fun loadJornadas() {
        scope.launch {
            loading = true
            error = ""
            try {
                val arr = ClubAereoApi.jornadas()
                jornadas = (0 until arr.length())
                    .map { arr.getJSONObject(it) }
                    .sortedByDescending { it.optString("fecha") }
            } catch (e: Exception) {
                error = e.message ?: "No se pudieron cargar jornadas"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadJornadas() }

    Column(
        Modifier
            .fillMaxSize()
            .background(OperationsBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Jornadas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = OperationsNavy
            )
            TextButton(onClick = onBack) { Text("Volver") }
        }
        Text("Selecciona una jornada para administrar sus bloques y vuelos.", color = Color.Gray)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Crear jornada", fontWeight = FontWeight.Bold, color = OperationsNavy)
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = base == "TOBALABA",
                        onClick = { base = "TOBALABA" },
                        label = { Text("Tobalaba") }
                    )
                    FilterChip(
                        selected = base == "TALCAHUANO",
                        onClick = { base = "TALCAHUANO" },
                        label = { Text("Talcahuano") }
                    )
                }
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = ""
                            try {
                                ClubAereoApi.crearJornada(fecha, base)
                                fecha = ""
                                loadJornadas()
                            } catch (e: Exception) {
                                error = e.message ?: "Error al crear jornada"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = fecha.matches(Regex("\\d{4}-\\d{2}-\\d{2}")),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear jornada")
                }
            }
        }

        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)

        Text("Jornadas registradas", fontWeight = FontWeight.Bold, color = OperationsNavy)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(jornadas, key = { it.optLong("id") }) { j ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(j.optLong("id"), j.optString("fecha"), j.optString("base"))
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            j.optString("fecha"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OperationsNavy
                        )
                        Text("Base: ${j.optString("base")}")
                        Text("Jornada #${j.optLong("id")} · Ver bloques →", color = OperationsBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun BloquesScreen(
    jornadaId: Long,
    fecha: String,
    base: String,
    onBack: () -> Unit,
    onSelectBlock: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    var bloques by remember { mutableStateOf(listOf<JSONObject>()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var inicio by remember { mutableStateOf("09:00") }
    var fin by remember { mutableStateOf("10:00") }

    fun loadBlocks() {
        scope.launch {
            loading = true
            error = ""
            try {
                val arr = ClubAereoApi.bloques(jornadaId)
                bloques = (0 until arr.length()).map { arr.getJSONObject(it) }
            } catch (e: Exception) {
                error = e.message ?: "No se pudieron cargar bloques"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(jornadaId) { loadBlocks() }

    Column(
        Modifier
            .fillMaxSize()
            .background(OperationsBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Jornadas") }
        Text(
            "Bloques de vuelo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = OperationsNavy
        )
        Text("$fecha · $base · Jornada #$jornadaId", color = Color.Gray)

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Agregar bloque horario", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inicio,
                        onValueChange = { inicio = it },
                        label = { Text("Inicio HH:mm") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fin,
                        onValueChange = { fin = it },
                        label = { Text("Fin HH:mm") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            error = ""
                            try {
                                ClubAereoApi.crearBloque(jornadaId, inicio, fin)
                                loadBlocks()
                            } catch (e: Exception) {
                                error = e.message ?: "Error al crear bloque"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = inicio.matches(Regex("\\d{2}:\\d{2}")) && fin.matches(Regex("\\d{2}:\\d{2}")),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear bloque")
                }
            }
        }

        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (error.isNotBlank()) Text(error, color = MaterialTheme.colorScheme.error)

        Text("Horarios", fontWeight = FontWeight.Bold, color = OperationsNavy)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(bloques, key = { it.optLong("id") }) { b ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelectBlock(b.optLong("id")) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "${b.optString("horaInicio")} – ${b.optString("horaFin")}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = OperationsNavy
                        )
                        Text(
                            "Bloque #${b.optLong("id")} · ${b.optJSONArray("asignaciones")?.length() ?: 0} asignaciones"
                        )
                        Text("Abrir operación →", color = OperationsBlue)
                    }
                }
            }
        }
    }
}
