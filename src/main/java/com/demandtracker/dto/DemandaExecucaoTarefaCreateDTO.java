package com.demandtracker.dto;

import com.demandtracker.entity.enums.StatusTarefa;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaCreateDTO {

    @NotNull(message = "ID da execução da demanda é obrigatório")
    private Long demandaExecucaoId;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 500)
    private String titulo;

    @Size(max = 9000)
    private String descricao;

    @NotNull(message = "Status é obrigatório")
    private StatusTarefa status;

    @NotBlank(message = "Prioridade é obrigatória")
    @Size(min = 1, max = 1)
    private String prioridade;

    @NotNull(message = "Data início planejada é obrigatória")
    private LocalDate dataInicioPlanejada;

    @NotNull(message = "Data fim planejada é obrigatória")
    private LocalDate dataFimPlanejada;

    private LocalDate dataInicioReal;
    private LocalDate dataFimReal;

    @NotNull(message = "Percentual de progresso é obrigatório")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal percentualProgresso;

    @NotNull(message = "Estimativa de horas é obrigatória")
    @DecimalMin(value = "0.00")
    private BigDecimal estimativaHoras;

    private Integer sequencia;
}
