package com.example.clubaereoapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clubaereoapp.data.remote.ClubAereoApi
import kotlinx.coroutines.launch
import org.json.JSONObject

private val OpNavy = Color(0xFF12304A)
private val OpBg = Color(0xFFF2F6FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperacionBloqueScreen(
    jornadaId: Long,
    bloqueId: Long,
    fecha: String,
    base: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var pasajeros by remember { mutableStateOf(listOf<JSONObject>()) }
    var aeronaves by remember { mutableStateOf(listOf<JSONObject>()) }
    var pilotos by remember { mutableStateOf(listOf<JSONObject>()) }
    var rut by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var selectedPassenger by remember { mutableStateOf<JSONObject?>(null) }
    var selectedAircraft by remember { mutableStateOf<JSONObject?>(null) }
    var selectedPilot by remember { mutableStateOf<JSONObject?>(null) }
    var horometro by remember { mutableStateOf("") }
    var vueloId by remember { mutableStateOf("") }
    var horometroFinal by remember { mutableStateOf("") }
    var tripulantes by remember { mutableStateOf("1") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    fun reload() {
        scope.launch {
            loading = true
            try {
                pasajeros = toList(ClubAereoApi.pasajeros(jornadaId))
                aeronaves = toList(ClubAereoApi.aeronaves())
                pilotos = toList(ClubAereoApi.pilotos())
            } catch (e: Exception) {
                message = e.message ?: "Error cargando datos"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(jornadaId, bloqueId) { reload() }

    Column(
        Modifier
            .fillMaxSize()
            .background(OpBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Volver a bloques") }
        Text(
            "Operación del vuelo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = OpNavy
        )
        Text("Jornada $fecha · $base · Bloque #$bloqueId", color = Color.Gray)

        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (message.isNotBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F1F8))) {
                Text(message, Modifier.padding(12.dp))
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Inscribir pasajero", fontWeight = FontWeight.Bold, color = OpNavy)
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre completo") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = rut,
                    onValueChange = { rut = it },
                    label = { Text("RUT") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            message = ""
                            try {
                                ClubAereoApi.inscribir(rut.trim(), nombre.trim(), jornadaId)
                                message = "Pasajero inscrito"
                                rut = ""
                                nombre = ""
                                reload()
                            } catch (e: Exception) {
                                message = e.message ?: "No se pudo inscribir"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = rut.isNotBlank() && nombre.isNotBlank() && !loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar pasajero")
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Asignar pasajero al bloque", fontWeight = FontWeight.Bold, color = OpNavy)
                JsonDropdown(
                    label = "Pasajero",
                    options = pasajeros,
                    selected = selectedPassenger,
                    onSelect = { selectedPassenger = it },
                    display = { "${it.optString("nombreCompleto")} · ${it.optString("rut")} (#${it.optLong("id")})" }
                )
                JsonDropdown(
                    label = "Aeronave disponible",
                    options = aeronaves.filter { it.optBoolean("disponible", true) },
                    selected = selectedAircraft,
                    onSelect = { selectedAircraft = it },
                    display = { "${it.optString("matricula")} · ${it.optString("modelo")} · ${it.optInt("capacidad")} plazas" }
                )
                JsonDropdown(
                    label = "Piloto",
                    options = pilotos.filter { it.optBoolean("licenciaDgacVigente", false) },
                    selected = selectedPilot,
                    onSelect = { selectedPilot = it },
                    display = { "${it.optString("nombre")} · licencia DGAC vigente" }
                )
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            message = ""
                            try {
                                val p = selectedPassenger ?: return@launch
                                val a = selectedAircraft ?: return@launch
                                val pilot = selectedPilot ?: return@launch
                                val result = ClubAereoApi.asignar(
                                    p.optLong("id"),
                                    bloqueId,
                                    a.optLong("id"),
                                    pilot.optLong("id")
                                )
                                message = "Asignación creada: #${result.optLong("id")} · ${result.optString("estado")}"
                            } catch (e: Exception) {
                                message = e.message ?: "No se pudo crear la asignación"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = selectedPassenger != null && selectedAircraft != null && selectedPilot != null && !loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Crear asignación")
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Registro de vuelo / horómetro", fontWeight = FontWeight.Bold, color = OpNavy)
                Text(
                    "El registro de vuelo se abre con aeronave y piloto. El backend valida disponibilidad y horómetro.",
                    style = MaterialTheme.typography.bodySmall
                )
                JsonDropdown(
                    label = "Aeronave",
                    options = aeronaves,
                    selected = selectedAircraft,
                    onSelect = { selectedAircraft = it },
                    display = { "${it.optString("matricula")} · ${it.optString("modelo")}" }
                )
                JsonDropdown(
                    label = "Piloto",
                    options = pilotos,
                    selected = selectedPilot,
                    onSelect = { selectedPilot = it },
                    display = { it.optString("nombre") }
                )
                OutlinedTextField(
                    value = horometro,
                    onValueChange = { horometro = it },
                    label = { Text("Horómetro inicial (ej. 1234.5)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        scope.launch {
                            loading = true
                            message = ""
                            try {
                                val a = selectedAircraft ?: return@launch
                                val pilot = selectedPilot ?: return@launch
                                val result = ClubAereoApi.abrirVuelo(
                                    a.optLong("id"),
                                    pilot.optLong("id"),
                                    jornadaId,
                                    horometro
                                )
                                vueloId = result.optLong("id").toString()
                                message = "Vuelo abierto: #$vueloId"
                            } catch (e: Exception) {
                                message = e.message ?: "No se pudo abrir el vuelo"
                            } finally {
                                loading = false
                            }
                        }
                    },
                    enabled = selectedAircraft != null && selectedPilot != null && horometro.toBigDecimalOrNull() != null && vueloId.isBlank() && !loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Abrir registro de vuelo")
                }

                if (vueloId.isNotBlank()) {
                    Text("Vuelo activo #$vueloId", color = OpNavy, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = horometroFinal,
                        onValueChange = { horometroFinal = it },
                        label = { Text("Horómetro final") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tripulantes,
                        onValueChange = { tripulantes = it.filter(Char::isDigit) },
                        label = { Text("N° tripulantes (mín. 1)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                message = ""
                                try {
                                    val result = ClubAereoApi.cerrarVuelo(
                                        vueloId.toLong(),
                                        horometroFinal,
                                        tripulantes.toInt()
                                    )
                                    message = "Vuelo cerrado · horas: ${result.optString("horasConsumidas")}"
                                    vueloId = ""
                                } catch (e: Exception) {
                                    message = e.message ?: "No se pudo cerrar el vuelo"
                                } finally {
                                    loading = false
                                }
                            }
                        },
                        enabled = horometroFinal.toBigDecimalOrNull() != null && (tripulantes.toIntOrNull() ?: 0) > 0 && !loading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar vuelo")
                    }
                }
            }
        }

        Text("Pasajeros de la jornada (${pasajeros.size})", fontWeight = FontWeight.Bold, color = OpNavy)
        LazyColumn(
            Modifier
                .heightIn(max = 260.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(pasajeros, key = { it.optLong("id") }) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(12.dp)) {
                        Text(p.optString("nombreCompleto"), fontWeight = FontWeight.Bold)
                        Text(
                            "RUT ${p.optString("rut")} · ID ${p.optLong("id")}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        OutlinedButton(onClick = { reload() }, modifier = Modifier.fillMaxWidth()) {
            Text("Actualizar datos")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JsonDropdown(
    label: String,
    options: List<JSONObject>,
    selected: JSONObject?,
    onSelect: (JSONObject) -> Unit,
    display: (JSONObject) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let(display) ?: "Seleccionar",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(display(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun toList(array: org.json.JSONArray): List<JSONObject> =
    (0 until array.length()).mapNotNull { array.optJSONObject(it) }