package com.clubaereo.aeronaves_service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Requiere PostgreSQL levantado. Quitar @Disabled cuando exista una BD de pruebas
// (por ejemplo con Testcontainers).
@Disabled("Requiere una base de datos PostgreSQL")
@SpringBootTest
class AeronavesServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
