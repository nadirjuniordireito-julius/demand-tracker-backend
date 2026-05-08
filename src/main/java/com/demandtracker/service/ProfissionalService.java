package com.demandtracker.service;

import com.demandtracker.dto.ProfissionalCreateDTO;
import com.demandtracker.dto.ProfissionalDTO;
import com.demandtracker.entity.Profissional;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.Perfil;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.ProfissionalRepository;
import com.demandtracker.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.demandtracker.dto.ProfissionalUpdateDTO;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final ProjetoRepository projetoRepository;
    private final com.demandtracker.repository.PerfilRepository perfilRepository;

    @Transactional(readOnly = true)
    public Page<ProfissionalDTO> findAll(String nome, Long projetoId, Pageable pageable) {
        Page<Profissional> profissionais;

        if (nome != null && projetoId != null) {
            profissionais = profissionalRepository.findByNomeContainingIgnoreCaseAndProjetoId(nome, projetoId, pageable);
        } else if (nome != null) {
            profissionais = profissionalRepository.findByNomeContainingIgnoreCase(nome, pageable);
        } else if (projetoId != null) {
            profissionais = profissionalRepository.findByProjetoId(projetoId, pageable);
        } else {
            profissionais = profissionalRepository.findAll(pageable);
        }

        return profissionais.map(ProfissionalDTO::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProfissionalDTO findById(Long id) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + id));
        return ProfissionalDTO.fromEntity(profissional);
    }

    @Transactional
    public ProfissionalDTO create(ProfissionalCreateDTO dto) {
        if (profissionalRepository.existsByDocumento(dto.getDocumento())) {
            throw new BadRequestException(
                    "Já existe um profissional cadastrado com o documento informado: " + dto.getDocumento());
        }

        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));

        Perfil perfil = perfilRepository.findById(dto.getPerfilId())
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + dto.getPerfilId()));

        Profissional profissional = new Profissional();
        profissional.setNome(dto.getNome());
        profissional.setDataInicioAtividade(dto.getDataInicioAtividade());
        profissional.setTipoPessoa(dto.getTipoPessoa());
        profissional.setDocumento(dto.getDocumento());
        profissional.setValorHora(dto.getValorHora());
        profissional.setCustoTotalMensal(dto.getCustoTotalMensal());
        profissional.setFuncao(dto.getFuncaoProfissional());
        profissional.setProjeto(projeto);
        profissional.setPerfil(perfil);

        Profissional saved = profissionalRepository.save(profissional);
        return ProfissionalDTO.fromEntity(saved);
    }

    @Transactional
    public ProfissionalDTO update(Long id, ProfissionalUpdateDTO dto) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado com ID: " + id));

        if (dto.getDocumento() != null && !dto.getDocumento().equals(profissional.getDocumento())
                && profissionalRepository.existsByDocumentoAndIdNot(dto.getDocumento(), id)) {
            throw new BadRequestException(
                    "Já existe um profissional cadastrado com o documento informado: " + dto.getDocumento());
        }

        if (dto.getNome() != null) {
            profissional.setNome(dto.getNome());
        }
        if (dto.getDataInicioAtividade() != null) {
            profissional.setDataInicioAtividade(dto.getDataInicioAtividade());
        }
        if (dto.getValorHora() != null) {
            profissional.setValorHora(dto.getValorHora());
        }
        if (dto.getCustoTotalMensal() != null) {
            profissional.setCustoTotalMensal(dto.getCustoTotalMensal());
        }
        if (dto.getProjetoId() != null) {
            Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));
            profissional.setProjeto(projeto);
        }
        if (dto.getTipoPessoa() != null) {
            profissional.setTipoPessoa(dto.getTipoPessoa());
        }
        if (dto.getDocumento() != null) {
            profissional.setDocumento(dto.getDocumento());
        }
        if (dto.getFuncao() != null) {
            profissional.setFuncao(dto.getFuncao());
        }
        if (dto.getPerfilId() != null) {
            Perfil perfil = perfilRepository.findById(dto.getPerfilId())
                    .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado com ID: " + dto.getPerfilId()));
            profissional.setPerfil(perfil);
        }

        Profissional saved = profissionalRepository.save(profissional);
        return ProfissionalDTO.fromEntity(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!profissionalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Profissional não encontrado com ID: " + id);
        }
        profissionalRepository.deleteById(id);
    }
}
