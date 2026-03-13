package com.demandtracker.controller;

import com.demandtracker.dto.TermoEncerramentoDocCreateDTO;
import com.demandtracker.dto.TermoEncerramentoDocResponseDTO;
import com.demandtracker.dto.TermoEncerramentoDocUpdateDTO;
import com.demandtracker.service.TermoEncerramentoDocService;
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
@RequestMapping("/api/termos-encerramento-doc")
@RequiredArgsConstructor
public class TermoEncerramentoDocController {

    private final TermoEncerramentoDocService service;

    /**
     * Cria um novo documento
     * POST /api/termos-encerramento-doc
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TermoEncerramentoDocResponseDTO> create(
            @RequestParam("termoEncerramentoId") Long termoEncerramentoId,
            @RequestParam(value = "dataAssinatura", required = false) String dataAssinatura,
            @RequestParam("arquivoPdf") MultipartFile arquivoPdf) throws IOException {
        
        TermoEncerramentoDocCreateDTO dto = new TermoEncerramentoDocCreateDTO();
        dto.setTermoEncerramentoId(termoEncerramentoId);
        dto.setArquivoPdf(arquivoPdf);
        
        TermoEncerramentoDocResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca documento por ID
     * GET /api/termos-encerramento-doc/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TermoEncerramentoDocResponseDTO> findById(@PathVariable Long id) {
        TermoEncerramentoDocResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca documento pelo ID do Termo de Encerramento
     * GET /api/termos-encerramento-doc/termo/{termoEncerramentoId}
     */
    @GetMapping("/termo/{termoEncerramentoId}")
    public ResponseEntity<TermoEncerramentoDocResponseDTO> findByTermoEncerramentoId(
            @PathVariable Long termoEncerramentoId) {
        TermoEncerramentoDocResponseDTO response = service.findByTermoEncerramentoId(termoEncerramentoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Download do arquivo PDF por ID do documento
     * GET /api/termos-encerramento-doc/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] arquivo = service.getArquivoPdf(id);
        TermoEncerramentoDocResponseDTO doc = service.findById(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", doc.getNomeArquivo());
        headers.setContentLength(arquivo.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(arquivo);
    }

    /**
     * Download do arquivo PDF pelo ID do Termo de Encerramento
     * GET /api/termos-encerramento-doc/termo/{termoEncerramentoId}/download
     */
    @GetMapping("/termo/{termoEncerramentoId}/download")
    public ResponseEntity<byte[]> downloadPdfByTermoEncerramentoId(
            @PathVariable Long termoEncerramentoId) {
        byte[] arquivo = service.getArquivoPdfByTermoEncerramentoId(termoEncerramentoId);
        TermoEncerramentoDocResponseDTO doc = service.findByTermoEncerramentoId(termoEncerramentoId);
        
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
     * GET /api/termos-encerramento-doc
     */
    @GetMapping
    public ResponseEntity<List<TermoEncerramentoDocResponseDTO>> findAll() {
        List<TermoEncerramentoDocResponseDTO> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza um documento
     * PUT /api/termos-encerramento-doc/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TermoEncerramentoDocResponseDTO> update(
            @PathVariable Long id,
            @RequestParam(value = "dataAssinatura", required = false) String dataAssinatura,
            @RequestParam(value = "arquivoPdf", required = false) MultipartFile arquivoPdf) throws IOException {
        
        TermoEncerramentoDocUpdateDTO dto = new TermoEncerramentoDocUpdateDTO();
        dto.setArquivoPdf(arquivoPdf);
        
        TermoEncerramentoDocResponseDTO response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta um documento
     * DELETE /api/termos-encerramento-doc/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica se existe documento para um Termo de Encerramento
     * GET /api/termos-encerramento-doc/termo/{termoEncerramentoId}/exists
     */
    @GetMapping("/termo/{termoEncerramentoId}/exists")
    public ResponseEntity<Boolean> existsByTermoEncerramentoId(@PathVariable Long termoEncerramentoId) {
        boolean exists = service.existsByTermoEncerramentoId(termoEncerramentoId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Assina o documento do termo de encerramento.
     * Path usa o ID do documento (TermoEncerramentoDoc).
     * Parâmetros: hashPdf, usuarioId, pageNumber (ou page), x, y, width, height.
     */
    @PutMapping("/{id}/assinar")
    public ResponseEntity<Void> assinar(
        @PathVariable Long id,
        @RequestParam String hashPdf,
        @RequestParam Long usuarioId,
        @RequestParam(value = "pageNumber", required = false) Long pageNumber,
        @RequestParam(value = "page", required = false) Long page,
        @RequestParam(value = "x", required = false) Float x,
        @RequestParam(value = "y", required = false) Float y,
        @RequestParam(value = "width", required = false) Long width,
        @RequestParam(value = "height", required = false) Long height,
        HttpServletRequest request) {
        Long pageParam = pageNumber != null ? pageNumber : page;
        service.assinar(id, hashPdf, usuarioId, pageParam, x, y, width, height, request);
        return ResponseEntity.noContent().build();
    }

}
