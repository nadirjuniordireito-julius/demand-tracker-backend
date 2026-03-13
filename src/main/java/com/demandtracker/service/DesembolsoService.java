package com.demandtracker.service;

import com.demandtracker.dto.DesembolsoCreateDTO;
import com.demandtracker.dto.DesembolsoDTO;
import com.demandtracker.dto.DesembolsoUpdateDTO;
import com.demandtracker.entity.Desembolso;
import com.demandtracker.entity.Projeto;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DesembolsoRepository;
import com.demandtracker.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DesembolsoService {

    private final DesembolsoRepository desembolsoRepository;
    private final ProjetoRepository projetoRepository;

    @Transactional(readOnly = true)
    public Page<DesembolsoDTO> findAll(Long projetoId, String documento, Pageable pageable) {
        if (projetoId == null) {
            throw new BadRequestException("projetoId é obrigatório.");
        }

        Page<Desembolso> page;

        if (documento != null && !documento.isBlank()) {
            page = desembolsoRepository.findByProjetoIdAndDocumentoContainingIgnoreCase(projetoId, documento, pageable);
        } else {
            page = desembolsoRepository.findByProjetoId(projetoId, pageable);
        }

        return page.map(DesembolsoDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public DesembolsoDTO findById(Long id) {
        Desembolso desembolso = desembolsoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desembolso não encontrado com ID: " + id));
        return DesembolsoDTO.fromEntity(desembolso);
    }

    @Transactional
    public DesembolsoDTO create(DesembolsoCreateDTO dto) {
        Desembolso desembolso = new Desembolso();
        desembolso.setDocumento(dto.getDocumento());
        desembolso.setValorPrevisto(dto.getValorPrevisto());
        desembolso.setValor(dto.getValor());
        desembolso.setDataDesembolso(dto.getDataDesembolso());
        desembolso.setDataPrevistaDesembolso(dto.getDataPrevistaDesembolso());

        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
        desembolso.setProjeto(projeto);

        Desembolso saved = desembolsoRepository.save(desembolso);
        return DesembolsoDTO.fromEntity(saved);
    }

    @Transactional
    public DesembolsoDTO update(Long id, DesembolsoUpdateDTO dto) {
        Desembolso desembolso = desembolsoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Desembolso não encontrado com ID: " + id));

        if (dto.getDocumento() != null) {
            desembolso.setDocumento(dto.getDocumento());
        }
        if (dto.getValorPrevisto() != null) {
            desembolso.setValorPrevisto(dto.getValorPrevisto());
        }
        if (dto.getValor() != null) {
            desembolso.setValor(dto.getValor());
        }
        if (dto.getDataDesembolso() != null) {
            desembolso.setDataDesembolso(dto.getDataDesembolso());
        }
        if (dto.getDataPrevistaDesembolso() != null) {
            desembolso.setDataPrevistaDesembolso(dto.getDataPrevistaDesembolso());
        }

        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            desembolso.setProjeto(projeto);
        }

        Desembolso saved = desembolsoRepository.save(desembolso);
        return DesembolsoDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!desembolsoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Desembolso não encontrado com ID: " + id);
        }
        desembolsoRepository.deleteById(id);
    }
}

