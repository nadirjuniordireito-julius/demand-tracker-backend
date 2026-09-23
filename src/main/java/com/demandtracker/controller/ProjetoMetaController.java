package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.ProjetoMetaService;
import com.demandtracker.service.TermoDownloadZipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projeto-metas")
@RequiredArgsConstructor
public class ProjetoMetaController {
    
    private final ProjetoMetaService projetoMetaService;
    private final TermoDownloadZipService termoDownloadZipService;
    
    @GetMapping
    public ResponseEntity<Page<ProjetoMetaDTO>> findAll(
        @RequestParam(required = false) String codigo,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) Long projetoId,
        @RequestParam(required = false) String status,
        Pageable pageable
    ) {
        return ResponseEntity.ok(projetoMetaService.findAll(codigo, nome, projetoId, status, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProjetoMetaDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(projetoMetaService.findById(id));
    }

    /**
     * Download em ZIP dos PDFs dos termos (abertura, planejamento e/ou encerramento)
     * das demandas técnicas vinculadas à meta (ProjetoMeta).
     * GET /api/projeto-metas/{id}/termos/download?abertura=&planejamento=&encerramento=
     */
    @GetMapping("/{id}/termos/download")
    public ResponseEntity<byte[]> downloadTermosZip(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean abertura,
            @RequestParam(defaultValue = "false") boolean planejamento,
            @RequestParam(defaultValue = "false") boolean encerramento) {

        TermoDownloadZipService.ZipDownloadResult result =
                termoDownloadZipService.downloadZip(id, abertura, planejamento, encerramento);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDispositionFormData("attachment", result.fileName());
        headers.setContentLength(result.content().length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.content());
    }
    
    @PostMapping
    public ResponseEntity<ProjetoMetaDTO> create(@Valid @RequestBody ProjetoMetaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projetoMetaService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProjetoMetaDTO> update(@PathVariable Long id, @Valid @RequestBody ProjetoMetaUpdateDTO dto) {
        return ResponseEntity.ok(projetoMetaService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projetoMetaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
