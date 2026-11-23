package com.example.clubaereoapp

import com.example.clubaereoapp.ui.viewModel.LoginViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel()
    }

    @Test
    fun testInitialUiStateIsEmpty() {
        assertEquals("", viewModel.uiState.email)
        assertEquals("", viewModel.uiState.password)
        assertNull(viewModel.errorMessage)
    }

    @Test
    fun testOnEmailChangedUpdatesState() {
        viewModel.onEmailChanged("test@capebe.cl")
        assertEquals("test@capebe.cl", viewModel.uiState.email)
    }

    @Test
    fun testOnPasswordChangedUpdatesState() {
        viewModel.onPasswordChanged("secret123")
        assertEquals("secret123", viewModel.uiState.password)
    }

    @Test
    fun testDeterminarRolAdminForCapebeEmail() {
        val role1 = viewModel.determinarRol("admin@capebe.cl")
        val role2 = viewModel.determinarRol("DIRECTOR@CAPEBE.CL")
        assertEquals("ADMIN", role1)
        assertEquals("ADMIN", role2)
    }

    @Test
    fun testDeterminarRolSocioForRegularEmail() {
        val role1 = viewModel.determinarRol("user@gmail.com")
        val role2 = viewModel.determinarRol("socio@outlook.es")
        assertEquals("SOCIO", role1)
        assertEquals("SOCIO", role2)
    }
}
