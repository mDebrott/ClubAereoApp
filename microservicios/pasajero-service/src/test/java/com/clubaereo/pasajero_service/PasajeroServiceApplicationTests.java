package com.clubaereo.pasajero_service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Requiere PostgreSQL levantado. Quitar @Disabled cuando exista una BD de pruebas.
@Disabled("Requiere una base de datos PostgreSQL")
@SpringBootTest
class PasajeroServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
