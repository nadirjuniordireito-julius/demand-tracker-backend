package com.demandtracker.service;

import com.demandtracker.dto.DemandaExecucaoTarefaRecursoCreateDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaRecursoDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaRecursoUpdateDTO;
import com.demandtracker.entity.DemandaExecucaoTarefa;
import com.demandtracker.entity.DemandaExecucaoTarefaRecurso;
import com.demandtracker.entity.Profissional;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaExecucaoTarefaRecursoRepository;
import com.demandtracker.repository.DemandaExecucaoTarefaRepository;
import com.demandtracker.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandaExecucaoTarefaRecursoService {

    private final DemandaExecucaoTarefaRecursoRepository repository;
    private final DemandaExecucaoTarefaRepository tarefaRepository;
    private final ProfissionalRepository profissionalRepository;

    @Transactional(readOnly = true)
    public List<DemandaExecucaoTarefaRecursoDTO> findByDemandaExecucaoTarefaId(Long tarefaId) {
        return repository.findByDemandaExecucaoTarefaId(tarefaId).stream()
                .map(DemandaExecucaoTarefaRecursoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DemandaExecucaoTarefaRecursoDTO findById(Long id) {
        DemandaExecucaoTarefaRecurso r = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado com ID: " + id));
        return DemandaExecucaoTarefaRecursoDTO.fromEntity(r);
    }

    @Transactional
    public DemandaExecucaoTarefaRecursoDTO create(DemandaExecucaoTarefaRecursoCreateDTO dto) {
        DemandaExecucaoTarefa tarefa = tarefaRepository.findById(dto.getDemandaExecucaoTarefaId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com ID: " + dto.getDemandaExecucaoTarefaId()));
        Profissional profissional = profissionalRepository.findById(dto.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + dto.getProfissionalId()));

        DemandaExecucaoTarefaRecurso r = new DemandaExecucaoTarefaRecurso();
        r.setDemandaExecucaoTarefa(tarefa);
        r.setProfissional(profissional);
        r.setHorasPlanejadas(dto.getHorasPlanejadas());
        return DemandaExecucaoTarefaRecursoDTO.fromEntity(repository.save(r));
    }

    @Transactional
    public DemandaExecucaoTarefaRecursoDTO update(Long id, DemandaExecucaoTarefaRecursoUpdateDTO dto) {
        DemandaExecucaoTarefaRecurso r = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recurso não encontrado com ID: " + id));
        if (dto.getProfissionalId() != null) {
            Profissional p = profissionalRepository.findById(dto.getProfissionalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + dto.getProfissionalId()));
            r.setProfissional(p);
        }
        if (dto.getHorasPlanejadas() != null) r.setHorasPlanejadas(dto.getHorasPlanejadas());
        return DemandaExecucaoTarefaRecursoDTO.fromEntity(repository.save(r));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Recurso não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }
}
