package com.demandtracker.service;

import com.demandtracker.dto.DemandaExecucaoTarefaCreateDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaUpdateDTO;
import com.demandtracker.entity.DemandaExecucao;
import com.demandtracker.entity.DemandaExecucaoTarefa;
import com.demandtracker.entity.enums.StatusTarefa;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaExecucaoRepository;
import com.demandtracker.repository.DemandaExecucaoTarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandaExecucaoTarefaService {

    private final DemandaExecucaoTarefaRepository repository;
    private final DemandaExecucaoRepository demandaExecucaoRepository;

    @Transactional(readOnly = true)
    public List<DemandaExecucaoTarefaDTO> findByDemandaExecucaoId(Long demandaExecucaoId) {
        return repository.findByDemandaExecucaoIdOrderBySequenciaAscIdAsc(demandaExecucaoId).stream()
                .map(DemandaExecucaoTarefaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DemandaExecucaoTarefaDTO findById(Long id) {
        DemandaExecucaoTarefa t = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com ID: " + id));
        return DemandaExecucaoTarefaDTO.fromEntity(t);
    }

    @Transactional
    public DemandaExecucaoTarefaDTO create(DemandaExecucaoTarefaCreateDTO dto) {
        DemandaExecucao execucao = demandaExecucaoRepository.findById(dto.getDemandaExecucaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Execução da demanda não encontrada com ID: " + dto.getDemandaExecucaoId()));

        DemandaExecucaoTarefa t = new DemandaExecucaoTarefa();
        t.setDemandaExecucao(execucao);
        t.setTitulo(dto.getTitulo());
        t.setDescricao(dto.getDescricao());
        t.setStatus(dto.getStatus());
        t.setPrioridade(dto.getPrioridade());
        t.setDataInicioPlanejada(dto.getDataInicioPlanejada());
        t.setDataFimPlanejada(dto.getDataFimPlanejada());
        t.setDataInicioReal(dto.getDataInicioReal());
        t.setDataFimReal(dto.getDataFimReal());
        t.setPercentualProgresso(dto.getPercentualProgresso());
        t.setEstimativaHoras(dto.getEstimativaHoras());
        t.setSequencia(dto.getSequencia());

        aplicarRegraStatusPorPercentual(t);

        return DemandaExecucaoTarefaDTO.fromEntity(repository.save(t));
    }

    @Transactional
    public DemandaExecucaoTarefaDTO update(Long id, DemandaExecucaoTarefaUpdateDTO dto) {
        DemandaExecucaoTarefa t = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com ID: " + id));

        if (dto.getTitulo() != null) t.setTitulo(dto.getTitulo());
        if (dto.getDescricao() != null) t.setDescricao(dto.getDescricao());
        if (dto.getStatus() != null) t.setStatus(dto.getStatus());
        if (dto.getPrioridade() != null) t.setPrioridade(dto.getPrioridade());
        if (dto.getDataInicioPlanejada() != null) t.setDataInicioPlanejada(dto.getDataInicioPlanejada());
        if (dto.getDataFimPlanejada() != null) t.setDataFimPlanejada(dto.getDataFimPlanejada());
        if (dto.getDataInicioReal() != null) t.setDataInicioReal(dto.getDataInicioReal());
        if (dto.getDataFimReal() != null) t.setDataFimReal(dto.getDataFimReal());
        if (dto.getPercentualProgresso() != null) t.setPercentualProgresso(dto.getPercentualProgresso());
        if (dto.getEstimativaHoras() != null) t.setEstimativaHoras(dto.getEstimativaHoras());
        if (dto.getSequencia() != null) t.setSequencia(dto.getSequencia());

        aplicarRegraStatusPorPercentual(t);

        return DemandaExecucaoTarefaDTO.fromEntity(repository.save(t));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tarefa não encontrada com ID: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Regra automática de status conforme o percentual de progresso da tarefa:
     *  - {@code percentualProgresso == 100} → status passa a {@link StatusTarefa#CONCLUIDA};
     *  - {@code percentualProgresso < 100} e status atual {@link StatusTarefa#CONCLUIDA} → reverte para
     *    {@link StatusTarefa#EM_ANDAMENTO}.
     *
     * Demais combinações (status PLANEJADA, BLOQUEADA, ATRASADA com percentual &lt; 100) são preservadas
     * para não sobrescrever escolhas legítimas do usuário.
     */
    private void aplicarRegraStatusPorPercentual(DemandaExecucaoTarefa t) {
        if (t == null || t.getPercentualProgresso() == null) {
            return;
        }
        int cmp = t.getPercentualProgresso().compareTo(BigDecimal.valueOf(100));
        if (cmp >= 0) {
            t.setStatus(StatusTarefa.CONCLUIDA);
        } else if (StatusTarefa.CONCLUIDA.equals(t.getStatus())) {
            t.setStatus(StatusTarefa.EM_ANDAMENTO);
        }
    }
}
