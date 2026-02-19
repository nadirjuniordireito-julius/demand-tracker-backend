package com.demandtracker.controller;

import com.demandtracker.dto.TemplateDemandaCreateDTO;
import com.demandtracker.dto.TemplateDemandaResponseDTO;
import com.demandtracker.dto.TemplateDemandaUpdateDTO;
import com.demandtracker.service.TemplateDemandaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/templates-demanda")
@RequiredArgsConstructor
public class TemplateDemandaController {

    private final TemplateDemandaService service;

    /**
     * Cria um novo template
     * POST /api/templates-demanda
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TemplateDemandaResponseDTO> create(
            @RequestParam("projetoId") Long projetoId,
            @RequestParam("tipo") String tipo,
            @RequestParam("arquivoDocx") MultipartFile arquivoDocx) throws IOException {
        
        TemplateDemandaCreateDTO dto = new TemplateDemandaCreateDTO();
        dto.setProjetoId(projetoId);
        dto.setTipo(tipo);
        dto.setArquivoDocx(arquivoDocx);
        
        TemplateDemandaResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca template por ID
     * GET /api/templates-demanda/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TemplateDemandaResponseDTO> findById(@PathVariable Long id) {
        TemplateDemandaResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca templates pelo ID do Projeto
     * GET /api/templates-demanda/projeto/{projetoId}
     */
    @GetMapping("/projeto/{projetoId}")
    public ResponseEntity<List<TemplateDemandaResponseDTO>> findByProjetoId(
            @PathVariable Long projetoId) {
        List<TemplateDemandaResponseDTO> response = service.findByProjetoId(projetoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca template pelo ID do Projeto e Tipo
     * GET /api/templates-demanda/projeto/{projetoId}/tipo/{tipo}
     */
    @GetMapping("/projeto/{projetoId}/tipo/{tipo}")
    public ResponseEntity<TemplateDemandaResponseDTO> findByProjetoIdAndTipo(
            @PathVariable Long projetoId,
            @PathVariable String tipo) {
        TemplateDemandaResponseDTO response = service.findByProjetoIdAndTipo(projetoId, tipo);
        return ResponseEntity.ok(response);
    }

    /**
     * Download do arquivo DOCX por ID do template
     * GET /api/templates-demanda/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocx(@PathVariable Long id) {
        byte[] arquivo = service.getArquivoDocx(id);
        TemplateDemandaResponseDTO template = service.findById(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDispositionFormData("attachment", template.getNomeArquivo());
        headers.setContentLength(arquivo.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(arquivo);
    }

    /**
     * Download do arquivo DOCX pelo ID do Projeto e Tipo
     * GET /api/templates-demanda/projeto/{projetoId}/tipo/{tipo}/download
     */
    @GetMapping("/projeto/{projetoId}/tipo/{tipo}/download")
    public ResponseEntity<byte[]> downloadDocxByProjetoIdAndTipo(
            @PathVariable Long projetoId,
            @PathVariable String tipo) {
        byte[] arquivo = service.getArquivoDocxByProjetoIdAndTipo(projetoId, tipo);
        TemplateDemandaResponseDTO template = service.findByProjetoIdAndTipo(projetoId, tipo);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
        headers.setContentDispositionFormData("attachment", template.getNomeArquivo());
        headers.setContentLength(arquivo.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(arquivo);
    }

    /**
     * Lista todos os templates
     * GET /api/templates-demanda
     */
    @GetMapping
    public ResponseEntity<List<TemplateDemandaResponseDTO>> findAll() {
        List<TemplateDemandaResponseDTO> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    /**
     * Atualiza um template
     * PUT /api/templates-demanda/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TemplateDemandaResponseDTO> update(
            @PathVariable Long id,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "arquivoDocx", required = false) MultipartFile arquivoDocx) throws IOException {
        
        TemplateDemandaUpdateDTO dto = new TemplateDemandaUpdateDTO();
        dto.setTipo(tipo);
        dto.setArquivoDocx(arquivoDocx);
        
        TemplateDemandaResponseDTO response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta um template
     * DELETE /api/templates-demanda/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica se existe template para um Projeto e Tipo
     * GET /api/templates-demanda/projeto/{projetoId}/tipo/{tipo}/exists
     */
    @GetMapping("/projeto/{projetoId}/tipo/{tipo}/exists")
    public ResponseEntity<Boolean> existsByProjetoIdAndTipo(
            @PathVariable Long projetoId,
            @PathVariable String tipo) {
        boolean exists = service.existsByProjetoIdAndTipo(projetoId, tipo);
        return ResponseEntity.ok(exists);
    }
}
