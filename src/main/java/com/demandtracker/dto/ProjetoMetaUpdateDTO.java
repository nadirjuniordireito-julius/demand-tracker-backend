package com.demandtracker.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoMetaUpdateDTO {
    private Long projetoId;
    
    @Size(max = 10, message = "Código deve ter no máximo 10 caracteres")
    private String codigo;
    
    @Size(max = 500, message = "Nome deve ter no máximo 500 caracteres")
    private String nome;
    
    private String descricao;
    
    @Size(max = 1, message = "Status deve ter no máximo 1 caractere")
    private String status;
}
