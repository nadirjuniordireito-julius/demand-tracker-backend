package com.demandtracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalCustoMensalUpdateDTO {

    private Long profissionalId;

    @Min(value = 1900, message = "Ano deve ser maior ou igual a 1900")
    @Max(value = 3000, message = "Ano deve ser menor ou igual a 3000")
    private Integer ano;

    @Min(value = 1, message = "Mês deve ser entre 1 e 12")
    @Max(value = 12, message = "Mês deve ser entre 1 e 12")
    private Integer mes;

    @DecimalMin(value = "0.00", message = "Custo total deve ser maior ou igual a zero")
    private BigDecimal custoTotal;
}
