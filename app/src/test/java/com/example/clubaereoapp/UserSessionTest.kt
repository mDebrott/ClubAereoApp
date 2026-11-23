package com.example.clubaereoapp

import com.example.clubaereoapp.data.UserRole
import com.example.clubaereoapp.data.UserSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UserSessionTest {

    @Before
    fun setUp() {
        UserSession.logout()
    }

    @Test
    fun testDefaultStateIsGuest() {
        assertFalse(UserSession.isLoggedIn)
        assertFalse(UserSession.isAdmin)
        assertEquals(UserRole.GUEST, UserSession.role)
        assertEquals("", UserSession.email)
    }

    @Test
    fun testLoginAsAdminWithCapebeDomain() {
        UserSession.login("admin@capebe.cl", "Director CAPEBE")
        assertTrue(UserSession.isLoggedIn)
        assertTrue(UserSession.isAdmin)
        assertEquals(UserRole.ADMIN, UserSession.role)
        assertEquals("admin@capebe.cl", UserSession.email)
        assertEquals("Director CAPEBE", UserSession.name)
    }

    @Test
    fun testLoginAsNormalSocio() {
        UserSession.login("pilot@gmail.com", "Juan Pérez")
        assertTrue(UserSession.isLoggedIn)
        assertFalse(UserSession.isAdmin)
        assertEquals(UserRole.SOCIO, UserSession.role)
        assertEquals("pilot@gmail.com", UserSession.email)
    }

    @Test
    fun testLogoutClearsSession() {
        UserSession.login("admin@capebe.cl", "Admin")
        assertTrue(UserSession.isLoggedIn)

        UserSession.logout()
        assertFalse(UserSession.isLoggedIn)
        assertFalse(UserSession.isAdmin)
        assertEquals("", UserSession.email)
        assertEquals("", UserSession.name)
    }
}
