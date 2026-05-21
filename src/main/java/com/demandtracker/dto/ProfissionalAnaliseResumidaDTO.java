package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalAnaliseResumidaDTO {

    private ProfissionalDTO profissional;
    private Integer ano;
    private Integer mes;
    private BigDecimal horasExecutadas;
    private BigDecimal valorPerfilMes;
    private BigDecimal valorCustoMes;
}
