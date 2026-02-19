package com.demandtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoCreateDTO {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;
    
    @NotBlank(message = "Código TED é obrigatório")
    @Size(max = 50, message = "Código TED deve ter no máximo 50 caracteres")
    private String codTed;
    
    @NotNull(message = "Data inicial é obrigatória")
    private LocalDate termoInicial;
    
    @NotNull(message = "Data final é obrigatória")
    private LocalDate termoFinal;
    
    private LocalDate dataEfetivaInicio;
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;
}
