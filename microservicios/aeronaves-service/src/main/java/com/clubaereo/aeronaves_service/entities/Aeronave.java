package com.clubaereo.aeronaves_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "aeronave")
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class Aeronave {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 10)
    private String matricula;

    @NotBlank 
    @Column(nullable = false, length = 50)
    private String modelo;

    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private Integer capacidad;

    @Column(nullable = false)
    private Boolean disponible = true;
}
