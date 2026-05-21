package com.demandtracker.controller;

import com.demandtracker.dto.DiaNaoUtilCreateDTO;
import com.demandtracker.dto.DiaNaoUtilDTO;
import com.demandtracker.dto.DiaNaoUtilUpdateDTO;
import com.demandtracker.service.DiaNaoUtilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dias-nao-uteis")
@RequiredArgsConstructor
public class DiaNaoUtilController {

    private final DiaNaoUtilService service;

    @GetMapping
    public ResponseEntity<Page<DiaNaoUtilDTO>> findAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            Pageable pageable) {
        return ResponseEntity.ok(service.findAll(dataInicio, dataFim, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiaNaoUtilDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<DiaNaoUtilDTO> create(@Valid @RequestBody DiaNaoUtilCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaNaoUtilDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DiaNaoUtilUpdateDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
