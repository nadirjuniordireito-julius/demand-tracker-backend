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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TermoEncerramentoService {
    
    private final TermoEncerramentoRepository termoRepository;
    private final DemandaTecnicaRepository demandaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    
    public Page<TermoEncerramentoDTO> findAll(Pageable pageable) {
        return termoRepository.findAll(pageable).map(TermoEncerramentoDTO::fromEntity);
    }
    
    public TermoEncerramentoDTO findById(Long id) {
        TermoEncerramento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de encerramento não encontrado com ID: " + id));
        return TermoEncerramentoDTO.fromEntity(termo);
    }
    
    public TermoEncerramentoDTO findByDemandaId(Long demandaId) {
        TermoEncerramento termo = termoRepository.findByDemandaTecnicaId(demandaId)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de encerramento não encontrado para demanda: " + demandaId));
        return TermoEncerramentoDTO.fromEntity(termo);
    }
    
    @Transactional
    public TermoEncerramentoDTO create(TermoEncerramentoCreateDTO dto) {
        DemandaTecnica demanda = demandaRepository.findById(dto.getDemandaTecnicaId())
            .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + dto.getDemandaTecnicaId()));
        
        if (termoRepository.findByDemandaTecnicaId(dto.getDemandaTecnicaId()).isPresent()) {
            throw new RuntimeException("Já existe termo de encerramento para esta demanda");
        }
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        TermoEncerramento termo = new TermoEncerramento();
        termo.setDemandaTecnica(demanda);
        termo.setResultadoEntregue(dto.getResultadoEntregue());
        termo.setUsuario(usuario);
        
        if (dto.getCustos() != null && !dto.getCustos().isEmpty()) {
            List<TermoEncerramentoCusto> custos = new ArrayList<>();
            for (TermoEncerramentoCustoCreateDTO custoDTO : dto.getCustos()) {
                Perfil perfil = perfilRepository.findById(custoDTO.getPerfilId())
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + custoDTO.getPerfilId()));
                
                TermoEncerramentoCusto custo = new TermoEncerramentoCusto();
                custo.setTermoEncerramento(termo);
                custo.setPerfil(perfil);
                custo.setQtdeHora(custoDTO.getQtdeHora());
                custo.setValorHora(custoDTO.getValorHora());
                custos.add(custo);
            }
            termo.setCustos(custos);
        }
        
        TermoEncerramento saved = termoRepository.save(termo);
        return TermoEncerramentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoEncerramentoDTO update(Long id, TermoEncerramentoUpdateDTO dto) {
        TermoEncerramento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de encerramento não encontrado com ID: " + id));
        
        if (dto.getResultadoEntregue() != null) {
            termo.setResultadoEntregue(dto.getResultadoEntregue());
        }
        
        if (dto.getCustos() != null) {
            termo.getCustos().clear();
            for (TermoEncerramentoCustoCreateDTO custoDTO : dto.getCustos()) {
                Perfil perfil = perfilRepository.findById(custoDTO.getPerfilId())
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + custoDTO.getPerfilId()));
                
                TermoEncerramentoCusto custo = new TermoEncerramentoCusto();
                custo.setTermoEncerramento(termo);
                custo.setPerfil(perfil);
                custo.setQtdeHora(custoDTO.getQtdeHora());
                custo.setValorHora(custoDTO.getValorHora());
                termo.getCustos().add(custo);
            }
        }
        
        TermoEncerramento saved = termoRepository.save(termo);
        return TermoEncerramentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoEncerramentoDTO sign(Long id) {
        TermoEncerramento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de encerramento não encontrado com ID: " + id));
        
        termo.setDataAssinatura(LocalDateTime.now());
        TermoEncerramento saved = termoRepository.save(termo);
        return TermoEncerramentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        if (!termoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Termo de encerramento não encontrado com ID: " + id);
        }
        termoRepository.deleteById(id);
    }
}
