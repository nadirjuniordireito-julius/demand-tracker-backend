package com.demandtracker.service;

import com.demandtracker.dto.*;
import com.demandtracker.entity.*;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.Year;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DemandaTecnicaService {
    
    private final DemandaTecnicaRepository demandaRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetaProdutoRepository metaProdutoRepository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;
    
    @Transactional(readOnly = true)
    public Page<DemandaTecnicaDTO> findAll(String codigo, String nome, Long projetoId, String status, Pageable pageable) {
        Page<DemandaTecnica> demandas;
       
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(
                        Sort.Order.asc("metaProduto.projetoMeta.codigo"),
                        Sort.Order.asc("metaProduto.codigo"),
                        Sort.Order.asc("codigo")
                )
        );
        String statusCode = (status != null && !status.isBlank()) ? mapStatusToCode(status) : null;
        
        if (statusCode != null && codigo == null && nome == null && projetoId == null) {
            demandas = demandaRepository.findByStatus(statusCode, sortedPageable);
        } else if (statusCode != null && codigo != null && nome != null && projetoId != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCaseAndProjetoIdAndStatus(codigo, nome, projetoId, statusCode, sortedPageable);
        } else if (statusCode != null && codigo != null && nome != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCaseAndStatus(codigo, nome, statusCode, sortedPageable);
        } else if (statusCode != null && codigo != null && projetoId != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndProjetoIdAndStatus(codigo, projetoId, statusCode, sortedPageable);
        } else if (statusCode != null && nome != null && projetoId != null) {
            demandas = demandaRepository.findByNomeContainingIgnoreCaseAndProjetoIdAndStatus(nome, projetoId, statusCode, sortedPageable);
        } else if (statusCode != null && codigo != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndStatus(codigo, statusCode, sortedPageable);
        } else if (statusCode != null && nome != null) {
            demandas = demandaRepository.findByNomeContainingIgnoreCaseAndStatus(nome, statusCode, sortedPageable);
        } else if (statusCode != null && projetoId != null) {
            demandas = demandaRepository.findByProjetoIdAndStatus(projetoId, statusCode, sortedPageable);
        } else if (codigo != null && nome != null && projetoId != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCaseAndProjetoId(codigo, nome, projetoId, sortedPageable);
        } else if (codigo != null && nome != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndNomeContainingIgnoreCase(codigo, nome, sortedPageable);
        } else if (codigo != null && projetoId != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCaseAndProjetoId(codigo, projetoId, sortedPageable);
        } else if (nome != null && projetoId != null) {
            demandas = demandaRepository.findByNomeContainingIgnoreCaseAndProjetoId(nome, projetoId, sortedPageable);
        } else if (codigo != null) {
            demandas = demandaRepository.findByCodigoContainingIgnoreCase(codigo, sortedPageable);
        } else if (nome != null) {
            demandas = demandaRepository.findByNomeContainingIgnoreCase(nome, sortedPageable);
        } else if (projetoId != null) {
            demandas = demandaRepository.findByProjetoId(projetoId, sortedPageable);
        } else {
            demandas = demandaRepository.findAll(sortedPageable);
        }
        
        return demandas.map(DemandaTecnicaDTO::fromEntity);
    }
    
    @Transactional(readOnly = true)
    public DemandaTecnicaDTO findById(Long id) {
        DemandaTecnica demanda = demandaRepository.findByIdWithMeta(id)
            .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + id));
        DemandaTecnicaDTO dto = DemandaTecnicaDTO.fromEntity(demanda);
        if (demanda.getMetaProduto() != null) {
            BigDecimal total = termoEncerramentoRepository.sumValorExecutadoByMetaProdutoIdAndStatus(
                demanda.getMetaProduto().getId(), DemandaTecnica.STATUS_ENCERRADA);
            dto.setTotalExecutadoProduto(total != null ? total : BigDecimal.ZERO);
        }
        return dto;
    }
    
    @Transactional
    public DemandaTecnicaDTO create(DemandaTecnicaCreateDTO dto) {
        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
            .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
        
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));

        if (dto.getMetaProdutoId() == null) {
            throw new BadRequestException("Para geração automática do código, informe o produto da meta (metaProdutoId).");
        }

        MetaProduto metaProduto = metaProdutoRepository.findById(dto.getMetaProdutoId())
            .orElseThrow(() -> new ResourceNotFoundException("Meta produto não encontrado com ID: " + dto.getMetaProdutoId()));

        int anoAbertura = Year.now().getValue();
        String codigoGerado = gerarCodigoDemandaTecnica(metaProduto.getProjetoMeta(), anoAbertura);
        if (demandaRepository.findByCodigo(codigoGerado).isPresent()) {
            throw new RuntimeException("Código gerado já existe: " + codigoGerado + ". Tente novamente.");
        }

        DemandaTecnica demanda = new DemandaTecnica();
        demanda.setProjeto(projeto);
        demanda.setCodigo(codigoGerado);
        demanda.setNome(dto.getNome());
        demanda.setDescricao(dto.getDescricao());
        demanda.setUsuario(usuario);
        demanda.setMetaProduto(metaProduto);
        
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            demanda.setStatus(mapStatusToCode(dto.getStatus()));
        } else {
            demanda.setStatus("A"); // Em elaboração - status inicial ao criar demanda
        }
        
        DemandaTecnica saved = demandaRepository.save(demanda);
        return DemandaTecnicaDTO.fromEntity(saved);
    }
    
    @Transactional
    public DemandaTecnicaDTO update(Long id, DemandaTecnicaUpdateDTO dto) {
        DemandaTecnica demanda = demandaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + id));
        
        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            demanda.setProjeto(projeto);
        }
        // Código não é alterável no update (gerado automaticamente na criação).
        if (dto.getNome() != null) {
            demanda.setNome(dto.getNome());
        }
        if (dto.getDescricao() != null) {
            demanda.setDescricao(dto.getDescricao());
        }
        if (dto.getMetaProdutoId() != null) {
            MetaProduto metaProduto = metaProdutoRepository.findById(dto.getMetaProdutoId())
                .orElseThrow(() -> new ResourceNotFoundException("Meta produto não encontrado com ID: " + dto.getMetaProdutoId()));
            demanda.setMetaProduto(metaProduto);
        }
        // Se metaProdutoId for null, mantém o valor atual (não remove o relacionamento)
        
        if (dto.getStatus() != null) {
            demanda.setStatus(dto.getStatus().isBlank() ? null : mapStatusToCode(dto.getStatus()));
        }
        
        DemandaTecnica saved = demandaRepository.save(demanda);
        return DemandaTecnicaDTO.fromEntity(saved);
    }
    
    /**
     * Gera código automático da demanda técnica: DT-M{codigoMeta3}-{sequencia3}-{ano4}.
     * (i) Prefixo DT-M; (ii) código da meta com zeros à esquerda (máx 3 caracteres);
     * (iii) hífen + sequência (3 dígitos) + hífen + ano (4 dígitos) da abertura.
     *
     * Regra da sequência:
     * - Recupera a última demanda da meta (projetoMeta) pelo código;
     * - Extrai a sequência do código (DT-M001-003-2026 -> 003) e soma +1;
     * - Se não existir demanda anterior para a meta, começa em 001.
     */
    private String gerarCodigoDemandaTecnica(ProjetoMeta projetoMeta, int ano) {
        String codigoMeta = projetoMeta.getCodigo() != null ? projetoMeta.getCodigo().trim() : "0";
        if (codigoMeta.length() > 3) {
            codigoMeta = codigoMeta.substring(codigoMeta.length() - 3);
        }
        codigoMeta = String.format("%3s", codigoMeta).replace(' ', '0');

        // Busca a última demanda (maior código) da meta em questão
        var ultimaDemandaOpt = demandaRepository.findFirstByMetaProdutoProjetoMetaIdOrderByCodigoDesc(projetoMeta.getId());

        int proximaSequencia = 1;
        if (ultimaDemandaOpt.isPresent()) {
            String codigoUltima = ultimaDemandaOpt.get().getCodigo();
            // Formato esperado: DT-M{codigoMeta3}-{seq3}-{ano4}, ex.: DT-M001-003-2026
            if (codigoUltima != null) {
                String[] partes = codigoUltima.split("-");
                if (partes.length >= 3) {
                    String seqStr = partes[2]; // índice 2 -> sequência (003)
                    try {
                        int seqAtual = Integer.parseInt(seqStr);
                        proximaSequencia = seqAtual + 1;
                    } catch (NumberFormatException e) {
                        proximaSequencia = 1;
                    }
                }
            }
        }

        String seq = String.format("%03d", proximaSequencia);
        String anoStr = String.format("%04d", ano);

        return "DT-M" + codigoMeta + "-" + seq + "-" + anoStr;
    }

    private static String mapStatusToCode(String status) {
        if (status == null || status.isBlank()) return null;
        String s = status.trim().toLowerCase();
        if (s.length() == 1) return s.toUpperCase();
        return switch (s) {
            default -> status.substring(0, 1).toUpperCase();
        };
    }
    
    @Transactional
    public void delete(Long id) {
        if (!demandaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Demanda não encontrada com ID: " + id);
        }
        demandaRepository.deleteById(id);
    }

    /**
     * Cancela a demanda técnica (status = "Z" - Cancelada).
     */
    @Transactional
    public DemandaTecnicaDTO cancelar(Long id) {
      
        DemandaTecnica demanda = demandaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demanda não encontrada com ID: " + id));
        demanda.setStatus("Z");
        DemandaTecnica saved = demandaRepository.save(demanda);
        return DemandaTecnicaDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<DemandaTecnicaDTO> findDemandasEmFluxoPaginado(
            Long projetoId,
            Pageable pageable) {

        if (projetoId == null) {
            throw new BadRequestException("projetoId é obrigatório.");
        }

        return demandaRepository
            .findByProjetoIdAndStatusEmFluxo(projetoId, pageable)
            .map(DemandaTecnicaDTO::fromEntity);
    }

}
