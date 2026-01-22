package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.*;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemandaTecnicaService {
    
    private final DemandaTecnicaRepository demandaRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    
    public Page<DemandaTecnicaDTO> findAll(String codigo, String nome, Long projetoId, String status, Pageable pageable) {
        Page<DemandaTecnica> demandas;
        
        if (codigo != null && nome != null && projetoId != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCaseAndProjetoId(codigo, nome, projetoId, pageable);
        } else if (codigo != null && nome != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCase(codigo, nome, pageable);
        } else if (codigo != null && projetoId != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndProjetoId(codigo, projetoId, pageable);
        } else if (nome != null && projetoId != null) {
            demandas = demandaRepository.findByNomeContainingIgnoreCaseAndProjetoId(nome, projetoId, pageable);
        } else if (codigo != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCase(codigo, pageable);
        } else if (nome != null) {
            demandas = demandaRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (projetoId != null) {
            demandas = demandaRepository.findByProjetoId(projetoId, pageable);
        } else {
            demandas = demandaRepository.findAll(pageable);
        }
        
        return demandas.map(DemandaTecnicaDTO::fromEntity);
    }
    
    public DemandaTecnicaDTO findById(Long id) {
        DemandaTecnica demanda = demandaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + id));
        return DemandaTecnicaDTO.fromEntity(demanda);
    }
    
    @Transactional
    public DemandaTecnicaDTO create(DemandaTecnicaCreateDTO dto) {
        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        if (demandaRepository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new RuntimeException("Código já existe: " + dto.getCodigo());
        }
        
        DemandaTecnica demanda = new DemandaTecnica();
        demanda.setProjeto(projeto);
        demanda.setCodigo(dto.getCodigo());
        demanda.setNome(dto.getNome());
        demanda.setUsuario(usuario);
        
        DemandaTecnica saved = demandaRepository.save(demanda);
        return DemandaTecnicaDTO.fromEntity(saved);
    }
    
    @Transactional
    public DemandaTecnicaDTO update(Long id, DemandaTecnicaUpdateDTO dto) {
        DemandaTecnica demanda = demandaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + id));
        
        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            demanda.setProjeto(projeto);
        }
        if (dto.getCodigo() != null) {
            demanda.setCodigo(dto.getCodigo());
        }
        if (dto.getNome() != null) {
            demanda.setNome(dto.getNome());
        }
        
        DemandaTecnica saved = demandaRepository.save(demanda);
        return DemandaTecnicaDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!demandaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Demanda não encontrada com ID: " + id);
        }
        demandaRepository.deleteById(id);
    }
}
