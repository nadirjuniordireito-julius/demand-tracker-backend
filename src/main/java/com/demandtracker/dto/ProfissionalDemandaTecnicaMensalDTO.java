package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalDemandaTecnicaMensalDTO {

    private Integer ano;
    private Integer mes;
    private BigDecimal totalPlanejado;
    private BigDecimal totalExecutado;
}
