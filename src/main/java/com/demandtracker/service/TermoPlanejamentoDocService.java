package com.demandtracker.service;

import com.demandtracker.dto.TermoPlanejamentoDocCreateDTO;
import com.demandtracker.dto.TermoPlanejamentoDocResponseDTO;
import com.demandtracker.dto.TermoPlanejamentoDocUpdateDTO;
import com.demandtracker.dto.TermoPlanejamentoDocValidacaoHashDTO;
import com.demandtracker.entity.TermoPlanejamento;
import com.demandtracker.entity.TermoPlanejamentoDoc;
import com.demandtracker.repository.TermoPlanejamentoDocRepository;
import com.demandtracker.repository.TermoPlanejamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import com.demandtracker.entity.Usuario;
import com.demandtracker.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class TermoPlanejamentoDocService {

    private final TermoPlanejamentoDocRepository repository;
    private final TermoPlanejamentoRepository termoPlanejamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TermoPlanejamentoService termoPlanejamentoService;
    private final DocumentoService documentoService;
    private final HashService hashService;
    /**
     * Cria um novo documento para um Termo de Planejamento
     */
    @Transactional
    public TermoPlanejamentoDocResponseDTO create(TermoPlanejamentoDocCreateDTO dto) throws IOException {
        // Valida se o termo existe
        TermoPlanejamento termoPlanejamento = termoPlanejamentoRepository.findById(dto.getTermoPlanejamentoId())
                .orElseThrow(() -> new RuntimeException("Termo de Planejamento não encontrado com ID: " + dto.getTermoPlanejamentoId()));
        termoPlanejamentoService.validarEnvioDocumentoPlanejamentoPermitida(dto.getTermoPlanejamentoId(), false);

        // Valida se já existe documento para este termo
        if (repository.existsByTermoPlanejamentoId(dto.getTermoPlanejamentoId())) {
            throw new RuntimeException("Já existe um documento para este Termo de Planejamento");
        }

        // Valida se o arquivo é PDF
        MultipartFile arquivo = dto.getArquivoPdf();
        if (!arquivo.getContentType().equals("application/pdf")) {
            throw new RuntimeException("O arquivo deve ser um PDF");
        }

        // Cria a entidade
        TermoPlanejamentoDoc doc = new TermoPlanejamentoDoc();
        doc.setTermoPlanejamento(termoPlanejamento);
        // doc.setDataAssinatura(dto.getDataAssinatura() != null ? dto.getDataAssinatura() : LocalDateTime.now());
        doc.setArquivoPdf(arquivo.getBytes());
        doc.setNomeArquivo(arquivo.getOriginalFilename());
        doc.setTipoConteudo(arquivo.getContentType());
        doc.setTamanhoArquivo(arquivo.getSize());

        // Salva
        TermoPlanejamentoDoc saved = repository.save(doc);
        termoPlanejamentoService.finalizarPlanejamentoComDocumento(dto.getTermoPlanejamentoId());
        return toResponseDTO(saved);
    }

    /**
     * Busca documento por ID
     */
    @Transactional(readOnly = true)
    public TermoPlanejamentoDocResponseDTO findById(Long id) {
        TermoPlanejamentoDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));
        return toResponseDTO(doc);
    }

    /**
     * Busca documento pelo ID do Termo de Planejamento
     */
    @Transactional(readOnly = true)
    public TermoPlanejamentoDocResponseDTO findByTermoPlanejamentoId(Long termoPlanejamentoId) {
        TermoPlanejamentoDoc doc = repository.findByTermoPlanejamentoId(termoPlanejamentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado para o Termo de Planejamento ID: " + termoPlanejamentoId));
        return toResponseDTO(doc);
    }

    /**
     * Retorna o arquivo PDF do documento
     */
    @Transactional(readOnly = true)
    public byte[] getArquivoPdf(Long id) {
        TermoPlanejamentoDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));
        return doc.getArquivoPdf();
    }

    /**
     * Retorna o arquivo PDF pelo ID do Termo de Planejamento
     */
    @Transactional(readOnly = true)
    public byte[] getArquivoPdfByTermoPlanejamentoId(Long termoPlanejamentoId) {
        TermoPlanejamentoDoc doc = repository.findByTermoPlanejamentoId(termoPlanejamentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado para o Termo de Planejamento ID: " + termoPlanejamentoId));
        return doc.getArquivoPdf();
    }

    /**
     * Lista todos os documentos
     */
    @Transactional(readOnly = true)
    public List<TermoPlanejamentoDocResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza um documento
     */
    @Transactional
    public TermoPlanejamentoDocResponseDTO update(Long id, TermoPlanejamentoDocUpdateDTO dto) throws IOException {
        TermoPlanejamentoDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));
        termoPlanejamentoService.validarEnvioDocumentoPlanejamentoPermitida(doc.getTermoPlanejamento().getId(), true);

        if (dto.getDataAssinatura() != null) {
            doc.setDataAssinatura(dto.getDataAssinatura());
        }

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

        TermoPlanejamentoDoc updated = repository.save(doc);
        termoPlanejamentoService.finalizarPlanejamentoComDocumento(doc.getTermoPlanejamento().getId());
        return toResponseDTO(updated);
    }

    /**
     * Deleta um documento
     */
    @Transactional
    public void delete(Long id) {
        TermoPlanejamentoDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));
        termoPlanejamentoService.validarExclusaoDocumentoPlanejamentoPermitida(doc.getTermoPlanejamento().getId());
        repository.deleteById(id);
    }

    /**
     * Deleta documento pelo ID do Termo de Planejamento
     */
    @Transactional
    public void deleteByTermoPlanejamentoId(Long termoPlanejamentoId) {
        repository.deleteByTermoPlanejamentoId(termoPlanejamentoId);
    }

    /**
     * Verifica se existe documento para um Termo de Planejamento
     */
    @Transactional(readOnly = true)
    public boolean existsByTermoPlanejamentoId(Long termoPlanejamentoId) {
        return repository.existsByTermoPlanejamentoId(termoPlanejamentoId);
    }

    /**
     * Converte entidade para DTO de resposta
     */
    private TermoPlanejamentoDocResponseDTO toResponseDTO(TermoPlanejamentoDoc doc) {
        return new TermoPlanejamentoDocResponseDTO(
                doc.getId(),
                doc.getTermoPlanejamento().getId(),
                doc.getDataAssinatura(),
                doc.getNomeArquivo(),
                doc.getTipoConteudo(),
                doc.getTamanhoArquivo()
        );
    }


    @Transactional
    public void assinar(Long documentoId, String hashPdf, Long usuarioId, Long paginanumber, Float x, Float y, Long width, Long height, HttpServletRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + usuarioId));

        TermoPlanejamentoDoc doc = repository.findById(documentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + documentoId));

        LocalDateTime dataAssinatura = LocalDateTime.now();
        doc.setHashPdf(hashPdf);
        doc.setIp(request.getRemoteAddr());
        doc.setUserAgent(request.getHeader("User-Agent"));
        doc.setDataAssinatura(dataAssinatura);
        doc.setUsuarioSignaturer(usuario);

        // carimbar o PDF (página 0-based; frontend pode enviar pageNumber 1-based)
        byte[] pdf = doc.getArquivoPdf();
        // Long pagina = (page != null && page > 0) ? page - 1 : 0L;
        try {
            byte[] newPdf = documentoService.carimbarAssinatura(
                    pdf, usuario.getNome(), hashPdf, doc.getIp(), dataAssinatura,
                    paginanumber, x, y, width, height);
            doc.setArquivoPdf(newPdf);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carimbar assinatura no PDF: " + e.getMessage(), e);
        }

        repository.save(doc);

        // Encerrar a demanda
        termoPlanejamentoService.sign(doc.getTermoPlanejamento().getId());
    }

    @Transactional(readOnly = true)
    public TermoPlanejamentoDocValidacaoHashDTO validarIntegridadeHash(Long documentoId, String hashPdfInformado) {
        TermoPlanejamentoDoc doc = repository.findById(documentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + documentoId));

        String hashCalculado = hashService.calcularSha256Hex(doc.getArquivoPdf());
        boolean valido = hashCalculado.equalsIgnoreCase(hashPdfInformado);

        return new TermoPlanejamentoDocValidacaoHashDTO(
                documentoId,
                valido,
                hashPdfInformado,
                hashCalculado
        );
    }

}

