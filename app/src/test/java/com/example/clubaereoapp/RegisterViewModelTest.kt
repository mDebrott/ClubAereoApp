package com.example.clubaereoapp

import com.example.clubaereoapp.ui.viewModel.RegisterViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class RegisterViewModelTest {

    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        viewModel = RegisterViewModel()
    }

    @Test
    fun testInitialRegisterStateIsEmpty() {
        assertEquals("", viewModel.uiState.name)
        assertEquals("", viewModel.uiState.email)
        assertEquals("", viewModel.uiState.password)
        assertEquals("", viewModel.uiState.confirmPassword)
        assertNull(viewModel.uiState.errorMessage)
    }

    @Test
    fun testFormInputChangesUpdateState() {
        viewModel.onNameChanged("Carlos Piloto")
        viewModel.onEmailChanged("carlos@capebe.cl")
        viewModel.onPasswordChanged("123456")
        viewModel.onConfirmPasswordChanged("123456")

        assertEquals("Carlos Piloto", viewModel.uiState.name)
        assertEquals("carlos@capebe.cl", viewModel.uiState.email)
        assertEquals("123456", viewModel.uiState.password)
        assertEquals("123456", viewModel.uiState.confirmPassword)
        assertNull(viewModel.uiState.errorMessage)
    }
}
