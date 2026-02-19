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

/**
 * Controller alternativo para endpoints no singular (/api/usuario-foto)
 * Para compatibilidade com o frontend
 */
@RestController
@RequestMapping("/api/usuario-foto")
@RequiredArgsConstructor
public class UsuarioFotoSingularController {
    
    private final UsuarioFotoService usuarioFotoService;
    
    /**
     * Cria ou atualiza uma foto para um usuário
     * POST /api/usuario-foto
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioFotoResponseDTO> createOrUpdate(
            @RequestParam(value = "usuarioId", required = false) Long usuarioId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        
        // Tenta pegar o arquivo de qualquer um dos parâmetros possíveis
        MultipartFile arquivoFoto = foto != null && !foto.isEmpty() ? foto : 
                                    (file != null && !file.isEmpty() ? file : 
                                     (image != null && !image.isEmpty() ? image : null));
        
        if (usuarioId == null) {
            throw new IllegalArgumentException("usuarioId é obrigatório");
        }
        
        if (arquivoFoto == null || arquivoFoto.isEmpty()) {
            throw new IllegalArgumentException("Foto é obrigatória. Envie o arquivo como 'foto', 'file' ou 'image'");
        }
        
        // Usa updateByUsuarioId que cria se não existir
        UsuarioFotoUpdateDTO dto = new UsuarioFotoUpdateDTO();
        dto.setFoto(arquivoFoto);
        
        UsuarioFotoResponseDTO response = usuarioFotoService.updateByUsuarioId(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Busca foto pelo ID do usuário
     * GET /api/usuario-foto/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<UsuarioFotoResponseDTO> findByUsuarioId(@PathVariable Long usuarioId) {
        UsuarioFotoResponseDTO response = usuarioFotoService.findByUsuarioId(usuarioId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Download da foto pelo ID do usuário
     * GET /api/usuario-foto/usuario/{usuarioId}/download
     */
    @GetMapping("/usuario/{usuarioId}/download")
    public ResponseEntity<byte[]> downloadFotoByUsuarioId(@PathVariable Long usuarioId) {
        byte[] foto = usuarioFotoService.getFotoByUsuarioId(usuarioId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentDispositionFormData("inline", "foto.jpg");
        headers.setContentLength(foto.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(foto);
    }
    
    /**
     * Atualiza foto pelo ID do usuário (cria se não existir)
     * PUT /api/usuario-foto/usuario/{usuarioId}
     */
    @PutMapping(value = "/usuario/{usuarioId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioFotoResponseDTO> updateByUsuarioId(
            @PathVariable Long usuarioId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "foto", required = false) MultipartFile foto) throws IOException {
        
        MultipartFile arquivoFoto = foto != null ? foto : file;
        
        if (arquivoFoto == null || arquivoFoto.isEmpty()) {
            throw new IllegalArgumentException("Foto é obrigatória");
        }
        
        UsuarioFotoUpdateDTO dto = new UsuarioFotoUpdateDTO();
        dto.setFoto(arquivoFoto);
        
        UsuarioFotoResponseDTO response = usuarioFotoService.updateByUsuarioId(usuarioId, dto);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Deleta foto pelo ID do usuário
     * DELETE /api/usuario-foto/usuario/{usuarioId}
     */
    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<Void> deleteByUsuarioId(@PathVariable Long usuarioId) {
        usuarioFotoService.deleteByUsuarioId(usuarioId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Verifica se existe foto para um usuário
     * GET /api/usuario-foto/usuario/{usuarioId}/exists
     */
    @GetMapping("/usuario/{usuarioId}/exists")
    public ResponseEntity<Boolean> existsByUsuarioId(@PathVariable Long usuarioId) {
        boolean exists = usuarioFotoService.existsByUsuarioId(usuarioId);
        return ResponseEntity.ok(exists);
    }
}
