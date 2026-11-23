package com.example.clubaereoapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.clubaereoapp.ui.screen.BloquesScreen
import com.example.clubaereoapp.ui.screen.IncioScreen
import com.example.clubaereoapp.ui.screen.JornadasScreen
import com.example.clubaereoapp.ui.screen.LoginScreen
import com.example.clubaereoapp.ui.screen.OperacionBloqueScreen
import com.example.clubaereoapp.ui.screen.PrincipalScreen
import com.example.clubaereoapp.ui.screen.RegisterScreen
import com.example.clubaereoapp.ui.screen.SeleccionVueloScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val PRINCIPAL = "principal"
    const val INICIO = "inicio"
    const val SELECCION_VUELO = "seleccion_vuelo"
    const val JORNADAS = "jornadas"
    const val BLOQUES = "bloques"
    const val OPERACION = "operacion"
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    var jornadaId by remember { mutableStateOf(0L) }
    var fecha by remember { mutableStateOf("") }
    var base by remember { mutableStateOf("") }
    var bloqueId by remember { mutableStateOf(0L) }

    NavHost(navController = nav, startDestination = Routes.PRINCIPAL) {
        composable(Routes.PRINCIPAL) {
            PrincipalScreen(
                onNavigateToLogin = { nav.navigate(Routes.LOGIN) },
                onNavigateToRegister = { nav.navigate(Routes.REGISTER) },
                onNavigateToSeleccionVuelo = { nav.navigate(Routes.SELECCION_VUELO) },
                onNavigateToOperaciones = { nav.navigate(Routes.JORNADAS) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { nav.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    nav.navigate(Routes.PRINCIPAL) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    nav.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.SELECCION_VUELO) {
            SeleccionVueloScreen(
                onBack = { nav.popBackStack() }
            )
        }

        composable(Routes.INICIO) {
            IncioScreen()
        }

        composable(Routes.JORNADAS) {
            JornadasScreen(
                onBack = { nav.popBackStack() },
                onSelect = { id, f, b ->
                    jornadaId = id
                    fecha = f
                    base = b
                    nav.navigate(Routes.BLOQUES)
                }
            )
        }

        composable(Routes.BLOQUES) {
            BloquesScreen(
                jornadaId,
                fecha,
                base,
                onBack = { nav.popBackStack() },
                onSelectBlock = { id ->
                    bloqueId = id
                    nav.navigate(Routes.OPERACION)
                }
            )
        }

        composable(Routes.OPERACION) {
            OperacionBloqueScreen(
                jornadaId,
                bloqueId,
                fecha,
                base,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
