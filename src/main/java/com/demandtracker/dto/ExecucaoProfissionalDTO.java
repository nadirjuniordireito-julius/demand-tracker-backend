package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecucaoProfissionalDTO {
    private Long id;
    private Integer mes;
    private Integer ano;
    private BigDecimal horasPrevistas;
    private BigDecimal horasExecutadas;
    private BigDecimal valorMensalRemuneracao;
    private BigDecimal valorTotalExecucao;
}
