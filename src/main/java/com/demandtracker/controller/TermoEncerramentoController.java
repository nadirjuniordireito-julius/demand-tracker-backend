package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.TermoEncerramentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/termos-encerramento")
@RequiredArgsConstructor
public class TermoEncerramentoController {
    
    private final TermoEncerramentoService termoService;
    
    @GetMapping
    public ResponseEntity<Page<TermoEncerramentoDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(termoService.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TermoEncerramentoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(termoService.findById(id));
    }
    
    @GetMapping("/demanda/{demandaId}")
    public ResponseEntity<TermoEncerramentoDTO> findByDemandaId(@PathVariable Long demandaId) {
        return ResponseEntity.ok(termoService.findByDemandaId(demandaId));
    }
    
    @PostMapping
    public ResponseEntity<TermoEncerramentoDTO> create(@Valid @RequestBody TermoEncerramentoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termoService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TermoEncerramentoDTO> update(@PathVariable Long id, @Valid @RequestBody TermoEncerramentoUpdateDTO dto) {
        return ResponseEntity.ok(termoService.update(id, dto));
    }
    
    @PostMapping("/{id}/assinar")
    public ResponseEntity<TermoEncerramentoDTO> sign(@PathVariable Long id) {
        return ResponseEntity.ok(termoService.sign(id));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        termoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
