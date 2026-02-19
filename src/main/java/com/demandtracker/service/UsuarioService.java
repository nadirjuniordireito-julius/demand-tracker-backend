package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.Usuario;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class UsuarioService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    
    public Page<UsuarioDTO> findAll(String nome, String perfil, String status, Pageable pageable) {
        Page<Usuario> usuarios;
        
        if (nome != null && perfil != null && status != null) {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCaseAndPerfilAndStatus(
                nome, Usuario.UserProfile.valueOf(perfil), Usuario.UserStatus.valueOf(status), pageable);
        } else if (nome != null && perfil != null) {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCaseAndPerfil(
                nome, Usuario.UserProfile.valueOf(perfil), pageable);
        } else if (nome != null && status != null) {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCaseAndStatus(
                nome, Usuario.UserStatus.valueOf(status), pageable);
        } else if (perfil != null && status != null) {
            usuarios = usuarioRepository.findByPerfilAndStatus(
                Usuario.UserProfile.valueOf(perfil), Usuario.UserStatus.valueOf(status), pageable);
        } else if (nome != null) {
            usuarios = usuarioRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (perfil != null) {
            usuarios = usuarioRepository.findByPerfil(Usuario.UserProfile.valueOf(perfil), pageable);
        } else if (status != null) {
            usuarios = usuarioRepository.findByStatus(Usuario.UserStatus.valueOf(status), pageable);
        } else {
            usuarios = usuarioRepository.findAll(pageable);
        }
        
        return usuarios.map(UsuarioDTO::fromEntity);
    }
    
    public UsuarioDTO findById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        return UsuarioDTO.fromEntity(usuario);
    }
    
    @Transactional
    public UsuarioDTO create(UsuarioCreateDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setEmail(dto.getNome().toLowerCase().replace(" ", ".") + "@empresa.com");
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        usuario.setPassword(encodedPassword);
        usuario.setPerfil(dto.getPerfil());
        usuario.setStatus(dto.getStatus());
        
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioDTO.fromEntity(saved);
    }
    
    @Transactional
    public UsuarioDTO update(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));
        
        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            usuario.setPassword(encodedPassword);
        }
        if (dto.getPerfil() != null) {
            usuario.setPerfil(dto.getPerfil());
        }
        if (dto.getStatus() != null) {
            usuario.setStatus(dto.getStatus());
        }
        
        Usuario saved = usuarioRepository.save(usuario);
        return UsuarioDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    @PostConstruct
    public void printHashDemandaTracker() {
        // utilitário para gerar hash de senha se necessário
    }

}
