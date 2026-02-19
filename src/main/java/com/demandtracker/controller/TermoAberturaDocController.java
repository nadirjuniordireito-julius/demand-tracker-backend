package com.demandtracker.controller;

import com.demandtracker.dto.TermoAberturaDocCreateDTO;
import com.demandtracker.dto.TermoAberturaDocResponseDTO;
import com.demandtracker.dto.TermoAberturaDocUpdateDTO;
import com.demandtracker.service.TermoAberturaDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/termos-abertura-doc")
@RequiredArgsConstructor
public class TermoAberturaDocController {

    private final TermoAberturaDocService service;

    /**
     * Cria um novo documento
     * POST /api/termos-abertura-doc
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TermoAberturaDocResponseDTO> create(
            @RequestParam("termoAberturaId") Long termoAberturaId,
            @RequestParam(value = "dataAssinatura", required = false) String dataAssinatura,
            @RequestParam("arquivoPdf") MultipartFile arquivoPdf) throws IOException {
        
        TermoAberturaDocCreateDTO dto = new TermoAberturaDocCreateDTO();
        dto.setTermoAberturaId(termoAberturaId);
        dto.setArquivoPdf(arquivoPdf);
        // Se dataAssinatura for fornecida, pode ser parseada aqui
        
        TermoAberturaDocResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca documento por ID
     * GET /api/termos-abertura-doc/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TermoAberturaDocResponseDTO> findById(@PathVariable Long id) {
        TermoAberturaDocResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca documento pelo ID do Termo de Abertura
     * GET /api/termos-abertura-doc/termo/{termoAberturaId}
     */
    @GetMapping("/termo/{termoAberturaId}")
    public ResponseEntity<TermoAberturaDocResponseDTO> findByTermoAberturaId(
            @PathVariable Long termoAberturaId) {
        TermoAberturaDocResponseDTO response = service.findByTermoAberturaId(termoAberturaId);
        return ResponseEntity.ok(response);
    }

    /**
     * Download do arquivo PDF por ID do documento
     * GET /api/termos-abertura-doc/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] arquivo = service.getArquivoPdf(id);
        TermoAberturaDocResponseDTO doc = service.findById(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", doc.getNomeArquivo());
        headers.setContentLength(arquivo.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(arquivo);
    }

    /**
     * Download do arquivo PDF pelo ID do Termo de Abertura
     * GET /api/termos-abertura-doc/termo/{termoAberturaId}/download
     */
    @GetMapping("/termo/{termoAberturaId}/download")
    public ResponseEntity<byte[]> downloadPdfByTermoAberturaId(
            @PathVariable Long termoAberturaId) {
        byte[] arquivo = service.getArquivoPdfByTermoAberturaId(termoAberturaId);
        TermoAberturaDocResponseDTO doc = service.findByTermoAberturaId(termoAberturaId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", doc.getNomeArquivo());
        headers.setContentLength(arquivo.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(arquivo);
    }

    /**
     * Lista todos os documentos
     * GET /api/termos-abertura-doc
     */
    @GetMapping
    public ResponseEntity<List<TermoAberturaDocResponseDTO>> findAll() {
        List<TermoAberturaDocResponseDTO> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza um documento
     * PUT /api/termos-abertura-doc/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TermoAberturaDocResponseDTO> update(
            @PathVariable Long id,
            @RequestParam(value = "dataAssinatura", required = false) String dataAssinatura,
            @RequestParam(value = "arquivoPdf", required = false) MultipartFile arquivoPdf) throws IOException {
        
        TermoAberturaDocUpdateDTO dto = new TermoAberturaDocUpdateDTO();
        dto.setArquivoPdf(arquivoPdf);
        // Se dataAssinatura for fornecida, pode ser parseada aqui
        
        TermoAberturaDocResponseDTO response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta um documento
     * DELETE /api/termos-abertura-doc/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica se existe documento para um Termo de Abertura
     * GET /api/termos-abertura-doc/termo/{termoAberturaId}/exists
     */
    @GetMapping("/termo/{termoAberturaId}/exists")
    public ResponseEntity<Boolean> existsByTermoAberturaId(@PathVariable Long termoAberturaId) {
        boolean exists = service.existsByTermoAberturaId(termoAberturaId);
        return ResponseEntity.ok(exists);
    }

    @PutMapping("/termo/{documentoId}/assinar")
    public ResponseEntity<Void> assinar(
        @PathVariable Long documentoId, 
        @RequestParam String hashPdf, 
        @RequestParam Long usuarioId,
        HttpServletRequest request) {
        service.assinar(documentoId, hashPdf, usuarioId, request);
        return ResponseEntity.noContent().build();
    }

}
