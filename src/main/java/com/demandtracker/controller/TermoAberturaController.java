package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.TermoAberturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/termos-abertura")
@RequiredArgsConstructor
public class TermoAberturaController {
    
    private final TermoAberturaService termoService;
    
    @GetMapping
    public ResponseEntity<Page<TermoAberturaDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(termoService.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TermoAberturaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(termoService.findById(id));
    }
    
    @GetMapping("/demanda/{demandaId}")
    public ResponseEntity<TermoAberturaDTO> findByDemandaId(@PathVariable Long demandaId) {
        return ResponseEntity.ok(termoService.findByDemandaId(demandaId));
    }
    
    @PostMapping
    public ResponseEntity<TermoAberturaDTO> create(@Valid @RequestBody TermoAberturaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termoService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TermoAberturaDTO> update(@PathVariable Long id, @Valid @RequestBody TermoAberturaUpdateDTO dto) {
        return ResponseEntity.ok(termoService.update(id, dto));
    }
    
    @PostMapping("/{id}/assinar")
    public ResponseEntity<TermoAberturaDTO> sign(@PathVariable Long id) {
        return ResponseEntity.ok(termoService.sign(id));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        termoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
