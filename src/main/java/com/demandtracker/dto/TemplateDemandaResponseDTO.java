package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de TemplateDemanda
 * Não inclui o arquivo DOCX completo, apenas metadados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDemandaResponseDTO {
    
    private Long id;
    
    private Long projetoId;
    
    private String tipo;
    
    private String nomeArquivo;
    
    private String tipoConteudo;
    
    private Long tamanhoArquivo;
}
