package com.demandtracker.service;

import com.demandtracker.dto.PerfilCreateDTO;
import com.demandtracker.dto.PerfilDTO;
import com.demandtracker.dto.PerfilUpdateDTO;
import com.demandtracker.entity.Perfil;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.Usuario;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.PerfilRepository;
import com.demandtracker.repository.ProjetoRepository;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerfilService {
    
    private final PerfilRepository perfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoRepository projetoRepository;
    
    public Page<PerfilDTO> findAll(String nome, Long projetoId, Pageable pageable) {
        Page<Perfil> perfis;
        
        if (nome != null && projetoId != null) {
            perfis = perfilRepository.findByNomeContainingIgnoreCaseAndProjetoId(nome, projetoId, pageable);
        } else if (nome != null) {
            perfis = perfilRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (projetoId != null) {
            perfis = perfilRepository.findByProjetoId(projetoId, pageable);
        } else {
            perfis = perfilRepository.findAll(pageable);
        }
        
        return perfis.map(PerfilDTO::fromEntity);
    }
    
    public PerfilDTO findById(Long id) {
        Perfil perfil = perfilRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + id));
        return PerfilDTO.fromEntity(perfil);
    }
    
    
    @Transactional
    public PerfilDTO create(PerfilCreateDTO dto) {
        if (dto.getTermoFinal().isBefore(dto.getTermoInicial())) {
            throw new BadRequestException("Data final deve ser maior ou igual à data inicial");
        }
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
        
        Perfil perfil = new Perfil();
        perfil.setNome(dto.getNome());
        perfil.setTermoInicial(dto.getTermoInicial());
        perfil.setTermoFinal(dto.getTermoFinal());
        perfil.setValor(dto.getValor());
        perfil.setUsuario(usuario);
        perfil.setProjeto(projeto);
        
        Perfil saved = perfilRepository.save(perfil);
        return PerfilDTO.fromEntity(saved);
    }
    
    @Transactional
    public PerfilDTO update(Long id, PerfilUpdateDTO dto) {
        Perfil perfil = perfilRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + id));
        
        if (dto.getNome() != null) {
            perfil.setNome(dto.getNome());
        }
        if (dto.getTermoInicial() != null) {
            perfil.setTermoInicial(dto.getTermoInicial());
        }
        if (dto.getTermoFinal() != null) {
            perfil.setTermoFinal(dto.getTermoFinal());
        }
        if (dto.getValor() != null) {
            perfil.setValor(dto.getValor());
        }
        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
            perfil.setUsuario(usuario);
        }
        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            perfil.setProjeto(projeto);
        }
        
        if (perfil.getTermoFinal().isBefore(perfil.getTermoInicial())) {
            throw new BadRequestException("Data final deve ser maior ou igual à data inicial");
        }
        
        Perfil saved = perfilRepository.save(perfil);
        return PerfilDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!perfilRepository.existsById(id)) {
            throw new ResourceNotFoundException("Perfil não encontrado com ID: " + id);
        }
        perfilRepository.deleteById(id);
    }
}
