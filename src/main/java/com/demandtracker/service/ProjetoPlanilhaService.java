package com.demandtracker.service;

import com.demandtracker.entity.DemandaTecnica;
import com.demandtracker.entity.MetaProduto;
import com.demandtracker.entity.Projeto;
import com.demandtracker.entity.ProjetoMeta;
import com.demandtracker.entity.enums.StatusDemandaTecnica;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.DemandaTecnicaRepository;
import com.demandtracker.repository.ProjetoRepository;
import com.demandtracker.repository.TermoEncerramentoDocRepository;
import com.demandtracker.repository.TermoEncerramentoRepository;
import com.demandtracker.repository.TermoPlanejamentoRepository;
import com.demandtracker.util.CodigoHierarquicoComparator;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjetoPlanilhaService {

    private static final String[] HEADERS = {
            "Meta",
            "Produto",
            "Código",
            "Descrição",
            "Status",
            "Executado",
            "Em execução",
            "Encerrado C/Assinatura",
            "Encerrado S/Assinatura"
    };

    private final ProjetoRepository projetoRepository;
    private final DemandaTecnicaRepository demandaTecnicaRepository;
    private final TermoPlanejamentoRepository termoPlanejamentoRepository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;
    private final TermoEncerramentoDocRepository termoEncerramentoDocRepository;

    public record PlanilhaDownloadResult(byte[] content, String fileName) {}

    @Transactional(readOnly = true)
    public PlanilhaDownloadResult gerarPlanilhaDemandas(Long projetoId) {
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado com ID: " + projetoId));

        List<DemandaTecnica> demandas = demandaTecnicaRepository.findByProjetoIdExcludingCanceladas(projetoId);
        demandas.sort(comparadorDemandas());

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Demandas");

            CellStyle headerStyle = criarEstiloCabecalho(workbook);
            CellStyle statusEncerradaStyle = criarEstiloStatusEncerrada(workbook);
            CellStyle valorStyle = criarEstiloValor(workbook);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (DemandaTecnica demanda : demandas) {
                Row row = sheet.createRow(rowIdx++);
                preencherLinha(row, demanda, statusEncerradaStyle, valorStyle);
            }

            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            String fileName = "demandas-" + sanitizeFileName(projeto.getCodTed()) + ".xlsx";
            return new PlanilhaDownloadResult(out.toByteArray(), fileName);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao gerar planilha xlsx do projeto ID: " + projetoId, e);
        }
    }

    private void preencherLinha(Row row, DemandaTecnica demanda, CellStyle statusEncerradaStyle, CellStyle valorStyle) {
        MetaProduto produto = demanda.getMetaProduto();
        ProjetoMeta meta = produto != null ? produto.getProjetoMeta() : null;

        setString(row, 0, meta != null ? meta.getCodigo() : null);
        setString(row, 1, produto != null ? produto.getCodigo() : null);
        setString(row, 2, demanda.getCodigo());
        setString(row, 3, demanda.getNome());

        String statusCodigo = demanda.getStatus();
        StatusDemandaTecnica statusEnum = StatusDemandaTecnica.fromCodigo(statusCodigo);
        String statusDescricao = statusEnum != null ? statusEnum.getDescricao() : (statusCodigo != null ? statusCodigo : "");
        Cell statusCell = row.createCell(4);
        statusCell.setCellValue(statusDescricao);
        if (StatusDemandaTecnica.G.getCodigo().equals(statusCodigo)) {
            statusCell.setCellStyle(statusEncerradaStyle);
        }

        ValoresLinha valores = calcularValores(demanda, statusCodigo);
        setValorOuVazio(row, 5, valores.executado(), valorStyle);
        setValorOuVazio(row, 6, valores.emExecucao(), valorStyle);
        setValorOuVazio(row, 7, valores.encerradoComAssinatura(), valorStyle);
        setValorOuVazio(row, 8, valores.encerradoSemAssinatura(), valorStyle);
    }

    private ValoresLinha calcularValores(DemandaTecnica demanda, String statusCodigo) {
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal emExecucao = zero;
        BigDecimal encerradoComAssinatura = zero;
        BigDecimal encerradoSemAssinatura = zero;

        if (StatusDemandaTecnica.E.getCodigo().equals(statusCodigo)) {
            BigDecimal planejado = termoPlanejamentoRepository.sumValorPlanejadoyDemandaTecnicaId(demanda.getId());
            emExecucao = planejado != null ? planejado : zero;
        } else if (StatusDemandaTecnica.F.getCodigo().equals(statusCodigo)
                || StatusDemandaTecnica.G.getCodigo().equals(statusCodigo)) {
            BigDecimal executado = termoEncerramentoRepository.sumValorExecutadoByDemandaTecnicaId(demanda.getId());
            BigDecimal valor = executado != null ? executado : zero;

            if (StatusDemandaTecnica.G.getCodigo().equals(statusCodigo)
                    && termoEncerramentoDocRepository
                            .existsByTermoEncerramentoDemandaTecnicaIdAndDataAssinaturaIsNotNull(demanda.getId())) {
                encerradoComAssinatura = valor;
            } else {
                encerradoSemAssinatura = valor;
            }
        }

        BigDecimal totalExecutado = emExecucao.add(encerradoComAssinatura).add(encerradoSemAssinatura);
        return new ValoresLinha(totalExecutado, emExecucao, encerradoComAssinatura, encerradoSemAssinatura);
    }

    private Comparator<DemandaTecnica> comparadorDemandas() {
        Comparator<String> codigoCmp = Comparator.nullsLast(CodigoHierarquicoComparator.INSTANCE);
        return Comparator
                .comparing((DemandaTecnica d) -> {
                    MetaProduto p = d.getMetaProduto();
                    return p != null && p.getProjetoMeta() != null ? p.getProjetoMeta().getCodigo() : null;
                }, codigoCmp)
                .thenComparing(d -> d.getMetaProduto() != null ? d.getMetaProduto().getCodigo() : null, codigoCmp)
                .thenComparing(DemandaTecnica::getCodigo, Comparator.nullsLast(String::compareToIgnoreCase));
    }

    private static void setString(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        if (value != null) {
            cell.setCellValue(value);
        }
    }

    private static void setValorOuVazio(Row row, int col, BigDecimal valor, CellStyle valorStyle) {
        Cell cell = row.createCell(col);
        if (valor != null && valor.compareTo(BigDecimal.ZERO) != 0) {
            cell.setCellValue(valor.doubleValue());
            cell.setCellStyle(valorStyle);
        }
    }

    private static CellStyle criarEstiloCabecalho(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle criarEstiloStatusEncerrada(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle criarEstiloValor(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private static String sanitizeFileName(String codTed) {
        if (codTed == null || codTed.isBlank()) {
            return "projeto";
        }
        return codTed.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private record ValoresLinha(
            BigDecimal executado,
            BigDecimal emExecucao,
            BigDecimal encerradoComAssinatura,
            BigDecimal encerradoSemAssinatura
    ) {}
}
