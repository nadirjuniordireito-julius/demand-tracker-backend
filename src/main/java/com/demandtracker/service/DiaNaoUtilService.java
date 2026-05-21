package com.demandtracker.service;

import com.demandtracker.dto.DiaNaoUtilCreateDTO;
import com.demandtracker.dto.DiaNaoUtilDTO;
import com.demandtracker.dto.DiaNaoUtilUpdateDTO;
import com.demandtracker.entity.DiaNaoUtil;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DiaNaoUtilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DiaNaoUtilService {

    private final DiaNaoUtilRepository repository;

    @Transactional(readOnly = true)
    public Page<DiaNaoUtilDTO> findAll(LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        if (dataInicio != null && dataFim != null && dataFim.isBefore(dataInicio)) {
            throw new BadRequestException("dataFim não pode ser anterior a dataInicio.");
        }
        Sort sort = pageable.getSort().isUnsorted() ? Sort.by("data").ascending() : pageable.getSort();
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<DiaNaoUtil> page;
        if (dataInicio != null && dataFim != null) {
            page = repository.findByDataBetween(dataInicio, dataFim, pageRequest);
        } else if (dataInicio != null) {
            page = repository.findByDataGreaterThanEqual(dataInicio, pageRequest);
        } else if (dataFim != null) {
            page = repository.findByDataLessThanEqual(dataFim, pageRequest);
        } else {
            page = repository.findAll(pageRequest);
        }
        return page.map(DiaNaoUtilDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public DiaNaoUtilDTO findById(Long id) {
        DiaNaoUtil entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dia não útil não encontrado com ID: " + id));
        return DiaNaoUtilDTO.fromEntity(entity);
    }

    @Transactional
    public DiaNaoUtilDTO create(DiaNaoUtilCreateDTO dto) {
        if (repository.existsByData(dto.getData())) {
            throw new BadRequestException("Já existe um dia não útil cadastrado para a data: " + dto.getData());
        }

        DiaNaoUtil entity = new DiaNaoUtil();
        entity.setData(dto.getData());
        entity.setDescricao(dto.getDescricao().trim());

        return DiaNaoUtilDTO.fromEntity(repository.save(entity));
    }

    @Transactional
    public DiaNaoUtilDTO update(Long id, DiaNaoUtilUpdateDTO dto) {
        DiaNaoUtil entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dia não útil não encontrado com ID: " + id));

        if (dto.getData() != null) {
            if (repository.existsByDataAndIdNot(dto.getData(), id)) {
                throw new BadRequestException("Já existe um dia não útil cadastrado para a data: " + dto.getData());
            }
            entity.setData(dto.getData());
        }
        if (dto.getDescricao() != null) {
            String descricao = dto.getDescricao().trim();
            if (descricao.isBlank()) {
                throw new BadRequestException("Descrição não pode ser vazia.");
            }
            entity.setDescricao(descricao);
        }

        return DiaNaoUtilDTO.fromEntity(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Dia não útil não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }
}
