package com.demandtracker.service;

import com.demandtracker.dto.TermoAberturaDocCreateDTO;
import com.demandtracker.dto.TermoAberturaDocResponseDTO;
import com.demandtracker.dto.TermoAberturaDocUpdateDTO;
import com.demandtracker.entity.TermoAbertura;
import com.demandtracker.entity.TermoAberturaDoc;
import com.demandtracker.repository.TermoAberturaDocRepository;
import com.demandtracker.repository.TermoAberturaRepository;
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
import com.demandtracker.service.TermoAberturaService;

@Service
@RequiredArgsConstructor
public class TermoAberturaDocService {

    private final TermoAberturaDocRepository repository;
    private final TermoAberturaRepository termoAberturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TermoAberturaService termoAberturaService;    
    /**
     * Cria um novo documento para um Termo de Abertura
     */
    @Transactional
    public TermoAberturaDocResponseDTO create(TermoAberturaDocCreateDTO dto) throws IOException {
        // Valida se o termo existe
        TermoAbertura termoAbertura = termoAberturaRepository.findById(dto.getTermoAberturaId())
                .orElseThrow(() -> new RuntimeException("Termo de Abertura não encontrado com ID: " + dto.getTermoAberturaId()));

        // Valida se já existe documento para este termo
        if (repository.existsByTermoAberturaId(dto.getTermoAberturaId())) {
            throw new RuntimeException("Já existe um documento para este Termo de Abertura");
        }

        // Valida se o arquivo é PDF
        MultipartFile arquivo = dto.getArquivoPdf();
        if (!arquivo.getContentType().equals("application/pdf")) {
            throw new RuntimeException("O arquivo deve ser um PDF");
        }

        // Cria a entidade
        TermoAberturaDoc doc = new TermoAberturaDoc();
        doc.setTermoAbertura(termoAbertura);
        doc.setDataAssinatura(dto.getDataAssinatura() != null ? dto.getDataAssinatura() : LocalDateTime.now());
        doc.setArquivoPdf(arquivo.getBytes());
        doc.setNomeArquivo(arquivo.getOriginalFilename());
        doc.setTipoConteudo(arquivo.getContentType());
        doc.setTamanhoArquivo(arquivo.getSize());

        // Salva o documento
        TermoAberturaDoc saved = repository.save(doc);
        
        // Atualiza o status do termo para assinado (marca dataAssinatura)
        termoAbertura.setDataAssinatura(doc.getDataAssinatura());
        termoAberturaRepository.save(termoAbertura);
        
        return toResponseDTO(saved);
    }

    /**
     * Busca documento por ID
     */
    @Transactional(readOnly = true)
    public TermoAberturaDocResponseDTO findById(Long id) {
        TermoAberturaDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));
        return toResponseDTO(doc);
    }

    /**
     * Busca documento pelo ID do Termo de Abertura
     */
    @Transactional(readOnly = true)
    public TermoAberturaDocResponseDTO findByTermoAberturaId(Long termoAberturaId) {
        TermoAberturaDoc doc = repository.findByTermoAberturaId(termoAberturaId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado para o Termo de Abertura ID: " + termoAberturaId));
        return toResponseDTO(doc);
    }

    /**
     * Retorna o arquivo PDF do documento
     */
    @Transactional(readOnly = true)
    public byte[] getArquivoPdf(Long id) {
        TermoAberturaDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));
        return doc.getArquivoPdf();
    }

    /**
     * Retorna o arquivo PDF pelo ID do Termo de Abertura
     */
    @Transactional(readOnly = true)
    public byte[] getArquivoPdfByTermoAberturaId(Long termoAberturaId) {
        TermoAberturaDoc doc = repository.findByTermoAberturaId(termoAberturaId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado para o Termo de Abertura ID: " + termoAberturaId));
        return doc.getArquivoPdf();
    }

    /**
     * Lista todos os documentos
     */
    @Transactional(readOnly = true)
    public List<TermoAberturaDocResponseDTO> findAll() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Atualiza um documento
     */
    @Transactional
    public TermoAberturaDocResponseDTO update(Long id, TermoAberturaDocUpdateDTO dto) throws IOException {
        TermoAberturaDoc doc = repository.findById(id)
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
            
            // Atualiza a data de assinatura do documento
            if (dto.getDataAssinatura() != null) {
                doc.setDataAssinatura(dto.getDataAssinatura());
            } else {
                doc.setDataAssinatura(LocalDateTime.now());
            }
        }

        TermoAberturaDoc updated = repository.save(doc);
        
        // Atualiza o status do termo para assinado (marca dataAssinatura)
        TermoAbertura termoAbertura = updated.getTermoAbertura();
        termoAbertura.setDataAssinatura(updated.getDataAssinatura());
        termoAberturaRepository.save(termoAbertura);
        
        return toResponseDTO(updated);
    }

    /**
     * Deleta um documento
     */
    @Transactional
    public void delete(Long id) {
        TermoAberturaDoc doc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado com ID: " + id));
        
        // Obtém o termo antes de deletar o documento
        TermoAbertura termoAbertura = doc.getTermoAbertura();
        
        // Deleta o documento
        repository.deleteById(id);
        
        // Atualiza o status do termo para aberto (limpa dataAssinatura)
        termoAbertura.setDataAssinatura(null);
        termoAberturaRepository.save(termoAbertura);
    }

    /**
     * Deleta documento pelo ID do Termo de Abertura
     */
    @Transactional
    public void deleteByTermoAberturaId(Long termoAberturaId) {
        TermoAberturaDoc doc = repository.findByTermoAberturaId(termoAberturaId)
                .orElseThrow(() -> new RuntimeException("Documento não encontrado para o Termo de Abertura ID: " + termoAberturaId));
        
        // Obtém o termo antes de deletar o documento
        TermoAbertura termoAbertura = doc.getTermoAbertura();
        
        // Deleta o documento
        repository.deleteByTermoAberturaId(termoAberturaId);
        
        // Atualiza o status do termo para aberto (limpa dataAssinatura)
        termoAbertura.setDataAssinatura(null);
        termoAberturaRepository.save(termoAbertura);
    }

    /**
     * Verifica se existe documento para um Termo de Abertura
     */
    @Transactional(readOnly = true)
    public boolean existsByTermoAberturaId(Long termoAberturaId) {
        return repository.existsByTermoAberturaId(termoAberturaId);
    }

    /**
     * Converte entidade para DTO de resposta
     */
    private TermoAberturaDocResponseDTO toResponseDTO(TermoAberturaDoc doc) {
        return new TermoAberturaDocResponseDTO(
                doc.getId(),
                doc.getTermoAbertura().getId(),
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
       
       TermoAberturaDoc doc = repository.findById(documentoId)
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
        
        System.out.println(doc.getId() );

        repository.save(doc);

        // Encerrar a demanda
        termoAberturaService.sign(doc.getTermoAbertura().getId());


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
