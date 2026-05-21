package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {
    
    private final ProfissionalService profissionalService;

    @GetMapping
    public ResponseEntity<Page<ProfissionalDTO>> findAll(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long projetoId,
            Pageable pageable) {
        return ResponseEntity.ok(profissionalService.findAll(nome, projetoId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(profissionalService.findById(id));
    }

    /**
     * Análise mensal resumida: horas executadas rateadas por dias úteis, valor por perfil e custo mensal.
     * GET /api/profissionais/{id}/analise-resumida?demandaExecucaoId={opcional}
     */
    @GetMapping("/{id}/analise-resumida")
    public ResponseEntity<List<ProfissionalAnaliseResumidaDTO>> getAnaliseResumida(
            @PathVariable Long id,
            @RequestParam(required = false) Long demandaExecucaoId) {
        return ResponseEntity.ok(profissionalService.getAnaliseResumida(id, demandaExecucaoId));
    }

    @PostMapping
    public ResponseEntity<ProfissionalDTO> create(@Valid @RequestBody ProfissionalCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profissionalService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> update(@PathVariable Long id, @Valid @RequestBody ProfissionalUpdateDTO dto) {
        return ResponseEntity.ok(profissionalService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        profissionalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
