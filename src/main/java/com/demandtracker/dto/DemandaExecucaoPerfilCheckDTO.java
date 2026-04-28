package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoPerfilCheckDTO {
    private Long perfilId;
    private String perfilNome;
    private BigDecimal horasPlanejadasTermo;
    private BigDecimal horasPlanejadasExecucao;
}
