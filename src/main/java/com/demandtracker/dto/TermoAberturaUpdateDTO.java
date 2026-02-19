package com.demandtracker.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoAberturaUpdateDTO {
    private LocalDateTime dataAbertura;
    
    @Size(min = 10, max = 2000, message = "Descrição deve ter entre 10 e 2000 caracteres")
    private String descricao;
}
