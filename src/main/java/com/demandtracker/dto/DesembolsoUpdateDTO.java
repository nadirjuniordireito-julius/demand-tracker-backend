package com.demandtracker.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesembolsoUpdateDTO {

    @Size(max = 500, message = "Documento deve ter no máximo 500 caracteres")
    private String documento;

    private BigDecimal valorPrevisto;

    private BigDecimal valor;

    private LocalDate dataDesembolso;

    private LocalDate dataPrevistaDesembolso;

    private Long projetoId;
}

