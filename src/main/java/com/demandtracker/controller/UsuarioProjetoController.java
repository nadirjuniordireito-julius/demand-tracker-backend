package com.demandtracker.controller;

import com.demandtracker.dto.UsuarioProjetoCreateDTO;
import com.demandtracker.dto.UsuarioProjetoDTO;
import com.demandtracker.dto.UsuarioProjetoUpdateDTO;
import com.demandtracker.service.UsuarioProjetoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuario-projeto")
@RequiredArgsConstructor
public class UsuarioProjetoController {
    
    private final UsuarioProjetoService usuarioProjetoService;
    
    @GetMapping
    public ResponseEntity<Page<UsuarioProjetoDTO>> findAll(
        @RequestParam(required = false) Long usuarioId,
        @RequestParam(required = false) Long projetoId,
        Pageable pageable
    ) {
        return ResponseEntity.ok(usuarioProjetoService.findAll(usuarioId, projetoId, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioProjetoDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioProjetoService.findById(id));
    }
    
    @PostMapping
    public ResponseEntity<UsuarioProjetoDTO> create(@Valid @RequestBody UsuarioProjetoCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioProjetoService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioProjetoDTO> update(@PathVariable Long id, @Valid @RequestBody UsuarioProjetoUpdateDTO dto) {
        return ResponseEntity.ok(usuarioProjetoService.update(id, dto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        usuarioProjetoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
