package com.demandtracker.dto;

import com.demandtracker.entity.ProjetoMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjetoMetaDTO {
    private Long id;
    private Long projetoId;
    private String codigo;
    private String nome;
    private String descricao;
    private String status;
    private ProjetoDTO projeto;
    private List<MetaProdutoDTO> produtos;
    
    public static ProjetoMetaDTO fromEntity(ProjetoMeta meta) {
        return fromEntity(meta, true);
    }
    
    public static ProjetoMetaDTO fromEntity(ProjetoMeta meta, boolean includeProdutos) {
        ProjetoMetaDTO dto = new ProjetoMetaDTO();
        dto.setId(meta.getId());
        dto.setProjetoId(meta.getProjeto().getId());
        dto.setCodigo(meta.getCodigo());
        dto.setNome(meta.getNome());
        dto.setDescricao(meta.getDescricao());
        dto.setStatus(meta.getStatus());
        if (meta.getProjeto() != null) {
            dto.setProjeto(ProjetoDTO.fromEntity(meta.getProjeto()));
        }
        if (includeProdutos && meta.getProdutos() != null) {
            dto.setProdutos(meta.getProdutos().stream()
                .map(p -> MetaProdutoDTO.fromEntity(p, false))
                .collect(Collectors.toList()));
        }
        return dto;
    }
}
