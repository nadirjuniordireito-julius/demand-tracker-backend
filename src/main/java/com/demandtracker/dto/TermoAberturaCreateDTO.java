package com.demandtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoAberturaCreateDTO {
    @NotNull(message = "ID da demanda técnica é obrigatório")
    private Long demandaTecnicaId;
    
    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 10, max = 2000, message = "Descrição deve ter entre 10 e 2000 caracteres")
    private String descricao;
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;
}
