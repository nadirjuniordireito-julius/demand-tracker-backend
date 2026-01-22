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
public class TermoEncerramentoUpdateDTO {
    @Size(min = 10, max = 5000, message = "Resultado entregue deve ter entre 10 e 5000 caracteres")
    private String resultadoEntregue;
    
    @Valid
    private List<TermoEncerramentoCustoCreateDTO> custos;
}
