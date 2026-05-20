package com.demandtracker.service;

import com.demandtracker.dto.ProfissionalCustoMensalCreateDTO;
import com.demandtracker.dto.ProfissionalCustoMensalDTO;
import com.demandtracker.dto.ProfissionalCustoMensalUpdateDTO;
import com.demandtracker.entity.Profissional;
import com.demandtracker.entity.ProfissionalCustoMensal;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.ProfissionalCustoMensalRepository;
import com.demandtracker.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfissionalCustoMensalService {

    private final ProfissionalCustoMensalRepository repository;
    private final ProfissionalRepository profissionalRepository;

    @Transactional(readOnly = true)
    public Page<ProfissionalCustoMensalDTO> findAll(Long profissionalId, Integer ano, Integer mes, Pageable pageable) {
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<ProfissionalCustoMensal> page = repository.findAllFiltered(profissionalId, ano, mes, pageRequest);
        return page.map(ProfissionalCustoMensalDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProfissionalCustoMensalDTO findById(Long id) {
        ProfissionalCustoMensal entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custo mensal não encontrado com ID: " + id));
        return ProfissionalCustoMensalDTO.fromEntity(entity);
    }

    @Transactional
    public ProfissionalCustoMensalDTO create(ProfissionalCustoMensalCreateDTO dto) {
        Profissional profissional = profissionalRepository.findById(dto.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + dto.getProfissionalId()));

        ProfissionalCustoMensal entity = new ProfissionalCustoMensal();
        entity.setProfissional(profissional);
        entity.setAno(dto.getAno());
        entity.setMes(dto.getMes());
        entity.setCustoTotal(dto.getCustoTotal());

        return ProfissionalCustoMensalDTO.fromEntity(repository.save(entity));
    }

    @Transactional
    public ProfissionalCustoMensalDTO update(Long id, ProfissionalCustoMensalUpdateDTO dto) {
        ProfissionalCustoMensal entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custo mensal não encontrado com ID: " + id));

        if (dto.getProfissionalId() != null) {
            Profissional profissional = profissionalRepository.findById(dto.getProfissionalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + dto.getProfissionalId()));
            entity.setProfissional(profissional);
        }
        if (dto.getAno() != null) {
            entity.setAno(dto.getAno());
        }
        if (dto.getMes() != null) {
            entity.setMes(dto.getMes());
        }
        if (dto.getCustoTotal() != null) {
            entity.setCustoTotal(dto.getCustoTotal());
        }

        return ProfissionalCustoMensalDTO.fromEntity(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Custo mensal não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }
}
