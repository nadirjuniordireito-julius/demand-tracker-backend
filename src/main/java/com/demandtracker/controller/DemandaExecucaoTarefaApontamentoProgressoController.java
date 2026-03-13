package com.demandtracker.controller;

import com.demandtracker.dto.DemandaExecucaoTarefaApontamentoProgressoCreateDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaApontamentoProgressoDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaApontamentoProgressoUpdateDTO;
import com.demandtracker.service.DemandaExecucaoTarefaApontamentoProgressoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandas-execucao-tarefas-apontamentos")
@RequiredArgsConstructor
public class DemandaExecucaoTarefaApontamentoProgressoController {

    private final DemandaExecucaoTarefaApontamentoProgressoService service;

    @GetMapping("/tarefa/{tarefaId}")
    public ResponseEntity<List<DemandaExecucaoTarefaApontamentoProgressoDTO>> findByDemandaExecucaoTarefaId(@PathVariable Long tarefaId) {
        return ResponseEntity.ok(service.findByDemandaExecucaoTarefaId(tarefaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandaExecucaoTarefaApontamentoProgressoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<DemandaExecucaoTarefaApontamentoProgressoDTO> create(@Valid @RequestBody DemandaExecucaoTarefaApontamentoProgressoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandaExecucaoTarefaApontamentoProgressoDTO> update(@PathVariable Long id, @Valid @RequestBody DemandaExecucaoTarefaApontamentoProgressoUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
