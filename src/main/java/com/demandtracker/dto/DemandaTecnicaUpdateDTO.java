package com.demandtracker.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaTecnicaUpdateDTO {
    private Long projetoId;
    
    /** Código não é alterável (gerado automaticamente na criação). Ignorado no update. */
    @Size(max = 50, message = "Código deve ter no máximo 50 caracteres")
    private String codigo;
    
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;
    
    private String descricao; // Campo opcional para descrição formatada (HTML)
    
    private Long metaProdutoId;
    
    @Size(max = 1, message = "Status deve ter no máximo 1 caractere")
    private String status;
}
