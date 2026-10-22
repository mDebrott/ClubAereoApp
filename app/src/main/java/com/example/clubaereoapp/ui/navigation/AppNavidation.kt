package com.example.clubaereoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.clubaereoapp.ui.screen.LoginScreen
import com.example.clubaereoapp.ui.screen.PrincipalScreen
import com.example.clubaereoapp.ui.screen.RegisterScreen

//Maldigo el dia en que me hice tan estupidamente flojo y desordenado

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PRINCIPAL = "principal"
}

@Composable
fun AppNavigation ()
{
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.PRINCIPAL
    ){
        composable(Routes.PRINCIPAL){
            PrincipalScreen(
                onNavigateToLogin = { //??? Algo se me esta pasando por alto (pero bueno mañana lo vere)
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER){
            RegisterScreen()
        }
    }
}
