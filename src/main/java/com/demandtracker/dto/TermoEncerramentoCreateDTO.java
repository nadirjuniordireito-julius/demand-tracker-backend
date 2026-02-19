package com.demandtracker.dto;

import com.demandtracker.config.LocalDateTimeFlexDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramentoCreateDTO {
    @NotNull(message = "ID da demanda técnica é obrigatório")
    private Long demandaTecnicaId;
    
    @NotNull(message = "Data do termo é obrigatória")
    @JsonDeserialize(using = LocalDateTimeFlexDeserializer.class)
    private LocalDateTime dataTermo;
    
    private LocalDate dataInicioExecucao;
    
    private LocalDate dataFimExecucao;
    
    @NotBlank(message = "Resultado entregue é obrigatório")
    @Size(min = 10, max = 5000, message = "Resultado entregue deve ter entre 10 e 5000 caracteres")
    private String resultadoEntregue;
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;
    
    @Valid
    private List<TermoEncerramentoCustoCreateDTO> custos;
}
