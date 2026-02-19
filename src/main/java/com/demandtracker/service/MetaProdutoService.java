package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.MetaProduto;
import com.demandtracker.entity.ProjetoMeta;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.MetaProdutoRepository;
import com.demandtracker.repository.ProjetoMetaRepository;
import com.demandtracker.repository.TermoEncerramentoCustoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MetaProdutoService {

    private final MetaProdutoRepository metaProdutoRepository;
    private final ProjetoMetaRepository projetoMetaRepository;
    private final TermoEncerramentoCustoRepository termoEncerramentoCustoRepository;

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
}
