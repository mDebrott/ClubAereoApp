package com.clubaereo.aeronaves_service.entities;

import java.math.BigDecimal;

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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "registros_vuelo")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class RegistroVuelo {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aeronave_id", nullable = false)
    private Aeronave aeronave;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "piloto_id", nullable = false)
    private Piloto piloto;

    @Column(name = "jornada_id", nullable = false)
    private Long jornadaId;

    @NotNull @DecimalMin("0.0")
    @Column(name = "horometro_inicial", nullable = false, precision = 10, scale = 1)
    private BigDecimal horometroInicial;

    @Column(name = "horometro_final", precision = 10, scale = 1)
    private BigDecimal horometroFinal;         

    @Column(name = "horas_consumidas", precision = 10, scale = 1)
    private BigDecimal horasConsumidas;         

    @Column(name = "n_tripulantes")
    private Integer nTripulantes;          

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoVuelo estado = EstadoVuelo.ABIERTO;
}
