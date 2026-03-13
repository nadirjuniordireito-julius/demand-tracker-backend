package com.demandtracker.dto;

import com.demandtracker.entity.TermoEncerramentoAnexo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta para anexo do Termo de Encerramento.
 * Não inclui o conteúdo do arquivo, apenas metadados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoEncerramentoAnexoResponseDTO {

    private Long id;
    private Long termoEncerramentoId;
    private String nomeArquivo;
    private String tipoConteudo;
    private Long tamanhoArquivo;
    private Long usuarioId;

    public static TermoEncerramentoAnexoResponseDTO fromEntity(TermoEncerramentoAnexo entity) {
        TermoEncerramentoAnexoResponseDTO dto = new TermoEncerramentoAnexoResponseDTO();
        dto.setId(entity.getId());
        dto.setTermoEncerramentoId(entity.getTermoEncerramento() != null ? entity.getTermoEncerramento().getId() : null);
        dto.setNomeArquivo(entity.getNomeArquivo());
        dto.setTipoConteudo(entity.getTipoConteudo());
        dto.setTamanhoArquivo(entity.getTamanhoArquivo());
        dto.setUsuarioId(entity.getUsuario() != null ? entity.getUsuario().getId() : null);
        return dto;
    }
}
