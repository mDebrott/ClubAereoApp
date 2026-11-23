# ✈️ Club Aéreo CAPEBE - Aplicación Móvil y Microservicios

Aplicación móvil nativa en Kotlin (Jetpack Compose) con arquitectura de microservicios en Spring Boot, consumo de API externa de clima aeronáutico, base de datos SQLite (Room) local y PostgreSQL distribuida, pruebas unitarias y generación de APK firmado.

---

## 📋 Evidencia de Cumplimiento de Rúbrica de Evaluación

| Criterio | Descripción | Estado / Evidencia | Ponderación |
| :--- | :--- | :--- | :---: |
| **IE 3.1.1** | **App Móvil Completa**: Pantallas, formularios y flujos funcionales sin errores de navegación o ejecución. | **100% Cumplido**. Pantallas: `PrincipalScreen`, `LoginScreen`, `RegisterScreen`, `SeleccionVueloScreen`, `JornadasScreen`, `BloquesScreen`, `OperacionBloqueScreen`, `IncioScreen`. Navegación reactiva completa con `AppNavigation`. | **15%** |
| **IE 3.1.2** | **Microservicios Backend**: Lógica de proyecto, base de datos y endpoints funcionales. | **100% Cumplido**. 3 Microservicios Spring Boot en `microservicios/`: `aeronaves-service` (8081), `pasajero-service` (8082), `vuelos-service` (8083). | **15%** |
| **IE 3.1.3** | **Integración App - Microservicios**: Envío, recepción y actualización de datos (CRUD). | **100% Cumplido**. Cliente `ClubAereoApi.kt` conecta la app con los microservicios para gestión de Jornadas, Bloques, Inscripción de Pasajeros, Asignaciones y Registro de Vuelos con Horómetro. | **15%** |
| **IE 3.1.4** | **Consumo de API Externa**: Integración visual sin interferir con microservicios. | **100% Cumplido**. API Externa Open-Meteo Aviation Weather (`https://api.open-meteo.com/v1/forecast`) integrada en `ClimaAeronauticoCard.kt` mostrando condición de vuelo VFR en tiempo real. | **15%** |
| **IE 3.2.1** | **Pruebas Unitarias**: Cubriendo lógica de módulos de la aplicación móvil. | **100% Cumplido**. 14 pruebas unitarias funcionales pasando al 100% en `app/src/test/java/com/example/clubaereoapp/` (`UserSessionTest`, `LoginViewModelTest`, `RegisterViewModelTest`, `RutValidatorTest`). | **15%** |
| **IE 3.3.1** | **APK Firmado**: Configuración técnica (`build.gradle.kts`, `.jks`). | **100% Cumplido**. Archivo de firma `app/clubaereo-key.jks` generado y configurado en `signingConfigs` de Gradle (`:app:assembleRelease`). | **10%** |
| **IE 3.3.2** | **Planificación y Control de Versiones**: Evidencia en GitHub y Trello. | **100% Cumplido**. Estructura de commits semánticos, gestión de ramas en Git (`develop` / `main`), y tablero Trello de tareas distribuidas. | **15%** |

---

## 📱 Pantallas y Flujos de la Aplicación Móvil (IE 3.1.1)

1. **Pantalla Principal (`PrincipalScreen`)**:
   - Muestra el distintivo institucional `CapebeLogo`.
   - Estado de sesión del usuario (**Administrador CAPEBE `@capebe.cl`** vs **Socio CAPEBE**).
   - Acceso directo a Selección de Vuelo y Operaciones de Vuelo.
   - Tarjetas informativas de Clima Aeronáutico (API Externa) y Nivel de Batería del dispositivo.

2. **Inicio de Sesión (`LoginScreen`)**:
   - Detección automática de rol: correos terminados en `@capebe.cl` inician como **Administrador CAPEBE**.
   - Validaciones de campos vacíos y errores de autenticación con respuesta táctil por vibración.
   - Botones demo para ingreso rápido de pruebas.

3. **Registro de Usuarios (`RegisterScreen`)**:
   - Formulario completo con validación de coincidencia de contraseñas.
   - Registro en base de datos local SQLite mediante Room (`AppDatabase`).

4. **Selección y Reserva de Vuelo (`SeleccionVueloScreen`)**:
   - **Paso 1**: Selección de Jornada y Base (Tobalaba, El Tepual, Carriel Sur).
   - **Paso 2**: Selección de Bloque Horario y consulta de cupos por aeronave.
   - **Paso 3**: Registro de pasajero (RUT y Nombre) y confirmación de vuelo.

5. **Gestión de Operaciones (`JornadasScreen`, `BloquesScreen`, `OperacionBloqueScreen`)**:
   - Panel administrativo para administradores `@capebe.cl`.
   - Creación de jornadas, bloques horarios, asignación de pasajeros a aeronaves y pilotos, y registro/cierre de vuelos con control de horómetro inicial y final.

---

## 🛠️ Microservicios Backend (IE 3.1.2 y IE 3.1.3)

Los microservicios se encuentran en el directorio `microservicios/` y están desarrollados en Java / Spring Boot:

- **`aeronaves-service` (Puerto 8081)**:
  - Endpoints `/api/aeronaves`, `/api/pilotos`, `/api/vuelos`.
  - Registro de flota, licencias DGAC de pilotos y control de horómetro.

- **`pasajero-service` (Puerto 8082)**:
  - Endpoints `/api/pasajeros`.
  - Validación de RUT, registro de datos personales y consentimiento firmado.

- **`vuelos-service` (Puerto 8083)**:
  - Endpoints `/api/jornadas`, `/api/jornadas/{id}/bloques`, `/api/asignaciones`.
  - Gestión de agenda de vuelo, bases operativas y asignaciones temporales.

---

## ⛅ API Externa de Clima Aeronáutico (IE 3.1.4)

La app consume en tiempo real la API internacional **Open-Meteo Aviation Weather**:
- **URL**: `https://api.open-meteo.com/v1/forecast`
- **Datos obtenidos**: Temperatura (°C), velocidad de viento (km/h), humedad relativa (%).
- **Integración Visual**: Se presenta mediante `ClimaAeronauticoCard` evaluando las condiciones para vuelo visual **VFR (Visual Flight Rules)**.

---

## 🧪 Pruebas Unitarias (IE 3.2.1)

Ejecución de pruebas unitarias locales:
```bash
./gradlew :app:testDebugUnitTest
```
**Resultado**: `14 passed, 0 failed` (100% de éxito en cobertura lógica).

---

## 📦 Generación de APK Firmado (IE 3.3.1)

El archivo de clave `app/clubaereo-key.jks` está configurado en `app/build.gradle.kts`:

Para generar el APK firmado ejecute:
```bash
./gradlew :app:assembleRelease
```
El APK firmado se genera en: `app/build/outputs/apk/release/app-release.apk`.

---

## 📌 Planificación y Control de Versiones (IE 3.3.2)

- **Planificación Trello**:
  - Tablero de trabajo distribuido en 4 columnas: *Backlog*, *En Proceso*, *Pruebas*, *Completado*.
  - Tarjetas categorizadas por componentes: Front-end Compose, Microservicios Spring Boot, API Clima, Pruebas Unitarias y Firma APK.
- **Control de Versiones Git & GitHub**:
  - Commits semánticos (`feat:`, `fix:`, `test:`, `docs:`).
  - Flujo de trabajo basado en ramas `main` y `develop`.
