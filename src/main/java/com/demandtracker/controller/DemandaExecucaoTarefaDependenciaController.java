package com.demandtracker.controller;

import com.demandtracker.dto.DemandaExecucaoTarefaDependenciaCreateDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaDependenciaDTO;
import com.demandtracker.service.DemandaExecucaoTarefaDependenciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandas-execucao-tarefas-dependencias")
@RequiredArgsConstructor
public class DemandaExecucaoTarefaDependenciaController {

    private final DemandaExecucaoTarefaDependenciaService service;

    @GetMapping("/tarefa-origem/{tarefaOrigemId}")
    public ResponseEntity<List<DemandaExecucaoTarefaDependenciaDTO>> findByTarefaOrigemId(@PathVariable Long tarefaOrigemId) {
        return ResponseEntity.ok(service.findByTarefaOrigemId(tarefaOrigemId));
    }

    @GetMapping("/tarefa-destino/{tarefaDestinoId}")
    public ResponseEntity<List<DemandaExecucaoTarefaDependenciaDTO>> findByTarefaDestinoId(@PathVariable Long tarefaDestinoId) {
        return ResponseEntity.ok(service.findByTarefaDestinoId(tarefaDestinoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandaExecucaoTarefaDependenciaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<DemandaExecucaoTarefaDependenciaDTO> create(@Valid @RequestBody DemandaExecucaoTarefaDependenciaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
