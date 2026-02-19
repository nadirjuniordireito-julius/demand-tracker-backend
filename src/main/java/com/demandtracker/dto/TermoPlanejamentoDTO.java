package com.demandtracker.dto;

import com.demandtracker.entity.TermoPlanejamento;
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
public class TermoPlanejamentoDTO {
    private Long id;
    private Long demandaTecnicaId;
    /** Valor previsto do produto (valorUnitario * quantidade) do MetaProduto da demanda. Para exibir no header do modal. */
    private BigDecimal valorPrevistoProduto;
    /** Valor total já executado do produto: somatória dos custos realizados de todas as demandas encerradas (status G) do produto. Para exibir no header do modal. */
    private BigDecimal valorTotalExecutadoProduto;
    private String especificacao;
    private String cronograma;
    private String resultadoEsperado;
    private LocalDateTime dataAbertura;
    private Long usuarioId;
    private LocalDateTime dataAssinatura;
    private LocalDate dataInicioExecucao;
    private LocalDate dataFimExecucao;
    private DemandaTecnicaDTO demandaTecnica;
    private UsuarioDTO usuario;
    private List<TermoPlanejamentoCustoDTO> custos;
    
    public static TermoPlanejamentoDTO fromEntity(TermoPlanejamento termo) {
        TermoPlanejamentoDTO dto = new TermoPlanejamentoDTO();
        dto.setId(termo.getId());
        dto.setDemandaTecnicaId(termo.getDemandaTecnica().getId());
        dto.setEspecificacao(termo.getEspecificacao());
        dto.setCronograma(termo.getCronograma());
        dto.setResultadoEsperado(termo.getResultadoEsperado());
        dto.setDataAbertura(termo.getDataAbertura());
        dto.setUsuarioId(termo.getUsuario().getId());
        dto.setDataAssinatura(termo.getDataAssinatura());
        dto.setDataInicioExecucao(termo.getDataInicioExecucao());
        dto.setDataFimExecucao(termo.getDataFimExecucao());
        if (termo.getUsuario() != null) {
            dto.setUsuario(UsuarioDTO.fromEntity(termo.getUsuario()));
        }
        if (termo.getCustos() != null) {
            dto.setCustos(termo.getCustos().stream()
                .map(TermoPlanejamentoCustoDTO::fromEntity)
                .collect(Collectors.toList()));
        }
        return dto;
    }
}
