package com.demandtracker.service;

import com.demandtracker.dto.TermoEncerramentoDocCreateDTO;
import com.demandtracker.dto.TermoEncerramentoDocResponseDTO;
import com.demandtracker.dto.TermoEncerramentoDocUpdateDTO;
import com.demandtracker.entity.TermoEncerramento;
import com.demandtracker.entity.TermoEncerramentoDoc;
import com.demandtracker.entity.Usuario;
import com.demandtracker.repository.TermoEncerramentoDocRepository;
import com.demandtracker.repository.TermoEncerramentoRepository;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.demandtracker.service.TermoEncerramentoService;

@Service
@RequiredArgsConstructor
public class TermoEncerramentoDocService {

    private final TermoEncerramentoDocRepository repository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;
    private final UsuarioRepository  usuarioRepository;
    private final TermoEncerramentoService termoEncerramentoService;
    private final DocumentoService documentoService;
    
    /**
     * Cria um novo documento para um Termo de Encerramento
     */
    @Transactional
    public TermoEncerramentoDocResponseDTO create(TermoEncerramentoDocCreateDTO dto) throws IOException {
        // Valida se o termo existe
        TermoEncerramento termoEncerramento = termoEncerramentoRepository.findById(dto.getTermoEncerramentoId())
                .orElseThrow(() -> new RuntimeException("Termo de Encerramento não encontrado com ID: " + dto.getTermoEncerramentoId()));

        // Valida se já existe documento para este termo
        if (repository.existsByTermoEncerramentoId(dto.getTermoEncerramentoId())) {
            throw new RuntimeException("Já existe um documento para este Termo de Encerramento");
        }

        // Valida se o arquivo é PDF
        MultipartFile arquivo = dto.getArquivoPdf();
        if (!arquivo.getContentType().equals("application/pdf")) {
            throw new RuntimeException("O arquivo deve ser um PDF");
        }

        // Cria a entidade
        TermoEncerramentoDoc doc = new TermoEncerramentoDoc();
        doc.setTermoEncerramento(termoEncerramento);
        // doc.setDataAssinatura(dto.getDataAssinatura() != null ? dto.getDataAssinatura() : LocalDateTime.now());
        doc.setArquivoPdf(arquivo.getBytes());
        doc.setNomeArquivo(arquivo.getOriginalFilename());
        doc.setTipoConteudo(arquivo.getContentType());
        doc.setTamanhoArquivo(arquivo.getSize());

        // Salva
        TermoEncerramentoDoc saved = repository.save(doc);
        return toResponseDTO(saved);
    }

    /**
     * Busca documento por ID
     */
    @Transactional(readOnly = true)
    public TermoEncerramentoDocResponseDTO findById(Long id) {
        TermoEncerramentoDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));
        return toResponseDTO(doc);
    }

    /**
     * Busca documento pelo ID do Termo de Encerramento
     */
    @Transactional(readOnly = true)
    public TermoEncerramentoDocResponseDTO findByTermoEncerramentoId(Long termoEncerramentoId) {
        TermoEncerramentoDoc doc = repository.findByTermoEncerramentoId(termoEncerramentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado para o Termo de Encerramento ID: " + termoEncerramentoId));
        return toResponseDTO(doc);
    }

    /**
     * Retorna o arquivo PDF do documento
     */
    @Transactional(readOnly = true)
    public byte[] getArquivoPdf(Long id) {
        TermoEncerramentoDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));
        return doc.getArquivoPdf();
    }

    /**
     * Retorna o arquivo PDF pelo ID do Termo de Encerramento
     */
    @Transactional(readOnly = true)
    public byte[] getArquivoPdfByTermoEncerramentoId(Long termoEncerramentoId) {
        TermoEncerramentoDoc doc = repository.findByTermoEncerramentoId(termoEncerramentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado para o Termo de Encerramento ID: " + termoEncerramentoId));
        return doc.getArquivoPdf();
    }

    /**
     * Lista todos os documentos
     */
    @Transactional(readOnly = true)
    public List<TermoEncerramentoDocResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza um documento
     */
    @Transactional
    public TermoEncerramentoDocResponseDTO update(Long id, TermoEncerramentoDocUpdateDTO dto) throws IOException {
        TermoEncerramentoDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));

        
        if (dto.getArquivoPdf() != null && !dto.getArquivoPdf().isEmpty()) {
            // Valida se o arquivo é PDF
            if (!dto.getArquivoPdf().getContentType().equals("application/pdf")) {
                throw new RuntimeException("O arquivo deve ser um PDF");
            }
            doc.setArquivoPdf(dto.getArquivoPdf().getBytes());
            doc.setNomeArquivo(dto.getArquivoPdf().getOriginalFilename());
            doc.setTipoConteudo(dto.getArquivoPdf().getContentType());
            doc.setTamanhoArquivo(dto.getArquivoPdf().getSize());
        }

        TermoEncerramentoDoc updated = repository.save(doc);
        return toResponseDTO(updated);
    }

    /**
     * Deleta um documento
     */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Documento não encontrado com ID: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Deleta documento pelo ID do Termo de Encerramento
     */
    @Transactional
    public void deleteByTermoEncerramentoId(Long termoEncerramentoId) {
        repository.deleteByTermoEncerramentoId(termoEncerramentoId);
    }

    /**
     * Verifica se existe documento para um Termo de Encerramento
     */
    @Transactional(readOnly = true)
    public boolean existsByTermoEncerramentoId(Long termoEncerramentoId) {
        return repository.existsByTermoEncerramentoId(termoEncerramentoId);
    }

    /**
     * Converte entidade para DTO de resposta
     */
    private TermoEncerramentoDocResponseDTO toResponseDTO(TermoEncerramentoDoc doc) {
        return new TermoEncerramentoDocResponseDTO(
                doc.getId(),
                doc.getTermoEncerramento().getId(),
                doc.getDataAssinatura(),
                doc.getNomeArquivo(),
                doc.getTipoConteudo(),
                doc.getTamanhoArquivo()
        );
    }

    @Transactional
    public void assinar(Long documentoId, String hashPdf, Long usuarioId, Long page, Float x, Float y, Long width, Long height, HttpServletRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + usuarioId));

        TermoEncerramentoDoc doc = repository.findById(documentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + documentoId));

        LocalDateTime dataAssinatura = LocalDateTime.now();
        doc.setHashPdf(hashPdf);
        doc.setIp(request.getRemoteAddr());
        doc.setUserAgent(request.getHeader("User-Agent"));
        doc.setDataAssinatura(dataAssinatura);
        doc.setUsuarioSignaturer(usuario);

        // carimbar o PDF (página 0-based; frontend pode enviar pageNumber 1-based)
        byte[] pdf = doc.getArquivoPdf();
        Long pagina = (page != null && page > 0) ? page - 1 : 0L;
        try {
            byte[] newPdf = documentoService.carimbarAssinatura(
                    pdf, usuario.getNome(), hashPdf, doc.getIp(), dataAssinatura,
                    pagina, x, y, width, height);
            doc.setArquivoPdf(newPdf);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carimbar assinatura no PDF: " + e.getMessage(), e);
        }

        repository.save(doc);

        // Encerrar a demanda
        termoEncerramentoService.sign(doc.getTermoEncerramento().getId());
    }
}

