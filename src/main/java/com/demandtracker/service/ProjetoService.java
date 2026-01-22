package com.demandtracker.service;

import com.demandtracker.dto.ProjetoCreateDTO;
import com.demandtracker.dto.ProjetoDTO;
import com.demandtracker.dto.ProjetoUpdateDTO;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.Usuario;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.ProjetoRepository;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjetoService {
    
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    
    public Page<ProjetoDTO> findAll(String nome, String codTed, Long usuarioId, Pageable pageable) {
        Page<Projeto> projetos;
        
        if (nome != null && codTed != null) {
            projetos = projetoRepository.findByNomeContainingIgnoreCaseAndCodTedContainingIgnoreCase(nome, codTed, pageable);
        } else if (nome != null) {
            projetos = projetoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (codTed != null) {
            projetos = projetoRepository.findByCodTedContainingIgnoreCase(codTed, pageable);
        } else if (usuarioId != null) {
            projetos = projetoRepository.findByUsuarioId(usuarioId, pageable);
        } else {
            projetos = projetoRepository.findAll(pageable);
        }
        
        return projetos.map(ProjetoDTO::fromEntity);
    }
    
    public ProjetoDTO findById(Long id) {
        Projeto projeto = projetoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + id));
        return ProjetoDTO.fromEntity(projeto);
    }
    
    @Transactional
    public ProjetoDTO create(ProjetoCreateDTO dto) {
        if (dto.getTermoFinal().isBefore(dto.getTermoInicial())) {
            throw new BadRequestException("Data final deve ser maior ou igual à data inicial");
        }
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        Projeto projeto = new Projeto();
        projeto.setNome(dto.getNome());
        projeto.setCodTed(dto.getCodTed());
        projeto.setTermoInicial(dto.getTermoInicial());
        projeto.setTermoFinal(dto.getTermoFinal());
        projeto.setUsuario(usuario);
        
        Projeto saved = projetoRepository.save(projeto);
        return ProjetoDTO.fromEntity(saved);
    }
    
    @Transactional
    public ProjetoDTO update(Long id, ProjetoUpdateDTO dto) {
        Projeto projeto = projetoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + id));
        
        if (dto.getNome() != null) {
            projeto.setNome(dto.getNome());
        }
        if (dto.getCodTed() != null) {
            projeto.setCodTed(dto.getCodTed());
        }
        if (dto.getTermoInicial() != null) {
            projeto.setTermoInicial(dto.getTermoInicial());
        }
        if (dto.getTermoFinal() != null) {
            projeto.setTermoFinal(dto.getTermoFinal());
        }
        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
            projeto.setUsuario(usuario);
        }
        
        if (projeto.getTermoFinal().isBefore(projeto.getTermoInicial())) {
            throw new BadRequestException("Data final deve ser maior ou igual à data inicial");
        }
        
        Projeto saved = projetoRepository.save(projeto);
        return ProjetoDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!projetoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Projeto não encontrado com ID: " + id);
        }
        projetoRepository.deleteById(id);
    }
}
