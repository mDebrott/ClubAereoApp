package com.example.clubaereoapp.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Para emulador Android: 10.0.2.2 apunta al PC anfitrión. En dispositivo físico cambiar por IP LAN.
object ClubAereoApi {
    private const val VUELOS = "http://10.0.2.2:8083"
    private const val AERONAVES = "http://10.0.2.2:8081"
    private const val PASAJEROS = "http://10.0.2.2:8082"

    private suspend fun request(base: String, path: String, method: String = "GET", body: JSONObject? = null): String = withContext(Dispatchers.IO) {
        val conn = (URL(base + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method; connectTimeout = 12000; readTimeout = 12000
            setRequestProperty("Accept", "application/json")
            if (body != null) { doOutput = true; setRequestProperty("Content-Type", "application/json") }
        }
        try {
            if (body != null) conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw IllegalStateException("HTTP $code: $text")
            text
        } finally { conn.disconnect() }
    }

    suspend fun jornadas(): JSONArray = JSONArray(request(VUELOS, "/api/jornadas"))
    suspend fun crearJornada(fecha: String, base: String): JSONObject = JSONObject(request(VUELOS, "/api/jornadas", "POST", JSONObject().put("fecha", fecha).put("base", base)))
    suspend fun bloques(id: Long): JSONArray = JSONArray(request(VUELOS, "/api/jornadas/$id/bloques"))
    suspend fun crearBloque(id: Long, inicio: String, fin: String): JSONObject = JSONObject(request(VUELOS, "/api/jornadas/$id/bloques", "POST", JSONObject().put("horaInicio", inicio).put("horaFin", fin)))

    suspend fun pasajeros(jornadaId: Long): JSONArray = JSONArray(request(PASAJEROS, "/api/pasajeros?jornadaId=$jornadaId"))
    suspend fun inscribir(rut: String, nombre: String, jornadaId: Long): JSONObject = JSONObject(request(PASAJEROS, "/api/pasajeros", "POST", JSONObject().put("rut", rut).put("nombreCompleto", nombre).put("jornadaId", jornadaId)))
    suspend fun aeronaves(): JSONArray = JSONArray(request(AERONAVES, "/api/aeronaves"))
    suspend fun pilotos(): JSONArray = JSONArray(request(AERONAVES, "/api/pilotos"))
    suspend fun asignar(pasajeroId: Long, bloqueId: Long, aeronaveId: Long, pilotoId: Long): JSONObject = JSONObject(request(VUELOS, "/api/asignaciones", "POST", JSONObject().put("pasajeroId", pasajeroId).put("bloqueId", bloqueId).put("aeronaveId", aeronaveId).put("pilotoId", pilotoId)))
    suspend fun confirmarAsignacion(id: Long): JSONObject = JSONObject(request(VUELOS, "/api/asignaciones/$id/confirmar", "PUT"))
    suspend fun abrirVuelo(aeronaveId: Long, pilotoId: Long, jornadaId: Long, horometro: String): JSONObject = JSONObject(request(AERONAVES, "/api/vuelos", "POST", JSONObject().put("aeronaveId", aeronaveId).put("pilotoId", pilotoId).put("jornadaId", jornadaId).put("horometroInicial", horometro)))
    suspend fun cerrarVuelo(id: Long, horometroFinal: String, tripulantes: Int): JSONObject = JSONObject(request(AERONAVES, "/api/vuelos/$id/cierre", "PUT", JSONObject().put("horometroFinal", horometroFinal).put("nTripulantes", tripulantes)))
}
