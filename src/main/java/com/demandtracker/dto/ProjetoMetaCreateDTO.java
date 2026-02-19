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
public class ProjetoMetaCreateDTO {
    @NotNull(message = "ID do projeto é obrigatório")
    private Long projetoId;
    
    @NotBlank(message = "Código é obrigatório")
    @Size(max = 10, message = "Código deve ter no máximo 10 caracteres")
    private String codigo;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 500, message = "Nome deve ter no máximo 500 caracteres")
    private String nome;
    
    private String descricao;
    
    @NotBlank(message = "Status é obrigatório")
    @Size(max = 1, message = "Status deve ter no máximo 1 caractere")
    private String status;
}
