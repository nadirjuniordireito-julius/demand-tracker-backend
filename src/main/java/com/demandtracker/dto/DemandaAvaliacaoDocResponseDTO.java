package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para resposta de DemandaAvaliacaoDoc (metadados, sem o PDF)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaAvaliacaoDocResponseDTO {

    private Long id;

    private Long avaliacaoId;

    private Long demandaId;

    private LocalDateTime dataUpload;

    private String nomeArquivo;

    private String tipoConteudo;

    private Long tamanhoArquivo;
}
