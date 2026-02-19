package com.demandtracker.controller;

import com.demandtracker.dto.ProjetoDocCreateDTO;
import com.demandtracker.dto.ProjetoDocResponseDTO;
import com.demandtracker.dto.ProjetoDocUpdateDTO;
import com.demandtracker.service.ProjetoDocService;
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
@RequestMapping("/api/projeto-docs")
@RequiredArgsConstructor
public class ProjetoDocController {

    private final ProjetoDocService service;

    /**
     * Cria um novo documento
     * POST /api/projeto-docs
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjetoDocResponseDTO> create(
            @RequestParam("projetoId") Long projetoId,
            @RequestParam("nome") String nome,
            @RequestParam("documento") MultipartFile documento) throws IOException {
        
        ProjetoDocCreateDTO dto = new ProjetoDocCreateDTO();
        dto.setProjetoId(projetoId);
        dto.setNome(nome);
        dto.setDocumento(documento);
        
        ProjetoDocResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Busca documento por ID
     * GET /api/projeto-docs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjetoDocResponseDTO> findById(@PathVariable Long id) {
        ProjetoDocResponseDTO response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Busca documentos pelo ID do Projeto
     * GET /api/projeto-docs/projeto/{projetoId}
     */
    @GetMapping("/projeto/{projetoId}")
    public ResponseEntity<List<ProjetoDocResponseDTO>> findByProjetoId(
            @PathVariable Long projetoId) {
        List<ProjetoDocResponseDTO> response = service.findByProjetoId(projetoId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos os documentos
     * GET /api/projeto-docs
     */
    @GetMapping
    public ResponseEntity<List<ProjetoDocResponseDTO>> findAll() {
        List<ProjetoDocResponseDTO> response = service.findAll();
        return ResponseEntity.ok(response);
    }

    /**
     * Download do arquivo por ID do documento
     * GET /api/projeto-docs/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocumento(@PathVariable Long id) {
        byte[] arquivo = service.getDocumento(id);
        ProjetoDocResponseDTO doc = service.findById(id);
        
        HttpHeaders headers = new HttpHeaders();
        if (doc.getTipoConteudo() != null) {
            headers.setContentType(MediaType.parseMediaType(doc.getTipoConteudo()));
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }
        String nomeArquivo = doc.getNomeArquivo() != null ? doc.getNomeArquivo() : doc.getNome();
        headers.setContentDispositionFormData("attachment", nomeArquivo);
        headers.setContentLength(arquivo.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(arquivo);
    }

    /**
     * Atualiza um documento
     * PUT /api/projeto-docs/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjetoDocResponseDTO> update(
            @PathVariable Long id,
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "documento", required = false) MultipartFile documento) throws IOException {
        
        ProjetoDocUpdateDTO dto = new ProjetoDocUpdateDTO();
        dto.setNome(nome);
        dto.setDocumento(documento);
        
        ProjetoDocResponseDTO response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Deleta um documento
     * DELETE /api/projeto-docs/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifica se existe documento para um Projeto
     * GET /api/projeto-docs/projeto/{projetoId}/exists
     */
    @GetMapping("/projeto/{projetoId}/exists")
    public ResponseEntity<Boolean> existsByProjetoId(@PathVariable Long projetoId) {
        boolean exists = service.existsByProjetoId(projetoId);
        return ResponseEntity.ok(exists);
    }
}
