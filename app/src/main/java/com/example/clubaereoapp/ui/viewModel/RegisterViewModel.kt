package com.example.clubaereoapp.ui.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.clubaereoapp.data.database.AppDatabase
import com.example.clubaereoapp.data.local.entity.UsuarioEntity
import com.example.clubaereoapp.model.RegisterModel
import kotlinx.coroutines.launch
import android.content.Context

class RegisterViewModel : ViewModel() {
    var uiState by mutableStateOf(value = RegisterModel())
        private set
    fun onNameChanged(name: String){ uiState = uiState.copy(name = name, errorMessage = null) }
    fun onEmailChanged(email: String){ uiState = uiState.copy(email = email, errorMessage = null) }
    fun onPasswordChanged(password: String){ uiState = uiState.copy(password = password, errorMessage = null) }
    fun onConfirmPasswordChanged(confirmPassword: String){ uiState = uiState.copy(confirmPassword = confirmPassword, errorMessage = null) }
    fun onRegisterClick(context: Context, onSuccess: () -> Unit){

        val state = uiState

        if (state.name.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            uiState = uiState.copy(errorMessage = "Por favor completa todos los campos")
            return
        }

        if (state.password != state.confirmPassword){
            uiState = uiState.copy(errorMessage = "Las contraseña no coinciden")
        }

        //Metodo asíncronico en SQLite con room
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val existingUser = db.usuarioDao().getUserByEmail(state.email)

            if(existingUser != null){
                uiState = uiState.copy(errorMessage = "El correo ya esta registrado.")
            }else{
                val newUser = UsuarioEntity(
                    name = state.name,
                    email = state.email,
                    password = state.password
                )
                db.usuarioDao().registerUser(newUser)
                uiState = uiState.copy(errorMessage = null)
                onSuccess()
            }
        }

    }

}
