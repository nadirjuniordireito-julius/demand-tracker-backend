package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoPlanejamentoCustoCreateDTO {
    @NotNull(message = "ID do perfil é obrigatório")
    private Long perfilId;
    
    @NotNull(message = "Quantidade de horas é obrigatória")
    @Positive(message = "Quantidade de horas deve ser maior que zero")
    private BigDecimal qtdeHora;
    
    @NotNull(message = "Valor hora é obrigatório")
    @Positive(message = "Valor hora deve ser maior que zero")
    private BigDecimal valorHora;
}
