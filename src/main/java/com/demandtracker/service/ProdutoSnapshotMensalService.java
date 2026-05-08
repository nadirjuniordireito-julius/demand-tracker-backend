package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.MetaProduto;
import com.demandtracker.entity.ProdutoSnapshotAcao;
import com.demandtracker.entity.ProdutoSnapshotMensal;
import com.demandtracker.entity.Usuario;
import com.demandtracker.entity.enums.ImpactoAcao;
import com.demandtracker.entity.enums.StatusAcaoProduto;
import com.demandtracker.entity.enums.StatusProdutoMes;
import com.demandtracker.repository.TermoEncerramentoRepository;
import com.demandtracker.repository.TermoPlanejamentoRepository;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.MetaProdutoRepository;
import com.demandtracker.repository.ProdutoSnapshotAcaoRepository;
import com.demandtracker.repository.ProdutoSnapshotMensalRepository;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regras de negócio do snapshot mensal por produto e suas ações vinculadas.
 *
 * <p>Regras principais:
 * <ul>
 *   <li>Único snapshot por (produto, ano, mês).</li>
 *   <li>Quando {@code fechado=true} o snapshot é imutável; ações continuam evoluindo.</li>
 *   <li>Reabertura é uma operação dedicada e gravita auditoria (quem/quando).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ProdutoSnapshotMensalService {

    private static final List<StatusAcaoProduto> STATUS_ATIVOS_ACAO = List.of(
            StatusAcaoProduto.ABERTA,
            StatusAcaoProduto.EM_ANDAMENTO
    );

    private final ProdutoSnapshotMensalRepository snapshotRepository;
    private final ProdutoSnapshotAcaoRepository acaoRepository;
    private final MetaProdutoRepository metaProdutoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetaProdutoService metaProdutoService;
    private final TermoPlanejamentoRepository termoPlanejamentoRepository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;

    // ---------------------------------------------------------------------
    // Consultas (snapshots)
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<ProdutoSnapshotMensalDTO> findAll(Long metaProdutoId, Integer ano, Integer mes, Pageable pageable) {
        Page<ProdutoSnapshotMensal> page;
        if (metaProdutoId != null && ano != null && mes != null) {
            page = snapshotRepository.findByMetaProdutoIdAndAnoAndMes(metaProdutoId, ano, mes, pageable);
        } else if (metaProdutoId != null && ano != null) {
            page = snapshotRepository.findByMetaProdutoIdAndAno(metaProdutoId, ano, pageable);
        } else if (metaProdutoId != null) {
            page = snapshotRepository.findByMetaProdutoId(metaProdutoId, pageable);
        } else if (ano != null && mes != null) {
            page = snapshotRepository.findByAnoAndMes(ano, mes, pageable);
        } else if (ano != null) {
            page = snapshotRepository.findByAno(ano, pageable);
        } else if (mes != null) {
            page = snapshotRepository.findByMes(mes, pageable);
        } else {
            page = snapshotRepository.findAll(pageable);
        }
        return page.map(ProdutoSnapshotMensalDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProdutoSnapshotMensalDTO findById(Long id) {
        return ProdutoSnapshotMensalDTO.fromEntity(getSnapshotOrThrow(id));
    }

    // ---------------------------------------------------------------------
    // Comandos (snapshots)
    // ---------------------------------------------------------------------

    @Transactional
    public ProdutoSnapshotMensalDTO create(ProdutoSnapshotMensalCreateDTO dto) {
        MetaProduto produto = metaProdutoRepository.findById(dto.getMetaProdutoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Produto da meta não encontrado com ID: " + dto.getMetaProdutoId()));

        if (snapshotRepository.existsByMetaProdutoIdAndAnoAndMes(produto.getId(), dto.getAno(), dto.getMes())) {
            throw new BadRequestException(
                    "Já existe snapshot para o produto " + produto.getId()
                            + " no período " + dto.getMes() + "/" + dto.getAno());
        }

        ProdutoSnapshotMensal entity = new ProdutoSnapshotMensal();
        entity.setMetaProduto(produto);
        entity.setAno(dto.getAno());
        entity.setMes(dto.getMes());
        entity.setStatusProdutoMes(dto.getStatusProdutoMes());
        entity.setSituacao(dto.getSituacao());
        entity.setResumoAnalitico(dto.getResumoAnalitico());

        aplicarMetricasFromDtoOrSnapshot(entity, produto, dto);

        entity.setFechado(Boolean.FALSE);
        return ProdutoSnapshotMensalDTO.fromEntity(snapshotRepository.save(entity));
    }

    @Transactional
    public ProdutoSnapshotMensalDTO update(Long id, ProdutoSnapshotMensalUpdateDTO dto) {
        ProdutoSnapshotMensal entity = getSnapshotOrThrow(id);
        garantirSnapshotAberto(entity);

        if (dto.getStatusProdutoMes() != null) {
            entity.setStatusProdutoMes(dto.getStatusProdutoMes());
        }
        if (dto.getSituacao() != null) {
            entity.setSituacao(dto.getSituacao());
        }
        if (dto.getValorTotalOrcamento() != null) {
            entity.setValorTotalOrcamento(dto.getValorTotalOrcamento());
        }
        if (dto.getValorTotalEmExecucao() != null) {
            entity.setValorTotalEmExecucao(dto.getValorTotalEmExecucao());
        }
        if (dto.getValorTotalExecutado() != null) {
            entity.setValorTotalExecutado(dto.getValorTotalExecutado());
        }
        if (dto.getPercentualExecucao() != null) {
            entity.setPercentualExecucao(dto.getPercentualExecucao());
        }
        if (dto.getValorMediaEntregaPrevistaMensal() != null) {
            entity.setValorMediaEntregaPrevistaMensal(dto.getValorMediaEntregaPrevistaMensal());
        }
        if (dto.getValorMediaEntregaRealMensal() != null) {
            entity.setValorMediaEntregaRealMensal(dto.getValorMediaEntregaRealMensal());
        }
        if (dto.getResumoAnalitico() != null) {
            entity.setResumoAnalitico(dto.getResumoAnalitico());
        }
        return ProdutoSnapshotMensalDTO.fromEntity(snapshotRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        ProdutoSnapshotMensal entity = getSnapshotOrThrow(id);
        if (Boolean.TRUE.equals(entity.getFechado())) {
            throw new BadRequestException("Snapshot fechado não pode ser excluído. Reabra antes de excluir.");
        }
        long acoes = acaoRepository.countBySnapshotId(id);
        if (acoes > 0) {
            throw new BadRequestException(
                    "Snapshot possui " + acoes + " ação(ões) vinculada(s). Remova-as antes de excluir.");
        }
        snapshotRepository.delete(entity);
    }

    @Transactional
    public ProdutoSnapshotMensalDTO fechar(Long id) {
        ProdutoSnapshotMensal entity = getSnapshotOrThrow(id);
        if (Boolean.TRUE.equals(entity.getFechado())) {
            throw new BadRequestException("Snapshot já está fechado.");
        }
        entity.setFechado(Boolean.TRUE);
        entity.setDataFechamento(LocalDateTime.now());
        getUsuarioLogado().ifPresent(entity::setUsuarioFechamento);
        return ProdutoSnapshotMensalDTO.fromEntity(snapshotRepository.save(entity));
    }

    @Transactional
    public ProdutoSnapshotMensalDTO reabrir(Long id) {
        ProdutoSnapshotMensal entity = getSnapshotOrThrow(id);
        if (!Boolean.TRUE.equals(entity.getFechado())) {
            throw new BadRequestException("Snapshot já está aberto.");
        }
        entity.setFechado(Boolean.FALSE);
        entity.setDataFechamento(null);
        entity.setUsuarioFechamento(null);
        return ProdutoSnapshotMensalDTO.fromEntity(snapshotRepository.save(entity));
    }

    // ---------------------------------------------------------------------
    // Ações vinculadas
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ProdutoSnapshotAcaoDTO> findAcoesBySnapshotId(Long snapshotId) {
        getSnapshotOrThrow(snapshotId);
        return acaoRepository.findBySnapshotIdOrderByPrazoAsc(snapshotId).stream()
                .map(ProdutoSnapshotAcaoDTO::fromEntity)
                .toList();
    }

    @Transactional
    public ProdutoSnapshotAcaoDTO createAcao(Long snapshotId, ProdutoSnapshotAcaoCreateDTO dto) {
        ProdutoSnapshotMensal snapshot = getSnapshotOrThrow(snapshotId);
        ProdutoSnapshotAcao acao = new ProdutoSnapshotAcao();
        acao.setSnapshot(snapshot);
        acao.setTipoAcao(dto.getTipoAcao());
        acao.setDescricao(dto.getDescricao());
        acao.setPrazo(dto.getPrazo());
        acao.setImpacto(dto.getImpacto());
        acao.setStatusAcao(dto.getStatusAcao() != null ? dto.getStatusAcao() : StatusAcaoProduto.ABERTA);
        acao.setObservacaoStatus(dto.getObservacaoStatus());
        aplicarResponsavel(acao, dto.getResponsavelId(), dto.getResponsavelNome());
        return ProdutoSnapshotAcaoDTO.fromEntity(acaoRepository.save(acao));
    }

    @Transactional
    public ProdutoSnapshotAcaoDTO updateAcao(Long snapshotId, Long acaoId, ProdutoSnapshotAcaoUpdateDTO dto) {
        ProdutoSnapshotAcao acao = getAcaoOrThrow(snapshotId, acaoId);
        if (dto.getTipoAcao() != null) {
            acao.setTipoAcao(dto.getTipoAcao());
        }
        if (dto.getDescricao() != null) {
            acao.setDescricao(dto.getDescricao());
        }
        if (dto.getPrazo() != null) {
            acao.setPrazo(dto.getPrazo());
        }
        if (dto.getImpacto() != null) {
            acao.setImpacto(dto.getImpacto());
        }
        if (dto.getResponsavelId() != null || dto.getResponsavelNome() != null) {
            aplicarResponsavel(acao, dto.getResponsavelId(), dto.getResponsavelNome());
        }
        return ProdutoSnapshotAcaoDTO.fromEntity(acaoRepository.save(acao));
    }

    @Transactional
    public ProdutoSnapshotAcaoDTO updateAcaoStatus(Long snapshotId, Long acaoId, ProdutoSnapshotAcaoUpdateStatusDTO dto) {
        ProdutoSnapshotAcao acao = getAcaoOrThrow(snapshotId, acaoId);
        acao.setStatusAcao(dto.getStatusAcao());
        acao.setObservacaoStatus(dto.getObservacaoStatus());
        acao.setDataStatus(LocalDateTime.now());
        return ProdutoSnapshotAcaoDTO.fromEntity(acaoRepository.save(acao));
    }

    @Transactional
    public void deleteAcao(Long snapshotId, Long acaoId) {
        ProdutoSnapshotAcao acao = getAcaoOrThrow(snapshotId, acaoId);
        acaoRepository.delete(acao);
    }

    // ---------------------------------------------------------------------
    // Relatório gerencial
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ProdutoSnapshotRelatorioGestorDTO getRelatorioGestor(Integer ano, Integer mes, Long projetoId) {
        if (ano == null || mes == null) {
            throw new BadRequestException("Ano e mês são obrigatórios para o relatório gerencial.");
        }

        List<ProdutoSnapshotMensal> snapshots = snapshotRepository.findRelatorioGestor(ano, mes, projetoId);
        List<Long> snapshotIds = snapshots.stream().map(ProdutoSnapshotMensal::getId).toList();
        Map<Long, List<ProdutoSnapshotAcao>> acoesPorSnapshot = new HashMap<>();
        if (!snapshotIds.isEmpty()) {
            for (ProdutoSnapshotAcao a : acaoRepository.findBySnapshotIdIn(snapshotIds)) {
                acoesPorSnapshot
                        .computeIfAbsent(a.getSnapshot().getId(), k -> new ArrayList<>())
                        .add(a);
            }
        }

        LocalDate hoje = LocalDate.now();
        List<ProdutoSnapshotRelatorioGestorItemDTO> itens = new ArrayList<>();
        for (ProdutoSnapshotMensal s : snapshots) {
            itens.add(montarItemRelatorio(
                    s,
                    acoesPorSnapshot.getOrDefault(s.getId(), Collections.emptyList()),
                    hoje
            ));
        }

        ProdutoSnapshotRelatorioGestorResumoDTO resumo = montarResumoRelatorio(itens);

        List<ProdutoSnapshotAcaoDTO> acoesVencidas = acaoRepository
                .findVencidas(ano, mes, projetoId, STATUS_ATIVOS_ACAO, hoje).stream()
                .map(ProdutoSnapshotAcaoDTO::fromEntity)
                .toList();

        List<ProdutoSnapshotRelatorioGestorItemDTO> criticos = itens.stream()
                .filter(i -> i.getStatusProdutoMes() == StatusProdutoMes.R
                        || (i.getStatusProdutoMes() == StatusProdutoMes.A && i.getAcoesVencidas() != null && i.getAcoesVencidas() > 0))
                .toList();

        ProdutoSnapshotRelatorioGestorDTO out = new ProdutoSnapshotRelatorioGestorDTO();
        out.setAno(ano);
        out.setMes(mes);
        out.setProjetoId(projetoId);
        out.setResumo(resumo);
        out.setProdutos(itens);
        out.setAcoesVencidas(acoesVencidas);
        out.setProdutosCriticos(criticos);
        return out;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private ProdutoSnapshotMensal getSnapshotOrThrow(Long id) {
        return snapshotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Snapshot mensal não encontrado com ID: " + id));
    }

    private ProdutoSnapshotAcao getAcaoOrThrow(Long snapshotId, Long acaoId) {
        ProdutoSnapshotAcao acao = acaoRepository.findById(acaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Ação não encontrada com ID: " + acaoId));
        if (acao.getSnapshot() == null || !snapshotId.equals(acao.getSnapshot().getId())) {
            throw new BadRequestException("Ação " + acaoId + " não pertence ao snapshot " + snapshotId + ".");
        }
        return acao;
    }

    private void garantirSnapshotAberto(ProdutoSnapshotMensal entity) {
        if (Boolean.TRUE.equals(entity.getFechado())) {
            throw new BadRequestException(
                    "Snapshot está fechado e não pode ser alterado. Reabra-o (perfil gestor) para editar.");
        }
    }

    private void aplicarResponsavel(ProdutoSnapshotAcao acao, Long responsavelId, String responsavelNome) {
        if (responsavelId != null) {
            Usuario u = usuarioRepository.findById(responsavelId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Usuário responsável não encontrado com ID: " + responsavelId));
            acao.setResponsavel(u);
            if (responsavelNome != null && !responsavelNome.isBlank()) {
                acao.setResponsavelNome(responsavelNome);
            } else {
                acao.setResponsavelNome(null);
            }
        } else {
            acao.setResponsavel(null);
            acao.setResponsavelNome(responsavelNome);
        }
    }

    /**
     * Preenche métricas do snapshot. Quando o DTO traz valores, prioriza-os.
     * Quando algum valor não é informado, calcula o resumo temporalizado no
     * período (ano/mês) do snapshot, congelando os números como estavam no fim
     * daquele mês.
     */
    private void aplicarMetricasFromDtoOrSnapshot(ProdutoSnapshotMensal entity,
                                                  MetaProduto produto,
                                                  ProdutoSnapshotMensalCreateDTO dto) {
        ProdutoResumoDTO resumo = null;
        if (dto.getValorTotalOrcamento() == null
                || dto.getValorTotalEmExecucao() == null
                || dto.getValorTotalExecutado() == null
                || dto.getPercentualExecucao() == null
                || dto.getValorMediaEntregaPrevistaMensal() == null
                || dto.getValorMediaEntregaRealMensal() == null) {
            try {
                resumo = montarProdutoResumoAteMes(produto, dto.getAno(), dto.getMes());
            } catch (Exception ignored) {
                // Em caso de falha no cálculo do resumo, persiste apenas os valores informados.
            }
        }

        entity.setValorTotalOrcamento(coalesce(dto.getValorTotalOrcamento(),
                resumo != null ? resumo.getValorTotalOrcamento() : null));
        entity.setValorTotalEmExecucao(coalesce(dto.getValorTotalEmExecucao(),
                resumo != null ? resumo.getValorTotalEmExecucao() : null));
        entity.setValorTotalExecutado(coalesce(dto.getValorTotalExecutado(),
                resumo != null ? resumo.getValorTotalExecutado() : null));
        entity.setPercentualExecucao(coalesce(dto.getPercentualExecucao(),
                resumo != null ? resumo.getPercentualExecucao() : null));
        entity.setValorMediaEntregaPrevistaMensal(coalesce(dto.getValorMediaEntregaPrevistaMensal(),
                resumo != null ? resumo.getValorMediaEntregaPrevistaMensal() : null));
        entity.setValorMediaEntregaRealMensal(coalesce(dto.getValorMediaEntregaRealMensal(),
                resumo != null ? resumo.getValorMediaEntregaRealMensal() : null));

        if (entity.getSituacao() == null && resumo != null) {
            entity.setSituacao(resumo.getSituacao());
        }
    }

    /**
     * Monta um resumo do produto "congelado" até o último instante do mês
     * informado, para garantir a fotografia histórica do snapshot.
     */
    private ProdutoResumoDTO montarProdutoResumoAteMes(MetaProduto produto, Integer ano, Integer mes) {
        LocalDate dataFimCorte = YearMonth.of(ano, mes).atEndOfMonth();
        LocalDateTime dataFimCorteDateTime = dataFimCorte.atTime(LocalTime.MAX);
        LocalDateTime dataInicioCalculo = LocalDateTime.of(1900, 1, 1, 0, 0, 0);

        BigDecimal valorTotalOrcamento = calcularValorTotalOrcamento(produto);
        BigDecimal valorTotalPlanejadoAteCorte = termoPlanejamentoRepository
                .sumValorPlanejadoByMetaProdutoIdAndDataAberturaBetween(
                        produto.getId(),
                        dataInicioCalculo,
                        dataFimCorteDateTime
                );
        BigDecimal valorTotalExecutado = termoEncerramentoRepository
                .sumValorExecutadoByMetaProdutoIdAndDataTermoBetween(
                        produto.getId(),
                        dataInicioCalculo,
                        dataFimCorteDateTime
                );

        if (valorTotalPlanejadoAteCorte == null) {
            valorTotalPlanejadoAteCorte = BigDecimal.ZERO;
        }
        if (valorTotalExecutado == null) {
            valorTotalExecutado = BigDecimal.ZERO;
        }
        BigDecimal valorTotalEmExecucao = valorTotalPlanejadoAteCorte.subtract(valorTotalExecutado);
        if (valorTotalEmExecucao.compareTo(BigDecimal.ZERO) < 0) {
            valorTotalEmExecucao = BigDecimal.ZERO;
        }

        BigDecimal percentualExecucao = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (valorTotalOrcamento.compareTo(BigDecimal.ZERO) > 0) {
            percentualExecucao = valorTotalExecutado
                    .multiply(BigDecimal.valueOf(100))
                    .divide(valorTotalOrcamento, 2, RoundingMode.HALF_UP);
        }

        int mesesPrevistosExecucao = calcularMesesPrevistosExecucao(produto.getDataInicio(), produto.getDataFim());
        int mesesExecucao = calcularMesesExecucaoAteCorte(produto.getId(), dataFimCorte);
        BigDecimal valorMediaEntregaPrevistaMensal = dividirPorMeses(valorTotalOrcamento, mesesPrevistosExecucao);
        BigDecimal valorMediaEntregaRealMensal = dividirPorMeses(
                valorTotalEmExecucao.add(valorTotalExecutado),
                mesesExecucao
        );

        ProdutoResumoDTO resumo = new ProdutoResumoDTO();
        resumo.setIdMeta(produto.getProjetoMeta() != null ? produto.getProjetoMeta().getId() : null);
        resumo.setCodigoMeta(produto.getProjetoMeta() != null ? produto.getProjetoMeta().getCodigo() : null);
        resumo.setNomeMeta(produto.getProjetoMeta() != null ? produto.getProjetoMeta().getNome() : null);
        resumo.setIdProduto(produto.getId());
        resumo.setCodigoProduto(produto.getCodigo());
        resumo.setNomeProduto(produto.getNome());
        resumo.setSituacao("");
        resumo.setInicioPrevisaoExecucao(produto.getDataInicio());
        resumo.setFimPrevisaoExecucao(produto.getDataFim());
        resumo.setMesesPrevistosExecucao(mesesPrevistosExecucao);
        resumo.setValorTotalOrcamento(valorTotalOrcamento);
        resumo.setValorTotalEmExecucao(valorTotalEmExecucao.setScale(2, RoundingMode.HALF_UP));
        resumo.setValorTotalExecutado(valorTotalExecutado.setScale(2, RoundingMode.HALF_UP));
        resumo.setPercentualExecucao(percentualExecucao);
        resumo.setValorMediaEntregaPrevistaMensal(valorMediaEntregaPrevistaMensal);
        resumo.setValorMediaEntregaRealMensal(valorMediaEntregaRealMensal);
        return resumo;
    }

    private static BigDecimal coalesce(BigDecimal a, BigDecimal b) {
        return a != null ? a : b;
    }

    private int calcularMesesPrevistosExecucao(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            return 0;
        }
        return (int) ChronoUnit.MONTHS.between(
                inicio.withDayOfMonth(1),
                fim.withDayOfMonth(1)
        ) + 1;
    }

    private int calcularMesesExecucaoAteCorte(Long metaProdutoId, LocalDate dataFimCorte) {
        LocalDateTime primeiraDataPlanejamento = termoPlanejamentoRepository
                .findFirstDataAberturaByMetaProdutoId(metaProdutoId)
                .orElse(null);
        if (primeiraDataPlanejamento == null) {
            return 0;
        }
        YearMonth inicioExecucao = YearMonth.from(primeiraDataPlanejamento);
        YearMonth mesCorte = YearMonth.from(dataFimCorte);
        if (inicioExecucao.isAfter(mesCorte)) {
            return 0;
        }
        return (int) ChronoUnit.MONTHS.between(inicioExecucao, mesCorte) + 1;
    }

    private BigDecimal calcularValorTotalOrcamento(MetaProduto produto) {
        if (produto.getValorUnitario() == null || produto.getQuantidade() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return produto.getValorUnitario()
                .multiply(BigDecimal.valueOf(produto.getQuantidade()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal dividirPorMeses(BigDecimal valor, int meses) {
        if (meses <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return valor.divide(BigDecimal.valueOf(meses), 2, RoundingMode.HALF_UP);
    }

    private ProdutoSnapshotRelatorioGestorItemDTO montarItemRelatorio(
            ProdutoSnapshotMensal s,
            List<ProdutoSnapshotAcao> acoes,
            LocalDate hoje
    ) {
        ProdutoSnapshotRelatorioGestorItemDTO i = new ProdutoSnapshotRelatorioGestorItemDTO();
        i.setSnapshotId(s.getId());
        i.setAno(s.getAno());
        i.setMes(s.getMes());
        i.setStatusProdutoMes(s.getStatusProdutoMes());
        i.setFechado(s.getFechado());
        i.setPercentualExecucao(s.getPercentualExecucao());
        i.setValorTotalOrcamento(s.getValorTotalOrcamento());
        i.setValorTotalEmExecucao(s.getValorTotalEmExecucao());
        i.setValorTotalExecutado(s.getValorTotalExecutado());
        if (s.getMetaProduto() != null) {
            MetaProduto mp = s.getMetaProduto();
            i.setMetaProdutoId(mp.getId());
            i.setCodigoProduto(mp.getCodigo());
            i.setNomeProduto(mp.getNome());
            if (mp.getProjetoMeta() != null) {
                i.setProjetoMetaId(mp.getProjetoMeta().getId());
                i.setCodigoMeta(mp.getProjetoMeta().getCodigo());
                i.setNomeMeta(mp.getProjetoMeta().getNome());
                if (mp.getProjetoMeta().getProjeto() != null) {
                    i.setProjetoId(mp.getProjetoMeta().getProjeto().getId());
                    i.setNomeProjeto(mp.getProjetoMeta().getProjeto().getNome());
                }
            }
        }
        i.setTotalAcoes(acoes.size());
        i.setAcoesAbertas(0);
        i.setAcoesEmAndamento(0);
        i.setAcoesConcluidas(0);
        i.setAcoesCanceladas(0);
        i.setAcoesVencidas(0);
        i.setAcoesImpactoAlto(0);
        Set<StatusAcaoProduto> ativos = Set.of(StatusAcaoProduto.ABERTA, StatusAcaoProduto.EM_ANDAMENTO);
        for (ProdutoSnapshotAcao a : acoes) {
            switch (a.getStatusAcao()) {
                case ABERTA -> i.setAcoesAbertas(i.getAcoesAbertas() + 1);
                case EM_ANDAMENTO -> i.setAcoesEmAndamento(i.getAcoesEmAndamento() + 1);
                case CONCLUIDA -> i.setAcoesConcluidas(i.getAcoesConcluidas() + 1);
                case CANCELADA -> i.setAcoesCanceladas(i.getAcoesCanceladas() + 1);
            }
            if (a.getImpacto() == ImpactoAcao.A) {
                i.setAcoesImpactoAlto(i.getAcoesImpactoAlto() + 1);
            }
            if (ativos.contains(a.getStatusAcao()) && a.getPrazo() != null && a.getPrazo().isBefore(hoje)) {
                i.setAcoesVencidas(i.getAcoesVencidas() + 1);
            }
        }
        return i;
    }

    private ProdutoSnapshotRelatorioGestorResumoDTO montarResumoRelatorio(List<ProdutoSnapshotRelatorioGestorItemDTO> itens) {
        ProdutoSnapshotRelatorioGestorResumoDTO r = new ProdutoSnapshotRelatorioGestorResumoDTO();
        r.setTotalProdutos(itens.size());
        int verde = 0, amarelo = 0, vermelho = 0;
        int fechados = 0, abertos = 0;
        int total = 0, abertas = 0, andamento = 0, concluidas = 0, canceladas = 0, vencidas = 0, impactoAlto = 0;
        BigDecimal somaOrc = BigDecimal.ZERO;
        BigDecimal somaExec = BigDecimal.ZERO;
        for (ProdutoSnapshotRelatorioGestorItemDTO i : itens) {
            if (i.getStatusProdutoMes() == StatusProdutoMes.V) verde++;
            else if (i.getStatusProdutoMes() == StatusProdutoMes.A) amarelo++;
            else if (i.getStatusProdutoMes() == StatusProdutoMes.R) vermelho++;
            if (Boolean.TRUE.equals(i.getFechado())) fechados++; else abertos++;
            total += nz(i.getTotalAcoes());
            abertas += nz(i.getAcoesAbertas());
            andamento += nz(i.getAcoesEmAndamento());
            concluidas += nz(i.getAcoesConcluidas());
            canceladas += nz(i.getAcoesCanceladas());
            vencidas += nz(i.getAcoesVencidas());
            impactoAlto += nz(i.getAcoesImpactoAlto());
            if (i.getValorTotalOrcamento() != null) somaOrc = somaOrc.add(i.getValorTotalOrcamento());
            if (i.getValorTotalExecutado() != null) somaExec = somaExec.add(i.getValorTotalExecutado());
        }
        r.setProdutosVerde(verde);
        r.setProdutosAmarelo(amarelo);
        r.setProdutosVermelho(vermelho);
        r.setSnapshotsFechados(fechados);
        r.setSnapshotsAbertos(abertos);
        r.setTotalAcoes(total);
        r.setAcoesAbertas(abertas);
        r.setAcoesEmAndamento(andamento);
        r.setAcoesConcluidas(concluidas);
        r.setAcoesCanceladas(canceladas);
        r.setAcoesVencidas(vencidas);
        r.setAcoesImpactoAlto(impactoAlto);
        r.setSomaValorTotalOrcamento(somaOrc);
        r.setSomaValorTotalExecutado(somaExec);
        if (somaOrc.compareTo(BigDecimal.ZERO) > 0) {
            r.setPercentualExecucaoConsolidado(
                    somaExec.multiply(BigDecimal.valueOf(100))
                            .divide(somaOrc, 2, RoundingMode.HALF_UP)
            );
        } else {
            r.setPercentualExecucaoConsolidado(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        return r;
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private java.util.Optional<Usuario> getUsuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return java.util.Optional.empty();
        }
        return usuarioRepository.findByEmail(auth.getName());
    }
}
