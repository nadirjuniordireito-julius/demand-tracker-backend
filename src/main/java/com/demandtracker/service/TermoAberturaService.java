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
import com.demandtracker.entity.enums.StatusDemandaTecnica;

@Service
@RequiredArgsConstructor
public class TermoAberturaService {
    
    private final TermoAberturaRepository termoRepository;
    private final DemandaTecnicaRepository demandaRepository;
    private final UsuarioRepository usuarioRepository;
    
    public Page<TermoAberturaDTO> findAll(Pageable pageable) {
        return termoRepository.findAll(pageable).map(TermoAberturaDTO::fromEntity);
    }
    
    public TermoAberturaDTO findById(Long id) {
        TermoAbertura termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de abertura não encontrado com ID: " + id));
        return TermoAberturaDTO.fromEntity(termo);
    }
    
    public TermoAberturaDTO findByDemandaId(Long demandaId) {
        TermoAbertura termo = termoRepository.findByDemandaTecnicaId(demandaId)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de abertura não encontrado para demanda: " + demandaId));
        return TermoAberturaDTO.fromEntity(termo);
    }
    
    @Transactional
    public TermoAberturaDTO create(TermoAberturaCreateDTO dto) {
        DemandaTecnica demanda = demandaRepository.findById(dto.getDemandaTecnicaId())
            .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + dto.getDemandaTecnicaId()));
        
        if (termoRepository.findByDemandaTecnicaId(dto.getDemandaTecnicaId()).isPresent()) {
            throw new RuntimeException("Já existe termo de abertura para esta demanda");
        }
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        TermoAbertura termo = new TermoAbertura();
        termo.setDemandaTecnica(demanda);
        termo.setDataAbertura(dto.getDataAbertura());
        termo.setDescricao(dto.getDescricao());
        termo.setUsuario(usuario);
        
        TermoAbertura saved = termoRepository.save(termo);
        return TermoAberturaDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoAberturaDTO update(Long id, TermoAberturaUpdateDTO dto) {
        TermoAbertura termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de abertura não encontrado com ID: " + id));
        
        if (dto.getDataAbertura() != null) {
            termo.setDataAbertura(dto.getDataAbertura());
        }
        if (dto.getDescricao() != null) {
            termo.setDescricao(dto.getDescricao());
        }
        
        TermoAbertura saved = termoRepository.save(termo);
        return TermoAberturaDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoAberturaDTO sign(Long id) {
        TermoAbertura termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de abertura não encontrado com ID: " + id));
        
        // termo.setDataAssinatura(LocalDateTime.now());
        TermoAbertura saved = termoRepository.save(termo);
       
        DemandaTecnica demanda = saved.getDemandaTecnica();
        if (demanda != null) {
            demanda.setStatus(StatusDemandaTecnica.C.getCodigo()    );
            demandaRepository.save(demanda);
        }

        return TermoAberturaDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!termoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Termo de abertura não encontrado com ID: " + id);
        }
        termoRepository.deleteById(id);
    }
}
