package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfis")
@RequiredArgsConstructor
public class PerfilController {
    
    private final PerfilService perfilService;
    
    @GetMapping
    public ResponseEntity<Page<PerfilDTO>> findAll(
        @RequestParam(required = false) String nome,
        Pageable pageable
    ) {
        return ResponseEntity.ok(perfilService.findAll(nome, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PerfilDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<PerfilDTO> create(@Valid @RequestBody PerfilCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(perfilService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<PerfilDTO> update(@PathVariable Long id, @Valid @RequestBody PerfilUpdateDTO dto) {
        return ResponseEntity.ok(perfilService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        perfilService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
