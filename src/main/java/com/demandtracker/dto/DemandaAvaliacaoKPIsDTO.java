package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaAvaliacaoKPIsDTO {
    private BigDecimal indiceSaude;
    private BigDecimal indiceConfiabilidade;
    private BigDecimal indiceRisco;
}
