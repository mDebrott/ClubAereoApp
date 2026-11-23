# ClubAereoApp – Operaciones por jornada y bloque

## Android
La app usa `ClubAereoApi` para consumir tres servicios locales desde el emulador Android:
- Vuelos: `http://10.0.2.2:8083`
- Aeronaves: `http://10.0.2.2:8081`
- Pasajeros: `http://10.0.2.2:8082`

En un teléfono físico, cambia `10.0.2.2` por la IP LAN del PC que ejecuta Spring Boot.

## Flujo
1. Principal > Operaciones > Jornadas.
2. Crear o seleccionar una jornada.
3. Crear o seleccionar bloque horario.
4. En Operación del bloque: registrar pasajeros, consultar pasajeros de la jornada, seleccionar pasajero/aeronave/piloto y crear asignación.
5. Abrir registro de vuelo y cerrar con horómetro final y número de tripulantes.

## Requisitos backend actuales
- El microservicio pasajeros solo recibe `rut`, `nombreCompleto` y `jornadaId`. La pantalla de inscripción se ajusta a ese contrato; oficina bancaria, condición de socio y teléfono todavía no están soportados por el DTO/backend.
- La confirmación de asignación exige consentimiento firmado y peso del pasajero; estos datos deben registrarse primero mediante endpoints de pasajeros.
- La pantalla no incorpora escaneo QR ni estado de embarque persistido en esta versión.
- Los endpoints existentes se usan sin inventar rutas nuevas.

## Ejecución
Inicia PostgreSQL y los tres Spring Boot services con sus configuraciones antes de probar la app. Abre el proyecto en Android Studio, deja que Gradle sincronice y ejecuta en un emulador.
