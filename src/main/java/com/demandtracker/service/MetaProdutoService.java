package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.MetaProduto;
import com.demandtracker.entity.ProjetoMeta;
import com.demandtracker.entity.enums.StatusDemandaTecnica;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.MetaProdutoRepository;
import com.demandtracker.repository.ProjetoMetaRepository;
import com.demandtracker.repository.TermoPlanejamentoRepository;
import com.demandtracker.repository.TermoEncerramentoCustoRepository;
import com.demandtracker.repository.TermoEncerramentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetaProdutoService {

    private final MetaProdutoRepository metaProdutoRepository;
    private final ProjetoMetaRepository projetoMetaRepository;
    private final TermoEncerramentoCustoRepository termoEncerramentoCustoRepository;
    private final TermoPlanejamentoRepository termoPlanejamentoRepository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;

    public Page<MetaProdutoDTO> findAll(String codigo, String nome, Long projetoMetaId, String status, Pageable pageable) {
        Page<MetaProduto> produtos;

        if (codigo != null && projetoMetaId != null) {
            produtos = metaProdutoRepository.findByProjetoMetaIdAndCodigoContainingIgnoreCase(projetoMetaId, codigo, pageable);
        } else if (nome != null && projetoMetaId != null) {
            produtos = metaProdutoRepository.findByProjetoMetaIdAndNomeContainingIgnoreCase(projetoMetaId, nome, pageable);
        } else if (projetoMetaId != null && status != null) {
            produtos = metaProdutoRepository.findByProjetoMetaIdAndStatus(projetoMetaId, status, pageable);
        } else if (projetoMetaId != null) {
            produtos = metaProdutoRepository.findByProjetoMetaId(projetoMetaId, pageable);
        } else if (codigo != null) {
            produtos = metaProdutoRepository.findByCodigoContainingIgnoreCase(codigo, pageable);
        } else if (nome != null) {
            produtos = metaProdutoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (status != null) {
            produtos = metaProdutoRepository.findByStatus(status, pageable);
        } else {
            produtos = metaProdutoRepository.findAll(pageable);
        }

        return produtos.map(MetaProdutoDTO::fromEntity);
    }

    public MetaProdutoDTO findById(Long id) {
        MetaProduto produto = metaProdutoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto da meta não encontrado com ID: " + id));
        return MetaProdutoDTO.fromEntity(produto);
    }

    @Transactional
    public MetaProdutoDTO create(MetaProdutoCreateDTO dto) {
        ProjetoMeta projetoMeta = projetoMetaRepository.findById(dto.getProjetoMetaId())
            .orElseThrow(() -> new ResourceNotFoundException("Meta do projeto não encontrada com ID: " + dto.getProjetoMetaId()));

        if (dto.getFim() < dto.getInicio()) {
            throw new BadRequestException("Fim deve ser maior ou igual ao início");
        }

        MetaProduto produto = new MetaProduto();
        produto.setProjetoMeta(projetoMeta);
        produto.setCodigo(dto.getCodigo());
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setUnidadeMedida(dto.getUnidadeMedida());
        produto.setQuantidade(dto.getQuantidade());
        produto.setValorUnitario(dto.getValorUnitario());
        produto.setInicio(dto.getInicio());
        produto.setFim(dto.getFim());
        produto.setStatus(dto.getStatus());
        produto.setPercExecutado(dto.getPercExecutado());

        MetaProduto saved = metaProdutoRepository.save(produto);
        return MetaProdutoDTO.fromEntity(saved);
    }

    @Transactional
    public MetaProdutoDTO update(Long id, MetaProdutoUpdateDTO dto) {
        MetaProduto produto = metaProdutoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Produto da meta não encontrado com ID: " + id));

        if (dto.getProjetoMetaId() != null) {
            ProjetoMeta projetoMeta = projetoMetaRepository.findById(dto.getProjetoMetaId())
                .orElseThrow(() -> new ResourceNotFoundException("Meta do projeto não encontrada com ID: " + dto.getProjetoMetaId()));
            produto.setProjetoMeta(projetoMeta);
        }
        if (dto.getCodigo() != null) {
            produto.setCodigo(dto.getCodigo());
        }
        if (dto.getNome() != null) {
            produto.setNome(dto.getNome());
        }
        if (dto.getDescricao() != null) {
            produto.setDescricao(dto.getDescricao());
        }
        if (dto.getUnidadeMedida() != null) {
            produto.setUnidadeMedida(dto.getUnidadeMedida());
        }
        if (dto.getQuantidade() != null) {
            produto.setQuantidade(dto.getQuantidade());
        }
        if (dto.getValorUnitario() != null) {
            produto.setValorUnitario(dto.getValorUnitario());
        }
        if (dto.getInicio() != null) {
            produto.setInicio(dto.getInicio());
        }
        if (dto.getFim() != null) {
            produto.setFim(dto.getFim());
        }
        if (dto.getStatus() != null) {
            produto.setStatus(dto.getStatus());
        }
        if (dto.getPercExecutado() != null) {
            produto.setPercExecutado(dto.getPercExecutado());
        }

        if (produto.getFim() < produto.getInicio()) {
            throw new BadRequestException("Fim deve ser maior ou igual ao início");
        }

        /**
         * atualizar data inicio e data fim com base no inicio e fim
         */
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
       Integer ano = produto.getProjetoMeta().getProjeto().getDataEfetivaInicio().getYear();
       Integer mes = produto.getProjetoMeta().getProjeto().getDataEfetivaInicio().getMonth().getValue();
       LocalDate di = LocalDate.parse("01/"+ String.format("%02d", mes ) +"/"+ano, formatter);
       LocalDate df = di.plusMonths(produto.getFim()-produto.getInicio());

        produto.setDataFim(df);
        produto.setDataInicio(di);

        MetaProduto saved = metaProdutoRepository.save(produto);
        return MetaProdutoDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!metaProdutoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Produto da meta não encontrado com ID: " + id);
        }
        metaProdutoRepository.deleteById(id);
    }

    /**
     * Recalcula percExecutado com base na soma dos custos (qtdeHora * valorHora) de TermoEncerramentoCusto
     * das demandas encerradas vinculadas a esta MetaProduto, em relação ao valor total da meta
     * (valorUnitario * quantidade). percExecutado = (somaCustos / valorTotal) * 100, máx. 100.
     */
    @Transactional
    public void recalculatePercExecutado(Long metaProdutoId) {
        if (metaProdutoId == null) {
            return;
        }
        MetaProduto meta = metaProdutoRepository.findById(metaProdutoId).orElse(null);
        if (meta == null) {
            return;
        }
        BigDecimal somaCustos = termoEncerramentoCustoRepository.sumCustosByMetaProdutoId(metaProdutoId);
        if (somaCustos == null) {
            somaCustos = BigDecimal.ZERO;
        }
        BigDecimal valorTotal = meta.getValorUnitario() != null && meta.getQuantidade() != null
            ? meta.getValorUnitario().multiply(BigDecimal.valueOf(meta.getQuantidade()))
            : BigDecimal.ZERO;
        int perc = 0;
        if (valorTotal.compareTo(BigDecimal.ZERO) > 0) {
            perc = somaCustos
                .multiply(BigDecimal.valueOf(100))
                .divide(valorTotal, 2, RoundingMode.HALF_UP)
                .min(BigDecimal.valueOf(100))
                .intValue();
        }
        meta.setPercExecutado(perc);
        metaProdutoRepository.save(meta);
    }

    @Transactional(readOnly = true)
    public MetaProdutoEvolucaoTrimestralDTO getEvolucaoTrimestral(Long metaProdutoId) {
        MetaProduto produto = metaProdutoRepository.findById(metaProdutoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto da meta não encontrado com ID: " + metaProdutoId));

        if (produto.getDataInicio() == null || produto.getDataFim() == null) {
            throw new BadRequestException("MetaProduto ID " + metaProdutoId + " não possui dataInicio/dataFim para análise trimestral.");
        }
        if (produto.getDataFim().isBefore(produto.getDataInicio())) {
            throw new BadRequestException("MetaProduto ID " + metaProdutoId + " possui dataFim menor que dataInicio.");
        }

        // Regra: previsto = soma dos Termos de Planejamento das DTs do produto
        BigDecimal totalPrevistoProduto = termoPlanejamentoRepository.sumValorPlanejadoByMetaProdutoId(produto.getId());
        if (totalPrevistoProduto == null) {
            totalPrevistoProduto = BigDecimal.ZERO;
        }
        // Regra: executado = soma dos Termos de Encerramento das DTs do produto
        BigDecimal totalExecutadoProduto = termoEncerramentoRepository.sumValorExecutadoByMetaProdutoId(produto.getId());
        if (totalExecutadoProduto == null) {
            totalExecutadoProduto = BigDecimal.ZERO;
        }

        List<MetaProdutoEvolucaoTrimestralItemDTO> itens = new ArrayList<>();
        LocalDate cursor = produto.getDataInicio();
        int seq = 1;
        while (!cursor.isAfter(produto.getDataFim())) {
            LocalDate fimTri = cursor.plusMonths(3).minusDays(1);
            if (fimTri.isAfter(produto.getDataFim())) {
                fimTri = produto.getDataFim();
            }

            BigDecimal previstoTrimestre = termoPlanejamentoRepository
                    .sumValorPlanejadoByMetaProdutoIdAndDataAberturaBetween(
                            produto.getId(),
                            LocalDateTime.of(cursor, java.time.LocalTime.MIN),
                            LocalDateTime.of(fimTri, java.time.LocalTime.MAX)
                    );
            if (previstoTrimestre == null) {
                previstoTrimestre = BigDecimal.ZERO;
            }

            BigDecimal executadoTrimestre = termoEncerramentoRepository
                    .sumValorExecutadoByMetaProdutoIdAndDataTermoBetween(
                            produto.getId(),
                            LocalDateTime.of(cursor, java.time.LocalTime.MIN),
                            LocalDateTime.of(fimTri, java.time.LocalTime.MAX)
                    );
            if (executadoTrimestre == null) {
                executadoTrimestre = BigDecimal.ZERO;
            }

            itens.add(new MetaProdutoEvolucaoTrimestralItemDTO(
                    seq,
                    cursor,
                    fimTri,
                    previstoTrimestre,
                    executadoTrimestre
            ));

            seq++;
            cursor = fimTri.plusDays(1);
        }

        MetaProdutoEvolucaoTrimestralDTO response = new MetaProdutoEvolucaoTrimestralDTO();
        response.setMetaProdutoId(produto.getId());
        response.setCodigoProduto(produto.getCodigo());
        response.setNomeProduto(produto.getNome());
        response.setDataInicioAnalise(produto.getDataInicio());
        response.setDataFimAnalise(produto.getDataFim());
        response.setTotalPrevistoProduto(totalPrevistoProduto);
        response.setTotalExecutadoProduto(totalExecutadoProduto);
        response.setTrimestres(itens);
        return response;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResumoDTO> getProdutoResumo(Long idMeta, Long idProduto) {
        if (idMeta == null && idProduto == null) {
            throw new BadRequestException("Informe ao menos um parâmetro: idMeta ou idProduto.");
        }

        List<MetaProduto> produtos;
        if (idProduto != null) {
            MetaProduto produto = metaProdutoRepository.findById(idProduto)
                    .orElseThrow(() -> new ResourceNotFoundException("Produto da meta não encontrado com ID: " + idProduto));

            if (idMeta != null && !idMeta.equals(produto.getProjetoMeta().getId())) {
                throw new BadRequestException("O idProduto informado não pertence ao idMeta informado.");
            }
            produtos = List.of(produto);
        } else {
            produtos = metaProdutoRepository.findByProjetoMetaId(idMeta);
        }

        List<ProdutoResumoDTO> resultado = new ArrayList<>();
        for (MetaProduto produto : produtos) {
            resultado.add(montarProdutoResumo(produto));
        }
        return resultado;
    }

    private ProdutoResumoDTO montarProdutoResumo(MetaProduto produto) {
        String situacaoProduto = "";
        LocalDate inicio = produto.getDataInicio();
        LocalDate fim = produto.getDataFim();
        LocalDateTime primeiraDataPlanejamento = termoPlanejamentoRepository
                .findFirstDataAberturaByMetaProdutoId(produto.getId())
                .orElse(null);
        LocalDate inicioRealExecucao = primeiraDataPlanejamento != null ? primeiraDataPlanejamento.toLocalDate() : null;
        int mesesPrevistosExecucao = calcularMesesPrevistosExecucao(inicio, fim);

        BigDecimal valorTotalOrcamento = calcularValorTotalOrcamento(produto);
        BigDecimal valorTotalEmExecucao = termoPlanejamentoRepository.sumValorPlanejadoByMetaProdutoIdAndDemandaStatusIn(
                produto.getId(),
                List.of(StatusDemandaTecnica.E.getCodigo(), StatusDemandaTecnica.F.getCodigo())
        );
        BigDecimal valorTotalExecutado = termoEncerramentoRepository.sumValorExecutadoByMetaProdutoIdAndStatus(
                produto.getId(),
                StatusDemandaTecnica.G.getCodigo()
        );

        BigDecimal percentualExecucao = produto.getPercExecutado() != null
                ? BigDecimal.valueOf(produto.getPercExecutado()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal percentualExecutado = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (valorTotalOrcamento != null && valorTotalOrcamento.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal executado = valorTotalExecutado != null ? valorTotalExecutado : BigDecimal.ZERO;
            percentualExecutado = executado
                    .multiply(BigDecimal.valueOf(100))
                    .divide(valorTotalOrcamento, 2, RoundingMode.HALF_UP);
        }

        int mesesExecucao = calcularMesesExecucao(primeiraDataPlanejamento);
        
        BigDecimal valorMediaEntregaPrevistaMensal = dividirPorMeses(valorTotalOrcamento, mesesPrevistosExecucao);
        BigDecimal valorMediaEntregaRealMensal = dividirPorMeses(
                valorTotalEmExecucao.add(valorTotalExecutado),
                mesesExecucao
        );

        return new ProdutoResumoDTO(
                produto.getProjetoMeta().getId(),
                produto.getProjetoMeta().getCodigo(),
                produto.getProjetoMeta().getNome(),
                produto.getId(),
                produto.getCodigo(),
                produto.getNome(),
                situacaoProduto,
                inicio,
                inicioRealExecucao,
                fim,
                mesesPrevistosExecucao,
                valorTotalOrcamento,
                valorTotalEmExecucao.setScale(2, RoundingMode.HALF_UP),
                valorTotalExecutado.setScale(2, RoundingMode.HALF_UP),
                percentualExecutado,
                percentualExecucao,
                valorMediaEntregaPrevistaMensal,
                valorMediaEntregaRealMensal
        );
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

    private int calcularMesesExecucao(LocalDateTime primeiraDataAbertura) {
        if (primeiraDataAbertura == null) {
            return 0;
        }

        YearMonth inicioExecucao = YearMonth.from(primeiraDataAbertura);
        YearMonth mesAtual = YearMonth.now();
        if (inicioExecucao.isAfter(mesAtual)) {
            return 0;
        }

        return (int) ChronoUnit.MONTHS.between(inicioExecucao, mesAtual) + 1;
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
}
