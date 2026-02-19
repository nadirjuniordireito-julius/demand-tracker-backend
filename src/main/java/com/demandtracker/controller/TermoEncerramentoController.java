package com.demandtracker.controller;

import com.demandtracker.dto.*;
import com.demandtracker.service.DocumentoService;
import com.demandtracker.service.TermoEncerramentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/termos-encerramento")
@RequiredArgsConstructor
public class TermoEncerramentoController {
    
    private final TermoEncerramentoService termoService;
    private final DocumentoService documentoService;
    
    @GetMapping
    public ResponseEntity<Page<TermoEncerramentoDTO>> findAll(Pageable pageable) {
        return ResponseEntity.ok(termoService.findAll(pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TermoEncerramentoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(termoService.findById(id));
    }
    
    @GetMapping("/demanda/{demandaId}")
    public ResponseEntity<TermoEncerramentoDTO> findByDemandaId(@PathVariable Long demandaId) {
        return ResponseEntity.ok(termoService.findByDemandaId(demandaId));
    }
    
    @PostMapping
    public ResponseEntity<TermoEncerramentoDTO> create(@Valid @RequestBody TermoEncerramentoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(termoService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TermoEncerramentoDTO> update(@PathVariable Long id, @Valid @RequestBody TermoEncerramentoUpdateDTO dto) {
        return ResponseEntity.ok(termoService.update(id, dto));
    }
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        termoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Gera PDF do Termo de Encerramento a partir do template DOCX
     * GET /api/termos-encerramento/{id}/gerar-pdf?projetoId={projetoId}&tipo={tipo}
     */
    @GetMapping("/{id}/gerar-pdf")
    public ResponseEntity<byte[]> gerarPdf(
            @PathVariable Long id,
            @RequestParam Long projetoId,
            @RequestParam String tipo) {
        
        try {
            byte[] pdf = documentoService.gerarPdfTermoEncerramento(projetoId, tipo, id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "termo-encerramento-" + id + ".pdf");
            headers.setContentLength(pdf.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdf);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }
}
