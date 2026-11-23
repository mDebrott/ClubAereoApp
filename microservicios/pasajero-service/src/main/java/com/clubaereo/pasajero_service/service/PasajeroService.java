package com.clubaereo.pasajero_service.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clubaereo.pasajero_service.dto.ConsentimientoRequest;
import com.clubaereo.pasajero_service.dto.PasajeroRequest;
import com.clubaereo.pasajero_service.dto.PasajeroResponse;
import com.clubaereo.pasajero_service.dto.PesoRequest;
import com.clubaereo.pasajero_service.entities.EstadoConsentimiento;
import com.clubaereo.pasajero_service.entities.Pasajero;
import com.clubaereo.pasajero_service.exception.BadRequestException;
import com.clubaereo.pasajero_service.exception.ConflictException;
import com.clubaereo.pasajero_service.exception.NotFoundException;
import com.clubaereo.pasajero_service.repository.PasajeroRepository;
import com.clubaereo.pasajero_service.util.RutValidator;

@Service
public class PasajeroService {

    private static final ZoneId ZONA = ZoneId.of("America/Santiago");

    private final PasajeroRepository repo;

    public PasajeroService(PasajeroRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public PasajeroResponse inscribir(PasajeroRequest req) {
        String rut = RutValidator.normalizar(req.rut());
        if (!RutValidator.esValido(rut))
            throw new BadRequestException("RUT inválido");
        if (repo.existsByRutAndJornadaId(rut, req.jornadaId()))
            throw new ConflictException("El pasajero ya está inscrito en esta jornada");

        Pasajero p = new Pasajero();
        p.setRut(rut);
        p.setNombreCompleto(req.nombreCompleto().trim());
        p.setJornadaId(req.jornadaId());
        return PasajeroResponse.from(repo.save(p));
    }

    @Transactional(readOnly = true)
    public List<PasajeroResponse> listarPorJornada(Long jornadaId) {
        return repo.findByJornadaId(jornadaId).stream()
            .map(PasajeroResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public PasajeroResponse obtener(Long id) {
        return PasajeroResponse.from(buscar(id));
    }

    @Transactional(readOnly = true)
    public PasajeroResponse buscarPorRut(String rut, Long jornadaId) {
        String rutNormalizado = RutValidator.normalizar(rut);
        if (!RutValidator.esValido(rutNormalizado))
            throw new BadRequestException("RUT inválido");
        return repo.findByRutAndJornadaId(rutNormalizado, jornadaId)
            .map(PasajeroResponse::from)
            .orElseThrow(() -> new NotFoundException("El RUT no está inscrito en esta jornada"));
    }

    @Transactional
    public PasajeroResponse registrarPeso(Long id, PesoRequest req) {
        Pasajero p = buscar(id);
        if (p.getConsentimiento() == EstadoConsentimiento.FIRMADO)
            throw new ConflictException("No se puede modificar el peso: el consentimiento ya fue firmado");
        p.setPesoKg(req.pesoKg());
        return PasajeroResponse.from(repo.save(p));
    }

    @Transactional
    public PasajeroResponse firmar(Long id, ConsentimientoRequest req) {
        Pasajero p = buscar(id);
        if (p.getConsentimiento() == EstadoConsentimiento.FIRMADO)
            throw new ConflictException("El consentimiento ya fue firmado");
        if (p.getPesoKg() == null)
            throw new ConflictException("Debe registrar el peso antes de firmar");
        validarBase64(req.firmaBase64());

        p.setFirmaBase64(req.firmaBase64());
        p.setFechaFirma(LocalDateTime.now(ZONA));
        p.setConsentimiento(EstadoConsentimiento.FIRMADO);
        return PasajeroResponse.from(repo.save(p));
    }

    // Acepta tanto Base64 puro como "data:image/png;base64,...."
    private void validarBase64(String firma) {
        String datos = firma.contains(",") ? firma.substring(firma.indexOf(',') + 1) : firma;
        try {
            Base64.getMimeDecoder().decode(datos);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("La firma no es un Base64 válido");
        }
    }

    private Pasajero buscar(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new NotFoundException("Pasajero no existe"));
    }
}
