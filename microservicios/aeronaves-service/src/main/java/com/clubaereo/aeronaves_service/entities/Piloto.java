package com.clubaereo.aeronaves_service.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table (name = "pilotos")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Piloto {

    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank 
    @Column (nullable = false, length = 50)
    private String nombre;

    @Column(name = "licencia_dgac_vigente", nullable = false)
    private Boolean licenciaDgacVigente = true;
}