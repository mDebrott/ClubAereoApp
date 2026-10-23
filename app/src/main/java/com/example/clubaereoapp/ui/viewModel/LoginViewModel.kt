package com.example.clubaereoapp.ui.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clubaereoapp.data.database.AppDatabase
import com.example.clubaereoapp.model.LoginModel
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel(){
    // Estado que administra los datos del formulario
    var uiState by mutableStateOf(value = LoginModel())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    //Metodo que actualiza el correo del usuario
    fun onEmailChanged(email: String){
        uiState = uiState.copy(email = email)
        errorMessage = null
    }
    //Metodo que actualiza la contraseña
    fun onPasswordChanged(password: String){
        uiState = uiState.copy(password = password)
        errorMessage = null
    }
    //Metodo que se ejecuta al hacer click sobre el boton ingresar
    fun onLoginClick(context: Context, onError: () -> Unit, onSuccess: () -> Unit) {
        val email = uiState.email
        val password = uiState.password

        if(email.isBlank() || password.isBlank()){
            errorMessage = "Ingrese correo y contraseña"
            onError()
            return
        }

        viewModelScope.launch {
            val db = AppDatabase.getDatabase(context)
            val user = db.usuarioDao().getUserByEmail(email)

            if(user != null && user.password == password){
                errorMessage = null
                onSuccess() // La sesion se inicia
            }else{
                errorMessage = "Correo o contraseña incorrectos"
                onError()
            }
        }
    }


}