package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.entity.enums.TipoRisco;
import com.demandtracker.service.DemandaAvaliacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandas/{demandaId}/avaliacao")
@RequiredArgsConstructor
public class DemandaAvaliacaoController {

    private final DemandaAvaliacaoService avaliacaoService;

    @PostMapping
    public ResponseEntity<DemandaAvaliacaoResponseDTO> create(
            @PathVariable Long demandaId,
            @Valid @RequestBody DemandaAvaliacaoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.create(demandaId, dto));
    }

    @GetMapping
    public ResponseEntity<DemandaAvaliacaoResponseDTO> get(@PathVariable Long demandaId) {
        return ResponseEntity.ok(avaliacaoService.findByDemandaId(demandaId));
    }

    @PutMapping
    public ResponseEntity<DemandaAvaliacaoResponseDTO> update(
            @PathVariable Long demandaId,
            @Valid @RequestBody DemandaAvaliacaoRequestDTO dto) {
        return ResponseEntity.ok(avaliacaoService.update(demandaId, dto));
    }

    @GetMapping("/kpis")
    public ResponseEntity<DemandaAvaliacaoKPIsDTO> getKpis(@PathVariable Long demandaId) {
        return ResponseEntity.ok(avaliacaoService.getKpis(demandaId));
    }

    @GetMapping("/risco")
    public ResponseEntity<List<TipoRisco>> getRiscos(@PathVariable Long demandaId) {
        return ResponseEntity.ok(avaliacaoService.getRiscos(demandaId));
    }

    @GetMapping("/analytics")
    public ResponseEntity<DemandaAvaliacaoAnalyticsDTO> getAnalytics() {
        return ResponseEntity.ok(avaliacaoService.getAnalytics());
    }
}
