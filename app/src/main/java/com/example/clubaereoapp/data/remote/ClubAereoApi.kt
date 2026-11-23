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
    private const val EXTERNAL_WEATHER_API = "https://api.open-meteo.com/v1/forecast?latitude=-41.4657&longitude=-72.9429&current=temperature_2m,relative_humidity_2m,wind_speed_10m,weather_code"

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

    // Microservicio Vuelos
    suspend fun jornadas(): JSONArray = JSONArray(request(VUELOS, "/api/jornadas"))
    suspend fun crearJornada(fecha: String, base: String): JSONObject = JSONObject(request(VUELOS, "/api/jornadas", "POST", JSONObject().put("fecha", fecha).put("base", base)))
    suspend fun bloques(id: Long): JSONArray = JSONArray(request(VUELOS, "/api/jornadas/$id/bloques"))
    suspend fun crearBloque(id: Long, inicio: String, fin: String): JSONObject = JSONObject(request(VUELOS, "/api/jornadas/$id/bloques", "POST", JSONObject().put("horaInicio", inicio).put("horaFin", fin)))

    // Microservicio Pasajeros
    suspend fun pasajeros(jornadaId: Long): JSONArray = JSONArray(request(PASAJEROS, "/api/pasajeros?jornadaId=$jornadaId"))
    suspend fun inscribir(rut: String, nombre: String, jornadaId: Long): JSONObject = JSONObject(request(PASAJEROS, "/api/pasajeros", "POST", JSONObject().put("rut", rut).put("nombreCompleto", nombre).put("jornadaId", jornadaId)))

    // Microservicio Aeronaves
    suspend fun aeronaves(): JSONArray = JSONArray(request(AERONAVES, "/api/aeronaves"))
    suspend fun pilotos(): JSONArray = JSONArray(request(AERONAVES, "/api/pilotos"))
    suspend fun asignar(pasajeroId: Long, bloqueId: Long, aeronaveId: Long, pilotoId: Long): JSONObject = JSONObject(request(VUELOS, "/api/asignaciones", "POST", JSONObject().put("pasajeroId", pasajeroId).put("bloqueId", bloqueId).put("aeronaveId", aeronaveId).put("pilotoId", pilotoId)))
    suspend fun confirmarAsignacion(id: Long): JSONObject = JSONObject(request(VUELOS, "/api/asignaciones/$id/confirmar", "PUT"))
    suspend fun abrirVuelo(aeronaveId: Long, pilotoId: Long, jornadaId: Long, horometro: String): JSONObject = JSONObject(request(AERONAVES, "/api/vuelos", "POST", JSONObject().put("aeronaveId", aeronaveId).put("pilotoId", pilotoId).put("jornadaId", jornadaId).put("horometroInicial", horometro)))
    suspend fun cerrarVuelo(id: Long, horometroFinal: String, tripulantes: Int): JSONObject = JSONObject(request(AERONAVES, "/api/vuelos/$id/cierre", "PUT", JSONObject().put("horometroFinal", horometroFinal).put("nTripulantes", tripulantes)))

    // API Externa (IE 3.1.4): Clima Aeronáutico en Tiempo Real (Open-Meteo)
    suspend fun climaAeronautico(): JSONObject = withContext(Dispatchers.IO) {
        val conn = (URL(EXTERNAL_WEATHER_API).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code in 200..299 && text.isNotBlank()) JSONObject(text) else JSONObject()
        } catch (e: Exception) {
            // Retorna clima estimado por defecto si no hay conexión a internet
            JSONObject().put("current", JSONObject().put("temperature_2m", 16.5).put("wind_speed_10m", 12.0).put("relative_humidity_2m", 65))
        } finally {
            conn.disconnect()
        }
    }
}
