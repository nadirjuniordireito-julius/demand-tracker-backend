package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.DemandaTecnicaService;
import com.demandtracker.service.DemandaTecnicaTimelineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandas")
@RequiredArgsConstructor
public class DemandaTecnicaController {

    private final DemandaTecnicaService demandaService;
    private final DemandaTecnicaTimelineService timelineService;
    
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

    /**
     * Timeline do rastro da demanda técnica (criação, abertura, assinaturas, planejamento, encerramento).
     * GET /api/demandas/{id}/timeline
     */
    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<DemandaTecnicaTimelineItemDTO>> getTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(timelineService.getTimelineByDemandaId(id));
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

    /**
     * Cancela uma demanda técnica (define status = "Z" - Cancelada).
     * PUT /api/demandas/{id}/cancelar
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<DemandaTecnicaDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(demandaService.cancelar(id));
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
