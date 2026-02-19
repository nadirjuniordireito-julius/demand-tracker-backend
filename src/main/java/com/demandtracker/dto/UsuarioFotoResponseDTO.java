package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de UsuarioFoto
 * Não inclui o arquivo completo, apenas metadados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioFotoResponseDTO {
    
    private Long id;
    
    private Long usuarioId;
    
    private Long tamanhoFoto;
}
