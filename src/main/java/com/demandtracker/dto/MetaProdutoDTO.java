package com.demandtracker.dto;

import com.demandtracker.entity.MetaProduto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetaProdutoDTO {
    private Long id;
    private Long projetoMetaId;
    private String codigo;
    private String nome;
    private String descricao;
    private String unidadeMedida;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private Integer inicio;
    private Integer fim;
    private String status;
    private Integer percExecutado;
    private ProjetoMetaDTO projetoMeta;

    public static MetaProdutoDTO fromEntity(MetaProduto produto) {
        return fromEntity(produto, true);
    }

    public static MetaProdutoDTO fromEntity(MetaProduto produto, boolean includeProjetoMeta) {
        MetaProdutoDTO dto = new MetaProdutoDTO();
        dto.setId(produto.getId());
        dto.setProjetoMetaId(produto.getProjetoMeta().getId());
        dto.setCodigo(produto.getCodigo());
        dto.setNome(produto.getNome());
        dto.setDescricao(produto.getDescricao());
        dto.setUnidadeMedida(produto.getUnidadeMedida());
        dto.setQuantidade(produto.getQuantidade());
        dto.setValorUnitario(produto.getValorUnitario());
        dto.setInicio(produto.getInicio());
        dto.setFim(produto.getFim());
        dto.setStatus(produto.getStatus());
        dto.setPercExecutado(produto.getPercExecutado());
        if (includeProjetoMeta && produto.getProjetoMeta() != null) {
            dto.setProjetoMeta(ProjetoMetaDTO.fromEntity(produto.getProjetoMeta(), false));
        }
        return dto;
    }
}
