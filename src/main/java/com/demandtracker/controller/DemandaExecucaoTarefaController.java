package com.demandtracker.controller;

import com.demandtracker.dto.DemandaExecucaoTarefaCreateDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaDTO;
import com.demandtracker.dto.DemandaExecucaoTarefaUpdateDTO;
import com.demandtracker.service.DemandaExecucaoTarefaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandas-execucao-tarefas")
@RequiredArgsConstructor
public class DemandaExecucaoTarefaController {

    private final DemandaExecucaoTarefaService service;

    @GetMapping("/execucao/{demandaExecucaoId}")
    public ResponseEntity<List<DemandaExecucaoTarefaDTO>> findByDemandaExecucaoId(@PathVariable Long demandaExecucaoId) {
        return ResponseEntity.ok(service.findByDemandaExecucaoId(demandaExecucaoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandaExecucaoTarefaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<DemandaExecucaoTarefaDTO> create(@Valid @RequestBody DemandaExecucaoTarefaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DemandaExecucaoTarefaDTO> update(@PathVariable Long id, @Valid @RequestBody DemandaExecucaoTarefaUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
