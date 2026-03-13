package com.demandtracker.controller;

import com.demandtracker.dto.DesembolsoCreateDTO;
import com.demandtracker.dto.DesembolsoDTO;
import com.demandtracker.dto.DesembolsoUpdateDTO;
import com.demandtracker.service.DesembolsoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/desembolsos")
@RequiredArgsConstructor
public class DesembolsoController {

    private final DesembolsoService desembolsoService;

    /**
     * Lista desembolsos com paginação e filtro opcional por documento.
     */
    @GetMapping
    public ResponseEntity<Page<DesembolsoDTO>> findAll(
            @RequestParam Long projetoId,
            @RequestParam(required = false) String documento,
            Pageable pageable) {
        return ResponseEntity.ok(desembolsoService.findAll(projetoId, documento, pageable));
    }

    /**
     * Busca desembolso por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DesembolsoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(desembolsoService.findById(id));
    }

    /**
     * Cria um novo desembolso.
     */
    @PostMapping
    public ResponseEntity<DesembolsoDTO> create(@Valid @RequestBody DesembolsoCreateDTO dto) {
        DesembolsoDTO created = desembolsoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Atualiza um desembolso existente.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DesembolsoDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DesembolsoUpdateDTO dto) {
        return ResponseEntity.ok(desembolsoService.update(id, dto));
    }

    /**
     * Remove um desembolso.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        desembolsoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

