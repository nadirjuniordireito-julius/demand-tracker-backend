package com.demandtracker.service;

import com.demandtracker.dto.TermoEncerramentoCustoProfissionalDTO;
import com.demandtracker.entity.*;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TermoEncerramentoCustoProfissionalService {

    private final TermoEncerramentoCustoProfissionalRepository repository;
    private final TermoEncerramentoCustoRepository termoEncerramentoCustoRepository;
    private final ProfissionalRepository profissionalRepository;

    @Transactional(readOnly = true)
    public Page<TermoEncerramentoCustoProfissionalDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(TermoEncerramentoCustoProfissionalDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<TermoEncerramentoCustoProfissionalDTO> findByTermoEncerramentoCustoId(Long termoEncerramentoCustoId) {
        return repository.findByTermoEncerramentoCustoId(termoEncerramentoCustoId).stream()
                .map(TermoEncerramentoCustoProfissionalDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TermoEncerramentoCustoProfissionalDTO findById(Long id) {
        TermoEncerramentoCustoProfissional entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custo profissional não encontrado com ID: " + id));

        return TermoEncerramentoCustoProfissionalDTO.fromEntity(entity);
    }

    @Transactional
    public TermoEncerramentoCustoProfissionalDTO create(TermoEncerramentoCustoProfissionalDTO dto) {
        if (dto.getTermoEncerramentoId() == null) {
            throw new BadRequestException("ID do termo de encerramento custo é obrigatório.");
        }
        if (dto.getProfissional() == null || dto.getProfissional().getId() == null) {
            throw new BadRequestException("Profissional é obrigatório (informe o ID do profissional).");
        }
        if (dto.getQtdeHora() == null || dto.getQtdeHora().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Quantidade de horas é obrigatória e deve ser maior que zero.");
        }
        if (dto.getValorHora() == null || dto.getValorHora().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Valor hora é obrigatório e deve ser maior que zero.");
        }

        TermoEncerramentoCusto termoEncerramentoCusto =
                termoEncerramentoCustoRepository.findById(dto.getTermoEncerramentoId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "TermoEncerramentoCusto não encontrado"));

        Profissional profissional =
                profissionalRepository.findById(dto.getProfissional().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Profissional não encontrado"));

        TermoEncerramentoCustoProfissional entity = new TermoEncerramentoCustoProfissional();
        entity.setTermoEncerramentoCusto(termoEncerramentoCusto);
        entity.setProfissional(profissional);
        entity.setQtdeHora(dto.getQtdeHora());
        entity.setValorHora(dto.getValorHora());

        return TermoEncerramentoCustoProfissionalDTO.fromEntity(repository.save(entity));
    }

    @Transactional
    public TermoEncerramentoCustoProfissionalDTO update(Long id, TermoEncerramentoCustoProfissionalDTO dto) {
        TermoEncerramentoCustoProfissional entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Custo profissional não encontrado com ID: " + id));

        if (dto.getQtdeHora() != null) {
            if (dto.getQtdeHora().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Quantidade de horas deve ser maior que zero.");
            }
            entity.setQtdeHora(dto.getQtdeHora());
        }
        if (dto.getValorHora() != null) {
            if (dto.getValorHora().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Valor hora deve ser maior que zero.");
            }
            entity.setValorHora(dto.getValorHora());
        }

        return TermoEncerramentoCustoProfissionalDTO.fromEntity(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Custo profissional não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPorTermoEncerramentoCusto(Long termoEncerramentoCustoId) {
        return repository.sumTotalByTermoEncerramentoCustoId(termoEncerramentoCustoId);
    }
}
