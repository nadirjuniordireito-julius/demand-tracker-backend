package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de ProjetoDoc
 * Não inclui o arquivo completo, apenas metadados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoDocResponseDTO {
    
    private Long id;
    
    private Long projetoId;
    
    private String nome;
    
    private String nomeArquivo;
    
    private String tipoConteudo;
    
    private Long tamanhoArquivo;
}
