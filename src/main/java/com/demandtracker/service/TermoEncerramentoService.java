package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.*;
import com.demandtracker.event.DemandaEncerradaEvent;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final MetaProdutoService metaProdutoService;
    private final ApplicationEventPublisher eventPublisher;
    
    public Page<TermoEncerramentoDTO> findAll(Pageable pageable) {
        return termoRepository.findAll(pageable).map(TermoEncerramentoDTO::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public TermoEncerramentoDTO findById(Long id) {
        TermoEncerramento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de encerramento não encontrado com ID: " + id));
        TermoEncerramentoDTO dto = TermoEncerramentoDTO.fromEntity(termo);
        preencherValoresProdutoNoModal(dto, termo.getDemandaTecnica());
        return dto;
    }
    
    @Transactional(readOnly = true)
    public TermoEncerramentoDTO findByDemandaId(Long demandaId) {
        TermoEncerramento termo = termoRepository.findByDemandaTecnicaId(demandaId)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de encerramento não encontrado para demanda: " + demandaId));
        TermoEncerramentoDTO dto = TermoEncerramentoDTO.fromEntity(termo);
        preencherValoresProdutoNoModal(dto, termo.getDemandaTecnica());
        return dto;
    }
    
    private void preencherValoresProdutoNoModal(TermoEncerramentoDTO dto, DemandaTecnica demanda) {
        if (demanda == null || demanda.getMetaProduto() == null) return;
        var produto = demanda.getMetaProduto();
        if (produto.getValorUnitario() != null && produto.getQuantidade() != null) {
            dto.setValorPrevistoProduto(produto.getValorUnitario().multiply(BigDecimal.valueOf(produto.getQuantidade())));
        }
        BigDecimal valorExecutado = termoRepository.sumValorExecutadoByMetaProdutoIdAndStatus(produto.getId(), DemandaTecnica.STATUS_ENCERRADA);
        dto.setValorTotalExecutadoProduto(valorExecutado != null ? valorExecutado : BigDecimal.ZERO);
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
        termo.setDataTermo(dto.getDataTermo());
        termo.setDataInicioExecucao(dto.getDataInicioExecucao());
        termo.setDataFimExecucao(dto.getDataFimExecucao());
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
        if (demanda.getMetaProduto() != null) {
            metaProdutoService.recalculatePercExecutado(demanda.getMetaProduto().getId());
        }
        return TermoEncerramentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoEncerramentoDTO update(Long id, TermoEncerramentoUpdateDTO dto) {
        TermoEncerramento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de encerramento não encontrado com ID: " + id));
        
        if (dto.getDataTermo() != null) {
            termo.setDataTermo(dto.getDataTermo());
        }
        if (dto.getDataInicioExecucao() != null) {
            termo.setDataInicioExecucao(dto.getDataInicioExecucao());
        }
        if (dto.getDataFimExecucao() != null) {
            termo.setDataFimExecucao(dto.getDataFimExecucao());
        }
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
        if (saved.getDemandaTecnica().getMetaProduto() != null) {
            metaProdutoService.recalculatePercExecutado(saved.getDemandaTecnica().getMetaProduto().getId());
        }
        return TermoEncerramentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoEncerramentoDTO sign(Long id) {
        TermoEncerramento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de encerramento não encontrado com ID: " + id));
        
        // termo.setDataAssinatura(LocalDateTime.now());
        TermoEncerramento saved = termoRepository.save(termo);

        DemandaTecnica demanda = saved.getDemandaTecnica();
        if (demanda != null) {
            demanda.setStatus(DemandaTecnica.STATUS_ENCERRADA);
            demanda.setAvaliacaoDisponivel(true);
            demandaRepository.save(demanda);
            eventPublisher.publishEvent(new DemandaEncerradaEvent(this, demanda.getId()));
        }

        return TermoEncerramentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public void delete(Long id) {
        TermoEncerramento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de encerramento não encontrado com ID: " + id));
        Long metaProdutoId = termo.getDemandaTecnica().getMetaProduto() != null
            ? termo.getDemandaTecnica().getMetaProduto().getId()
            : null;
        termoRepository.deleteById(id);
        if (metaProdutoId != null) {
            metaProdutoService.recalculatePercExecutado(metaProdutoId);
        }
    }
}
