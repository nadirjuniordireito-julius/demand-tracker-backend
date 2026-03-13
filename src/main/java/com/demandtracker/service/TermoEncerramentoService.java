package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.*;
import com.demandtracker.event.DemandaEncerradaEvent;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TermoEncerramentoService {
    
    private final TermoEncerramentoRepository termoRepository;
    private final TermoEncerramentoCustoRepository termoEncerramentoCustoRepository;
    private final TermoEncerramentoCustoProfissionalRepository termoEncerramentoCustoProfissionalRepository;
    private final TermoEncerramentoAnexoRepository termoEncerramentoAnexoRepository;
    private final DemandaTecnicaRepository demandaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final ProfissionalRepository profissionalRepository;
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

    /** Retorna o termo de encerramento da demanda se existir (para encerramento idempotente). */
    @Transactional(readOnly = true)
    public java.util.Optional<TermoEncerramentoDTO> findByDemandaIdOptional(Long demandaId) {
        return termoRepository.findByDemandaTecnicaId(demandaId)
            .map(termo -> {
                TermoEncerramentoDTO dto = TermoEncerramentoDTO.fromEntity(termo);
                preencherValoresProdutoNoModal(dto, termo.getDemandaTecnica());
                return dto;
            });
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
            throw new BadRequestException("Já existe termo de encerramento para esta demanda");
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
        
        TermoEncerramento saved = termoRepository.save(termo);

        if (dto.getCustos() != null && !dto.getCustos().isEmpty()) {
            List<TermoEncerramentoCusto> custos = new ArrayList<>();
            for (TermoEncerramentoCustoCreateDTO custoDTO : dto.getCustos()) {
                TermoEncerramentoCusto custo = montarCustoComProfissionais(saved, custoDTO);
                custos.add(termoEncerramentoCustoRepository.save(custo));
            }
            saved.setCustos(custos);
        }
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
            String status = termo.getDemandaTecnica() != null ? termo.getDemandaTecnica().getStatus() : null;
            if (!"E".equals(status)) {
                List<TermoEncerramentoCusto> custosAtuais = termo.getCustos() != null ? termo.getCustos() : List.of();
                Set<Long> perfisAtuais = custosAtuais.stream()
                        .map(c -> c.getPerfil() != null ? c.getPerfil().getId() : null)
                        .filter(pid -> pid != null)
                        .collect(Collectors.toSet());
                Set<Long> perfisNovos = dto.getCustos().stream()
                        .map(TermoEncerramentoCustoCreateDTO::getPerfilId)
                        .filter(pid -> pid != null)
                        .collect(Collectors.toSet());
                if (!perfisNovos.containsAll(perfisAtuais)) {
                    throw new BadRequestException(
                            "Não é permitido excluir um perfil da aba de custos quando o status da demanda técnica for diferente de 'E' (Planejado e assinado).");
                }
            }
            // Excluir primeiro profissionais (FK para custo), depois custos; depois incluir novos
            termoEncerramentoCustoProfissionalRepository.deleteByTermoEncerramentoId(id);
            termoEncerramentoCustoRepository.deleteByTermoEncerramentoId(id);
            termoEncerramentoCustoRepository.flush();
            List<TermoEncerramentoCusto> novosCustos = new ArrayList<>();
            for (TermoEncerramentoCustoCreateDTO custoDTO : dto.getCustos()) {
                TermoEncerramentoCusto custo = montarCustoComProfissionais(termo, custoDTO);
                novosCustos.add(termoEncerramentoCustoRepository.save(custo));
            }
            termo.setCustos(novosCustos);
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
        // Se a demanda estiver com status 'E', exclui automaticamente os anexos do termo
        if ("E".equals(termo.getDemandaTecnica().getStatus())) {
            termoEncerramentoAnexoRepository.deleteByTermoEncerramentoId(id);
        }
        termoRepository.deleteById(id);
        if (metaProdutoId != null) {
            metaProdutoService.recalculatePercExecutado(metaProdutoId);
        }
    }

    /**
     * Monta um TermoEncerramentoCusto com sua lista de profissionais a partir do DTO.
     * Compoção em uma única chamada: custo + termos_encerramento_custos_profissionais.
     */
    private TermoEncerramentoCusto montarCustoComProfissionais(TermoEncerramento termo, TermoEncerramentoCustoCreateDTO custoDTO) {
        Perfil perfil = perfilRepository.findById(custoDTO.getPerfilId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + custoDTO.getPerfilId()));

        TermoEncerramentoCusto custo = new TermoEncerramentoCusto();
        custo.setTermoEncerramento(termo);
        custo.setPerfil(perfil);
        custo.setQtdeHora(custoDTO.getQtdeHora());
        custo.setValorHora(custoDTO.getValorHora());

        if (custoDTO.getProfissionais() != null && !custoDTO.getProfissionais().isEmpty()) {
            for (TermoEncerramentoCustoProfissionalItemDTO item : custoDTO.getProfissionais()) {
                Profissional profissional = profissionalRepository.findById(item.getProfissionalId())
                        .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + item.getProfissionalId()));
                TermoEncerramentoCustoProfissional cp = new TermoEncerramentoCustoProfissional();
                cp.setTermoEncerramentoCusto(custo);
                cp.setProfissional(profissional);
                cp.setQtdeHora(item.getQtdeHora());
                cp.setValorHora(item.getValorHora());
                custo.getProfissionais().add(cp);
            }
        }

        return custo;
    }
}
