package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de TermoEncerramentoDoc
 * Não inclui o arquivo PDF completo, apenas metadados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramentoDocResponseDTO {
    
    private Long id;
    
    private Long termoEncerramentoId;
    
    private LocalDateTime dataAssinatura;
    
    private String nomeArquivo;
    
    private String tipoConteudo;
    
    private Long tamanhoArquivo;
}
