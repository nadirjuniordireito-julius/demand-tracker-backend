package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaProdutoEvolucaoTrimestralItemDTO {
    private Integer trimestreSequencia;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private BigDecimal totalPrevisto;
    private BigDecimal totalExecutado;
}
