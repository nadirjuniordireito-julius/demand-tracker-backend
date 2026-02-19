package com.demandtracker.controller;

import com.demandtracker.dto.UsuarioFotoResponseDTO;
import com.demandtracker.dto.UsuarioFotoUpdateDTO;
import com.demandtracker.service.UsuarioFotoService;
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
@RequestMapping("/api/usuario-fotos")
@RequiredArgsConstructor
public class UsuarioFotoController {
    
    private final UsuarioFotoService usuarioFotoService;
    
    /**
     * Cria uma nova foto para um usuário
     * POST /api/usuario-fotos
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioFotoResponseDTO> create(
            @RequestParam("usuarioId") Long usuarioId,
            @RequestParam("foto") MultipartFile foto) throws IOException {
        
        com.demandtracker.dto.UsuarioFotoCreateDTO dto = new com.demandtracker.dto.UsuarioFotoCreateDTO();
        dto.setUsuarioId(usuarioId);
        dto.setFoto(foto);
        
        UsuarioFotoResponseDTO response = usuarioFotoService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    
    /**
     * Busca foto por ID
     * GET /api/usuario-fotos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioFotoResponseDTO> findById(@PathVariable Long id) {
        UsuarioFotoResponseDTO response = usuarioFotoService.findById(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Busca foto pelo ID do usuário
     * GET /api/usuario-fotos/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<UsuarioFotoResponseDTO> findByUsuarioId(@PathVariable Long usuarioId) {
        UsuarioFotoResponseDTO response = usuarioFotoService.findByUsuarioId(usuarioId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lista todas as fotos
     * GET /api/usuario-fotos
     */
    @GetMapping
    public ResponseEntity<List<UsuarioFotoResponseDTO>> findAll() {
        List<UsuarioFotoResponseDTO> response = usuarioFotoService.findAll();
        return ResponseEntity.ok(response);
    }
    
    /**
     * Download da foto por ID
     * GET /api/usuario-fotos/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFoto(@PathVariable Long id) {
        byte[] foto = usuarioFotoService.getFoto(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Ajuste conforme necessário
        headers.setContentDispositionFormData("inline", "foto.jpg");
        headers.setContentLength(foto.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(foto);
    }
    
    /**
     * Download da foto pelo ID do usuário
     * GET /api/usuario-fotos/usuario/{usuarioId}/download
     */
    @GetMapping("/usuario/{usuarioId}/download")
    public ResponseEntity<byte[]> downloadFotoByUsuarioId(@PathVariable Long usuarioId) {
        byte[] foto = usuarioFotoService.getFotoByUsuarioId(usuarioId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // Ajuste conforme necessário
        headers.setContentDispositionFormData("inline", "foto.jpg");
        headers.setContentLength(foto.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(foto);
    }
    
    /**
     * Atualiza uma foto por ID
     * PUT /api/usuario-fotos/{id}
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioFotoResponseDTO> update(
            @PathVariable Long id,
            @RequestParam(value = "foto", required = false) MultipartFile foto) throws IOException {
        
        UsuarioFotoUpdateDTO dto = new UsuarioFotoUpdateDTO();
        dto.setFoto(foto);
        
        UsuarioFotoResponseDTO response = usuarioFotoService.update(id, dto);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Atualiza foto pelo ID do usuário (cria se não existir)
     * PUT /api/usuario-fotos/usuario/{usuarioId}
     */
    @PutMapping(value = "/usuario/{usuarioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioFotoResponseDTO> updateByUsuarioId(
            @PathVariable Long usuarioId,
            @RequestParam("foto") MultipartFile foto) throws IOException {
        
        UsuarioFotoUpdateDTO dto = new UsuarioFotoUpdateDTO();
        dto.setFoto(foto);
        
        UsuarioFotoResponseDTO response = usuarioFotoService.updateByUsuarioId(usuarioId, dto);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deleta uma foto por ID
     * DELETE /api/usuario-fotos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioFotoService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Deleta foto pelo ID do usuário
     * DELETE /api/usuario-fotos/usuario/{usuarioId}
     */
    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<Void> deleteByUsuarioId(@PathVariable Long usuarioId) {
        usuarioFotoService.deleteByUsuarioId(usuarioId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Verifica se existe foto para um usuário
     * GET /api/usuario-fotos/usuario/{usuarioId}/exists
     */
    @GetMapping("/usuario/{usuarioId}/exists")
    public ResponseEntity<Boolean> existsByUsuarioId(@PathVariable Long usuarioId) {
        boolean exists = usuarioFotoService.existsByUsuarioId(usuarioId);
        return ResponseEntity.ok(exists);
    }
}
