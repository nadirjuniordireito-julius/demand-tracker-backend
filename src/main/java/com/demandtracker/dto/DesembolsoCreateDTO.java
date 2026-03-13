package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DesembolsoCreateDTO {

    @Size(max = 500, message = "Documento deve ter no máximo 500 caracteres")
    private String documento;

    @NotNull(message = "Valor previsto é obrigatório")
    private BigDecimal valorPrevisto;

    @NotNull(message = "Valor do desembolso é obrigatório")
    private BigDecimal valor;

    @NotNull(message = "Data de desembolso é obrigatória")
    private LocalDate dataDesembolso;

    @NotNull(message = "Data prevista de desembolso é obrigatória")
    private LocalDate dataPrevistaDesembolso;

    @NotNull(message = "ID do projeto é obrigatório")
    private Long projetoId;
}

