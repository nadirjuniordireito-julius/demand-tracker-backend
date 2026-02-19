package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.DemandaTecnicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demandas")
@RequiredArgsConstructor
public class DemandaTecnicaController {
    
    private final DemandaTecnicaService demandaService;
    
    @GetMapping
    public ResponseEntity<Page<DemandaTecnicaDTO>> findAll(
        @RequestParam(required = false) String codigo,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) Long projetoId,
        @RequestParam(required = false) String status,
        Pageable pageable
    ) {
        return ResponseEntity.ok(demandaService.findAll(codigo, nome, projetoId, status, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DemandaTecnicaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(demandaService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<DemandaTecnicaDTO> create(@Valid @RequestBody DemandaTecnicaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(demandaService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DemandaTecnicaDTO> update(@PathVariable Long id, @Valid @RequestBody DemandaTecnicaUpdateDTO dto) {
        return ResponseEntity.ok(demandaService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/demandas/projeto/{projetoId}/em-fluxo/paginado?page=0&size=10&sort=dataAbertura,desc
    @GetMapping("/projeto/{projetoId}/em-fluxo/paginado")
    public ResponseEntity<Page<DemandaTecnicaDTO>> findEmFluxoPorProjetoPaginado(
            @PathVariable Long projetoId,
            Pageable pageable) {

        return ResponseEntity.ok(
            demandaService.findDemandasEmFluxoPaginado(projetoId, pageable)
        );
    }
}
