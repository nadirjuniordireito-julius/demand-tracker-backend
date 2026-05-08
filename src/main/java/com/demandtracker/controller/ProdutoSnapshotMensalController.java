package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.ProdutoSnapshotMensalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos-snapshots-mensais")
@RequiredArgsConstructor
public class ProdutoSnapshotMensalController {

    private final ProdutoSnapshotMensalService service;

    // ------------- Relatório gerencial (precede mapeamentos com {id}) -------------

    @GetMapping("/relatorio-gestor")
    public ResponseEntity<ProdutoSnapshotRelatorioGestorDTO> getRelatorioGestor(
            @RequestParam Integer ano,
            @RequestParam Integer mes,
            @RequestParam(required = false) Long projetoId
    ) {
        return ResponseEntity.ok(service.getRelatorioGestor(ano, mes, projetoId));
    }

    // ------------- CRUD do snapshot -------------

    @GetMapping
    public ResponseEntity<Page<ProdutoSnapshotMensalDTO>> findAll(
            @RequestParam(required = false) Long metaProdutoId,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes,
            Pageable pageable
    ) {
        return ResponseEntity.ok(service.findAll(metaProdutoId, ano, mes, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoSnapshotMensalDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ProdutoSnapshotMensalDTO> create(
            @Valid @RequestBody ProdutoSnapshotMensalCreateDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoSnapshotMensalDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoSnapshotMensalUpdateDTO dto
    ) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ------------- Transições de fechamento -------------

    @PostMapping("/{id}/fechar")
    public ResponseEntity<ProdutoSnapshotMensalDTO> fechar(@PathVariable Long id) {
        return ResponseEntity.ok(service.fechar(id));
    }

    @PostMapping("/{id}/reabrir")
    public ResponseEntity<ProdutoSnapshotMensalDTO> reabrir(@PathVariable Long id) {
        return ResponseEntity.ok(service.reabrir(id));
    }

    // ------------- Ações vinculadas -------------

    @GetMapping("/{snapshotId}/acoes")
    public ResponseEntity<List<ProdutoSnapshotAcaoDTO>> findAcoes(@PathVariable Long snapshotId) {
        return ResponseEntity.ok(service.findAcoesBySnapshotId(snapshotId));
    }

    @PostMapping("/{snapshotId}/acoes")
    public ResponseEntity<ProdutoSnapshotAcaoDTO> createAcao(
            @PathVariable Long snapshotId,
            @Valid @RequestBody ProdutoSnapshotAcaoCreateDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAcao(snapshotId, dto));
    }

    @PutMapping("/{snapshotId}/acoes/{acaoId}")
    public ResponseEntity<ProdutoSnapshotAcaoDTO> updateAcao(
            @PathVariable Long snapshotId,
            @PathVariable Long acaoId,
            @Valid @RequestBody ProdutoSnapshotAcaoUpdateDTO dto
    ) {
        return ResponseEntity.ok(service.updateAcao(snapshotId, acaoId, dto));
    }

    @PutMapping("/{snapshotId}/acoes/{acaoId}/status")
    public ResponseEntity<ProdutoSnapshotAcaoDTO> updateAcaoStatus(
            @PathVariable Long snapshotId,
            @PathVariable Long acaoId,
            @Valid @RequestBody ProdutoSnapshotAcaoUpdateStatusDTO dto
    ) {
        return ResponseEntity.ok(service.updateAcaoStatus(snapshotId, acaoId, dto));
    }

    @DeleteMapping("/{snapshotId}/acoes/{acaoId}")
    public ResponseEntity<Void> deleteAcao(
            @PathVariable Long snapshotId,
            @PathVariable Long acaoId
    ) {
        service.deleteAcao(snapshotId, acaoId);
        return ResponseEntity.noContent().build();
    }
}
