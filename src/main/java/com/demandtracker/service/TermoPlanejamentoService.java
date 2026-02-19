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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.demandtracker.entity.DemandaTecnica;

@Service
@RequiredArgsConstructor
public class TermoPlanejamentoService {
    
    private final TermoPlanejamentoRepository termoRepository;
    private final DemandaTecnicaRepository demandaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;
    
    public Page<TermoPlanejamentoDTO> findAll(Pageable pageable) {
        return termoRepository.findAll(pageable).map(TermoPlanejamentoDTO::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public TermoPlanejamentoDTO findById(Long id) {
        TermoPlanejamento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + id));
        TermoPlanejamentoDTO dto = TermoPlanejamentoDTO.fromEntity(termo);
        preencherValoresProdutoNoModal(dto, termo.getDemandaTecnica());
        return dto;
    }
    
    @Transactional(readOnly = true)
    public TermoPlanejamentoDTO findByDemandaId(Long demandaId) {
        TermoPlanejamento termo = termoRepository.findByDemandaTecnicaId(demandaId).orElse(null);
            // .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado para demanda: " + demandaId));
        TermoPlanejamentoDTO dto = TermoPlanejamentoDTO.fromEntity(termo);
        preencherValoresProdutoNoModal(dto, termo.getDemandaTecnica());
        return dto;
    }
    
    private void preencherValoresProdutoNoModal(TermoPlanejamentoDTO dto, DemandaTecnica demanda) {
        if (demanda == null || demanda.getMetaProduto() == null) return;
        var produto = demanda.getMetaProduto();
        if (produto.getValorUnitario() != null && produto.getQuantidade() != null) {
            dto.setValorPrevistoProduto(produto.getValorUnitario().multiply(BigDecimal.valueOf(produto.getQuantidade())));
        }
        BigDecimal valorExecutado = termoEncerramentoRepository.sumValorExecutadoByMetaProdutoIdAndStatus(produto.getId(), DemandaTecnica.STATUS_ENCERRADA);
        dto.setValorTotalExecutadoProduto(valorExecutado != null ? valorExecutado : BigDecimal.ZERO);
    }
    
    @Transactional
    public TermoPlanejamentoDTO create(TermoPlanejamentoCreateDTO dto) {
        DemandaTecnica demanda = demandaRepository.findById(dto.getDemandaTecnicaId())
            .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + dto.getDemandaTecnicaId()));
        
        if (termoRepository.findByDemandaTecnicaId(dto.getDemandaTecnicaId()).isPresent()) {
            throw new RuntimeException("Já existe termo de planejamento para esta demanda");
        }
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        TermoPlanejamento termo = new TermoPlanejamento();
        termo.setDemandaTecnica(demanda);
        termo.setDataAbertura(dto.getDataAbertura());
        termo.setDataInicioExecucao(dto.getDataInicioExecucao());
        termo.setDataFimExecucao(dto.getDataFimExecucao());
        termo.setEspecificacao(dto.getEspecificacao());
        termo.setCronograma(dto.getCronograma());
        termo.setResultadoEsperado(dto.getResultadoEsperado());
        termo.setUsuario(usuario);
        
        if (dto.getCustos() != null && !dto.getCustos().isEmpty()) {
            List<TermoPlanejamentoCusto> custos = new ArrayList<>();
            for (TermoPlanejamentoCustoCreateDTO custoDTO : dto.getCustos()) {
                Perfil perfil = perfilRepository.findById(custoDTO.getPerfilId())
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + custoDTO.getPerfilId()));
                
                TermoPlanejamentoCusto custo = new TermoPlanejamentoCusto();
                custo.setTermoPlanejamento(termo);
                custo.setPerfil(perfil);
                custo.setQtdeHora(custoDTO.getQtdeHora());
                custo.setValorHora(custoDTO.getValorHora());
                custos.add(custo);
            }
            termo.setCustos(custos);
        }
        
        TermoPlanejamento saved = termoRepository.save(termo);
        return TermoPlanejamentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoPlanejamentoDTO update(Long id, TermoPlanejamentoUpdateDTO dto) {
        TermoPlanejamento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + id));
        
        if (dto.getDataAbertura() != null) {
            termo.setDataAbertura(dto.getDataAbertura());
        }
        if (dto.getDataInicioExecucao() != null) {
            termo.setDataInicioExecucao(dto.getDataInicioExecucao());
        }
        if (dto.getDataFimExecucao() != null) {
            termo.setDataFimExecucao(dto.getDataFimExecucao());
        }
        if (dto.getEspecificacao() != null) {
            termo.setEspecificacao(dto.getEspecificacao());
        }
        if (dto.getCronograma() != null) {
            termo.setCronograma(dto.getCronograma());
        }
        if (dto.getResultadoEsperado() != null) {
            termo.setResultadoEsperado(dto.getResultadoEsperado());
        }
        
        if (dto.getCustos() != null) {
            termo.getCustos().clear();
            for (TermoPlanejamentoCustoCreateDTO custoDTO : dto.getCustos()) {
                Perfil perfil = perfilRepository.findById(custoDTO.getPerfilId())
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + custoDTO.getPerfilId()));
                
                TermoPlanejamentoCusto custo = new TermoPlanejamentoCusto();
                custo.setTermoPlanejamento(termo);
                custo.setPerfil(perfil);
                custo.setQtdeHora(custoDTO.getQtdeHora());
                custo.setValorHora(custoDTO.getValorHora());
                termo.getCustos().add(custo);
            }
        }
        
        TermoPlanejamento saved = termoRepository.save(termo);
        return TermoPlanejamentoDTO.fromEntity(saved);
    }
    
    @Transactional
    public TermoPlanejamentoDTO sign(Long id) {
        TermoPlanejamento termo = termoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + id));
        
        // termo.setDataAssinatura(LocalDateTime.now());
        TermoPlanejamento saved = termoRepository.save(termo);
       
        DemandaTecnica dt = saved.getDemandaTecnica();
        if (dt != null) {
            dt.setStatus(StatusDemandaTecnica.E.getCodigo());
            demandaRepository.save(dt);
        }

        return TermoPlanejamentoDTO.fromEntity(saved);

    }
    
    @Transactional
    public void delete(Long id) {
        if (!termoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Termo de planejamento não encontrado com ID: " + id);
        }
        termoRepository.deleteById(id);
    }
}
