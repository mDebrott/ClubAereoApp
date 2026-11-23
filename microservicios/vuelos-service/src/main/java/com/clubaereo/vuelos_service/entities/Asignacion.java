package com.clubaereo.vuelos_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "asignacion")
@AllArgsConstructor 
@NoArgsConstructor 
@Getter 
@Setter

public class Asignacion {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull @Column(name = "pasajero_id", nullable = false)
    private Long pasajeroId;

    @NotNull @Column(name = "aeronave_id", nullable = false)
    private Long aeronaveId;

    @NotNull @Column(name = "piloto_id", nullable = false)
    private Long pilotoId;

    // Relación dentro de este mismo servicio
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bloque_id", nullable = false)
    private BloqueHorario bloque;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAsignacion estado = EstadoAsignacion.PENDIENTE;
}
