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
public class DemandaTecnicaCreateDTO {
    @NotNull(message = "ID do projeto é obrigatório")
    private Long projetoId;
    
    /** Gerado automaticamente (DT-M{codigoMeta3}-{seq3}-{ano4}). Ignorado na criação. */
    @Size(max = 50, message = "Código deve ter no máximo 50 caracteres")
    private String codigo;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 200, message = "Nome deve ter no máximo 200 caracteres")
    private String nome;
    
    private String descricao; // Campo opcional para descrição formatada (HTML)
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;
    
    @NotNull(message = "ID do produto da meta é obrigatório para geração do código (metaProdutoId)")
    private Long metaProdutoId;
    
    @Size(max = 1, message = "Status deve ter no máximo 1 caractere")
    private String status;
}
