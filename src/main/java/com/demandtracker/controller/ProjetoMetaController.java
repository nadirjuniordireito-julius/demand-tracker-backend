package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.ProjetoMetaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projeto-metas")
@RequiredArgsConstructor
public class ProjetoMetaController {
    
    private final ProjetoMetaService projetoMetaService;
    
    @GetMapping
    public ResponseEntity<Page<ProjetoMetaDTO>> findAll(
        @RequestParam(required = false) String codigo,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) Long projetoId,
        @RequestParam(required = false) String status,
        Pageable pageable
    ) {
        return ResponseEntity.ok(projetoMetaService.findAll(codigo, nome, projetoId, status, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProjetoMetaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projetoMetaService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<ProjetoMetaDTO> create(@Valid @RequestBody ProjetoMetaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoMetaService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProjetoMetaDTO> update(@PathVariable Long id, @Valid @RequestBody ProjetoMetaUpdateDTO dto) {
        return ResponseEntity.ok(projetoMetaService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projetoMetaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
