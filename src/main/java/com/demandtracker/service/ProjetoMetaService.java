package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.ProjetoMeta;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.ProjetoMetaRepository;
import com.demandtracker.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjetoMetaService {
    
    private final ProjetoMetaRepository projetoMetaRepository;
    private final ProjetoRepository projetoRepository;
    
    public Page<ProjetoMetaDTO> findAll(String codigo, String nome, Long projetoId, String status, Pageable pageable) {
        Page<ProjetoMeta> metas;
        
        if (codigo != null && projetoId != null) {
            metas = projetoMetaRepository.findByProjetoIdAndCodigoContainingIgnoreCase(projetoId, codigo, pageable);
        } else if (nome != null && projetoId != null) {
            metas = projetoMetaRepository.findByProjetoIdAndNomeContainingIgnoreCase(projetoId, nome, pageable);
        } else if (projetoId != null && status != null) {
            metas = projetoMetaRepository.findByProjetoIdAndStatus(projetoId, status, pageable);
        } else if (projetoId != null) {
            metas = projetoMetaRepository.findByProjetoId(projetoId, pageable);
        } else if (codigo != null) {
            metas = projetoMetaRepository.findByCodigoContainingIgnoreCase(codigo, pageable);
        } else if (nome != null) {
            metas = projetoMetaRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (status != null) {
            metas = projetoMetaRepository.findByStatus(status, pageable);
        } else {
            metas = projetoMetaRepository.findAll(pageable);
        }
        
        return metas.map(ProjetoMetaDTO::fromEntity);
    }
    
    public ProjetoMetaDTO findById(Long id) {
        ProjetoMeta meta = projetoMetaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Meta do projeto não encontrada com ID: " + id));
        return ProjetoMetaDTO.fromEntity(meta);
    }
    
    @Transactional
    public ProjetoMetaDTO create(ProjetoMetaCreateDTO dto) {
        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
        
        ProjetoMeta meta = new ProjetoMeta();
        meta.setProjeto(projeto);
        meta.setCodigo(dto.getCodigo());
        meta.setNome(dto.getNome());
        meta.setDescricao(dto.getDescricao());
        meta.setStatus(dto.getStatus());
        
        ProjetoMeta saved = projetoMetaRepository.save(meta);
        return ProjetoMetaDTO.fromEntity(saved);
    }
    
    @Transactional
    public ProjetoMetaDTO update(Long id, ProjetoMetaUpdateDTO dto) {
        ProjetoMeta meta = projetoMetaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Meta do projeto não encontrada com ID: " + id));
        
        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            meta.setProjeto(projeto);
        }
        if (dto.getCodigo() != null) {
            meta.setCodigo(dto.getCodigo());
        }
        if (dto.getNome() != null) {
            meta.setNome(dto.getNome());
        }
        if (dto.getDescricao() != null) {
            meta.setDescricao(dto.getDescricao());
        }
        if (dto.getStatus() != null) {
            meta.setStatus(dto.getStatus());
        }
        
        ProjetoMeta saved = projetoMetaRepository.save(meta);
        return ProjetoMetaDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!projetoMetaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Meta do projeto não encontrada com ID: " + id);
        }
        projetoMetaRepository.deleteById(id);
    }
}
