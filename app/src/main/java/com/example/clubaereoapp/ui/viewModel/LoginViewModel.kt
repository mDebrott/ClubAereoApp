package com.example.clubaereoapp.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.clubaereoapp.model.LoginModel

class LoginViewModel : ViewModel(){
    // Estado que administra los datos del formulario
    var uiState by mutableStateOf(value = LoginModel())
        private set
    //Metodo que actualiza el correo del usuario
    fun onEmailChanged(email: String){
        uiState = uiState.copy(email = email)
    }
    //Metodo que actualiza la contraseña
    fun onPasswordChanged(password: String){
        uiState = uiState.copy(password = password)
    }
    //Metodo que se ejecuta al hacer click sobre el boton ingresar
    fun onLoginClick() {
        val email = uiState.email
        val password = uiState.password

        println("Iniciando sesion con Email: $email y contraseña: $password")
    }


}