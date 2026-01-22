package com.demandtracker.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilUpdateDTO {
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;
    
    private LocalDate termoInicial;
    
    private LocalDate termoFinal;
    
    private Long usuarioId;
    
    private Long projetoId;
}
