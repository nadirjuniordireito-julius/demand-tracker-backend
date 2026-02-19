package com.demandtracker.service;

import com.demandtracker.dto.BubbleNodeDto;
import com.demandtracker.entity.*;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de domínio para cálculo de saúde do TED (Projeto).
 * Apenas leitura + cálculo - não altera tabelas nem fluxos.
 */
@Service
@RequiredArgsConstructor
public class TedHealthService {

    private final ProjetoRepository projetoRepository;
    private final ProjetoMetaRepository projetoMetaRepository;
    private final MetaProdutoRepository metaProdutoRepository;
    private final DemandaTecnicaRepository demandaRepository;

    private static final String VERDE = "#22c55e";
    private static final String AMARELO = "#eab308";
    private static final String VERMELHO = "#ef4444";

    @Transactional(readOnly = true)
    public BubbleNodeDto calculateHealth(Long tedId) {
        Projeto projeto = projetoRepository.findById(tedId)
                .orElseThrow(() -> new ResourceNotFoundException("TED/Projeto não encontrado com ID: " + tedId));

        List<ProjetoMeta> metas = projetoMetaRepository.findByProjetoId(tedId, Pageable.unpaged()).getContent();
        List<DemandaTecnica> demandas = demandaRepository.findByProjetoId(tedId, Pageable.unpaged()).getContent();

        // Carrega produtos por meta
        for (ProjetoMeta meta : metas) {
            List<MetaProduto> produtos = metaProdutoRepository.findByProjetoMetaId(meta.getId(), Pageable.unpaged()).getContent();
            meta.setProdutos(produtos);
        }

        // Agrega dados por demanda (planejamento e apontamentos reais)
        List<DemandaAggregate> demandaAggregates = new ArrayList<>();
        for (DemandaTecnica demanda : demandas) {
            DemandaAggregate agg = aggregateDemanda(demanda, projeto.getTermoFinal());
            demandaAggregates.add(agg);
        }

        // Agrupa demandas por metaProduto
        return buildTedNode(projeto, metas, demandaAggregates);
    }

    private DemandaAggregate aggregateDemanda(DemandaTecnica demanda, LocalDate dataPrevistaProjeto) {
        BigDecimal valorPlanejado = BigDecimal.ZERO;
        BigDecimal valorRealizado = BigDecimal.ZERO;
        BigDecimal horasPlanejadas = BigDecimal.ZERO;
        BigDecimal horasRealizadas = BigDecimal.ZERO;
        LocalDate dataFimExecucaoPlanejada = null;
        LocalDate dataReal = null;

        if (demanda.getTermoPlanejamento() != null && demanda.getTermoPlanejamento().getCustos() != null) {
            for (TermoPlanejamentoCusto c : demanda.getTermoPlanejamento().getCustos()) {
                BigDecimal valor = c.getQtdeHora().multiply(c.getValorHora());
                valorPlanejado = valorPlanejado.add(valor);
                horasPlanejadas = horasPlanejadas.add(c.getQtdeHora());
            }
            if (demanda.getTermoPlanejamento().getDataFimExecucao() != null) {
                dataFimExecucaoPlanejada = demanda.getTermoPlanejamento().getDataFimExecucao();
            }
        }
        if (dataFimExecucaoPlanejada == null) {
            dataFimExecucaoPlanejada = dataPrevistaProjeto;
        }

        if (demanda.getTermoEncerramento() != null && demanda.getTermoEncerramento().getCustos() != null) {
            for (TermoEncerramentoCusto c : demanda.getTermoEncerramento().getCustos()) {
                BigDecimal valor = c.getQtdeHora().multiply(c.getValorHora());
                valorRealizado = valorRealizado.add(valor);
                horasRealizadas = horasRealizadas.add(c.getQtdeHora());
            }
            TermoEncerramento te = demanda.getTermoEncerramento();
            if (te.getDataFimExecucao() != null) {
                dataReal = te.getDataFimExecucao();
            } else if (te.getDataAssinatura() != null) {
                dataReal = te.getDataAssinatura().toLocalDate();
            }
        }

        return new DemandaAggregate(demanda, valorPlanejado, valorRealizado, horasPlanejadas, horasRealizadas,
                dataFimExecucaoPlanejada, dataReal);
    }

    private BubbleNodeDto buildTedNode(Projeto projeto, List<ProjetoMeta> metas, List<DemandaAggregate> demandaAggregates) {
        // TED tem lista de Metas, Meta tem lista de Produtos, Produto tem lista de Demandas
        List<DemandaAggregate> demandasComProduto = demandaAggregates.stream()
                .filter(da -> da.demanda.getMetaProduto() != null)
                .collect(Collectors.toList());

        BigDecimal valueTed = sumValorRealizado(demandasComProduto);
        BigDecimal valorPlanejadoTed = sumValorPlanejado(demandasComProduto);
        BigDecimal valorRealizadoTed = valueTed;
        BigDecimal horasPlanejadasTed = sumHorasPlanejadas(demandasComProduto);
        BigDecimal horasRealizadasTed = sumHorasRealizadas(demandasComProduto);

        List<BubbleNodeDto> children = new ArrayList<>();

        for (ProjetoMeta meta : metas) {
            BigDecimal valueMeta = BigDecimal.ZERO;
            BigDecimal valorPlanejadoMeta = BigDecimal.ZERO;
            BigDecimal valorRealizadoMeta = BigDecimal.ZERO;
            BigDecimal horasPlanejadasMeta = BigDecimal.ZERO;
            BigDecimal horasRealizadasMeta = BigDecimal.ZERO;
            List<BubbleNodeDto> produtoNodes = new ArrayList<>();

            for (MetaProduto produto : meta.getProdutos() != null ? meta.getProdutos() : List.<MetaProduto>of()) {
                List<DemandaAggregate> demandasDoProduto = demandasComProduto.stream()
                        .filter(da -> da.demanda.getMetaProduto().getId().equals(produto.getId()))
                        .collect(Collectors.toList());

                // Só inclui produto se tiver ao menos uma demanda vinculada
                if (demandasDoProduto.isEmpty()) continue;

                BubbleNodeDto produtoNode = buildProdutoNode(produto, demandasDoProduto, valueMeta);
                produtoNodes.add(produtoNode);

                valueMeta = valueMeta.add(sumValorRealizado(demandasDoProduto));
                valorPlanejadoMeta = valorPlanejadoMeta.add(sumValorPlanejado(demandasDoProduto));
                valorRealizadoMeta = valorRealizadoMeta.add(sumValorRealizado(demandasDoProduto));
                horasPlanejadasMeta = horasPlanejadasMeta.add(sumHorasPlanejadas(demandasDoProduto));
                horasRealizadasMeta = horasRealizadasMeta.add(sumHorasRealizadas(demandasDoProduto));
            }

            // Para nível meta: valor total previsto = soma (valorUnitario * quantidade) de todos os produtos da meta
            BigDecimal valorTotalPrevistoMeta = BigDecimal.ZERO;
            for (MetaProduto produto : meta.getProdutos() != null ? meta.getProdutos() : List.<MetaProduto>of()) {
                if (produto.getValorUnitario() != null && produto.getQuantidade() != null) {
                    valorTotalPrevistoMeta = valorTotalPrevistoMeta.add(
                            produto.getValorUnitario().multiply(BigDecimal.valueOf(produto.getQuantidade())));
                }
            }
            // Valor total executado = soma dos custos realizados das demandas com termo de encerramento vinculadas aos produtos da meta
            BigDecimal valorTotalExecutadoMeta = valorRealizadoMeta;
            Double percentualExecucaoMeta = null;
            if (valorTotalPrevistoMeta.compareTo(BigDecimal.ZERO) > 0) {
                double perc = valorTotalExecutadoMeta.compareTo(BigDecimal.ZERO) == 0 ? 0
                        : valorTotalExecutadoMeta.multiply(BigDecimal.valueOf(100)).divide(valorTotalPrevistoMeta, 2, java.math.RoundingMode.HALF_UP).doubleValue();
                percentualExecucaoMeta = Math.min(100.0, perc);
            }

            BubbleNodeDto metaNode = buildMetaNode(meta, produtoNodes, valorPlanejadoMeta, valorRealizadoMeta,
                    horasPlanejadasMeta, horasRealizadasMeta, valueTed,
                    valorTotalPrevistoMeta, valorTotalExecutadoMeta, percentualExecucaoMeta);
            children.add(metaNode);
        }

        return buildNode(projeto.getId(), null, projeto.getNome(), "ted", valorPlanejadoTed, valorRealizadoTed,
                horasPlanejadasTed, horasRealizadasTed, projeto.getTermoFinal(), null, null, children, null, null, null, null, null, null);
    }

    private BubbleNodeDto buildMetaNode(ProjetoMeta meta, List<BubbleNodeDto> produtoNodes,
                                        BigDecimal valorPlanejadoMeta, BigDecimal valorRealizadoMeta,
                                        BigDecimal horasPlanejadasMeta, BigDecimal horasRealizadasMeta,
                                        BigDecimal parentValue,
                                        BigDecimal valorTotalPrevisto, BigDecimal valorTotalExecutado, Double percentualExecucao) {
        BubbleNodeDto dto = buildNode(meta.getId(), meta.getCodigo(), meta.getNome(), "meta", valorPlanejadoMeta, valorRealizadoMeta,
                horasPlanejadasMeta, horasRealizadasMeta, null, null, parentValue, produtoNodes, valorTotalPrevisto, valorTotalExecutado, percentualExecucao, null, null, null);
        return dto;
    }

    private BubbleNodeDto buildProdutoNode(MetaProduto produto, List<DemandaAggregate> demandasDoProduto, BigDecimal parentValue) {
        BigDecimal valorPlanejadoProduto = sumValorPlanejado(demandasDoProduto);
        BigDecimal valorRealizadoProduto = sumValorRealizado(demandasDoProduto);
        BigDecimal horasPlanejadasProduto = sumHorasPlanejadas(demandasDoProduto);
        BigDecimal horasRealizadasProduto = sumHorasRealizadas(demandasDoProduto);
        List<BubbleNodeDto> demandaNodes = new ArrayList<>();

        for (DemandaAggregate da : demandasDoProduto) {
            demandaNodes.add(buildDemandaNode(da, valorRealizadoProduto));
        }

        // Para nível produto: valor total previsto = valorUnitario * quantidade do produto
        BigDecimal valorTotalPrevistoProduto = BigDecimal.ZERO;
        if (produto.getValorUnitario() != null && produto.getQuantidade() != null) {
            valorTotalPrevistoProduto = produto.getValorUnitario().multiply(BigDecimal.valueOf(produto.getQuantidade()));
        }
        // Valor total já executado = somatória de todas as demandas encerradas (status G) do produto
        List<DemandaAggregate> demandasEncerradasProduto = demandasDoProduto.stream()
                .filter(da -> DemandaTecnica.STATUS_ENCERRADA.equals(da.demanda.getStatus()))
                .collect(Collectors.toList());
        BigDecimal valorTotalExecutadoProduto = sumValorRealizado(demandasEncerradasProduto);
        Double percentualExecucaoProduto = null;
        if (valorTotalPrevistoProduto.compareTo(BigDecimal.ZERO) > 0) {
            double perc = valorTotalExecutadoProduto.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : valorTotalExecutadoProduto.multiply(BigDecimal.valueOf(100)).divide(valorTotalPrevistoProduto, 2, java.math.RoundingMode.HALF_UP).doubleValue();
            percentualExecucaoProduto = Math.min(100.0, perc);
        }

        return buildNode(produto.getId(), produto.getCodigo(), produto.getNome(), "produto", valorPlanejadoProduto, valorRealizadoProduto,
                horasPlanejadasProduto, horasRealizadasProduto, null, null, parentValue, demandaNodes,
                valorTotalPrevistoProduto, valorTotalExecutadoProduto, percentualExecucaoProduto, null, null, null);
    }

    private BubbleNodeDto buildDemandaNode(DemandaAggregate da, BigDecimal parentValue) {
        // Para nível demanda: valor previsto = termo planejamento; valor executado = termo encerramento; percentual, status, codigoMeta, codigoProduto
        BigDecimal valorPrevistoDemanda = da.valorPlanejado;
        BigDecimal valorExecutadoDemanda = da.valorRealizado;
        Double percentualExecucaoDemanda = null;
        if (valorPrevistoDemanda != null && valorPrevistoDemanda.compareTo(BigDecimal.ZERO) > 0) {
            double perc = valorExecutadoDemanda == null || valorExecutadoDemanda.compareTo(BigDecimal.ZERO) == 0 ? 0
                    : valorExecutadoDemanda.multiply(BigDecimal.valueOf(100)).divide(valorPrevistoDemanda, 2, java.math.RoundingMode.HALF_UP).doubleValue();
            percentualExecucaoDemanda = Math.min(100.0, perc);
        }
        String statusDemanda = da.demanda.getStatus();
        String codigoMetaDemanda = null;
        String codigoProdutoDemanda = null;
        if (da.demanda.getMetaProduto() != null) {
            codigoProdutoDemanda = da.demanda.getMetaProduto().getCodigo();
            if (da.demanda.getMetaProduto().getProjetoMeta() != null) {
                codigoMetaDemanda = da.demanda.getMetaProduto().getProjetoMeta().getCodigo();
            }
        }
        return buildNode(da.demanda.getId(), da.demanda.getCodigo(), da.demanda.getNome(), "demanda",
                da.valorRealizado, da.valorRealizado, da.horasPlanejadas, da.horasRealizadas,
                da.dataPrevista, da.dataReal, parentValue, null,
                valorPrevistoDemanda, valorExecutadoDemanda, percentualExecucaoDemanda, statusDemanda,
                codigoMetaDemanda, codigoProdutoDemanda);
    }

    private BubbleNodeDto buildNode(Long id, String codigo, String name, String level,
                                    BigDecimal valorPlanejado, BigDecimal valorRealizado,
                                    BigDecimal horasPlanejadas, BigDecimal horasRealizadas,
                                    LocalDate dataPrevista, LocalDate dataReal,
                                    BigDecimal parentValue, List<BubbleNodeDto> children,
                                    BigDecimal valorTotalPrevisto, BigDecimal valorTotalExecutado, Double percentualExecucao,
                                    String status, String codigoMeta, String codigoProduto) {
        BubbleNodeDto dto = new BubbleNodeDto();
        dto.setId(id);
        dto.setCodigo(codigo);
        dto.setName(name);
        dto.setLevel(level);
        dto.setValue(valorRealizado != null ? valorRealizado : BigDecimal.ZERO);
        dto.setChildren(children != null ? children : List.of());
        dto.setValorTotalPrevisto(valorTotalPrevisto);
        dto.setValorTotalExecutado(valorTotalExecutado);
        dto.setPercentualExecucao(percentualExecucao);
        dto.setStatus(status);
        dto.setCodigoMeta(codigoMeta);
        dto.setCodigoProduto(codigoProduto);

        double horasPlan = horasPlanejadas != null ? horasPlanejadas.doubleValue() : 0;
        double horasReal = horasRealizadas != null ? horasRealizadas.doubleValue() : 0;
        double valorPlan = valorPlanejado != null ? valorPlanejado.doubleValue() : 0;
        double valorReal = valorRealizado != null ? valorRealizado.doubleValue() : 0;

        double deviation = horasReal - horasPlan;
        dto.setDeviation(deviation);

        double fillPercent = valorPlan > 0 ? (valorReal / valorPlan) * 100 : 0;
        dto.setFillPercent(fillPercent);

        // Demanda encerrada: atrasado se data real fim > dataFimExecucao planejada (TermoPlanejamento)
        // Demanda não encerrada: atrasado se hoje > dataFimExecucao planejada (TermoPlanejamento)
        boolean atrasado = (dataReal != null && dataPrevista != null && dataReal.isAfter(dataPrevista))
                || (dataReal == null && dataPrevista != null && LocalDate.now().isAfter(dataPrevista));
        String statusColor = computeStatusColor(dataPrevista, dataReal, atrasado);
        dto.setStatusColor(statusColor);

        double valueNode = dto.getValue().doubleValue();
        double impactoNoPai = (parentValue != null && parentValue.doubleValue() > 0)
                ? valueNode / parentValue.doubleValue() : 0;
        dto.setImpactoNoPai(impactoNoPai);

        double ist = computeIst(atrasado, deviation, valorPlan, valorReal);
        dto.setIst(ist);

        return dto;
    }

    private BigDecimal sumValorPlanejado(List<DemandaAggregate> list) {
        return list.stream().map(da -> da.valorPlanejado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumValorRealizado(List<DemandaAggregate> list) {
        return list.stream().map(da -> da.valorRealizado).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumHorasPlanejadas(List<DemandaAggregate> list) {
        return list.stream().map(da -> da.horasPlanejadas).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumHorasRealizadas(List<DemandaAggregate> list) {
        return list.stream().map(da -> da.horasRealizadas).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String computeStatusColor(LocalDate dataPrevista, LocalDate dataReal, boolean atrasado) {
        if (!atrasado) return VERDE;
        if (dataPrevista == null) return AMARELO;
        LocalDate dataRef = dataReal != null ? dataReal : LocalDate.now();
        long diasAtraso = ChronoUnit.DAYS.between(dataPrevista, dataRef);
        if (diasAtraso <= 7) return AMARELO;
        return VERMELHO;
    }

    private double computeIst(boolean atrasado, double deviation, double valorPlan, double valorReal) {
        double prazoScore = atrasado ? 40 : 100;
        double esforcoScore = 100 - Math.min(100, Math.abs(deviation));
        double financeiroScore = valorPlan > 0
                ? 100 - Math.min(100, Math.abs(valorReal - valorPlan) / valorPlan * 100)
                : 100;
        return prazoScore * 0.4 + esforcoScore * 0.3 + financeiroScore * 0.3;
    }

    private record DemandaAggregate(DemandaTecnica demanda, BigDecimal valorPlanejado, BigDecimal valorRealizado,
                                   BigDecimal horasPlanejadas, BigDecimal horasRealizadas,
                                   LocalDate dataPrevista, LocalDate dataReal) {}
}
