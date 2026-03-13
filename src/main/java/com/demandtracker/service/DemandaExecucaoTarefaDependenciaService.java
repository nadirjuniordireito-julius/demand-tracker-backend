package com.demandtracker.service;

import com.demandtracker.dto.DemandaExecucaoTarefaDependenciaCreateDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaDependenciaDTO;
import com.demandtracker.entity.DemandaExecucaoTarefa;
import com.demandtracker.entity.DemandaExecucaoTarefaDependencia;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaExecucaoTarefaDependenciaRepository;
import com.demandtracker.repository.DemandaExecucaoTarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandaExecucaoTarefaDependenciaService {

    private final DemandaExecucaoTarefaDependenciaRepository repository;
    private final DemandaExecucaoTarefaRepository tarefaRepository;

    @Transactional(readOnly = true)
    public List<DemandaExecucaoTarefaDependenciaDTO> findByTarefaOrigemId(Long tarefaOrigemId) {
        return repository.findByTarefaOrigemId(tarefaOrigemId).stream()
                .map(DemandaExecucaoTarefaDependenciaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DemandaExecucaoTarefaDependenciaDTO> findByTarefaDestinoId(Long tarefaDestinoId) {
        return repository.findByTarefaDestinoId(tarefaDestinoId).stream()
                .map(DemandaExecucaoTarefaDependenciaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DemandaExecucaoTarefaDependenciaDTO findById(Long id) {
        DemandaExecucaoTarefaDependencia d = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dependência não encontrada com ID: " + id));
        return DemandaExecucaoTarefaDependenciaDTO.fromEntity(d);
    }

    @Transactional
    public DemandaExecucaoTarefaDependenciaDTO create(DemandaExecucaoTarefaDependenciaCreateDTO dto) {
        if (dto.getTarefaOrigemId().equals(dto.getTarefaDestinoId())) {
            throw new BadRequestException("Tarefa origem e destino não podem ser iguais.");
        }
        DemandaExecucaoTarefa origem = tarefaRepository.findById(dto.getTarefaOrigemId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa origem não encontrada com ID: " + dto.getTarefaOrigemId()));
        DemandaExecucaoTarefa destino = tarefaRepository.findById(dto.getTarefaDestinoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa destino não encontrada com ID: " + dto.getTarefaDestinoId()));

        DemandaExecucaoTarefaDependencia dep = new DemandaExecucaoTarefaDependencia();
        dep.setTarefaOrigem(origem);
        dep.setTarefaDestino(destino);
        return DemandaExecucaoTarefaDependenciaDTO.fromEntity(repository.save(dep));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Dependência não encontrada com ID: " + id);
        }
        repository.deleteById(id);
    }
}
