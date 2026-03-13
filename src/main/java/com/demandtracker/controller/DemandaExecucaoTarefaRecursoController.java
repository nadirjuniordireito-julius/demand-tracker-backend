package com.demandtracker.controller;

import com.demandtracker.dto.DemandaExecucaoTarefaRecursoCreateDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaRecursoDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaRecursoUpdateDTO;
import com.demandtracker.service.DemandaExecucaoTarefaRecursoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandas-execucao-tarefas-recursos")
@RequiredArgsConstructor
public class DemandaExecucaoTarefaRecursoController {

    private final DemandaExecucaoTarefaRecursoService service;

    @GetMapping("/tarefa/{tarefaId}")
    public ResponseEntity<List<DemandaExecucaoTarefaRecursoDTO>> findByDemandaExecucaoTarefaId(@PathVariable Long tarefaId) {
        return ResponseEntity.ok(service.findByDemandaExecucaoTarefaId(tarefaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandaExecucaoTarefaRecursoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<DemandaExecucaoTarefaRecursoDTO> create(@Valid @RequestBody DemandaExecucaoTarefaRecursoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandaExecucaoTarefaRecursoDTO> update(@PathVariable Long id, @Valid @RequestBody DemandaExecucaoTarefaRecursoUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
