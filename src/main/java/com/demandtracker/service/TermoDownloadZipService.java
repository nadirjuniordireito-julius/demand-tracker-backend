package com.demandtracker.service;

import com.demandtracker.entity.ProjetoMeta;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.ProjetoMetaRepository;
import com.demandtracker.repository.TermoAberturaDocRepository;
import com.demandtracker.repository.TermoEncerramentoDocRepository;
import com.demandtracker.repository.TermoPlanejamentoDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class TermoDownloadZipService {

    private final ProjetoMetaRepository projetoMetaRepository;
    private final TermoAberturaDocRepository termoAberturaDocRepository;
    private final TermoPlanejamentoDocRepository termoPlanejamentoDocRepository;
    private final TermoEncerramentoDocRepository termoEncerramentoDocRepository;

    public record ZipDownloadResult(byte[] content, String fileName) {}

    private record ZipEntryData(String codigo, char tipo, byte[] pdf) {
        int tipoOrder() {
            return switch (tipo) {
                case 'A' -> 0;
                case 'P' -> 1;
                case 'E' -> 2;
                default -> 9;
            };
        }

        String entryName() {
            return "T" + tipo + "-" + sanitizeCodigoForZipEntry(codigo) + ".pdf";
        }
    }

    @Transactional(readOnly = true)
    public ZipDownloadResult downloadZip(Long projetoMetaId, boolean abertura, boolean planejamento, boolean encerramento) {
        ProjetoMeta meta = projetoMetaRepository.findById(projetoMetaId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta do projeto não encontrada com ID: " + projetoMetaId));

        if (!abertura && !planejamento && !encerramento) {
            throw new BadRequestException(
                    "Informe ao menos um tipo de termo para download: abertura, planejamento ou encerramento.");
        }

        List<ZipEntryData> entries = new ArrayList<>();

        if (abertura) {
            addEntries(entries, termoAberturaDocRepository.findArquivosPdfByProjetoMetaId(projetoMetaId), 'A');
        }
        if (planejamento) {
            addEntries(entries, termoPlanejamentoDocRepository.findArquivosPdfByProjetoMetaId(projetoMetaId), 'P');
        }
        if (encerramento) {
            addEntries(entries, termoEncerramentoDocRepository.findArquivosPdfByProjetoMetaId(projetoMetaId), 'E');
        }

        if (entries.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Nenhum documento de termo encontrado para a meta ID " + projetoMetaId
                            + " com os tipos solicitados.");
        }

        entries.sort(Comparator
                .comparing(ZipEntryData::codigo, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparingInt(ZipEntryData::tipoOrder));

        byte[] zipBytes = buildZip(entries);
        String fileName = "termos-meta-" + sanitizeFileName(meta.getCodigo()) + ".zip";
        return new ZipDownloadResult(zipBytes, fileName);
    }

    private void addEntries(List<ZipEntryData> entries, List<Object[]> rows, char tipo) {
        if (rows == null) {
            return;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }
            String codigo = row[0] != null ? row[0].toString() : null;
            byte[] pdf = row[1] instanceof byte[] bytes ? bytes : null;
            if (codigo == null || codigo.isBlank() || pdf == null || pdf.length == 0) {
                continue;
            }
            entries.add(new ZipEntryData(codigo, tipo, pdf));
        }
    }

    private byte[] buildZip(List<ZipEntryData> entries) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (ZipEntryData entry : entries) {
                zos.putNextEntry(new ZipEntry(entry.entryName()));
                zos.write(entry.pdf());
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao gerar o arquivo ZIP dos termos.", e);
        }
    }

    /** Evita que \ ou / no código criem pastas dentro do ZIP. */
    private static String sanitizeCodigoForZipEntry(String codigo) {
        return codigo.replace('\\', '_').replace('/', '_');
    }

    /** Remove caracteres inválidos em nome de arquivo do ZIP de resposta. */
    private static String sanitizeFileName(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return "sem-codigo";
        }
        return codigo.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
