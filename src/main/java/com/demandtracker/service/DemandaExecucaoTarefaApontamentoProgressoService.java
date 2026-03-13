package com.demandtracker.service;

import com.demandtracker.dto.DemandaExecucaoTarefaApontamentoProgressoCreateDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaApontamentoProgressoDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaApontamentoProgressoUpdateDTO;
import com.demandtracker.entity.DemandaExecucaoTarefa;
import com.demandtracker.entity.DemandaExecucaoTarefaApontamentoProgresso;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaExecucaoTarefaApontamentoProgressoRepository;
import com.demandtracker.repository.DemandaExecucaoTarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandaExecucaoTarefaApontamentoProgressoService {

    private final DemandaExecucaoTarefaApontamentoProgressoRepository repository;
    private final DemandaExecucaoTarefaRepository tarefaRepository;

    @Transactional(readOnly = true)
    public List<DemandaExecucaoTarefaApontamentoProgressoDTO> findByDemandaExecucaoTarefaId(Long tarefaId) {
        return repository.findByDemandaExecucaoTarefaIdOrderByDataAsc(tarefaId).stream()
                .map(DemandaExecucaoTarefaApontamentoProgressoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DemandaExecucaoTarefaApontamentoProgressoDTO findById(Long id) {
        DemandaExecucaoTarefaApontamentoProgresso a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apontamento não encontrado com ID: " + id));
        return DemandaExecucaoTarefaApontamentoProgressoDTO.fromEntity(a);
    }

    @Transactional
    public DemandaExecucaoTarefaApontamentoProgressoDTO create(DemandaExecucaoTarefaApontamentoProgressoCreateDTO dto) {
        DemandaExecucaoTarefa tarefa = tarefaRepository.findById(dto.getDemandaExecucaoTarefaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com ID: " + dto.getDemandaExecucaoTarefaId()));

        DemandaExecucaoTarefaApontamentoProgresso a = new DemandaExecucaoTarefaApontamentoProgresso();
        a.setDemandaExecucaoTarefa(tarefa);
        a.setData(dto.getData());
        a.setPercentual(dto.getPercentual());
        a.setComentario(dto.getComentario());
        return DemandaExecucaoTarefaApontamentoProgressoDTO.fromEntity(repository.save(a));
    }

    @Transactional
    public DemandaExecucaoTarefaApontamentoProgressoDTO update(Long id, DemandaExecucaoTarefaApontamentoProgressoUpdateDTO dto) {
        DemandaExecucaoTarefaApontamentoProgresso a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apontamento não encontrado com ID: " + id));
        if (dto.getData() != null) a.setData(dto.getData());
        if (dto.getPercentual() != null) a.setPercentual(dto.getPercentual());
        if (dto.getComentario() != null) a.setComentario(dto.getComentario());
        return DemandaExecucaoTarefaApontamentoProgressoDTO.fromEntity(repository.save(a));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Apontamento não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }
}
