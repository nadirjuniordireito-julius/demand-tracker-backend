package com.demandtracker.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoPlanejamentoUpdateDTO {
    @Size(min = 10, max = 5000, message = "Especificação deve ter entre 10 e 5000 caracteres")
    private String especificacao;
    
    @Size(min = 10, max = 2000, message = "Cronograma deve ter entre 10 e 2000 caracteres")
    private String cronograma;
    
    @Size(min = 10, max = 2000, message = "Resultado esperado deve ter entre 10 e 2000 caracteres")
    private String resultadoEsperado;
    
    @Valid
    private List<TermoPlanejamentoCustoCreateDTO> custos;
}
