package com.clubaereo.pasajero_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.clubaereo.pasajero_service.dto.ConsentimientoRequest;
import com.clubaereo.pasajero_service.dto.PasajeroRequest;
import com.clubaereo.pasajero_service.dto.PasajeroResponse;
import com.clubaereo.pasajero_service.dto.PesoRequest;
import com.clubaereo.pasajero_service.entities.EstadoConsentimiento;
import com.clubaereo.pasajero_service.entities.Pasajero;
import com.clubaereo.pasajero_service.exception.BadRequestException;
import com.clubaereo.pasajero_service.exception.ConflictException;
import com.clubaereo.pasajero_service.exception.NotFoundException;
import com.clubaereo.pasajero_service.fake.FakePasajeroRepository;
import com.clubaereo.pasajero_service.service.PasajeroService;

/**
 * Pruebas unitarias de PasajeroService SIN Mockito: el repositorio es un fake en memoria y
 * las verificaciones se hacen sobre el ESTADO que queda en ese fake, no sobre llamadas.
 */
class PasajeroServiceTest {

    private static final Long JORNADA_1 = 1L;
    private static final Long JORNADA_2 = 2L;

    // RUT válidos (dígito verificador calculado con RutValidator)
    private static final String RUT_A = "12345678-5";
    private static final String RUT_B = "11111111-1";
    private static final String RUT_CON_K = "10000013-K";
    private static final String RUT_7_DIGITOS = "7654321-6";

    private static final String FIRMA_VALIDA = "aGVsbG8=";
    private static final String FIRMA_DATA_URI = "data:image/png;base64,aGVsbG8=";
    private static final LocalDateTime FECHA_FIRMA_PREVIA = LocalDateTime.of(2026, 3, 15, 10, 30);
    private static final ZoneId SANTIAGO = ZoneId.of("America/Santiago");

    private FakePasajeroRepository fake;
    private PasajeroService service;

    @BeforeEach
    void setUp() {
        fake = new FakePasajeroRepository();          // repositorio vacío y nuevo en cada test
        service = new PasajeroService(fake.repo());   // único campo del constructor: repo
    }

    // =====================================================================
    // Ayudantes (Arrange): dejan datos directamente en el fake, sin pasar por el service
    // =====================================================================

    private Pasajero sembrar(String rut, Long jornadaId) {
        Pasajero p = new Pasajero();
        p.setRut(rut);
        p.setNombreCompleto("Persona " + rut);
        p.setJornadaId(jornadaId);
        return fake.repo().save(p);
    }

    private Pasajero sembrarConPeso(String rut, Long jornadaId, String peso) {
        Pasajero p = sembrar(rut, jornadaId);
        p.setPesoKg(new BigDecimal(peso));
        return fake.repo().save(p);
    }

    private Pasajero sembrarFirmado(String rut, Long jornadaId) {
        Pasajero p = sembrarConPeso(rut, jornadaId, "70.5");
        p.setFirmaBase64(FIRMA_VALIDA);
        p.setFechaFirma(FECHA_FIRMA_PREVIA);
        p.setConsentimiento(EstadoConsentimiento.FIRMADO);
        return fake.repo().save(p);
    }

    /** Lee el estado real que quedó "en la base de datos" (el fake). */
    private Pasajero leer(Long id) {
        return fake.repo().findById(id).orElseThrow();
    }

    private static PesoRequest peso(String valor) {
        return new PesoRequest(new BigDecimal(valor));
    }

    // =====================================================================
    // inscribir
    // =====================================================================

    @Test
    @DisplayName("inscribir: con datos válidos guarda un pasajero PENDIENTE sin peso ni firma y devuelve la respuesta")
    void inscribir_datosValidos_guardaPasajeroPendienteYDevuelveRespuesta() {
        PasajeroRequest req = new PasajeroRequest(RUT_A, "Juan Pérez", JORNADA_1);

        PasajeroResponse respuesta = service.inscribir(req);

        assertNotNull(respuesta.id());
        assertEquals(RUT_A, respuesta.rut());
        assertEquals("Juan Pérez", respuesta.nombreCompleto());
        assertEquals(JORNADA_1, respuesta.jornadaId());
        assertNull(respuesta.pesoKg());
        assertEquals(EstadoConsentimiento.PENDIENTE, respuesta.consentimiento());
        assertNull(respuesta.fechaFirma());

        assertEquals(1L, fake.repo().count());
        Pasajero guardado = leer(respuesta.id());
        assertEquals(RUT_A, guardado.getRut());
        assertEquals("Juan Pérez", guardado.getNombreCompleto());
        assertEquals(JORNADA_1, guardado.getJornadaId());
        assertNull(guardado.getPesoKg());
        assertEquals(EstadoConsentimiento.PENDIENTE, guardado.getConsentimiento());
        assertNull(guardado.getFirmaBase64());
        assertNull(guardado.getFechaFirma());
    }

    @Test
    @DisplayName("inscribir: un RUT con puntos y guion se guarda normalizado")
    void inscribir_rutConPuntosYGuion_guardaRutNormalizado() {
        PasajeroResponse respuesta = service.inscribir(new PasajeroRequest("12.345.678-5", "Ana", JORNADA_1));

        assertEquals(RUT_A, respuesta.rut());
        assertEquals(RUT_A, leer(respuesta.id()).getRut());
    }

    @Test
    @DisplayName("inscribir: un RUT sin guion se guarda con guion")
    void inscribir_rutSinGuion_guardaRutConGuion() {
        PasajeroResponse respuesta = service.inscribir(new PasajeroRequest("123456785", "Ana", JORNADA_1));

        assertEquals(RUT_A, leer(respuesta.id()).getRut());
    }

    @Test
    @DisplayName("inscribir: un dígito verificador 'k' minúscula se guarda como 'K'")
    void inscribir_digitoVerificadorKMinuscula_seGuardaEnMayuscula() {
        PasajeroResponse respuesta = service.inscribir(new PasajeroRequest("10.000.013-k", "Ana", JORNADA_1));

        assertEquals(RUT_CON_K, leer(respuesta.id()).getRut());
    }

    @Test
    @DisplayName("inscribir: un RUT de 7 dígitos (límite inferior) es válido")
    void inscribir_rutDeSieteDigitos_esValido() {
        PasajeroResponse respuesta = service.inscribir(new PasajeroRequest(RUT_7_DIGITOS, "Ana", JORNADA_1));

        assertEquals(RUT_7_DIGITOS, leer(respuesta.id()).getRut());
    }

    @Test
    @DisplayName("inscribir: el nombre se guarda sin espacios al inicio ni al final")
    void inscribir_nombreConEspaciosExtremos_guardaNombreRecortado() {
        PasajeroResponse respuesta = service.inscribir(new PasajeroRequest(RUT_A, "  Juan Pérez  ", JORNADA_1));

        assertEquals("Juan Pérez", respuesta.nombreCompleto());
        assertEquals("Juan Pérez", leer(respuesta.id()).getNombreCompleto());
    }

    @Test
    @DisplayName("inscribir: un dígito verificador incorrecto lanza BadRequestException y no guarda nada")
    void inscribir_digitoVerificadorIncorrecto_lanzaBadRequestYNoGuarda() {
        PasajeroRequest req = new PasajeroRequest("12345678-9", "Juan", JORNADA_1);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.inscribir(req));

        assertEquals("RUT inválido", ex.getMessage());
        assertEquals(0L, fake.repo().count());
    }

    @Test
    @DisplayName("inscribir: un RUT con letras lanza BadRequestException y no guarda nada")
    void inscribir_rutConLetras_lanzaBadRequestYNoGuarda() {
        PasajeroRequest req = new PasajeroRequest("abc", "Juan", JORNADA_1);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.inscribir(req));

        assertEquals("RUT inválido", ex.getMessage());
        assertEquals(0L, fake.repo().count());
    }

    @Test
    @DisplayName("inscribir: un RUT en blanco lanza BadRequestException y no guarda nada")
    void inscribir_rutEnBlanco_lanzaBadRequestYNoGuarda() {
        PasajeroRequest req = new PasajeroRequest("   ", "Juan", JORNADA_1);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.inscribir(req));

        assertEquals("RUT inválido", ex.getMessage());
        assertEquals(0L, fake.repo().count());
    }

    @Test
    @DisplayName("inscribir: un RUT de 6 dígitos (bajo el mínimo) lanza BadRequestException")
    void inscribir_rutDeSeisDigitos_lanzaBadRequest() {
        PasajeroRequest req = new PasajeroRequest("123456-5", "Juan", JORNADA_1);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.inscribir(req));

        assertEquals("RUT inválido", ex.getMessage());
        assertEquals(0L, fake.repo().count());
    }

    @Test
    @DisplayName("inscribir: un RUT de 9 dígitos (sobre el máximo) lanza BadRequestException")
    void inscribir_rutDeNueveDigitos_lanzaBadRequest() {
        PasajeroRequest req = new PasajeroRequest("123456789-0", "Juan", JORNADA_1);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.inscribir(req));

        assertEquals("RUT inválido", ex.getMessage());
        assertEquals(0L, fake.repo().count());
    }

    @Test
    @DisplayName("inscribir: el mismo RUT en la misma jornada lanza ConflictException y no duplica ni pisa datos")
    void inscribir_mismoRutEnLaMismaJornada_lanzaConflictYNoDuplica() {
        Pasajero existente = sembrar(RUT_A, JORNADA_1);
        PasajeroRequest req = new PasajeroRequest(RUT_A, "Otro Nombre", JORNADA_1);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.inscribir(req));

        assertEquals("El pasajero ya está inscrito en esta jornada", ex.getMessage());
        assertEquals(1L, fake.repo().count());
        assertEquals("Persona " + RUT_A, leer(existente.getId()).getNombreCompleto());
    }

    @Test
    @DisplayName("inscribir: el duplicado se detecta aunque el RUT venga con otro formato")
    void inscribir_mismoRutConOtroFormato_lanzaConflict() {
        sembrar(RUT_A, JORNADA_1);
        PasajeroRequest req = new PasajeroRequest("12.345.678-5", "Juan", JORNADA_1);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.inscribir(req));

        assertEquals("El pasajero ya está inscrito en esta jornada", ex.getMessage());
        assertEquals(1L, fake.repo().count());
    }

    @Test
    @DisplayName("inscribir: el mismo RUT en otra jornada sí se permite")
    void inscribir_mismoRutEnOtraJornada_permiteLaInscripcion() {
        sembrar(RUT_A, JORNADA_1);

        PasajeroResponse respuesta = service.inscribir(new PasajeroRequest(RUT_A, "Juan", JORNADA_2));

        assertEquals(2L, fake.repo().count());
        assertEquals(JORNADA_2, leer(respuesta.id()).getJornadaId());
    }

    @Test
    @DisplayName("inscribir: otro RUT en la misma jornada sí se permite y recibe un id distinto")
    void inscribir_otroRutEnLaMismaJornada_permiteLaInscripcion() {
        Pasajero existente = sembrar(RUT_A, JORNADA_1);

        PasajeroResponse respuesta = service.inscribir(new PasajeroRequest(RUT_B, "Juan", JORNADA_1));

        assertEquals(2L, fake.repo().count());
        assertFalse(existente.getId().equals(respuesta.id()));
    }

    // =====================================================================
    // listarPorJornada
    // =====================================================================

    @Test
    @DisplayName("listarPorJornada: devuelve solo los pasajeros de la jornada pedida")
    void listarPorJornada_conPasajeros_devuelveSoloLosDeEsaJornada() {
        sembrar(RUT_A, JORNADA_1);
        sembrar(RUT_B, JORNADA_1);
        sembrar(RUT_CON_K, JORNADA_2);

        List<PasajeroResponse> respuesta = service.listarPorJornada(JORNADA_1);

        Set<String> ruts = respuesta.stream().map(PasajeroResponse::rut).collect(Collectors.toSet());
        assertEquals(2, respuesta.size());
        assertEquals(Set.of(RUT_A, RUT_B), ruts);
    }

    @Test
    @DisplayName("listarPorJornada: una jornada sin pasajeros devuelve lista vacía aunque haya otras jornadas con datos")
    void listarPorJornada_jornadaSinPasajeros_devuelveListaVacia() {
        sembrar(RUT_A, JORNADA_1);

        List<PasajeroResponse> respuesta = service.listarPorJornada(JORNADA_2);

        assertTrue(respuesta.isEmpty());
    }

    @Test
    @DisplayName("listarPorJornada: con el repositorio vacío devuelve lista vacía")
    void listarPorJornada_repositorioVacio_devuelveListaVacia() {
        assertTrue(service.listarPorJornada(JORNADA_1).isEmpty());
    }

    @Test
    @DisplayName("listarPorJornada: una jornada nula devuelve lista vacía")
    void listarPorJornada_jornadaNula_devuelveListaVacia() {
        sembrar(RUT_A, JORNADA_1);

        assertTrue(service.listarPorJornada(null).isEmpty());
    }

    @Test
    @DisplayName("listarPorJornada: la respuesta incluye peso, estado y fecha de firma de un pasajero firmado")
    void listarPorJornada_pasajeroFirmado_mapeaTodosLosCampos() {
        Pasajero firmado = sembrarFirmado(RUT_A, JORNADA_1);

        PasajeroResponse r = service.listarPorJornada(JORNADA_1).get(0);

        assertEquals(firmado.getId(), r.id());
        assertEquals(RUT_A, r.rut());
        assertEquals("Persona " + RUT_A, r.nombreCompleto());
        assertEquals(JORNADA_1, r.jornadaId());
        assertEquals(new BigDecimal("70.5"), r.pesoKg());
        assertEquals(EstadoConsentimiento.FIRMADO, r.consentimiento());
        assertEquals(FECHA_FIRMA_PREVIA, r.fechaFirma());
    }

    // =====================================================================
    // obtener
    // =====================================================================

    @Test
    @DisplayName("obtener: con un id existente devuelve el pasajero")
    void obtener_idExistente_devuelveElPasajero() {
        Pasajero p = sembrar(RUT_A, JORNADA_1);

        PasajeroResponse respuesta = service.obtener(p.getId());

        assertEquals(p.getId(), respuesta.id());
        assertEquals(RUT_A, respuesta.rut());
        assertEquals(EstadoConsentimiento.PENDIENTE, respuesta.consentimiento());
    }

    @Test
    @DisplayName("obtener: con un pasajero firmado informa estado FIRMADO y la fecha de firma")
    void obtener_pasajeroFirmado_informaEstadoYFechaDeFirma() {
        Pasajero p = sembrarFirmado(RUT_A, JORNADA_1);

        PasajeroResponse respuesta = service.obtener(p.getId());

        assertEquals(EstadoConsentimiento.FIRMADO, respuesta.consentimiento());
        assertEquals(FECHA_FIRMA_PREVIA, respuesta.fechaFirma());
    }

    @Test
    @DisplayName("obtener: con un id inexistente lanza NotFoundException")
    void obtener_idInexistente_lanzaNotFound() {
        sembrar(RUT_A, JORNADA_1);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.obtener(999L));

        assertEquals("Pasajero no existe", ex.getMessage());
    }

    // =====================================================================
    // buscarPorRut
    // =====================================================================

    @Test
    @DisplayName("buscarPorRut: un RUT inscrito en la jornada devuelve el pasajero")
    void buscarPorRut_rutInscrito_devuelveElPasajero() {
        Pasajero p = sembrar(RUT_A, JORNADA_1);

        PasajeroResponse respuesta = service.buscarPorRut(RUT_A, JORNADA_1);

        assertEquals(p.getId(), respuesta.id());
        assertEquals(RUT_A, respuesta.rut());
    }

    @Test
    @DisplayName("buscarPorRut: encuentra al pasajero aunque el RUT se escriba con puntos")
    void buscarPorRut_rutConPuntos_encuentraAlPasajeroNormalizado() {
        Pasajero p = sembrar(RUT_A, JORNADA_1);

        PasajeroResponse respuesta = service.buscarPorRut("12.345.678-5", JORNADA_1);

        assertEquals(p.getId(), respuesta.id());
    }

    @Test
    @DisplayName("buscarPorRut: encuentra al pasajero con 'k' minúscula")
    void buscarPorRut_kMinuscula_encuentraAlPasajero() {
        Pasajero p = sembrar(RUT_CON_K, JORNADA_1);

        PasajeroResponse respuesta = service.buscarPorRut("10000013-k", JORNADA_1);

        assertEquals(p.getId(), respuesta.id());
    }

    @Test
    @DisplayName("buscarPorRut: un RUT inválido lanza BadRequestException aunque existan datos")
    void buscarPorRut_rutInvalido_lanzaBadRequest() {
        sembrar(RUT_A, JORNADA_1);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.buscarPorRut("12345678-9", JORNADA_1));

        assertEquals("RUT inválido", ex.getMessage());
    }

    @Test
    @DisplayName("buscarPorRut: un RUT válido que no está inscrito lanza NotFoundException")
    void buscarPorRut_rutValidoNoInscrito_lanzaNotFound() {
        sembrar(RUT_A, JORNADA_1);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.buscarPorRut(RUT_B, JORNADA_1));

        assertEquals("El RUT no está inscrito en esta jornada", ex.getMessage());
    }

    @Test
    @DisplayName("buscarPorRut: un RUT inscrito solo en otra jornada lanza NotFoundException")
    void buscarPorRut_rutInscritoEnOtraJornada_lanzaNotFound() {
        sembrar(RUT_A, JORNADA_1);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.buscarPorRut(RUT_A, JORNADA_2));

        assertEquals("El RUT no está inscrito en esta jornada", ex.getMessage());
    }

    @Test
    @DisplayName("buscarPorRut: con el mismo RUT en dos jornadas devuelve el de la jornada pedida")
    void buscarPorRut_mismoRutEnDosJornadas_devuelveElDeLaJornadaPedida() {
        sembrar(RUT_A, JORNADA_1);
        Pasajero enJornada2 = sembrar(RUT_A, JORNADA_2);

        PasajeroResponse respuesta = service.buscarPorRut(RUT_A, JORNADA_2);

        assertEquals(enJornada2.getId(), respuesta.id());
        assertEquals(JORNADA_2, respuesta.jornadaId());
    }

    @Test
    @DisplayName("buscarPorRut: una jornada nula no encuentra a nadie y lanza NotFoundException")
    void buscarPorRut_jornadaNula_lanzaNotFound() {
        sembrar(RUT_A, JORNADA_1);

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.buscarPorRut(RUT_A, null));

        assertEquals("El RUT no está inscrito en esta jornada", ex.getMessage());
    }

    // =====================================================================
    // registrarPeso
    // =====================================================================

    @Test
    @DisplayName("registrarPeso: en un pasajero pendiente guarda el peso y mantiene el estado PENDIENTE")
    void registrarPeso_pasajeroPendiente_guardaElPesoYDevuelveRespuesta() {
        Pasajero p = sembrar(RUT_A, JORNADA_1);

        PasajeroResponse respuesta = service.registrarPeso(p.getId(), peso("72.5"));

        assertEquals(new BigDecimal("72.5"), respuesta.pesoKg());
        assertEquals(EstadoConsentimiento.PENDIENTE, respuesta.consentimiento());
        Pasajero guardado = leer(p.getId());
        assertEquals(new BigDecimal("72.5"), guardado.getPesoKg());
        assertEquals(EstadoConsentimiento.PENDIENTE, guardado.getConsentimiento());
        assertNull(guardado.getFirmaBase64());
        assertNull(guardado.getFechaFirma());
    }

    @Test
    @DisplayName("registrarPeso: un peso ya registrado se reemplaza mientras no se firme")
    void registrarPeso_pesoYaRegistrado_reemplazaElPesoAnterior() {
        Pasajero p = sembrarConPeso(RUT_A, JORNADA_1, "70.0");

        service.registrarPeso(p.getId(), peso("68.4"));

        assertEquals(new BigDecimal("68.4"), leer(p.getId()).getPesoKg());
    }

    @Test
    @DisplayName("registrarPeso: los valores límite del DTO (20.0 y 250.0) se guardan tal cual, sin redondeo")
    void registrarPeso_valoresLimiteDelDto_seGuardanSinRedondeo() {
        Pasajero p = sembrar(RUT_A, JORNADA_1);

        service.registrarPeso(p.getId(), peso("20.0"));
        assertEquals(new BigDecimal("20.0"), leer(p.getId()).getPesoKg());

        service.registrarPeso(p.getId(), peso("250.0"));
        assertEquals(new BigDecimal("250.0"), leer(p.getId()).getPesoKg());
    }

    @Test
    @DisplayName("registrarPeso: solo modifica al pasajero indicado")
    void registrarPeso_soloModificaAlPasajeroIndicado() {
        Pasajero objetivo = sembrar(RUT_A, JORNADA_1);
        Pasajero otro = sembrar(RUT_B, JORNADA_1);

        service.registrarPeso(objetivo.getId(), peso("80.0"));

        assertNull(leer(otro.getId()).getPesoKg());
    }

    @Test
    @DisplayName("registrarPeso: un pasajero inexistente lanza NotFoundException y no crea nada")
    void registrarPeso_pasajeroInexistente_lanzaNotFoundYNoCreaNada() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.registrarPeso(999L, peso("70.0")));

        assertEquals("Pasajero no existe", ex.getMessage());
        assertEquals(0L, fake.repo().count());
    }

    @Test
    @DisplayName("registrarPeso: con el consentimiento firmado lanza ConflictException y conserva el peso original")
    void registrarPeso_consentimientoFirmado_lanzaConflictYConservaElPeso() {
        Pasajero firmado = sembrarFirmado(RUT_A, JORNADA_1); // peso 70.5

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.registrarPeso(firmado.getId(), peso("99.9")));

        assertEquals("No se puede modificar el peso: el consentimiento ya fue firmado", ex.getMessage());
        assertEquals(new BigDecimal("70.5"), leer(firmado.getId()).getPesoKg());
    }

    // =====================================================================
    // firmar
    // =====================================================================

    @Test
    @DisplayName("firmar: con peso registrado guarda la firma, la fecha (hora de Santiago) y pasa a FIRMADO")
    void firmar_pasajeroConPeso_firmaYGuardaLaFecha() {
        Pasajero p = sembrarConPeso(RUT_A, JORNADA_1, "72.5");
        LocalDateTime antes = LocalDateTime.now(SANTIAGO);

        PasajeroResponse respuesta = service.firmar(p.getId(), new ConsentimientoRequest(FIRMA_VALIDA));

        LocalDateTime despues = LocalDateTime.now(SANTIAGO);
        Pasajero guardado = leer(p.getId());
        assertEquals(EstadoConsentimiento.FIRMADO, guardado.getConsentimiento());
        assertEquals(FIRMA_VALIDA, guardado.getFirmaBase64());
        assertNotNull(guardado.getFechaFirma());
        assertFalse(guardado.getFechaFirma().isBefore(antes), "la fecha de firma no puede ser anterior a la llamada");
        assertFalse(guardado.getFechaFirma().isAfter(despues), "la fecha de firma no puede ser posterior a la llamada");
        assertEquals(EstadoConsentimiento.FIRMADO, respuesta.consentimiento());
        assertEquals(guardado.getFechaFirma(), respuesta.fechaFirma());
        // el resto de los datos no cambia
        assertEquals(RUT_A, guardado.getRut());
        assertEquals("Persona " + RUT_A, guardado.getNombreCompleto());
        assertEquals(JORNADA_1, guardado.getJornadaId());
        assertEquals(new BigDecimal("72.5"), guardado.getPesoKg());
    }

    @Test
    @DisplayName("firmar: acepta una firma con prefijo data URI y la guarda completa")
    void firmar_firmaConPrefijoDataUri_laGuardaCompleta() {
        Pasajero p = sembrarConPeso(RUT_A, JORNADA_1, "72.5");

        service.firmar(p.getId(), new ConsentimientoRequest(FIRMA_DATA_URI));

        Pasajero guardado = leer(p.getId());
        assertEquals(EstadoConsentimiento.FIRMADO, guardado.getConsentimiento());
        assertEquals(FIRMA_DATA_URI, guardado.getFirmaBase64());
    }

    @Test
    @DisplayName("firmar: acepta Base64 con saltos de línea (formato MIME)")
    void firmar_base64ConSaltosDeLinea_esAceptado() {
        Pasajero p = sembrarConPeso(RUT_A, JORNADA_1, "72.5");

        service.firmar(p.getId(), new ConsentimientoRequest("aGVs\r\nbG8="));

        assertEquals(EstadoConsentimiento.FIRMADO, leer(p.getId()).getConsentimiento());
    }

    @Test
    @DisplayName("firmar: un pasajero inexistente lanza NotFoundException y no crea nada")
    void firmar_pasajeroInexistente_lanzaNotFoundYNoCreaNada() {
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> service.firmar(999L, new ConsentimientoRequest(FIRMA_VALIDA)));

        assertEquals("Pasajero no existe", ex.getMessage());
        assertEquals(0L, fake.repo().count());
    }

    @Test
    @DisplayName("firmar: si ya está firmado lanza ConflictException y no sobrescribe la firma ni la fecha")
    void firmar_yaFirmado_lanzaConflictYNoSobrescribe() {
        Pasajero firmado = sembrarFirmado(RUT_A, JORNADA_1);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.firmar(firmado.getId(), new ConsentimientoRequest("b3RyYQ==")));

        assertEquals("El consentimiento ya fue firmado", ex.getMessage());
        Pasajero guardado = leer(firmado.getId());
        assertEquals(FIRMA_VALIDA, guardado.getFirmaBase64());
        assertEquals(FECHA_FIRMA_PREVIA, guardado.getFechaFirma());
    }

    @Test
    @DisplayName("firmar: sin peso registrado lanza ConflictException y el pasajero sigue PENDIENTE")
    void firmar_sinPeso_lanzaConflictYNoFirma() {
        Pasajero p = sembrar(RUT_A, JORNADA_1);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.firmar(p.getId(), new ConsentimientoRequest(FIRMA_VALIDA)));

        assertEquals("Debe registrar el peso antes de firmar", ex.getMessage());
        Pasajero guardado = leer(p.getId());
        assertEquals(EstadoConsentimiento.PENDIENTE, guardado.getConsentimiento());
        assertNull(guardado.getFirmaBase64());
        assertNull(guardado.getFechaFirma());
    }

    @Test
    @DisplayName("firmar: una firma con Base64 inválido lanza BadRequestException y no firma")
    void firmar_firmaInvalida_lanzaBadRequestYNoFirma() {
        Pasajero p = sembrarConPeso(RUT_A, JORNADA_1, "72.5");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.firmar(p.getId(), new ConsentimientoRequest("abcde")));

        assertEquals("La firma no es un Base64 válido", ex.getMessage());
        Pasajero guardado = leer(p.getId());
        assertEquals(EstadoConsentimiento.PENDIENTE, guardado.getConsentimiento());
        assertNull(guardado.getFirmaBase64());
        assertNull(guardado.getFechaFirma());
        assertEquals(new BigDecimal("72.5"), guardado.getPesoKg());
    }

    @Test
    @DisplayName("firmar: un Base64 inválido detrás de un prefijo data URI también se rechaza")
    void firmar_firmaInvalidaConPrefijoDataUri_lanzaBadRequest() {
        Pasajero p = sembrarConPeso(RUT_A, JORNADA_1, "72.5");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.firmar(p.getId(), new ConsentimientoRequest("data:image/png;base64,abcde")));

        assertEquals("La firma no es un Base64 válido", ex.getMessage());
        assertEquals(EstadoConsentimiento.PENDIENTE, leer(p.getId()).getConsentimiento());
    }

    @Test
    @DisplayName("firmar: sin peso y con firma inválida se informa primero la falta de peso")
    void firmar_sinPesoYFirmaInvalida_reportaPrimeroLaFaltaDePeso() {
        Pasajero p = sembrar(RUT_A, JORNADA_1);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.firmar(p.getId(), new ConsentimientoRequest("abcde")));

        assertEquals("Debe registrar el peso antes de firmar", ex.getMessage());
    }

    @Test
    @DisplayName("firmar: ya firmado y con firma inválida se informa primero que ya estaba firmado")
    void firmar_yaFirmadoYFirmaInvalida_reportaPrimeroQueYaEstabaFirmado() {
        Pasajero firmado = sembrarFirmado(RUT_A, JORNADA_1);

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.firmar(firmado.getId(), new ConsentimientoRequest("abcde")));

        assertEquals("El consentimiento ya fue firmado", ex.getMessage());
    }

    @Test
    @DisplayName("firmar: solo firma al pasajero indicado")
    void firmar_soloFirmaAlPasajeroIndicado() {
        Pasajero objetivo = sembrarConPeso(RUT_A, JORNADA_1, "72.5");
        Pasajero otro = sembrarConPeso(RUT_B, JORNADA_1, "65.0");

        service.firmar(objetivo.getId(), new ConsentimientoRequest(FIRMA_VALIDA));

        Pasajero sinFirmar = leer(otro.getId());
        assertEquals(EstadoConsentimiento.PENDIENTE, sinFirmar.getConsentimiento());
        assertNull(sinFirmar.getFirmaBase64());
    }

    // =====================================================================
    // Flujos completos (varios métodos del service encadenados)
    // =====================================================================

    @Test
    @DisplayName("flujo: inscribir, registrar peso y firmar deja al pasajero FIRMADO")
    void flujo_inscribirRegistrarPesoYFirmar_dejaAlPasajeroFirmado() {
        Long id = service.inscribir(new PasajeroRequest("12.345.678-5", " Juan Pérez ", JORNADA_1)).id();

        service.registrarPeso(id, peso("72.5"));
        service.firmar(id, new ConsentimientoRequest(FIRMA_DATA_URI));

        PasajeroResponse resultado = service.buscarPorRut(RUT_A, JORNADA_1);
        assertEquals(id, resultado.id());
        assertEquals("Juan Pérez", resultado.nombreCompleto());
        assertEquals(new BigDecimal("72.5"), resultado.pesoKg());
        assertEquals(EstadoConsentimiento.FIRMADO, resultado.consentimiento());
        assertNotNull(resultado.fechaFirma());
    }

    @Test
    @DisplayName("flujo: después de firmar ya no se puede cambiar el peso")
    void flujo_despuesDeFirmar_noSePuedeCambiarElPeso() {
        Long id = service.inscribir(new PasajeroRequest(RUT_A, "Juan", JORNADA_1)).id();
        service.registrarPeso(id, peso("72.5"));
        service.firmar(id, new ConsentimientoRequest(FIRMA_VALIDA));

        ConflictException ex = assertThrows(ConflictException.class,
                () -> service.registrarPeso(id, peso("60.0")));

        assertEquals("No se puede modificar el peso: el consentimiento ya fue firmado", ex.getMessage());
        assertEquals(new BigDecimal("72.5"), service.obtener(id).pesoKg());
    }

    // =====================================================================
    // BUGS CONOCIDOS: estas pruebas describen el comportamiento CORRECTO y hoy fallarían.
    // Están @Disabled para no romper el build; quita el @Disabled cuando corrijas validarBase64.
    // =====================================================================

    @Test
    @Disabled("BUG: validarBase64 usa getMimeDecoder(), que IGNORA los caracteres fuera del alfabeto Base64")
    @DisplayName("firmar: caracteres fuera del alfabeto Base64 deberían rechazarse (hoy se aceptan)")
    void firmar_caracteresFueraDelAlfabeto_deberiaLanzarBadRequest() {
        Pasajero p = sembrarConPeso(RUT_A, JORNADA_1, "72.5");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.firmar(p.getId(), new ConsentimientoRequest("@@@@")));

        assertEquals("La firma no es un Base64 válido", ex.getMessage());
        assertEquals(EstadoConsentimiento.PENDIENTE, leer(p.getId()).getConsentimiento());
    }

    @Test
    @Disabled("BUG: un data URI sin contenido ('data:image/png;base64,') decodifica a 0 bytes y se acepta")
    @DisplayName("firmar: una firma sin contenido (solo el prefijo data URI) debería rechazarse (hoy se acepta)")
    void firmar_dataUriSinContenido_deberiaLanzarBadRequest() {
        Pasajero p = sembrarConPeso(RUT_A, JORNADA_1, "72.5");

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> service.firmar(p.getId(), new ConsentimientoRequest("data:image/png;base64,")));

        assertEquals("La firma no es un Base64 válido", ex.getMessage());
        assertEquals(EstadoConsentimiento.PENDIENTE, leer(p.getId()).getConsentimiento());
    }
}
