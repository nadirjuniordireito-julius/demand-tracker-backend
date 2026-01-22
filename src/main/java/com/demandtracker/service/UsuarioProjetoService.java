package com.demandtracker.service;

import com.demandtracker.dto.UsuarioProjetoCreateDTO;
import com.demandtracker.dto.UsuarioProjetoDTO;
import com.demandtracker.dto.UsuarioProjetoUpdateDTO;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.Usuario;
import com.demandtracker.entity.UsuarioProjeto;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.ProjetoRepository;
import com.demandtracker.repository.UsuarioProjetoRepository;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioProjetoService {
    
    private final UsuarioProjetoRepository usuarioProjetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoRepository projetoRepository;
    
    public Page<UsuarioProjetoDTO> findAll(Long usuarioId, Long projetoId, Pageable pageable) {
        Page<UsuarioProjeto> usuarioProjetos;
        
        if (usuarioId != null && projetoId != null) {
            usuarioProjetos = usuarioProjetoRepository.findByUsuarioIdAndProjetoId(usuarioId, projetoId, pageable);
        } else if (usuarioId != null) {
            usuarioProjetos = usuarioProjetoRepository.findByUsuarioId(usuarioId, pageable);
        } else if (projetoId != null) {
            usuarioProjetos = usuarioProjetoRepository.findByProjetoId(projetoId, pageable);
        } else {
            usuarioProjetos = usuarioProjetoRepository.findAll(pageable);
        }
        
        return usuarioProjetos.map(UsuarioProjetoDTO::fromEntity);
    }
    
    public UsuarioProjetoDTO findById(Long id) {
        UsuarioProjeto usuarioProjeto = usuarioProjetoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Relação Usuário-Projeto não encontrada com ID: " + id));
        return UsuarioProjetoDTO.fromEntity(usuarioProjeto);
    }
    
    @Transactional
    public UsuarioProjetoDTO create(UsuarioProjetoCreateDTO dto) {
        // Verificar se já existe a relação
        if (usuarioProjetoRepository.existsByUsuarioIdAndProjetoId(dto.getUsuarioId(), dto.getProjetoId())) {
            throw new BadRequestException("Já existe uma relação entre este usuário e este projeto");
        }
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
        
        UsuarioProjeto usuarioProjeto = new UsuarioProjeto();
        usuarioProjeto.setUsuario(usuario);
        usuarioProjeto.setProjeto(projeto);
        
        UsuarioProjeto saved = usuarioProjetoRepository.save(usuarioProjeto);
        return UsuarioProjetoDTO.fromEntity(saved);
    }
    
    @Transactional
    public UsuarioProjetoDTO update(Long id, UsuarioProjetoUpdateDTO dto) {
        UsuarioProjeto usuarioProjeto = usuarioProjetoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Relação Usuário-Projeto não encontrada com ID: " + id));
        
        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
            
            // Verificar se já existe a relação com o novo usuário e o projeto atual
            if (usuarioProjetoRepository.existsByUsuarioIdAndProjetoId(dto.getUsuarioId(), usuarioProjeto.getProjeto().getId()) 
                && !usuarioProjeto.getUsuario().getId().equals(dto.getUsuarioId())) {
                throw new BadRequestException("Já existe uma relação entre este usuário e este projeto");
            }
            
            usuarioProjeto.setUsuario(usuario);
        }
        
        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            
            // Verificar se já existe a relação com o usuário atual e o novo projeto
            if (usuarioProjetoRepository.existsByUsuarioIdAndProjetoId(usuarioProjeto.getUsuario().getId(), dto.getProjetoId())
                && !usuarioProjeto.getProjeto().getId().equals(dto.getProjetoId())) {
                throw new BadRequestException("Já existe uma relação entre este usuário e este projeto");
            }
            
            usuarioProjeto.setProjeto(projeto);
        }
        
        UsuarioProjeto saved = usuarioProjetoRepository.save(usuarioProjeto);
        return UsuarioProjetoDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!usuarioProjetoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Relação Usuário-Projeto não encontrada com ID: " + id);
        }
        usuarioProjetoRepository.deleteById(id);
    }
}
