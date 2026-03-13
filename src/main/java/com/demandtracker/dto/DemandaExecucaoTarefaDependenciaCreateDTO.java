package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoTarefaDependenciaCreateDTO {

    @NotNull(message = "ID da tarefa origem é obrigatório")
    private Long tarefaOrigemId;

    @NotNull(message = "ID da tarefa destino é obrigatório")
    private Long tarefaDestinoId;
}
