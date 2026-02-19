package com.demandtracker.service;

import com.demandtracker.dto.DemandaAvaliacaoDocCreateDTO;
import com.demandtracker.dto.DemandaAvaliacaoDocResponseDTO;
import com.demandtracker.dto.DemandaAvaliacaoDocUpdateDTO;
import com.demandtracker.entity.DemandaAvaliacao;
import com.demandtracker.entity.DemandaAvaliacaoDoc;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaAvaliacaoDocRepository;
import com.demandtracker.repository.DemandaAvaliacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandaAvaliacaoDocService {

    private final DemandaAvaliacaoDocRepository repository;
    private final DemandaAvaliacaoRepository avaliacaoRepository;

    @Transactional
    public DemandaAvaliacaoDocResponseDTO create(DemandaAvaliacaoDocCreateDTO dto) throws IOException {
        DemandaAvaliacao avaliacao = avaliacaoRepository.findById(dto.getAvaliacaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada com ID: " + dto.getAvaliacaoId()));

        if (repository.existsByAvaliacao_Id(dto.getAvaliacaoId())) {
            throw new BadRequestException("Já existe um documento para esta Avaliação");
        }

        MultipartFile arquivo = dto.getArquivoPdf();
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BadRequestException("Arquivo PDF é obrigatório");
        }
        if (!"application/pdf".equals(arquivo.getContentType())) {
            throw new BadRequestException("O arquivo deve ser um PDF");
        }

        DemandaAvaliacaoDoc doc = new DemandaAvaliacaoDoc();
        doc.setAvaliacao(avaliacao);
        doc.setDataUpload(dto.getDataUpload() != null ? dto.getDataUpload() : LocalDateTime.now());
        doc.setArquivoPdf(arquivo.getBytes());
        doc.setNomeArquivo(arquivo.getOriginalFilename());
        doc.setTipoConteudo(arquivo.getContentType());
        doc.setTamanhoArquivo(arquivo.getSize());

        DemandaAvaliacaoDoc saved = repository.save(doc);
        return toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public DemandaAvaliacaoDocResponseDTO findById(Long id) {
        DemandaAvaliacaoDoc doc = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + id));
        return toResponseDTO(doc);
    }

    @Transactional(readOnly = true)
    public DemandaAvaliacaoDocResponseDTO findByAvaliacaoId(Long avaliacaoId) {
        DemandaAvaliacaoDoc doc = repository.findByAvaliacao_Id(avaliacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado para a Avaliação ID: " + avaliacaoId));
        return toResponseDTO(doc);
    }

    /**
     * Busca documento da avaliação pela demanda (1:1 avaliação por demanda).
     */
    @Transactional(readOnly = true)
    public DemandaAvaliacaoDocResponseDTO findByDemandaId(Long demandaId) {
        DemandaAvaliacao avaliacao = avaliacaoRepository.findByDemandaId(demandaId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada para a Demanda ID: " + demandaId));
        return findByAvaliacaoId(avaliacao.getId());
    }

    @Transactional(readOnly = true)
    public byte[] getArquivoPdf(Long id) {
        DemandaAvaliacaoDoc doc = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + id));
        return doc.getArquivoPdf();
    }

    @Transactional(readOnly = true)
    public byte[] getArquivoPdfByAvaliacaoId(Long avaliacaoId) {
        DemandaAvaliacaoDoc doc = repository.findByAvaliacao_Id(avaliacaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado para a Avaliação ID: " + avaliacaoId));
        return doc.getArquivoPdf();
    }

    @Transactional(readOnly = true)
    public byte[] getArquivoPdfByDemandaId(Long demandaId) {
        DemandaAvaliacao avaliacao = avaliacaoRepository.findByDemandaId(demandaId)
                .orElseThrow(() -> new ResourceNotFoundException("Avaliação não encontrada para a Demanda ID: " + demandaId));
        return getArquivoPdfByAvaliacaoId(avaliacao.getId());
    }

    @Transactional(readOnly = true)
    public List<DemandaAvaliacaoDocResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DemandaAvaliacaoDocResponseDTO update(Long id, DemandaAvaliacaoDocUpdateDTO dto) throws IOException {
        DemandaAvaliacaoDoc doc = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + id));

        if (dto.getDataUpload() != null) {
            doc.setDataUpload(dto.getDataUpload());
        }

        if (dto.getArquivoPdf() != null && !dto.getArquivoPdf().isEmpty()) {
            if (!"application/pdf".equals(dto.getArquivoPdf().getContentType())) {
                throw new BadRequestException("O arquivo deve ser um PDF");
            }
            doc.setArquivoPdf(dto.getArquivoPdf().getBytes());
            doc.setNomeArquivo(dto.getArquivoPdf().getOriginalFilename());
            doc.setTipoConteudo(dto.getArquivoPdf().getContentType());
            doc.setTamanhoArquivo(dto.getArquivoPdf().getSize());
        }

        DemandaAvaliacaoDoc updated = repository.save(doc);
        return toResponseDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Documento não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public void deleteByAvaliacaoId(Long avaliacaoId) {
        repository.deleteByAvaliacao_Id(avaliacaoId);
    }

    @Transactional(readOnly = true)
    public boolean existsByAvaliacaoId(Long avaliacaoId) {
        return repository.existsByAvaliacao_Id(avaliacaoId);
    }

    @Transactional(readOnly = true)
    public boolean existsByDemandaId(Long demandaId) {
        return avaliacaoRepository.findByDemandaId(demandaId)
                .map(a -> repository.existsByAvaliacao_Id(a.getId()))
                .orElse(false);
    }

    private DemandaAvaliacaoDocResponseDTO toResponseDTO(DemandaAvaliacaoDoc doc) {
        Long demandaId = doc.getAvaliacao() != null && doc.getAvaliacao().getDemanda() != null
                ? doc.getAvaliacao().getDemanda().getId()
                : null;
        return new DemandaAvaliacaoDocResponseDTO(
                doc.getId(),
                doc.getAvaliacao().getId(),
                demandaId,
                doc.getDataUpload(),
                doc.getNomeArquivo(),
                doc.getTipoConteudo(),
                doc.getTamanhoArquivo()
        );
    }
}
