package com.demandtracker.controller;

import com.demandtracker.dto.TermoEncerramentoCustoProfissionalDTO;
import com.demandtracker.service.TermoEncerramentoCustoProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/termos-encerramento-custos-profissionais")
@RequiredArgsConstructor
public class TermoEncerramentoCustoProfissionalController {

    private final TermoEncerramentoCustoProfissionalService service;

    @GetMapping
    public ResponseEntity<Page<TermoEncerramentoCustoProfissionalDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    /**
     * Lista todos os profissionais vinculados a um custo do termo de encerramento.
     * GET /api/termos-encerramento-custos-profissionais/termo-encerramento-custo/{termoEncerramentoCustoId}
     */
    @GetMapping("/termo-encerramento-custo/{termoEncerramentoCustoId}")
    public ResponseEntity<List<TermoEncerramentoCustoProfissionalDTO>> findByTermoEncerramentoCustoId(
            @PathVariable Long termoEncerramentoCustoId) {
        return ResponseEntity.ok(service.findByTermoEncerramentoCustoId(termoEncerramentoCustoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TermoEncerramentoCustoProfissionalDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Soma total (qtdeHora * valorHora) dos profissionais de um custo do termo de encerramento.
     * GET /api/termos-encerramento-custos-profissionais/termo-encerramento-custo/{termoEncerramentoCustoId}/total
     */
    @GetMapping("/termo-encerramento-custo/{termoEncerramentoCustoId}/total")
    public ResponseEntity<BigDecimal> getTotalByTermoEncerramentoCustoId(
            @PathVariable Long termoEncerramentoCustoId) {
        return ResponseEntity.ok(service.calcularTotalPorTermoEncerramentoCusto(termoEncerramentoCustoId));
    }

    @PostMapping
    public ResponseEntity<TermoEncerramentoCustoProfissionalDTO> create(
            @Valid @RequestBody TermoEncerramentoCustoProfissionalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TermoEncerramentoCustoProfissionalDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TermoEncerramentoCustoProfissionalDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
