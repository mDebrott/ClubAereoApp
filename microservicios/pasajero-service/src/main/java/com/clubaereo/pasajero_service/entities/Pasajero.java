package com.clubaereo.pasajero_service.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@AllArgsConstructor
@NoArgsConstructor 
@Getter 
@Setter 
@Table(name = "pasajero",
       uniqueConstraints = @UniqueConstraint(name = "uq_pasajero_rut_jornada",
                                             columnNames = {"rut", "jornada_id"}))
public class Pasajero 
{
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false, length = 12)
    private String rut;

    @Column(name = "nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "jornada_id", nullable = false)
    private Long jornadaId;

    @Column(name = "peso_kg", precision = 5, scale = 1)
    private BigDecimal pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoConsentimiento consentimiento = EstadoConsentimiento.PENDIENTE;

    @Column(name = "firma_base64", columnDefinition = "TEXT")
    private String firmaBase64;

    @Column(name = "fecha_firma")
    private LocalDateTime fechaFirma;


}
