package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaTecnicaProdutoViewDTO {
    private String codigo;
    private LocalDate dataInicioExecucao;
    private String nome;
    private String status;
    private BigDecimal totalPrevisto;
    private BigDecimal totalExecutado;
}
