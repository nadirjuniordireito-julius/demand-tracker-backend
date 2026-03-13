package com.demandtracker.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class DemandaExecucaoTarefaApontamentoProgressoCreateDTO {

    @NotNull(message = "ID da tarefa é obrigatório")
    private Long demandaExecucaoTarefaId;

    @NotNull(message = "Data é obrigatória")
    private LocalDate data;

    @NotNull(message = "Percentual é obrigatório")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal percentual;

    @NotBlank(message = "Comentário é obrigatório")
    @Size(max = 9000)
    private String comentario;
}
