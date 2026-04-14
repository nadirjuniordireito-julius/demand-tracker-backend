package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.MetaProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meta-produtos")
@RequiredArgsConstructor
public class MetaProdutoController {
    
    private final MetaProdutoService metaProdutoService;
    
    @GetMapping
    public ResponseEntity<Page<MetaProdutoDTO>> findAll(
        @RequestParam(required = false) String codigo,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) Long projetoMetaId,
        @RequestParam(required = false) String status,
        Pageable pageable
    ) {
        return ResponseEntity.ok(metaProdutoService.findAll(codigo, nome, projetoMetaId, status, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<MetaProdutoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(metaProdutoService.findById(id));
    }

    @GetMapping("/{id}/evolucao-trimestral")
    public ResponseEntity<MetaProdutoEvolucaoTrimestralDTO> getEvolucaoTrimestral(@PathVariable Long id) {
        return ResponseEntity.ok(metaProdutoService.getEvolucaoTrimestral(id));
    }
    
    @PostMapping
    public ResponseEntity<MetaProdutoDTO> create(@Valid @RequestBody MetaProdutoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaProdutoService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<MetaProdutoDTO> update(@PathVariable Long id, @Valid @RequestBody MetaProdutoUpdateDTO dto) {
        return ResponseEntity.ok(metaProdutoService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        metaProdutoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
