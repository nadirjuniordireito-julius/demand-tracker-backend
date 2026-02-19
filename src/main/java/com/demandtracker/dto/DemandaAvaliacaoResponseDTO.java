package com.demandtracker.dto;

import com.demandtracker.entity.DemandaAvaliacao;
import com.demandtracker.entity.enums.Reutilizacao;
import com.demandtracker.entity.enums.TipoRisco;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaAvaliacaoResponseDTO {
    private Long id;
    private Long demandaId;
    private Long usuarioId;
    private LocalDateTime dataHoraPreenchimento;
    private Long termoEncerramentoId;
    private LocalDateTime dataAvaliacao;
    private LocalDate dataEncerramento;
    private Boolean atraso;
    private BigDecimal desvioPrazoPercentual;
    private Integer impactoAtraso;
    private BigDecimal desvioCustoPercentual;
    private Integer impactoFinanceiro;
    private Integer atendimentoRequisitos;
    private Integer estabilidade;
    private Integer retrabalho;
    private Integer satisfacaoUsuario;
    private Integer clarezaRequisitos;
    private Integer qualidadePlanejamento;
    private Integer aderenciaCronograma;
    private Integer comunicacao;
    private Integer capacidadeEquipe;
    private Integer disponibilidadeEquipe;
    private Boolean possuiBackupCritico;
    private Boolean rotatividadeImpactou;
    private Integer valorPercebido;
    private Integer alinhamentoMeta;
    private Reutilizacao reutilizacao;
    private Integer avaliacaoGeral;
    private Boolean repetiriaModelo;
    private BigDecimal indiceSaude;
    private BigDecimal indiceConfiabilidade;
    private BigDecimal indiceRisco;
    private List<TipoRisco> riscos;
    private DemandaAvaliacaoTextoResponseDTO texto;

    public static DemandaAvaliacaoResponseDTO fromEntity(DemandaAvaliacao a) {
        DemandaAvaliacaoResponseDTO dto = new DemandaAvaliacaoResponseDTO();
        dto.setId(a.getId());
        dto.setDemandaId(a.getDemanda() != null ? a.getDemanda().getId() : null);
        dto.setUsuarioId(a.getUsuario() != null ? a.getUsuario().getId() : null);
        dto.setDataHoraPreenchimento(a.getDataHoraPreenchimento());
        dto.setTermoEncerramentoId(a.getTermoEncerramentoId());
        dto.setDataAvaliacao(a.getDataAvaliacao());
        dto.setDataEncerramento(a.getDataEncerramento());
        dto.setAtraso(a.getAtraso());
        dto.setDesvioPrazoPercentual(a.getDesvioPrazoPercentual());
        dto.setImpactoAtraso(a.getImpactoAtraso());
        dto.setDesvioCustoPercentual(a.getDesvioCustoPercentual());
        dto.setImpactoFinanceiro(a.getImpactoFinanceiro());
        dto.setAtendimentoRequisitos(a.getAtendimentoRequisitos());
        dto.setEstabilidade(a.getEstabilidade());
        dto.setRetrabalho(a.getRetrabalho());
        dto.setSatisfacaoUsuario(a.getSatisfacaoUsuario());
        dto.setClarezaRequisitos(a.getClarezaRequisitos());
        dto.setQualidadePlanejamento(a.getQualidadePlanejamento());
        dto.setAderenciaCronograma(a.getAderenciaCronograma());
        dto.setComunicacao(a.getComunicacao());
        dto.setCapacidadeEquipe(a.getCapacidadeEquipe());
        dto.setDisponibilidadeEquipe(a.getDisponibilidadeEquipe());
        dto.setPossuiBackupCritico(a.getPossuiBackupCritico());
        dto.setRotatividadeImpactou(a.getRotatividadeImpactou());
        dto.setValorPercebido(a.getValorPercebido());
        dto.setAlinhamentoMeta(a.getAlinhamentoMeta());
        dto.setReutilizacao(a.getReutilizacao());
        dto.setAvaliacaoGeral(a.getAvaliacaoGeral());
        dto.setRepetiriaModelo(a.getRepetiriaModelo());
        dto.setIndiceSaude(a.getIndiceSaude());
        dto.setIndiceConfiabilidade(a.getIndiceConfiabilidade());
        dto.setIndiceRisco(a.getIndiceRisco());
        if (a.getRiscos() != null) {
            dto.setRiscos(a.getRiscos().stream().map(r -> r.getTipo()).collect(Collectors.toList()));
        }
        if (a.getTexto() != null) {
            dto.setTexto(DemandaAvaliacaoTextoResponseDTO.fromEntity(a.getTexto()));
        }
        return dto;
    }
}
