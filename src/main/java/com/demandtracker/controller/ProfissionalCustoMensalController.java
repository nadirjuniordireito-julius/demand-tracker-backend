package com.demandtracker.controller;

import com.demandtracker.dto.ProfissionalCustoMensalCreateDTO;
import com.demandtracker.dto.ProfissionalCustoMensalDTO;
import com.demandtracker.dto.ProfissionalCustoMensalUpdateDTO;
import com.demandtracker.service.ProfissionalCustoMensalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profissionais-custos-mensais")
@RequiredArgsConstructor
public class ProfissionalCustoMensalController {

    private final ProfissionalCustoMensalService service;

    @GetMapping
    public ResponseEntity<Page<ProfissionalCustoMensalDTO>> findAll(
            @RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes,
            Pageable pageable) {
        return ResponseEntity.ok(service.findAll(profissionalId, ano, mes, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalCustoMensalDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProfissionalCustoMensalDTO> create(@Valid @RequestBody ProfissionalCustoMensalCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalCustoMensalDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProfissionalCustoMensalUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
