package com.demandtracker.controller;

import com.demandtracker.dto.DemandaAvaliacaoDocCreateDTO;
import com.demandtracker.dto.DemandaAvaliacaoDocResponseDTO;
import com.demandtracker.dto.DemandaAvaliacaoDocUpdateDTO;
import com.demandtracker.repository.DemandaAvaliacaoRepository;
import com.demandtracker.service.DemandaAvaliacaoDocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/demandas/{demandaId}/avaliacao/doc")
@RequiredArgsConstructor
public class DemandaAvaliacaoDocController {

    private final DemandaAvaliacaoDocService service;
    private final DemandaAvaliacaoRepository avaliacaoRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DemandaAvaliacaoDocResponseDTO> create(
            @PathVariable Long demandaId,
            @RequestParam(value = "arquivoPdf", required = false) MultipartFile arquivoPdf,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        MultipartFile arquivo = (arquivoPdf != null && !arquivoPdf.isEmpty()) ? arquivoPdf : file;
        if (arquivo == null || arquivo.isEmpty()) {
            throw new com.demandtracker.exception.BadRequestException(
                    "Envie o PDF no campo 'arquivoPdf' ou 'file' (multipart/form-data).");
        }
        Long avaliacaoId = avaliacaoRepository.findByDemandaId(demandaId)
                .orElseThrow(() -> new com.demandtracker.exception.ResourceNotFoundException(
                        "Avaliação não encontrada para a Demanda ID: " + demandaId))
                .getId();
        DemandaAvaliacaoDocCreateDTO dto = new DemandaAvaliacaoDocCreateDTO();
        dto.setAvaliacaoId(avaliacaoId);
        dto.setArquivoPdf(arquivo);
        DemandaAvaliacaoDocResponseDTO response = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<DemandaAvaliacaoDocResponseDTO> getByDemandaId(@PathVariable Long demandaId) {
        return ResponseEntity.ok(service.findByDemandaId(demandaId));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@PathVariable Long demandaId) {
        byte[] arquivo = service.getArquivoPdfByDemandaId(demandaId);
        DemandaAvaliacaoDocResponseDTO doc = service.findByDemandaId(demandaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", doc.getNomeArquivo());
        headers.setContentLength(arquivo.length);
        return ResponseEntity.ok().headers(headers).body(arquivo);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DemandaAvaliacaoDocResponseDTO> update(
            @PathVariable Long demandaId,
            @RequestParam(value = "dataUpload", required = false) String dataUpload,
            @RequestParam(value = "arquivoPdf", required = false) MultipartFile arquivoPdf,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {
        MultipartFile arquivo = (arquivoPdf != null && !arquivoPdf.isEmpty()) ? arquivoPdf : file;
        DemandaAvaliacaoDocResponseDTO current = service.findByDemandaId(demandaId);
        DemandaAvaliacaoDocUpdateDTO dto = new DemandaAvaliacaoDocUpdateDTO();
        dto.setArquivoPdf(arquivo);
        DemandaAvaliacaoDocResponseDTO response = service.update(current.getId(), dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long demandaId) {
        DemandaAvaliacaoDocResponseDTO current = service.findByDemandaId(demandaId);
        service.delete(current.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> exists(@PathVariable Long demandaId) {
        return ResponseEntity.ok(service.existsByDemandaId(demandaId));
    }
}
