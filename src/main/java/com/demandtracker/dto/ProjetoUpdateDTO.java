package com.demandtracker.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoUpdateDTO {
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;
    
    @Size(max = 50, message = "Código TED deve ter no máximo 50 caracteres")
    private String codTed;
    
    private LocalDate termoInicial;
    
    private LocalDate termoFinal;
    
    private LocalDate dataEfetivaInicio;
    
    private Long usuarioId;
}
