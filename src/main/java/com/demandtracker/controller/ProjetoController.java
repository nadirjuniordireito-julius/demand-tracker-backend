package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.ProjetoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
public class ProjetoController {
    
    private final ProjetoService projetoService;
    
    @GetMapping
    public ResponseEntity<Page<ProjetoDTO>> findAll(
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String codTed,
        @RequestParam(required = false) Long usuarioId,
        Pageable pageable
    ) {
        return ResponseEntity.ok(projetoService.findAll(nome, codTed, usuarioId, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProjetoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projetoService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<ProjetoDTO> create(@Valid @RequestBody ProjetoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProjetoDTO> update(@PathVariable Long id, @Valid @RequestBody ProjetoUpdateDTO dto) {
        return ResponseEntity.ok(projetoService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projetoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
