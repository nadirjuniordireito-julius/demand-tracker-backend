package com.demandtracker.dto;

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
public class TermoPlanejamentoCreateDTO {
    @NotNull(message = "ID da demanda técnica é obrigatório")
    private Long demandaTecnicaId;
    
    @NotNull(message = "Data de abertura é obrigatória")
    private LocalDateTime dataAbertura;
    
    private LocalDate dataInicioExecucao;
    
    private LocalDate dataFimExecucao;
    
    @NotBlank(message = "Especificação é obrigatória")
    @Size(min = 10, max = 5000, message = "Especificação deve ter entre 10 e 5000 caracteres")
    private String especificacao;
    
    @NotBlank(message = "Cronograma é obrigatório")
    @Size(min = 10, max = 2000, message = "Cronograma deve ter entre 10 e 2000 caracteres")
    private String cronograma;
    
    @NotBlank(message = "Resultado esperado é obrigatório")
    @Size(min = 10, max = 2000, message = "Resultado esperado deve ter entre 10 e 2000 caracteres")
    private String resultadoEsperado;
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;
    
    @Valid
    private List<TermoPlanejamentoCustoCreateDTO> custos;
}
