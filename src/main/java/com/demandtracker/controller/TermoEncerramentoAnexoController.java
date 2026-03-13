package com.demandtracker.controller;

import com.demandtracker.dto.TermoEncerramentoAnexoCreateDTO;
import com.demandtracker.dto.TermoEncerramentoAnexoResponseDTO;
import com.demandtracker.service.TermoEncerramentoAnexoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Controller para anexos do Termo de Encerramento.
 * Inclusão e exclusão só permitidas quando status da demanda técnica = "E".
 */
@RestController
@RequestMapping("/api/termos-encerramento-anexos")
@RequiredArgsConstructor
public class TermoEncerramentoAnexoController {

    private final TermoEncerramentoAnexoService service;

    /**
     * Inclui um anexo no termo de encerramento.
     * Permitido apenas quando status da demanda técnica = "E".
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TermoEncerramentoAnexoResponseDTO> create(
            @RequestParam("termoEncerramentoId") Long termoEncerramentoId,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "usuarioId", required = false) Long usuarioId) throws IOException {
        TermoEncerramentoAnexoCreateDTO dto = new TermoEncerramentoAnexoCreateDTO();
        dto.setTermoEncerramentoId(termoEncerramentoId);
        dto.setArquivo(arquivo);
        dto.setUsuarioId(usuarioId);
        TermoEncerramentoAnexoResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TermoEncerramentoAnexoResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/termo/{termoEncerramentoId}")
    public ResponseEntity<List<TermoEncerramentoAnexoResponseDTO>> findByTermoEncerramentoId(
            @PathVariable Long termoEncerramentoId) {
        return ResponseEntity.ok(service.findByTermoEncerramentoId(termoEncerramentoId));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        byte[] arquivo = service.getArquivo(id);
        TermoEncerramentoAnexoResponseDTO doc = service.findById(id);
        MediaType mediaType = doc.getTipoConteudo() != null && !doc.getTipoConteudo().isBlank()
                ? MediaType.parseMediaType(doc.getTipoConteudo())
                : MediaType.APPLICATION_OCTET_STREAM;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", doc.getNomeArquivo() != null ? doc.getNomeArquivo() : "anexo");
        headers.setContentLength(arquivo.length);
        return ResponseEntity.ok().headers(headers).body(arquivo);
    }

    /**
     * Remove um anexo.
     * Permitido apenas quando status da demanda técnica = "E".
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
