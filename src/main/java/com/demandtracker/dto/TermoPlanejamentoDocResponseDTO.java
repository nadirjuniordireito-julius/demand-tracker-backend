package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de TermoPlanejamentoDoc
 * Não inclui o arquivo PDF completo, apenas metadados
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoPlanejamentoDocResponseDTO {
    
    private Long id;
    
    private Long termoPlanejamentoId;
    
    private LocalDateTime dataAssinatura;
    
    private String nomeArquivo;
    
    private String tipoConteudo;
    
    private Long tamanhoArquivo;
}
