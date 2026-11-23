package com.example.clubaereoapp.ui.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clubaereoapp.data.UserSession
import com.example.clubaereoapp.data.database.AppDatabase
import com.example.clubaereoapp.data.local.entity.UsuarioEntity
import com.example.clubaereoapp.model.LoginModel
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    var uiState by mutableStateOf(value = LoginModel())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun onEmailChanged(email: String) {
        uiState = uiState.copy(email = email)
        errorMessage = null
    }

    fun onPasswordChanged(password: String) {
        uiState = uiState.copy(password = password)
        errorMessage = null
    }

    fun determinarRol(correo: String): String {
        return if (correo.trim().lowercase().endsWith("@capebe.cl")) "ADMIN" else "SOCIO"
    }

    fun onLoginClick(context: Context, onError: () -> Unit, onSuccess: (role: String) -> Unit) {
        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Ingrese correo y contraseña"
            onError()
            return
        }

        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val user = db.usuarioDao().getUserByEmail(email)

            if (user != null) {
                if (user.password == password) {
                    val role = determinarRol(email)
                    UserSession.login(email, user.name)
                    errorMessage = null
                    onSuccess(role)
                } else {
                    errorMessage = "Contraseña incorrecta"
                    onError()
                }
            } else if (email.lowercase().endsWith("@capebe.cl")) {
                // Auto-creación de cuenta Admin para facilitar el inicio de sesión @capebe.cl
                val adminName = "Administrador CAPEBE"
                val newAdmin = UsuarioEntity(name = adminName, email = email, password = password)
                db.usuarioDao().registerUser(newAdmin)
                UserSession.login(email, adminName)
                errorMessage = null
                onSuccess("ADMIN")
            } else {
                errorMessage = "Correo no registrado. Por favor regístrese."
                onError()
            }
        }
    }
}
