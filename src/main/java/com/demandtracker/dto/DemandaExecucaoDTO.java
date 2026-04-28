package com.demandtracker.dto;

import com.demandtracker.entity.DemandaExecucao;
import com.demandtracker.entity.enums.StatusTarefa;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DemandaExecucaoDTO {
    private Long id;
    private Long demandaTecnicaId;
    private Long usuarioId;
    private UsuarioDTO usuario;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicioPlanejada;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimPlanejada;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicioReal;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFimReal;
    private String status;
    /** "Atrasada" ou "Normal" — derivado de datas planejadas/reais, progresso e tarefas. */
    private String situacao;
    private BigDecimal percentualProgresso;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataCriacaoExecucao;
    private List<DemandaExecucaoTarefaDTO> tarefas;

    public static DemandaExecucaoDTO fromEntity(DemandaExecucao e) {
        if (e == null) return null;
        DemandaExecucaoDTO dto = new DemandaExecucaoDTO();
        dto.setId(e.getId());
        dto.setDemandaTecnicaId(e.getDemanda() != null ? e.getDemanda().getId() : null);
        dto.setUsuarioId(e.getUsuario() != null ? e.getUsuario().getId() : null);
        dto.setUsuario(e.getUsuario() != null ? UsuarioDTO.fromEntity(e.getUsuario()) : null);
        dto.setDataInicioPlanejada(e.getDataInicioPlanejada());
        dto.setDataFimPlanejada(e.getDataFimPlanejada());
        dto.setDataInicioReal(e.getDataInicioReal());
        dto.setDataFimReal(e.getDataFimReal());
        dto.setStatus(e.getStatus());
        dto.setPercentualProgresso(e.getPercentualProgresso());
        dto.setDataCriacaoExecucao(e.getDataCriacaoExecucao());
        dto.setTarefas(e.getTarefas() != null
                ? e.getTarefas().stream().map(DemandaExecucaoTarefaDTO::fromEntity).collect(Collectors.toList())
                : null);
        dto.setSituacao(calcularSituacao(dto));
        return dto;
    }

    private static final String SITUACAO_ATRASADA = "Atrasada";
    private static final String SITUACAO_NORMAL = "Normal";

    /**
     * Define situação da execução com base nas informações do próprio DTO.
     * <ul>
     *   <li>Concluída: atraso se término real após fim planejado.</li>
     *   <li>Em andamento: atraso se hoje passou do fim planejado; início real depois do planejado;
     *       progresso abaixo do esperado no tempo; ou qualquer tarefa atrasada / fora do prazo.</li>
     * </ul>
     */
    private static String calcularSituacao(DemandaExecucaoDTO dto) {
        if (dto == null) {
            return SITUACAO_NORMAL;
        }
        LocalDate hoje = LocalDate.now();
        LocalDate iniP = dto.getDataInicioPlanejada();
        LocalDate fimP = dto.getDataFimPlanejada();
        LocalDate iniR = dto.getDataInicioReal();
        LocalDate fimR = dto.getDataFimReal();
        String st = dto.getStatus() != null ? dto.getStatus().trim() : "";
        boolean concluida = "CONCLUIDA".equalsIgnoreCase(st);

        if (concluida && fimR != null && fimP != null && fimR.isAfter(fimP)) {
            return SITUACAO_ATRASADA;
        }
        if (iniR != null && iniP != null && iniR.isAfter(iniP)) {
            return SITUACAO_ATRASADA;
        }
        if (!concluida && fimP != null && hoje.isAfter(fimP)) {
            return SITUACAO_ATRASADA;
        }
        if (!concluida && iniP != null && fimP != null && !hoje.isBefore(iniP) && !hoje.isAfter(fimP)) {
            long totalDias = ChronoUnit.DAYS.between(iniP, fimP) + 1;
            if (totalDias > 0) {
                long diasPassados = ChronoUnit.DAYS.between(iniP, hoje) + 1;
                // Evita marcar atraso nos primeiros dias com progresso ainda zerado
                if (diasPassados >= 3) {
                    BigDecimal esperadoPct = BigDecimal.valueOf(diasPassados)
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(totalDias), 2, RoundingMode.HALF_UP);
                    BigDecimal prog = dto.getPercentualProgresso() != null ? dto.getPercentualProgresso() : BigDecimal.ZERO;
                    BigDecimal limiar = esperadoPct.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_UP);
                    if (prog.compareTo(limiar) < 0) {
                        return SITUACAO_ATRASADA;
                    }
                }
            }
        }
        if (dto.getTarefas() != null) {
            for (DemandaExecucaoTarefaDTO t : dto.getTarefas()) {
                if (t == null) {
                    continue;
                }
                if (t.getStatus() == StatusTarefa.ATRASADA) {
                    return SITUACAO_ATRASADA;
                }
                LocalDate tfimP = t.getDataFimPlanejada();
                LocalDate tfimR = t.getDataFimReal();
                LocalDate tiniP = t.getDataInicioPlanejada();
                LocalDate tiniR = t.getDataInicioReal();
                if (tfimR != null && tfimP != null && tfimR.isAfter(tfimP)) {
                    return SITUACAO_ATRASADA;
                }
                if (tiniR != null && tiniP != null && tiniR.isAfter(tiniP)) {
                    return SITUACAO_ATRASADA;
                }
                boolean tConcluida = t.getStatus() == StatusTarefa.CONCLUIDA;
                if (!tConcluida && tfimP != null && hoje.isAfter(tfimP)) {
                    return SITUACAO_ATRASADA;
                }
            }
        }
        return SITUACAO_NORMAL;
    }
}
