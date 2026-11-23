package com.example.clubaereoapp.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class UserRole {
    GUEST,
    SOCIO,
    ADMIN
}

object UserSession {
    var email by mutableStateOf("")
    var name by mutableStateOf("")
    var role by mutableStateOf(UserRole.GUEST)

    val isLoggedIn: Boolean
        get() = role != UserRole.GUEST

    val isAdmin: Boolean
        get() = role == UserRole.ADMIN

    fun login(userEmail: String, userName: String = "") {
        email = userEmail.trim()
        name = if (userName.isNotBlank()) userName else email.substringBefore("@")
        role = if (email.lowercase().endsWith("@capebe.cl")) {
            UserRole.ADMIN
        } else {
            UserRole.SOCIO
        }
    }

    fun logout() {
        email = ""
        name = ""
        role = UserRole.GUEST
    }
}
