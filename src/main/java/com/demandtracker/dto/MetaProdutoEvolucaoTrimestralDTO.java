package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaProdutoEvolucaoTrimestralDTO {
    private Long metaProdutoId;
    private String codigoProduto;
    private String nomeProduto;
    private LocalDate dataInicioAnalise;
    private LocalDate dataFimAnalise;
    private BigDecimal totalPrevistoProduto;
    private BigDecimal totalExecutadoProduto;
    private List<MetaProdutoEvolucaoTrimestralItemDTO> trimestres = new ArrayList<>();
}
