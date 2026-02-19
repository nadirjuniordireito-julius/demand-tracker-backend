package com.demandtracker.service;

import com.demandtracker.dto.TermoPlanejamentoDocCreateDTO;
import com.demandtracker.dto.TermoPlanejamentoDocResponseDTO;
import com.demandtracker.dto.TermoPlanejamentoDocUpdateDTO;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import java.io.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import com.demandtracker.service.TermoPlanejamentoService;

@Service
@RequiredArgsConstructor
public class TermoPlanejamentoDocService {

    private final TermoPlanejamentoDocRepository repository;
    private final TermoPlanejamentoRepository termoPlanejamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TermoPlanejamentoService termoPlanejamentoService;

    /**
     * Cria um novo documento para um Termo de Planejamento
     */
    @Transactional
    public TermoPlanejamentoDocResponseDTO create(TermoPlanejamentoDocCreateDTO dto) throws IOException {
        // Valida se o termo existe
        TermoPlanejamento termoPlanejamento = termoPlanejamentoRepository.findById(dto.getTermoPlanejamentoId())
                .orElseThrow(() -> new RuntimeException("Termo de Planejamento não encontrado com ID: " + dto.getTermoPlanejamentoId()));

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
    public void assinar(Long documentoId, String hashPdf, Long usuarioId, HttpServletRequest request) {
       Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com ID: " + usuarioId));
       
       TermoPlanejamentoDoc doc = repository.findById(documentoId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + documentoId));
       
        LocalDateTime dataAssinatura = LocalDateTime.now();
        doc.setHashPdf(hashPdf);
        doc.setIp( request.getRemoteAddr() );
        doc.setUserAgent( request.getHeader("User-Agent") );
        doc.setDataAssinatura(dataAssinatura);
        doc.setUsuarioSignaturer(usuario);

        // carimbar o PDF
        byte[] pdf = doc.getArquivoPdf();
        byte[] newPdf = null;
        try {
            newPdf = carimbarAssinatura(pdf, usuario.getNome(), hashPdf, doc.getIp(), dataAssinatura );
            doc.setArquivoPdf(newPdf);
        } catch (Exception e) {
           
        }
        
        repository.save(doc);

        // Encerrar a demanda
        termoPlanejamentoService.sign(doc.getTermoPlanejamento().getId());


    }

  
    private byte[] carimbarAssinatura(
        byte[] pdfOriginal,
        String usuario,
        String hash,
        String ip, 
        LocalDateTime dataAssinatura
    ) throws IOException {

        PDDocument document = PDDocument.load(pdfOriginal);

        for (PDPage page : document.getPages()) {
    
            PDRectangle box = page.getMediaBox();
            float pageWidth = box.getWidth();
            float pageHeight = box.getHeight();
    
            // Posição: lado direito, próximo ao rodapé
            float baseX = pageWidth - 20; // margem direita
            float baseY = 80;             // sobe a partir do footer
    
            try (PDPageContentStream cs = new PDPageContentStream(
                    document,
                    page,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                    true
            )) {
    
                cs.saveGraphicsState();
    
                // Rotaciona 90° para subir verticalmente (sem espelhar)
                cs.transform(new Matrix(
                        0, 1,   // a, b
                        -1, 0,  // c, d
                        baseX, baseY
                ));
    
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 9);
                cs.setNonStrokingColor(120, 120, 120);
    
                cs.newLineAtOffset(0, 0);
                cs.showText("ASSINADO ELETRONICAMENTE por " + usuario + " em " + dataAssinatura);
                cs.newLineAtOffset(0, -12);
                cs.showText("Hash: " + hash+" - IP: " + ip);
    
                cs.endText();
                cs.restoreGraphicsState();
            }
        }
    
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        return out.toByteArray();
        
    }
}

