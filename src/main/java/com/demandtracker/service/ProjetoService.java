package com.demandtracker.service;

import com.demandtracker.dto.ProjetoCreateDTO;
import com.demandtracker.dto.ProjetoDTO;
import com.demandtracker.dto.ProjetoTotaisDTO;
import com.demandtracker.dto.ProjetoUpdateDTO;
import com.demandtracker.dto.SemaforoNodeDTO;
import com.demandtracker.entity.DemandaTecnica;
import com.demandtracker.entity.MetaProduto;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.ProjetoMeta;
import com.demandtracker.entity.Usuario;
import com.demandtracker.entity.enums.SemaforoNivel;
import com.demandtracker.entity.enums.SemaforoStatus;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaTecnicaRepository;
import com.demandtracker.repository.MetaProdutoRepository;
import com.demandtracker.repository.ProjetoMetaRepository;
import com.demandtracker.repository.ProjetoRepository;
import com.demandtracker.repository.TermoEncerramentoCustoRepository;
import com.demandtracker.repository.TermoEncerramentoRepository;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demandtracker.repository.DesembolsoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetoService {
    
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetaProdutoRepository metaProdutoRepository;
    private final TermoEncerramentoCustoRepository termoEncerramentoCustoRepository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;
    private final ProjetoMetaRepository projetoMetaRepository;
    private final DemandaTecnicaRepository demandaTecnicaRepository;
    private final DesembolsoRepository desembolsoRepository;

    public Page<ProjetoDTO> findAll(String nome, String codTed, Long usuarioId, Pageable pageable) {
        Page<Projeto> projetos;
        
        if (nome != null && codTed != null) {
            projetos = projetoRepository.findByNomeContainingIgnoreCaseAndCodTedContainingIgnoreCase(nome, codTed, pageable);
        } else if (nome != null) {
            projetos = projetoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (codTed != null) {
            projetos = projetoRepository.findByCodTedContainingIgnoreCase(codTed, pageable);
        } else if (usuarioId != null) {
            projetos = projetoRepository.findByUsuarioId(usuarioId, pageable);
        } else {
            projetos = projetoRepository.findAll(pageable);
        }
        
        return projetos.map(ProjetoDTO::fromEntity);
    }
    
    public ProjetoDTO findById(Long id) {
        Projeto projeto = projetoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + id));
        return ProjetoDTO.fromEntity(projeto);
    }

    /**
     * Retorna valor total do projeto (soma quantidade*valorUnitario dos produtos das metas)
     * e valor total executado (soma dos custos de termos de encerramento das demandas do projeto).
     */
    @Transactional(readOnly = true)
    public ProjetoTotaisDTO getTotais(Long projetoId) {
        if (!projetoRepository.existsById(projetoId)) {
            throw new ResourceNotFoundException("Projeto não encontrado com ID: " + projetoId);
        }
        
        BigDecimal valorTotalProjeto = metaProdutoRepository.sumValorTotalByProjetoId(projetoId);
        if (valorTotalProjeto == null) {
            valorTotalProjeto = BigDecimal.ZERO;
        }
        
       
        BigDecimal valorTotalExecutado = termoEncerramentoCustoRepository.sumCustosByProjetoId(projetoId);
        if (valorTotalExecutado == null) {
            valorTotalExecutado = BigDecimal.ZERO;
        }
        return new ProjetoTotaisDTO(valorTotalProjeto, valorTotalExecutado);
    }
    
    @Transactional
    public ProjetoDTO create(ProjetoCreateDTO dto) {
        if (dto.getTermoFinal().isBefore(dto.getTermoInicial())) {
            throw new BadRequestException("Data final deve ser maior ou igual à data inicial");
        }
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        Projeto projeto = new Projeto();
        projeto.setNome(dto.getNome());
        projeto.setCodTed(dto.getCodTed());
        projeto.setTermoInicial(dto.getTermoInicial());
        projeto.setTermoFinal(dto.getTermoFinal());
        projeto.setDataEfetivaInicio(dto.getDataEfetivaInicio());
        projeto.setUsuario(usuario);
        
        Projeto saved = projetoRepository.save(projeto);
        return ProjetoDTO.fromEntity(saved);
    }
    
    @Transactional
    public ProjetoDTO update(Long id, ProjetoUpdateDTO dto) {
        Projeto projeto = projetoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + id));
        
        if (dto.getNome() != null) {
            projeto.setNome(dto.getNome());
        }
        if (dto.getCodTed() != null) {
            projeto.setCodTed(dto.getCodTed());
        }
        if (dto.getTermoInicial() != null) {
            projeto.setTermoInicial(dto.getTermoInicial());
        }
        if (dto.getTermoFinal() != null) {
            projeto.setTermoFinal(dto.getTermoFinal());
        }
        if (dto.getDataEfetivaInicio() != null) {
            projeto.setDataEfetivaInicio(dto.getDataEfetivaInicio());
        }
        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
            projeto.setUsuario(usuario);
        }
        
        if (projeto.getTermoFinal().isBefore(projeto.getTermoInicial())) {
            throw new BadRequestException("Data final deve ser maior ou igual à data inicial");
        }

        // Sempre que houver update, recalcula datas de início/fim dos produtos das metas com base na dataEfetivaInicio
        // atualizarDatasProdutosDoProjeto(projeto);

        Projeto saved = projetoRepository.save(projeto);
        return ProjetoDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public SemaforoNodeDTO getSemaforo(Long projetoId) {
        Projeto projeto = projetoRepository.findById(projetoId)
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + projetoId));

        SemaforoNodeDTO projetoNode = new SemaforoNodeDTO();
        projetoNode.setId(projeto.getId());
        projetoNode.setNivel(SemaforoNivel.PROJETO);
        projetoNode.setCodigo(projeto.getCodTed());
        projetoNode.setNome(projeto.getNome());

        LocalDate dataInicioProjeto = projeto.getDataEfetivaInicio();
        projetoNode.setDataInicio(dataInicioProjeto);
        if (dataInicioProjeto != null) {
            projetoNode.setDataFim(dataInicioProjeto.plusMonths(36));
        }

        List<ProjetoMeta> metas = projetoMetaRepository.findByProjetoId(projetoId, org.springframework.data.domain.Pageable.unpaged()).getContent();

        int totalDemandasProjeto = 0;
        int totalDemandasEncerradasProjeto = 0;
        BigDecimal somaPercExecMetas = BigDecimal.ZERO;
        int qtdMetasComPerc = 0;

        SemaforoStatus statusProjeto = SemaforoStatus.CINZA;

        for (ProjetoMeta meta : metas) {
            SemaforoNodeDTO metaNode = buildMetaNode(meta);
            projetoNode.getChildren().add(metaNode);

            if (metaNode.getQtdDemandas() != null) {
                totalDemandasProjeto += metaNode.getQtdDemandas();
            }
            if (metaNode.getQtdDemandasEncerradas() != null) {
                totalDemandasEncerradasProjeto += metaNode.getQtdDemandasEncerradas();
            }
            if (metaNode.getPercentualExecutado() != null) {
                somaPercExecMetas = somaPercExecMetas.add(metaNode.getPercentualExecutado());
                qtdMetasComPerc++;
            }

            statusProjeto = agregarStatus(statusProjeto, metaNode.getStatus());
        }

        if (qtdMetasComPerc > 0) {
            projetoNode.setPercentualExecutado(somaPercExecMetas.divide(BigDecimal.valueOf(qtdMetasComPerc), 2, java.math.RoundingMode.HALF_UP));
        }
        projetoNode.setQtdDemandas(totalDemandasProjeto);
        projetoNode.setQtdDemandasEncerradas(totalDemandasEncerradasProjeto);
        projetoNode.setStatus(statusProjeto);

        return projetoNode;
    }

    /**
     * Atualiza, para todos os produtos das metas do projeto, os campos dataInicio e dataFim com base em dataEfetivaInicio.
     * Regra:
     *  - dataInicio = dataEfetivaInicio + inicio meses - 1 dia, depois ajustar o dia para 1 (primeiro dia do mês)
     *  - dataFim    = dataEfetivaInicio + fim meses - 1 dia, depois ajustar o dia para o último dia do mês
     */
    private void atualizarDatasProdutosDoProjeto_Deprecated(Projeto projeto) {
        LocalDate dataEfetiva = projeto.getDataEfetivaInicio();
        if (dataEfetiva == null || projeto.getId() == null) {
            return;
        }

        List<MetaProduto> produtos = metaProdutoRepository.findByProjetoMetaProjetoId(projeto.getId());
        for (MetaProduto produto : produtos) {
            Integer inicioMes = produto.getInicio();
            Integer fimMes = produto.getFim();

            if (inicioMes != null) {
                LocalDate baseInicio = dataEfetiva.plusMonths(inicioMes).minusDays(1);
                produto.setDataInicio(baseInicio.withDayOfMonth(1));
            } else {
                produto.setDataInicio(null);
            }

            if (fimMes != null) {
                LocalDate baseFim = dataEfetiva.plusMonths(fimMes).minusDays(1);
                produto.setDataFim(baseFim.withDayOfMonth(baseFim.lengthOfMonth()));
            } else {
                produto.setDataFim(null);
            }
        }
    }

    /**
     * Calculo do nó da M E T A (ProjetoMeta)
     * @param meta
     * @return
     */
    private SemaforoNodeDTO buildMetaNode(ProjetoMeta meta) {
        SemaforoNodeDTO metaNode = new SemaforoNodeDTO();
        metaNode.setId(meta.getId());
        metaNode.setNivel(SemaforoNivel.META);
        metaNode.setCodigo(meta.getCodigo());
        metaNode.setNome(meta.getNome());

        List<MetaProduto> produtos = metaProdutoRepository.findByProjetoMetaId(meta.getId(), org.springframework.data.domain.Pageable.unpaged()).getContent();

        LocalDate minInicio = null;
        LocalDate maxFim = null;
        int totalDemandasMeta = 0;
        int totalDemandasEncerradasMeta = 0;
        BigDecimal somaPercExecProdutos = BigDecimal.ZERO;
        int qtdProdutosComPerc = 0;
        BigDecimal somaValorPrevisto = BigDecimal.ZERO;
        BigDecimal somaValorExecutado = BigDecimal.ZERO;

        SemaforoStatus statusMeta = SemaforoStatus.CINZA;

        for (MetaProduto produto : produtos) {
            SemaforoNodeDTO produtoNode = buildProdutoNode(produto);
            metaNode.getChildren().add(produtoNode);

            LocalDate di = produtoNode.getDataInicio();
            LocalDate df = produtoNode.getDataFim();
            if (di != null && (minInicio == null || di.isBefore(minInicio))) {
                minInicio = di;
            }
            if (df != null && (maxFim == null || df.isAfter(maxFim))) {
                maxFim = df;
            }

            if (produtoNode.getQtdDemandas() != null) {
                totalDemandasMeta += produtoNode.getQtdDemandas();
            }
            if (produtoNode.getQtdDemandasEncerradas() != null) {
                totalDemandasEncerradasMeta += produtoNode.getQtdDemandasEncerradas();
            }
            if (produtoNode.getPercentualExecutado() != null) {
                somaPercExecProdutos = somaPercExecProdutos.add(produtoNode.getPercentualExecutado());
                qtdProdutosComPerc++;
            }
            if (produtoNode.getValorTotalPrevisto() != null) {
                somaValorPrevisto = somaValorPrevisto.add(produtoNode.getValorTotalPrevisto());
            }
            if (produtoNode.getValorTotalExecutado() != null) {
                somaValorExecutado = somaValorExecutado.add(produtoNode.getValorTotalExecutado());
            }

            statusMeta = agregarStatus(statusMeta, produtoNode.getStatus());
        }

        metaNode.setDataInicio(minInicio);
        metaNode.setDataFim(maxFim);

        if (qtdProdutosComPerc > 0) {
            metaNode.setPercentualExecutado(somaPercExecProdutos.divide(BigDecimal.valueOf(qtdProdutosComPerc), 2, java.math.RoundingMode.HALF_UP));
        }
        metaNode.setQtdDemandas(totalDemandasMeta);
        metaNode.setQtdDemandasEncerradas(totalDemandasEncerradasMeta);
        metaNode.setValorTotalPrevisto(somaValorPrevisto);
        metaNode.setValorTotalExecutado(somaValorExecutado);
        metaNode.setStatus(statusMeta);

        return metaNode;
    }

    /**
     * Calculo do nó do P R O D U T O (MetaProduto)
     * @param produto
     * @return
     */
    private SemaforoNodeDTO buildProdutoNode(MetaProduto produto) {
        SemaforoNodeDTO node = new SemaforoNodeDTO();
        node.setId(produto.getId());
        node.setNivel(SemaforoNivel.PRODUTO);
        node.setCodigo(produto.getCodigo());
        node.setNome(produto.getNome());

        /** 
        node.setDataInicio(produto.getDataInicio());
        **/
        node.setDataInicio(produto.getDataPrimeiraEntrega());
        node.setDataFim(produto.getDataFim());

        if (produto.getValorUnitario() != null && produto.getQuantidade() != null) {
            node.setValorTotalPrevisto(produto.getValorUnitario().multiply(BigDecimal.valueOf(produto.getQuantidade())));
        }
        BigDecimal valorExecutado = termoEncerramentoRepository.sumValorExecutadoByMetaProdutoIdAndStatus(
                produto.getId(), com.demandtracker.entity.DemandaTecnica.STATUS_ENCERRADA);
        node.setValorTotalExecutado(valorExecutado != null ? valorExecutado : BigDecimal.ZERO);

        if (produto.getPercExecutado() != null) {
            node.setPercentualExecutado(BigDecimal.valueOf(produto.getPercExecutado()).setScale(2));
        }

        // Não considerar demandas canceladas (status Z) no semáforo
        final String statusCancelada = "Z";
        long qtdDemandas = demandaTecnicaRepository.countByMetaProdutoIdAndStatusNot(produto.getId(), statusCancelada);
        long qtdDemandasEncerradas = demandaTecnicaRepository.countByMetaProdutoIdAndStatus(produto.getId(), com.demandtracker.entity.DemandaTecnica.STATUS_ENCERRADA);
        node.setQtdDemandas((int) qtdDemandas);
        node.setQtdDemandasEncerradas((int) qtdDemandasEncerradas);

        node.setStatus(calcularStatusProduto(produto));

        List<DemandaTecnica> demandas = demandaTecnicaRepository.findByMetaProdutoIdAndStatusNot(produto.getId(), statusCancelada);
        BigDecimal valorTotalPrevistoProduto = node.getValorTotalPrevisto() != null ? node.getValorTotalPrevisto() : BigDecimal.ZERO;
        for (DemandaTecnica demanda : demandas) {
            node.getChildren().add(buildDemandaNode(demanda, valorTotalPrevistoProduto));
        }

        return node;
    }

    /**
     * Calculo do nó da D E M A N D A (DemandaTecnica)
     * @param demanda
     * @param valorTotalPrevistoProduto
     * @return
     */
    private SemaforoNodeDTO buildDemandaNode(DemandaTecnica demanda, BigDecimal valorTotalPrevistoProduto) {
        String statusDemanda = demanda.getStatus() != null ? demanda.getStatus() : "";
        SemaforoNodeDTO node = new SemaforoNodeDTO();
        node.setId(demanda.getId());
        node.setNivel(SemaforoNivel.DEMANDA);
        node.setCodigo(demanda.getCodigo());
        node.setNome(demanda.getNome());
        node.setStatus(mapStatusDemandaToSemaforo(statusDemanda));
        node.setStatusDemanda(statusDemanda);
        node.setChildren(List.of());

        BigDecimal valorExecutadoDemanda = termoEncerramentoRepository.sumValorExecutadoByDemandaTecnicaId(demanda.getId());
        node.setValorTotalExecutado(valorExecutadoDemanda != null ? valorExecutadoDemanda : BigDecimal.ZERO);

        // Percentual = quanto o valor executado da demanda representa do valor total previsto do produto
        if (valorTotalPrevistoProduto != null && valorTotalPrevistoProduto.compareTo(BigDecimal.ZERO) > 0 && node.getValorTotalExecutado() != null) {
            BigDecimal perc = node.getValorTotalExecutado()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(valorTotalPrevistoProduto, 2, java.math.RoundingMode.HALF_UP);
            node.setPercentualExecutado(perc);
        }

        return node;
    }

    private SemaforoStatus mapStatusDemandaToSemaforo(String statusDemanda) {
        if (statusDemanda == null || statusDemanda.isBlank()) return SemaforoStatus.CINZA;
        return switch (statusDemanda.toUpperCase()) {
            case "G" -> SemaforoStatus.VERDE;   // Encerrado e assinado
            case "Z" -> SemaforoStatus.CINZA;   // Cancelada
            default -> SemaforoStatus.AMARELO;  // Em andamento (A,B,C,D,E,F)
        };
    }

    /**
     * Calcula o status do semáforo do produto considerando:
     * - Período oficial (dataInicio, dataFim) e % executado.
     * - Antecipação: se a data de início é futura mas já há % de execução (demanda antecipada), não retorna CINZA.
     */
    private SemaforoStatus calcularStatusProduto(MetaProduto produto) {
        
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = produto.getDataPrimeiraEntrega();
        LocalDate fim = produto.getDataFim();

        if (inicio == null || fim == null) {
            return SemaforoStatus.CINZA;
        }

        System.out.println( "produto: "+produto.getCodigo());
        System.out.println( "datainicio: "+produto.getDataInicio());
        System.out.println( "getDataPrimeiraEntrega: "+produto.getDataPrimeiraEntrega());
        System.out.println( "getIntervaloEntregaEmMeses: "+produto.getIntervaloEntregaEmMeses());


        Integer percExec = produto.getPercExecutado();
        int perc = percExec != null ? percExec : 0;

        // Período ainda não começou (hoje < dataInicio)
        if (hoje.isBefore(inicio)) {
            if (perc <= 0) {
                return SemaforoStatus.CINZA; // Sem execução = não iniciado
            }
            // Há execução antecipada: considerar como adiantado em relação ao cronograma → VERDE
            return SemaforoStatus.VERDE;
        }

        // Período já passou (hoje > dataFim)
        if (hoje.isAfter(fim)) {
            if (perc >= 100) {
                return SemaforoStatus.VERDE;
            }
            return SemaforoStatus.VERMELHO;
        }

        // Dentro do período (dataInicio <= hoje <= dataFim): comparar % executado com progresso do tempo
        long totalDias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fim) + 1;
        long diasPassados = java.time.temporal.ChronoUnit.DAYS.between(inicio, hoje) + 1;
        double progressoTempo = totalDias > 0 ? (double) diasPassados / totalDias : 0.0;
        double esperado = progressoTempo * 100.0;

        if (perc >= esperado * 0.9) {
            return SemaforoStatus.VERDE;
        } else if (perc >= esperado * 0.5) {
            return SemaforoStatus.AMARELO;
        } else {
            return SemaforoStatus.VERMELHO;
        }
    }

    private SemaforoStatus agregarStatus(SemaforoStatus atual, SemaforoStatus novo) {
        if (novo == null) return atual;
        if (atual == SemaforoStatus.VERMELHO || novo == SemaforoStatus.VERMELHO) {
            return SemaforoStatus.VERMELHO;
        }
        if (atual == SemaforoStatus.AMARELO || novo == SemaforoStatus.AMARELO) {
            return SemaforoStatus.AMARELO;
        }
        if (atual == SemaforoStatus.VERDE || novo == SemaforoStatus.VERDE) {
            return SemaforoStatus.VERDE;
        }
        return SemaforoStatus.CINZA;
    }
    
    @Transactional
    public void delete(Long id) {
        if (!projetoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Projeto não encontrado com ID: " + id);
        }
        projetoRepository.deleteById(id);
    }
}
