package com.demandtracker.service;

import com.demandtracker.dto.TemplateDemandaCreateDTO;
import com.demandtracker.dto.TemplateDemandaResponseDTO;
import com.demandtracker.dto.TemplateDemandaUpdateDTO;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.TemplateDemanda;
import com.demandtracker.repository.ProjetoRepository;
import com.demandtracker.repository.TemplateDemandaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateDemandaService {

    private final TemplateDemandaRepository repository;
    private final ProjetoRepository projetoRepository;

    /**
     * Cria um novo template de demanda
     */
    @Transactional
    public TemplateDemandaResponseDTO create(TemplateDemandaCreateDTO dto) throws IOException {
        // Valida se o projeto existe
        Projeto projeto = projetoRepository.findById(dto.getProjetoId())
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado com ID: " + dto.getProjetoId()));

        // Valida se o tipo tem apenas 1 caractere
        if (dto.getTipo() == null || dto.getTipo().length() != 1) {
            throw new RuntimeException("O tipo deve ter exatamente 1 caractere");
        }

        // Valida se já existe template para este projeto e tipo
        if (repository.existsByProjetoIdAndTipo(dto.getProjetoId(), dto.getTipo())) {
            throw new RuntimeException("Já existe um template para este Projeto e Tipo");
        }

        // Valida se o arquivo é DOCX
        MultipartFile arquivo = dto.getArquivoDocx();
        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            throw new RuntimeException("O arquivo deve ser um DOCX");
        }

        // Cria a entidade
        TemplateDemanda template = new TemplateDemanda();
        template.setProjeto(projeto);
        template.setTipo(dto.getTipo());
        template.setArquivoDocx(arquivo.getBytes());
        template.setNomeArquivo(arquivo.getOriginalFilename());
        template.setTipoConteudo(arquivo.getContentType());
        template.setTamanhoArquivo(arquivo.getSize());

        // Salva o template
        TemplateDemanda saved = repository.save(template);
        
        return toResponseDTO(saved);
    }

    /**
     * Busca template por ID
     */
    @Transactional(readOnly = true)
    public TemplateDemandaResponseDTO findById(Long id) {
        TemplateDemanda template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado com ID: " + id));
        return toResponseDTO(template);
    }

    /**
     * Busca templates por ID do Projeto
     */
    @Transactional(readOnly = true)
    public List<TemplateDemandaResponseDTO> findByProjetoId(Long projetoId) {
        return repository.findByProjetoId(projetoId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca template por ID do Projeto e Tipo
     */
    @Transactional(readOnly = true)
    public TemplateDemandaResponseDTO findByProjetoIdAndTipo(Long projetoId, String tipo) {
        TemplateDemanda template = repository.findByProjetoIdAndTipo(projetoId, tipo)
                .orElseThrow(() -> new RuntimeException("Template não encontrado para o Projeto ID: " + projetoId + " e Tipo: " + tipo));
        return toResponseDTO(template);
    }

    /**
     * Retorna o arquivo DOCX do template
     */
    @Transactional(readOnly = true)
    public byte[] getArquivoDocx(Long id) {
        TemplateDemanda template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado com ID: " + id));
        return template.getArquivoDocx();
    }

    /**
     * Retorna o arquivo DOCX pelo ID do Projeto e Tipo
     */
    @Transactional(readOnly = true)
    public byte[] getArquivoDocxByProjetoIdAndTipo(Long projetoId, String tipo) {
        TemplateDemanda template = repository.findByProjetoIdAndTipo(projetoId, tipo)
                .orElseThrow(() -> new RuntimeException("Template não encontrado para o Projeto ID: " + projetoId + " e Tipo: " + tipo));
        return template.getArquivoDocx();
    }

    /**
     * Lista todos os templates
     */
    @Transactional(readOnly = true)
    public List<TemplateDemandaResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza um template
     */
    @Transactional
    public TemplateDemandaResponseDTO update(Long id, TemplateDemandaUpdateDTO dto) throws IOException {
        TemplateDemanda template = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template não encontrado com ID: " + id));

        if (dto.getTipo() != null) {
            // Valida se o tipo tem apenas 1 caractere
            if (dto.getTipo().length() != 1) {
                throw new RuntimeException("O tipo deve ter exatamente 1 caractere");
            }
            
            // Valida se já existe outro template para este projeto e tipo
            if (repository.existsByProjetoIdAndTipo(template.getProjeto().getId(), dto.getTipo()) 
                    && !template.getTipo().equals(dto.getTipo())) {
                throw new RuntimeException("Já existe um template para este Projeto e Tipo");
            }
            
            template.setTipo(dto.getTipo());
        }

        if (dto.getArquivoDocx() != null && !dto.getArquivoDocx().isEmpty()) {
            // Valida se o arquivo é DOCX
            String contentType = dto.getArquivoDocx().getContentType();
            if (contentType == null || !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                throw new RuntimeException("O arquivo deve ser um DOCX");
            }
            template.setArquivoDocx(dto.getArquivoDocx().getBytes());
            template.setNomeArquivo(dto.getArquivoDocx().getOriginalFilename());
            template.setTipoConteudo(dto.getArquivoDocx().getContentType());
            template.setTamanhoArquivo(dto.getArquivoDocx().getSize());
        }

        TemplateDemanda updated = repository.save(template);
        
        return toResponseDTO(updated);
    }

    /**
     * Deleta um template
     */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Template não encontrado com ID: " + id);
        }
        
        repository.deleteById(id);
    }

    /**
     * Deleta templates por ID do Projeto
     */
    @Transactional
    public void deleteByProjetoId(Long projetoId) {
        repository.deleteByProjetoId(projetoId);
    }

    /**
     * Verifica se existe template para um Projeto e Tipo
     */
    @Transactional(readOnly = true)
    public boolean existsByProjetoIdAndTipo(Long projetoId, String tipo) {
        return repository.existsByProjetoIdAndTipo(projetoId, tipo);
    }

    /**
     * Converte entidade para DTO de resposta
     */
    private TemplateDemandaResponseDTO toResponseDTO(TemplateDemanda template) {
        return new TemplateDemandaResponseDTO(
                template.getId(),
                template.getProjeto().getId(),
                template.getTipo(),
                template.getNomeArquivo(),
                template.getTipoConteudo(),
                template.getTamanhoArquivo()
        );
    }
}
