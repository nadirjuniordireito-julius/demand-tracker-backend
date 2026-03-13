package com.demandtracker.dto;

import com.demandtracker.entity.enums.StatusTarefa;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaUpdateDTO {

    @Size(max = 500)
    private String titulo;

    @Size(max = 9000)
    private String descricao;

    private StatusTarefa status;

    @Size(min = 1, max = 1)
    private String prioridade;

    private LocalDate dataInicioPlanejada;
    private LocalDate dataFimPlanejada;
    private LocalDate dataInicioReal;
    private LocalDate dataFimReal;

    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal percentualProgresso;

    @DecimalMin(value = "0.00")
    private BigDecimal estimativaHoras;
}
