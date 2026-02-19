package com.demandtracker.service;

import com.demandtracker.dto.ProjetoDocCreateDTO;
import com.demandtracker.dto.ProjetoDocResponseDTO;
import com.demandtracker.dto.ProjetoDocUpdateDTO;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.ProjetoDoc;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.ProjetoDocRepository;
import com.demandtracker.repository.ProjetoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetoDocService {

    private final ProjetoDocRepository repository;
    private final ProjetoRepository projetoRepository;

    /**
     * Cria um novo documento para um Projeto
     */
    @Transactional
    public ProjetoDocResponseDTO create(ProjetoDocCreateDTO dto) throws IOException {
        // Valida se o projeto existe
        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + dto.getProjetoId()));

        // Valida se o arquivo foi enviado
        MultipartFile arquivo = dto.getDocumento();
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RuntimeException("Documento é obrigatório");
        }

        // Cria a entidade
        ProjetoDoc doc = new ProjetoDoc();
        doc.setProjeto(projeto);
        doc.setNome(dto.getNome());
        doc.setDocumento(arquivo.getBytes());
        doc.setNomeArquivo(arquivo.getOriginalFilename());
        doc.setTipoConteudo(arquivo.getContentType());
        doc.setTamanhoArquivo(arquivo.getSize());

        // Salva o documento
        ProjetoDoc saved = repository.save(doc);
        
        return toResponseDTO(saved);
    }

    /**
     * Busca documento por ID
     */
    @Transactional(readOnly = true)
    public ProjetoDocResponseDTO findById(Long id) {
        ProjetoDoc doc = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + id));
        return toResponseDTO(doc);
    }

    /**
     * Busca documentos pelo ID do Projeto
     */
    @Transactional(readOnly = true)
    public List<ProjetoDocResponseDTO> findByProjetoId(Long projetoId) {
        List<ProjetoDoc> docs = repository.findByProjetoId(projetoId);
        return docs.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna o arquivo do documento
     */
    @Transactional(readOnly = true)
    public byte[] getDocumento(Long id) {
        ProjetoDoc doc = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + id));
        return doc.getDocumento();
    }

    /**
     * Lista todos os documentos
     */
    @Transactional(readOnly = true)
    public List<ProjetoDocResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza um documento
     */
    @Transactional
    public ProjetoDocResponseDTO update(Long id, ProjetoDocUpdateDTO dto) throws IOException {
        ProjetoDoc doc = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Documento não encontrado com ID: " + id));

        if (dto.getNome() != null) {
            doc.setNome(dto.getNome());
        }

        if (dto.getDocumento() != null && !dto.getDocumento().isEmpty()) {
            doc.setDocumento(dto.getDocumento().getBytes());
            doc.setNomeArquivo(dto.getDocumento().getOriginalFilename());
            doc.setTipoConteudo(dto.getDocumento().getContentType());
            doc.setTamanhoArquivo(dto.getDocumento().getSize());
        }

        ProjetoDoc updated = repository.save(doc);
        
        return toResponseDTO(updated);
    }

    /**
     * Deleta um documento
     */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Documento não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Deleta documentos pelo ID do Projeto
     */
    @Transactional
    public void deleteByProjetoId(Long projetoId) {
        repository.deleteByProjetoId(projetoId);
    }

    /**
     * Verifica se existe documento para um Projeto
     */
    @Transactional(readOnly = true)
    public boolean existsByProjetoId(Long projetoId) {
        return repository.existsByProjetoId(projetoId);
    }

    /**
     * Converte entidade para DTO de resposta
     */
    private ProjetoDocResponseDTO toResponseDTO(ProjetoDoc doc) {
        return new ProjetoDocResponseDTO(
                doc.getId(),
                doc.getProjeto().getId(),
                doc.getNome(),
                doc.getNomeArquivo(),
                doc.getTipoConteudo(),
                doc.getTamanhoArquivo()
        );
    }
}
