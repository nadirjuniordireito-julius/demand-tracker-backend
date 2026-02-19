package com.demandtracker.dto;

import com.demandtracker.entity.DemandaTecnica;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaTecnicaDTO {
    private Long id;
    private Long projetoId;
    private String codigo;
    private String nome;
    private String descricao; // Campo opcional para descrição formatada (HTML)
    private LocalDateTime dataAbertura;
    private Long usuarioId;
    private ProjetoDTO projeto;
    private UsuarioDTO usuario;
    private TermoAberturaDTO termoAbertura;
    private TermoPlanejamentoDTO termoPlanejamento;
    private TermoEncerramentoDTO termoEncerramento;
    private Long metaProdutoId;
    private MetaProdutoDTO metaProduto;
    /** Valor total executado do produto: somatória dos custos realizados de todas as demandas encerradas (status G) do produto vinculado a esta demanda. */
    private BigDecimal totalExecutadoProduto;
    private String status;
    private Boolean avaliacaoDisponivel;

    public static DemandaTecnicaDTO fromEntity(DemandaTecnica demanda) {
        DemandaTecnicaDTO dto = new DemandaTecnicaDTO();
        dto.setId(demanda.getId());
        dto.setProjetoId(demanda.getProjeto().getId());
        dto.setCodigo(demanda.getCodigo());
        dto.setNome(demanda.getNome());
        dto.setDescricao(demanda.getDescricao());
        dto.setDataAbertura(demanda.getDataAbertura());
        dto.setUsuarioId(demanda.getUsuario().getId());
        
        if (demanda.getProjeto() != null) {
            dto.setProjeto(ProjetoDTO.fromEntity(demanda.getProjeto()));
        }
        if (demanda.getUsuario() != null) {
            dto.setUsuario(UsuarioDTO.fromEntity(demanda.getUsuario()));
        }
        if (demanda.getTermoAbertura() != null) {
            dto.setTermoAbertura(TermoAberturaDTO.fromEntity(demanda.getTermoAbertura()));
        }
        if (demanda.getTermoPlanejamento() != null) {
            dto.setTermoPlanejamento(TermoPlanejamentoDTO.fromEntity(demanda.getTermoPlanejamento()));
        }
        if (demanda.getTermoEncerramento() != null) {
            dto.setTermoEncerramento(TermoEncerramentoDTO.fromEntity(demanda.getTermoEncerramento()));
        }
        if (demanda.getMetaProduto() != null) {
            dto.setMetaProdutoId(demanda.getMetaProduto().getId());
            dto.setMetaProduto(MetaProdutoDTO.fromEntity(demanda.getMetaProduto(), true));
        }
        
        dto.setStatus(demanda.getStatus());
        dto.setAvaliacaoDisponivel(Boolean.TRUE.equals(demanda.getAvaliacaoDisponivel()));

        return dto;
    }
}
