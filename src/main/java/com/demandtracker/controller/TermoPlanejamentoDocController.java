package com.demandtracker.controller;

import com.demandtracker.dto.TermoPlanejamentoDocCreateDTO;
import com.demandtracker.dto.TermoPlanejamentoDocResponseDTO;
import com.demandtracker.dto.TermoPlanejamentoDocUpdateDTO;
import com.demandtracker.service.TermoPlanejamentoDocService;
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
@RequestMapping("/api/termos-planejamento-doc")
@RequiredArgsConstructor
public class TermoPlanejamentoDocController {

    private final TermoPlanejamentoDocService service;

    /**
     * Cria um novo documento
     * POST /api/termos-planejamento-doc
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TermoPlanejamentoDocResponseDTO> create(
            @RequestParam("termoPlanejamentoId") Long termoPlanejamentoId,
            @RequestParam(value = "dataAssinatura", required = false) String dataAssinatura,
            @RequestParam("arquivoPdf") MultipartFile arquivoPdf) throws IOException {
        
        TermoPlanejamentoDocCreateDTO dto = new TermoPlanejamentoDocCreateDTO();
        dto.setTermoPlanejamentoId(termoPlanejamentoId);
        dto.setArquivoPdf(arquivoPdf);
        
        TermoPlanejamentoDocResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca documento por ID
     * GET /api/termos-planejamento-doc/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TermoPlanejamentoDocResponseDTO> findById(@PathVariable Long id) {
        TermoPlanejamentoDocResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca documento pelo ID do Termo de Planejamento
     * GET /api/termos-planejamento-doc/termo/{termoPlanejamentoId}
     */
    @GetMapping("/termo/{termoPlanejamentoId}")
    public ResponseEntity<TermoPlanejamentoDocResponseDTO> findByTermoPlanejamentoId(
            @PathVariable Long termoPlanejamentoId) {
        TermoPlanejamentoDocResponseDTO response = service.findByTermoPlanejamentoId(termoPlanejamentoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Download do arquivo PDF por ID do documento
     * GET /api/termos-planejamento-doc/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] arquivo = service.getArquivoPdf(id);
        TermoPlanejamentoDocResponseDTO doc = service.findById(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", doc.getNomeArquivo());
        headers.setContentLength(arquivo.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(arquivo);
    }

    /**
     * Download do arquivo PDF pelo ID do Termo de Planejamento
     * GET /api/termos-planejamento-doc/termo/{termoPlanejamentoId}/download
     */
    @GetMapping("/termo/{termoPlanejamentoId}/download")
    public ResponseEntity<byte[]> downloadPdfByTermoPlanejamentoId(
            @PathVariable Long termoPlanejamentoId) {
        byte[] arquivo = service.getArquivoPdfByTermoPlanejamentoId(termoPlanejamentoId);
        TermoPlanejamentoDocResponseDTO doc = service.findByTermoPlanejamentoId(termoPlanejamentoId);
        
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
     * GET /api/termos-planejamento-doc
     */
    @GetMapping
    public ResponseEntity<List<TermoPlanejamentoDocResponseDTO>> findAll() {
        List<TermoPlanejamentoDocResponseDTO> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza um documento
     * PUT /api/termos-planejamento-doc/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TermoPlanejamentoDocResponseDTO> update(
            @PathVariable Long id,
            @RequestParam(value = "dataAssinatura", required = false) String dataAssinatura,
            @RequestParam(value = "arquivoPdf", required = false) MultipartFile arquivoPdf) throws IOException {
        
        TermoPlanejamentoDocUpdateDTO dto = new TermoPlanejamentoDocUpdateDTO();
        dto.setArquivoPdf(arquivoPdf);
        
        TermoPlanejamentoDocResponseDTO response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta um documento
     * DELETE /api/termos-planejamento-doc/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica se existe documento para um Termo de Planejamento
     * GET /api/termos-planejamento-doc/termo/{termoPlanejamentoId}/exists
     */
    @GetMapping("/termo/{termoPlanejamentoId}/exists")
    public ResponseEntity<Boolean> existsByTermoPlanejamentoId(@PathVariable Long termoPlanejamentoId) {
        boolean exists = service.existsByTermoPlanejamentoId(termoPlanejamentoId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Assina o documento do termo de planejamento.
     * Path usa o ID do documento (TermoPlanejamentoDoc).
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
