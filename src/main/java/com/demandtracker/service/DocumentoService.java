package com.demandtracker.service;

import com.demandtracker.entity.*;
import com.demandtracker.repository.*;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.xmlbeans.XmlException;

/**
 * Serviço para geração de documentos PDF a partir de templates DOCX
 */
@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final TemplateDemandaRepository templateRepository;
    private final TermoAberturaRepository termoAberturaRepository;
    private final TermoPlanejamentoRepository termoPlanejamentoRepository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;
    private final DemandaTecnicaRepository demandaRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;

    /**
     * Gera PDF do Termo de Abertura a partir do template DOCX
     * 
     * @param projetoId ID do projeto
     * @param tipo Tipo do template (ex: "A" para Abertura)
     * @param termoAberturaId ID do Termo de Abertura
     * @return byte[] com o conteúdo do PDF
     */
    @Transactional(readOnly = true)
    public byte[] gerarPdfTermoAbertura(Long projetoId, String tipo, Long termoAberturaId) throws IOException {
        try {
            // Busca o template
            TemplateDemanda template = templateRepository.findByProjetoIdAndTipo(projetoId, tipo)
                    .orElseThrow(() -> new RuntimeException("Template não encontrado para o Projeto ID: " + projetoId + " e Tipo: " + tipo));

            // Busca o termo de abertura com todos os dados necessários
            TermoAbertura termoAbertura = termoAberturaRepository.findById(termoAberturaId)
                    .orElseThrow(() -> new RuntimeException("Termo de Abertura não encontrado com ID: " + termoAberturaId));

            // Carrega os dados relacionados (força o carregamento devido ao LAZY)
            Long demandaId = termoAbertura.getDemandaTecnica() != null 
                    ? termoAbertura.getDemandaTecnica().getId() 
                    : null;
            if (demandaId == null) {
                throw new RuntimeException("Demanda Técnica não associada ao Termo de Abertura");
            }
            
            DemandaTecnica demanda = demandaRepository.findById(demandaId)
                    .orElseThrow(() -> new RuntimeException("Demanda Técnica não encontrada com ID: " + demandaId));
            
            Long projetoIdFromDemanda = demanda.getProjeto() != null ? demanda.getProjeto().getId() : null;
            if (projetoIdFromDemanda == null) {
                throw new RuntimeException("Projeto não associado à Demanda Técnica");
            }
            
            Projeto projeto = projetoRepository.findById(projetoIdFromDemanda)
                    .orElseThrow(() -> new RuntimeException("Projeto não encontrado com ID: " + projetoIdFromDemanda));
            
            Long usuarioDemandaId = demanda.getUsuario() != null ? demanda.getUsuario().getId() : null;
            if (usuarioDemandaId == null) {
                throw new RuntimeException("Usuário não associado à Demanda Técnica");
            }
            
            Usuario usuarioDemanda = usuarioRepository.findById(usuarioDemandaId)
                    .orElseThrow(() -> new RuntimeException("Usuário da demanda não encontrado com ID: " + usuarioDemandaId));
            
            Long usuarioTermoId = termoAbertura.getUsuario() != null ? termoAbertura.getUsuario().getId() : null;
            if (usuarioTermoId == null) {
                throw new RuntimeException("Usuário não associado ao Termo de Abertura");
            }
            
            Usuario usuarioTermo = usuarioRepository.findById(usuarioTermoId)
                    .orElseThrow(() -> new RuntimeException("Usuário do termo não encontrado com ID: " + usuarioTermoId));

            // Verifica se o template tem arquivo
            byte[] templateBytes = template.getArquivoDocx();
            if (templateBytes == null || templateBytes.length == 0) {
                throw new RuntimeException("Template está vazio ou não encontrado");
            }

            // Prepara os dados para substituição
            DadosTermoAbertura dados = new DadosTermoAbertura();
            dados.setProjetoNome(projeto.getNome());
            dados.setProjetoCodTed(projeto.getCodTed());
            dados.setDemandaCodigo(demanda.getCodigo());
            dados.setDemandaNome(demanda.getNome());
            dados.setDemandaDescricao(demanda.getDescricao() != null ? demanda.getDescricao() : "");
            dados.setTermoDescricao(termoAbertura.getDescricao());
            dados.setDataAbertura(formatarData(termoAbertura.getDataAbertura()));
            dados.setDataAberturaCompleta(formatarDataCompleta(termoAbertura.getDataAbertura()));
            dados.setUsuarioDemandaNome(usuarioDemanda.getNome());
            dados.setUsuarioDemandaEmail(usuarioDemanda.getEmail());
            dados.setUsuarioTermoNome(usuarioTermo.getNome());
            dados.setUsuarioTermoEmail(usuarioTermo.getEmail());
            dados.setDataAssinatura(termoAbertura.getDataAssinatura() != null 
                    ? formatarDataCompleta(termoAbertura.getDataAssinatura()) 
                    : "");

            // Detecta o tipo do arquivo e processa adequadamente
            String tipoConteudo = template.getTipoConteudo();
            boolean isPdf = tipoConteudo != null && tipoConteudo.equals("application/pdf");
            
            // Se não conseguir detectar pelo tipoConteudo, tenta detectar pelo conteúdo
            if (!isPdf && templateBytes.length >= 4) {
                // PDF começa com %PDF
                String inicio = new String(templateBytes, 0, Math.min(4, templateBytes.length));
                isPdf = inicio.startsWith("%PDF");
            }

            if (isPdf) {
                // Processa template PDF
                return gerarPdfDeTemplatePdf(templateBytes, dados);
            } else {
                // Processa template DOCX
                return gerarPdfDeTemplateDocx(templateBytes, dados);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do Termo de Abertura: " + e.getMessage(), e);
        }
    }

    /**
     * Gera PDF do Termo de Planejamento a partir do template DOCX
     * 
     * @param projetoId ID do projeto
     * @param tipo Tipo do template (ex: "P" para Planejamento)
     * @param termoPlanejamentoId ID do Termo de Planejamento
     * @return byte[] com o conteúdo do PDF
     */
    @Transactional(readOnly = true)
    public byte[] gerarPdfTermoPlanejamento(Long projetoId, String tipo, Long termoPlanejamentoId) throws IOException {
        try {
            // Busca o template
            TemplateDemanda template = templateRepository.findByProjetoIdAndTipo(projetoId, tipo)
                    .orElseThrow(() -> new RuntimeException("Template não encontrado para o Projeto ID: " + projetoId + " e Tipo: " + tipo));

            // Busca o termo de planejamento com todos os dados necessários
            TermoPlanejamento termoPlanejamento = termoPlanejamentoRepository.findById(termoPlanejamentoId)
                    .orElseThrow(() -> new RuntimeException("Termo de Planejamento não encontrado com ID: " + termoPlanejamentoId));

            // Carrega os dados relacionados (força o carregamento devido ao LAZY)
            Long demandaId = termoPlanejamento.getDemandaTecnica() != null 
                    ? termoPlanejamento.getDemandaTecnica().getId() 
                    : null;
            if (demandaId == null) {
                throw new RuntimeException("Demanda Técnica não associada ao Termo de Planejamento");
            }
            
            DemandaTecnica demanda = demandaRepository.findById(demandaId)
                    .orElseThrow(() -> new RuntimeException("Demanda Técnica não encontrada com ID: " + demandaId));
            
            Long projetoIdFromDemanda = demanda.getProjeto() != null ? demanda.getProjeto().getId() : null;
            if (projetoIdFromDemanda == null) {
                throw new RuntimeException("Projeto não associado à Demanda Técnica");
            }
            
            Projeto projeto = projetoRepository.findById(projetoIdFromDemanda)
                    .orElseThrow(() -> new RuntimeException("Projeto não encontrado com ID: " + projetoIdFromDemanda));
            
            Long usuarioDemandaId = demanda.getUsuario() != null ? demanda.getUsuario().getId() : null;
            if (usuarioDemandaId == null) {
                throw new RuntimeException("Usuário não associado à Demanda Técnica");
            }
            
            Usuario usuarioDemanda = usuarioRepository.findById(usuarioDemandaId)
                    .orElseThrow(() -> new RuntimeException("Usuário da demanda não encontrado com ID: " + usuarioDemandaId));
            
            Long usuarioTermoId = termoPlanejamento.getUsuario() != null ? termoPlanejamento.getUsuario().getId() : null;
            if (usuarioTermoId == null) {
                throw new RuntimeException("Usuário não associado ao Termo de Planejamento");
            }
            
            Usuario usuarioTermo = usuarioRepository.findById(usuarioTermoId)
                    .orElseThrow(() -> new RuntimeException("Usuário do termo não encontrado com ID: " + usuarioTermoId));

            // Carrega os custos do termo (força o carregamento LAZY)
            List<TermoPlanejamentoCusto> custos = termoPlanejamento.getCustos();
            if (custos == null) {
                custos = new ArrayList<>();
            }
            
            // Carrega os perfis dos custos e calcula totais
            BigDecimal custoTotal = BigDecimal.ZERO;
            List<CustoDetalhado> custosDetalhados = new ArrayList<>();
            for (TermoPlanejamentoCusto custo : custos) {
                Long perfilId = custo.getPerfil() != null ? custo.getPerfil().getId() : null;
                Perfil perfil = null;
                if (perfilId != null) {
                    perfil = perfilRepository.findById(perfilId)
                            .orElse(null);
                }
                
                BigDecimal qtdeHora = custo.getQtdeHora() != null ? custo.getQtdeHora() : BigDecimal.ZERO;
                BigDecimal valorHora = custo.getValorHora() != null ? custo.getValorHora() : BigDecimal.ZERO;
                BigDecimal totalLinha = qtdeHora.multiply(valorHora);
                custoTotal = custoTotal.add(totalLinha);
                
                CustoDetalhado custoDetalhado = new CustoDetalhado();
                custoDetalhado.perfilNome = perfil != null ? perfil.getNome() : "N/A";
                custoDetalhado.qtdeHora = qtdeHora;
                custoDetalhado.valorHora = valorHora;
                custoDetalhado.total = totalLinha;
                custosDetalhados.add(custoDetalhado);
            }

            // Verifica se o template tem arquivo
            byte[] templateBytes = template.getArquivoDocx();
            if (templateBytes == null || templateBytes.length == 0) {
                throw new RuntimeException("Template está vazio ou não encontrado");
            }

            // Prepara os dados para substituição
            DadosTermoPlanejamento dados = new DadosTermoPlanejamento();
            dados.setProjetoNome(projeto.getNome());
            dados.setProjetoCodTed(projeto.getCodTed());
            dados.setDemandaCodigo(demanda.getCodigo());
            dados.setDemandaNome(demanda.getNome());
            dados.setDemandaDescricao(demanda.getDescricao() != null ? demanda.getDescricao() : "");
            dados.setEspecificacao(termoPlanejamento.getEspecificacao() != null ? termoPlanejamento.getEspecificacao() : "");
            dados.setCronograma(termoPlanejamento.getCronograma() != null ? termoPlanejamento.getCronograma() : "");
            dados.setResultadoEsperado(termoPlanejamento.getResultadoEsperado() != null ? termoPlanejamento.getResultadoEsperado() : "");
            dados.setDataAbertura(formatarData(termoPlanejamento.getDataAbertura()));
            dados.setDataAberturaCompleta(formatarDataCompleta(termoPlanejamento.getDataAbertura()));
            dados.setUsuarioDemandaNome(usuarioDemanda.getNome());
            dados.setUsuarioDemandaEmail(usuarioDemanda.getEmail());
            dados.setUsuarioTermoNome(usuarioTermo.getNome());
            dados.setUsuarioTermoEmail(usuarioTermo.getEmail());
            dados.setDataAssinatura(termoPlanejamento.getDataAssinatura() != null 
                    ? formatarDataCompleta(termoPlanejamento.getDataAssinatura()) 
                    : "");
            dados.setCustosDetalhados(gerarTabelaCustos(custosDetalhados));
            dados.setCustoTotal(formatarMoeda(custoTotal));

            // Detecta o tipo do arquivo e processa adequadamente
            String tipoConteudo = template.getTipoConteudo();
            boolean isPdf = tipoConteudo != null && tipoConteudo.equals("application/pdf");
            
            // Se não conseguir detectar pelo tipoConteudo, tenta detectar pelo conteúdo
            if (!isPdf && templateBytes.length >= 4) {
                // PDF começa com %PDF
                String inicio = new String(templateBytes, 0, Math.min(4, templateBytes.length));
                isPdf = inicio.startsWith("%PDF");
            }

            if (isPdf) {
                // Processa template PDF
                return gerarPdfDeTemplatePdfPlanejamento(templateBytes, dados);
            } else {
                // Processa template DOCX
                return gerarPdfDeTemplateDocxPlanejamento(templateBytes, dados);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do Termo de Planejamento: " + e.getMessage(), e);
        }
    }

    /**
     * Gera PDF do Termo de Encerramento a partir do template DOCX
     * 
     * @param projetoId ID do projeto
     * @param tipo Tipo do template (ex: "E" para Encerramento)
     * @param termoEncerramentoId ID do Termo de Encerramento
     * @return byte[] com o conteúdo do PDF
     */
    @Transactional(readOnly = true)
    public byte[] gerarPdfTermoEncerramento(Long projetoId, String tipo, Long termoEncerramentoId) throws IOException {
        try {
            // Busca o template
            TemplateDemanda template = templateRepository.findByProjetoIdAndTipo(projetoId, tipo)
                    .orElseThrow(() -> new RuntimeException("Template não encontrado para o Projeto ID: " + projetoId + " e Tipo: " + tipo));

            // Busca o termo de encerramento com todos os dados necessários
            TermoEncerramento termoEncerramento = termoEncerramentoRepository.findById(termoEncerramentoId)
                    .orElseThrow(() -> new RuntimeException("Termo de Encerramento não encontrado com ID: " + termoEncerramentoId));

            // Carrega os dados relacionados (força o carregamento devido ao LAZY)
            Long demandaId = termoEncerramento.getDemandaTecnica() != null 
                    ? termoEncerramento.getDemandaTecnica().getId() 
                    : null;
            if (demandaId == null) {
                throw new RuntimeException("Demanda Técnica não associada ao Termo de Encerramento");
            }
            
            DemandaTecnica demanda = demandaRepository.findById(demandaId)
                    .orElseThrow(() -> new RuntimeException("Demanda Técnica não encontrada com ID: " + demandaId));
            
            Long projetoIdFromDemanda = demanda.getProjeto() != null ? demanda.getProjeto().getId() : null;
            if (projetoIdFromDemanda == null) {
                throw new RuntimeException("Projeto não associado à Demanda Técnica");
            }
            
            Projeto projeto = projetoRepository.findById(projetoIdFromDemanda)
                    .orElseThrow(() -> new RuntimeException("Projeto não encontrado com ID: " + projetoIdFromDemanda));
            
            Long usuarioDemandaId = demanda.getUsuario() != null ? demanda.getUsuario().getId() : null;
            if (usuarioDemandaId == null) {
                throw new RuntimeException("Usuário não associado à Demanda Técnica");
            }
            
            Usuario usuarioDemanda = usuarioRepository.findById(usuarioDemandaId)
                    .orElseThrow(() -> new RuntimeException("Usuário da demanda não encontrado com ID: " + usuarioDemandaId));
            
            Long usuarioTermoId = termoEncerramento.getUsuario() != null ? termoEncerramento.getUsuario().getId() : null;
            if (usuarioTermoId == null) {
                throw new RuntimeException("Usuário não associado ao Termo de Encerramento");
            }
            
            Usuario usuarioTermo = usuarioRepository.findById(usuarioTermoId)
                    .orElseThrow(() -> new RuntimeException("Usuário do termo não encontrado com ID: " + usuarioTermoId));

            // Carrega os custos do termo de encerramento
            List<TermoEncerramentoCusto> custos = termoEncerramento.getCustos();
            if (custos == null) {
                custos = new ArrayList<>();
            }
            
            // Carrega os perfis dos custos e calcula totais
            BigDecimal custoTotal = BigDecimal.ZERO;
            List<CustoDetalhado> custosDetalhados = new ArrayList<>();
            for (TermoEncerramentoCusto custo : custos) {
                Long perfilId = custo.getPerfil() != null ? custo.getPerfil().getId() : null;
                Perfil perfil = null;
                if (perfilId != null) {
                    perfil = perfilRepository.findById(perfilId)
                            .orElse(null);
                }
                
                BigDecimal qtdeHora = custo.getQtdeHora() != null ? custo.getQtdeHora() : BigDecimal.ZERO;
                BigDecimal valorHora = custo.getValorHora() != null ? custo.getValorHora() : BigDecimal.ZERO;
                BigDecimal totalLinha = qtdeHora.multiply(valorHora);
                custoTotal = custoTotal.add(totalLinha);
                
                CustoDetalhado custoDetalhado = new CustoDetalhado();
                custoDetalhado.perfilNome = perfil != null ? perfil.getNome() : "N/A";
                custoDetalhado.qtdeHora = qtdeHora;
                custoDetalhado.valorHora = valorHora;
                custoDetalhado.total = totalLinha;
                custosDetalhados.add(custoDetalhado);
            }

            // Verifica se o template tem arquivo
            byte[] templateBytes = template.getArquivoDocx();
            if (templateBytes == null || templateBytes.length == 0) {
                throw new RuntimeException("Template está vazio ou não encontrado");
            }

            // Prepara os dados para substituição
            DadosTermoEncerramento dados = new DadosTermoEncerramento();
            dados.setProjetoNome(projeto.getNome());
            dados.setProjetoCodTed(projeto.getCodTed());
            dados.setDemandaCodigo(demanda.getCodigo());
            dados.setDemandaNome(demanda.getNome());
            dados.setDemandaDescricao(demanda.getDescricao() != null ? demanda.getDescricao() : "");
            dados.setResultadoEntregue(termoEncerramento.getResultadoEntregue() != null ? termoEncerramento.getResultadoEntregue() : "");
            dados.setDataTermo(formatarData(termoEncerramento.getDataTermo()));
            dados.setDataTermoCompleta(formatarDataCompleta(termoEncerramento.getDataTermo()));
            dados.setUsuarioDemandaNome(usuarioDemanda.getNome());
            dados.setUsuarioDemandaEmail(usuarioDemanda.getEmail());
            dados.setUsuarioTermoNome(usuarioTermo.getNome());
            dados.setUsuarioTermoEmail(usuarioTermo.getEmail());
            dados.setDataAssinatura(termoEncerramento.getDataAssinatura() != null 
                    ? formatarDataCompleta(termoEncerramento.getDataAssinatura()) 
                    : "");
            dados.setCustosDetalhados(gerarTabelaCustos(custosDetalhados));
            dados.setCustoTotal(formatarMoeda(custoTotal));

            // Detecta o tipo do arquivo e processa adequadamente
            String tipoConteudo = template.getTipoConteudo();
            boolean isPdf = tipoConteudo != null && tipoConteudo.equals("application/pdf");
            
            // Se não conseguir detectar pelo tipoConteudo, tenta detectar pelo conteúdo
            if (!isPdf && templateBytes.length >= 4) {
                // PDF começa com %PDF
                String inicio = new String(templateBytes, 0, Math.min(4, templateBytes.length));
                isPdf = inicio.startsWith("%PDF");
            }

            if (isPdf) {
                // Processa template PDF
                return gerarPdfDeTemplatePdfEncerramento(templateBytes, dados);
            } else {
                // Processa template DOCX
                return gerarPdfDeTemplateDocxEncerramento(templateBytes, dados);
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do Termo de Encerramento: " + e.getMessage(), e);
        }
    }

    /**
     * Gera PDF a partir de template PDF
     */
    private byte[] gerarPdfDeTemplatePdf(byte[] templateBytes, DadosTermoAbertura dados) throws IOException {
        try {
            // Tenta carregar o PDF usando PDFBox
            PDDocument document = PDDocument.load(templateBytes);
            try {
                // Substitui variáveis no PDF
                substituirVariaveisNoPdf(document, dados);
                
                // Salva o PDF modificado
                ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
                document.save(pdfOutputStream);
                
                return pdfOutputStream.toByteArray();
            } finally {
                document.close();
            }
        } catch (NoClassDefFoundError | Exception e) {
            // Se PDFBox não estiver disponível ou houver erro, retorna o PDF original
            // Isso permite que o sistema funcione mesmo sem PDFBox instalado
            throw new RuntimeException("Erro ao processar template PDF. Certifique-se de que as dependências do PDFBox foram baixadas. " +
                    "Execute 'mvn clean install' ou recarregue o projeto no IDE. Erro: " + e.getMessage(), e);
        }
    }

    /**
     * Valida que os bytes parecem um DOCX válido (ZIP com assinatura PK e tamanho mínimo).
     * DOCX é um arquivo ZIP; arquivo incompleto/corrompido causa "Premature end of file" no POI.
     */
    private void validarBytesDocx(byte[] templateBytes) {
        if (templateBytes == null || templateBytes.length < 22) {
            throw new RuntimeException("Template DOCX inválido ou vazio (arquivo muito pequeno ou incompleto). " +
                    "Faça download do template novamente e faça upload de um arquivo DOCX válido.");
        }
        // Assinatura ZIP: PK (0x50 0x4B)
        if (templateBytes[0] != 0x50 || templateBytes[1] != 0x4B) {
            throw new RuntimeException("Template não parece ser um arquivo DOCX válido (formato ZIP). " +
                    "Use um arquivo .docx gerado pelo Word ou LibreOffice.");
        }
    }

    /**
     * Monta mensagem de erro amigável para falha na conversão DOCX para PDF.
     * Inclui dica específica quando o erro indica arquivo truncado/corrompido (Premature end of file).
     */
    private String mensagemErroConversaoDocx(Exception ultimoErro) {
        String causa = ultimoErro != null ? ultimoErro.getMessage() : "Desconhecido";
        boolean arquivoIncompleto = (ultimoErro instanceof XmlException)
                || (causa != null && causa.contains("Premature end of file"));
        StringBuilder msg = new StringBuilder();
        msg.append("Erro ao converter DOCX para PDF.");
        if (arquivoIncompleto) {
            msg.append(" O arquivo do template parece estar incompleto ou corrompido (XML truncado).");
        } else {
            msg.append(" O template pode ter sido gerado no Google Docs e tem incompatibilidade com a biblioteca de conversão.");
        }
        msg.append("\n\nSOLUÇÕES POSSÍVEIS:\n");
        msg.append("1. Abra o template DOCX no Microsoft Word ou LibreOffice Writer\n");
        msg.append("2. Salve novamente como DOCX (isso normaliza a estrutura XML)\n");
        msg.append("3. Faça upload do template normalizado novamente\n\n");
        msg.append("Erro técnico: ").append(causa);
        return msg.toString();
    }

    /**
     * Gera PDF a partir de template DOCX
     * Tenta múltiplas estratégias para lidar com templates do Google Docs
     */
    private byte[] gerarPdfDeTemplateDocx(byte[] templateBytes, DadosTermoAbertura dados) throws IOException {
        validarBytesDocx(templateBytes);
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        Exception ultimoErro = null;
        
        // Estratégia 0: Criar novo documento limpo copiando apenas o conteúdo (mais agressiva, mas garante estilos)
        // Esta estratégia cria um documento completamente novo que sempre tem estilos
        try {
            XWPFDocument documentOriginal = new XWPFDocument(new ByteArrayInputStream(templateBytes));
            XWPFDocument documentNovo = new XWPFDocument();
            
            // Força criação de estilos no novo documento acessando-os
            // Um novo XWPFDocument deve ter estilos, mas acessá-los garante que sejam criados
            try {
                org.apache.poi.xwpf.usermodel.XWPFStyles styles = documentNovo.getStyles();
                if (styles == null) {
                    // Se não existir, tenta criar
                    try {
                        documentNovo.createStyles();
                    } catch (Exception e2) {
                        // Se createStyles não existir, tenta forçar criação criando um parágrafo com estilo
                        XWPFParagraph p = documentNovo.createParagraph();
                        p.setStyle("Normal");
                    }
                }
            } catch (Exception e) {
                // Se houver erro, continua - o documento pode ter estilos mesmo assim
            }
            
            // Copia parágrafos
            for (XWPFParagraph paragraph : documentOriginal.getParagraphs()) {
                String texto = paragraph.getText();
                if (texto != null && !texto.trim().isEmpty()) {
                    XWPFParagraph novoParagrafo = documentNovo.createParagraph();
                    // Substitui variáveis no texto
                    texto = substituirTodasVariaveis(texto, dados);
                    novoParagrafo.createRun().setText(texto);
                }
            }
            
            // Copia tabelas
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : documentOriginal.getTables()) {
                org.apache.poi.xwpf.usermodel.XWPFTable novaTabela = documentNovo.createTable();
                boolean primeiraLinha = true;
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                    org.apache.poi.xwpf.usermodel.XWPFTableRow novaRow = primeiraLinha ? novaTabela.getRow(0) : novaTabela.createRow();
                    primeiraLinha = false;
                    
                    int cellIndex = 0;
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        org.apache.poi.xwpf.usermodel.XWPFTableCell novaCell;
                        if (cellIndex < novaRow.getTableCells().size()) {
                            novaCell = novaRow.getCell(cellIndex);
                        } else {
                            novaCell = novaRow.createCell();
                        }
                        cellIndex++;
                        
                        String texto = cell.getText();
                        if (texto != null && !texto.trim().isEmpty()) {
                            texto = substituirTodasVariaveis(texto, dados);
                            if (novaCell.getParagraphs().isEmpty()) {
                                novaCell.addParagraph().createRun().setText(texto);
                            } else {
                                novaCell.getParagraphs().get(0).createRun().setText(texto);
                            }
                        }
                    }
                }
            }
            
            documentOriginal.close();
            
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(documentNovo, pdfOutputStream, options);
            documentNovo.close();
            
            if (pdfOutputStream.size() > 0) {
                return pdfOutputStream.toByteArray();
            }
        } catch (Exception e) {
            ultimoErro = e;
            pdfOutputStream.reset();
        }
        
        // Se todas as estratégias falharam, lança exceção com mensagem detalhada
        throw new RuntimeException(mensagemErroConversaoDocx(ultimoErro), ultimoErro);
    }
    
    /**
     * Garante que o documento XWPFDocument tenha uma parte de estilos
     * Necessário para conversão PDF de templates do Google Docs
     * 
     * Tenta acessar os estilos - se não existirem, o POI pode criar automaticamente
     * ao salvar o documento. Esta é uma tentativa de forçar a inicialização.
     */
    private void garantirParteDeEstilos(XWPFDocument document) {
        try {
            // Tenta acessar a parte de estilos - isso pode forçar sua criação
            org.apache.poi.xwpf.usermodel.XWPFStyles styles = document.getStyles();
            
            // Se existir, tenta garantir que tenha pelo menos um estilo básico
            if (styles != null) {
                try {
                    // Tenta acessar um estilo padrão - isso pode ajudar na inicialização
                    styles.getStyle("Normal");
                } catch (Exception e) {
                    // Se não existir estilo Normal, não é crítico
                    // O POI pode criar automaticamente quando necessário
                }
            }
            // Se styles == null, o POI criará ao salvar o documento
        } catch (Exception e) {
            // Se houver erro, não faz nada - deixa as estratégias seguintes tentarem
            // O erro será capturado na estratégia seguinte
        }
    }

    /**
     * Gera PDF a partir de template PDF (para TermoPlanejamento)
     */
    private byte[] gerarPdfDeTemplatePdfPlanejamento(byte[] templateBytes, DadosTermoPlanejamento dados) throws IOException {
        try {
            // Tenta carregar o PDF usando PDFBox
            PDDocument document = PDDocument.load(templateBytes);
            try {
                // Substitui variáveis no PDF
                substituirVariaveisNoPdfPlanejamento(document, dados);
                
                // Salva o PDF modificado
                ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
                document.save(pdfOutputStream);
                
                return pdfOutputStream.toByteArray();
            } finally {
                document.close();
            }
        } catch (NoClassDefFoundError | Exception e) {
            throw new RuntimeException("Erro ao processar template PDF. Certifique-se de que as dependências do PDFBox foram baixadas. " +
                    "Execute 'mvn clean install' ou recarregue o projeto no IDE. Erro: " + e.getMessage(), e);
        }
    }

    /**
     * Gera PDF a partir de template DOCX (para TermoPlanejamento)
     * Tenta múltiplas estratégias para lidar com templates do Google Docs
     */
    private byte[] gerarPdfDeTemplateDocxPlanejamento(byte[] templateBytes, DadosTermoPlanejamento dados) throws IOException {
        validarBytesDocx(templateBytes);
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        Exception ultimoErro = null;
        
        // Estratégia 0: Criar novo documento limpo copiando apenas o conteúdo (mais agressiva, mas garante estilos)
        // Esta estratégia cria um documento completamente novo que sempre tem estilos
        try {
            XWPFDocument documentOriginal = new XWPFDocument(new ByteArrayInputStream(templateBytes));
            XWPFDocument documentNovo = new XWPFDocument();
            
            // Força criação de estilos no novo documento acessando-os
            // Um novo XWPFDocument deve ter estilos, mas acessá-los garante que sejam criados
            try {
                org.apache.poi.xwpf.usermodel.XWPFStyles styles = documentNovo.getStyles();
                if (styles == null) {
                    // Se não existir, tenta criar
                    try {
                        documentNovo.createStyles();
                    } catch (Exception e2) {
                        // Se createStyles não existir, tenta forçar criação criando um parágrafo com estilo
                        XWPFParagraph p = documentNovo.createParagraph();
                        p.setStyle("Normal");
                    }
                }
            } catch (Exception e) {
                // Se houver erro, continua - o documento pode ter estilos mesmo assim
            }
            
            // Copia parágrafos
            for (XWPFParagraph paragraph : documentOriginal.getParagraphs()) {
                String texto = paragraph.getText();
                if (texto != null && !texto.trim().isEmpty()) {
                    XWPFParagraph novoParagrafo = documentNovo.createParagraph();
                    // Substitui variáveis no texto
                    texto = substituirTodasVariaveisPlanejamento(texto, dados);
                    novoParagrafo.createRun().setText(texto);
                }
            }
            
            // Copia tabelas
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : documentOriginal.getTables()) {
                org.apache.poi.xwpf.usermodel.XWPFTable novaTabela = documentNovo.createTable();
                boolean primeiraLinha = true;
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                    org.apache.poi.xwpf.usermodel.XWPFTableRow novaRow = primeiraLinha ? novaTabela.getRow(0) : novaTabela.createRow();
                    primeiraLinha = false;
                    
                    int cellIndex = 0;
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        org.apache.poi.xwpf.usermodel.XWPFTableCell novaCell;
                        if (cellIndex < novaRow.getTableCells().size()) {
                            novaCell = novaRow.getCell(cellIndex);
                        } else {
                            novaCell = novaRow.createCell();
                        }
                        cellIndex++;
                        
                        String texto = cell.getText();
                        if (texto != null && !texto.trim().isEmpty()) {
                            texto = substituirTodasVariaveisPlanejamento(texto, dados);
                            if (novaCell.getParagraphs().isEmpty()) {
                                novaCell.addParagraph().createRun().setText(texto);
                            } else {
                                novaCell.getParagraphs().get(0).createRun().setText(texto);
                            }
                        }
                    }
                }
            }
            
            documentOriginal.close();
            
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(documentNovo, pdfOutputStream, options);
            documentNovo.close();
            
            if (pdfOutputStream.size() > 0) {
                return pdfOutputStream.toByteArray();
            }
        } catch (Exception e) {
            ultimoErro = e;
            pdfOutputStream.reset();
        }
        
        // Estratégia 1: Conversão direta (mais rápida, funciona para templates do Word)
        try {
            XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(templateBytes));
            substituirVariaveisPlanejamento(document, dados);
            
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, pdfOutputStream, options);
            document.close();
            
            if (pdfOutputStream.size() > 0) {
                return pdfOutputStream.toByteArray();
            }
        } catch (Exception e) {
            ultimoErro = e;
            pdfOutputStream.reset();
        }
        
        // Estratégia 2: Salvar e recarregar para normalizar (remove problemas de XML)
        try {
            XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(templateBytes));
            substituirVariaveisPlanejamento(document, dados);
            
            // Salva em memória e recarrega para normalizar o XML
            ByteArrayOutputStream docxNormalizado = new ByteArrayOutputStream();
            document.write(docxNormalizado);
            document.close();
            
            XWPFDocument documentNormalizado = new XWPFDocument(new ByteArrayInputStream(docxNormalizado.toByteArray()));
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(documentNormalizado, pdfOutputStream, options);
            documentNormalizado.close();
            
            if (pdfOutputStream.size() > 0) {
                return pdfOutputStream.toByteArray();
            }
        } catch (Exception e) {
            ultimoErro = e;
            pdfOutputStream.reset();
        }
        
        // Se todas as estratégias falharam, lança exceção com mensagem detalhada
        throw new RuntimeException(mensagemErroConversaoDocx(ultimoErro), ultimoErro);
    }

    /**
     * Substitui variáveis no documento PDF
     * 
     * NOTA: Substituir texto em PDFs é muito complexo porque PDFs não são formatos editáveis.
     * Esta é uma implementação simplificada que funciona para PDFs básicos.
     * 
     * Para uma solução mais robusta, recomenda-se:
     * 1. Usar campos de formulário PDF (AcroForm) no template
     * 2. Ou converter o template PDF para DOCX, fazer a substituição, e converter de volta
     * 3. Ou usar uma biblioteca especializada como iText com suporte a campos de formulário
     * 
     * Por enquanto, esta implementação retorna o PDF original sem substituição,
     * pois a substituição de texto em PDFs requer manipulação complexa do content stream.
     */
    private void substituirVariaveisNoPdf(PDDocument document, DadosTermoAbertura dados) throws IOException {
        // NOTA: Substituição de texto em PDFs é muito complexa
        // Esta implementação básica não faz substituição real
        // Para uma solução completa, seria necessário:
        // 1. Parsear o content stream de cada página
        // 2. Encontrar operadores de texto (Tj, TJ)
        // 3. Modificar os COSString
        // 4. Reescrever o content stream
        
        // Por enquanto, apenas valida que o PDF pode ser carregado
        // A substituição real requer implementação mais complexa ou uso de campos de formulário
    }
    
    /**
     * Gera PDF a partir de template PDF (para TermoEncerramento)
     */
    private byte[] gerarPdfDeTemplatePdfEncerramento(byte[] templateBytes, DadosTermoEncerramento dados) throws IOException {
        try {
            // Tenta carregar o PDF usando PDFBox
            PDDocument document = PDDocument.load(templateBytes);
            try {
                // Substitui variáveis no PDF
                substituirVariaveisNoPdfEncerramento(document, dados);
                
                // Salva o PDF modificado
                ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
                document.save(pdfOutputStream);
                
                return pdfOutputStream.toByteArray();
            } finally {
                document.close();
            }
        } catch (NoClassDefFoundError | Exception e) {
            throw new RuntimeException("Erro ao processar template PDF. Certifique-se de que as dependências do PDFBox foram baixadas. " +
                    "Execute 'mvn clean install' ou recarregue o projeto no IDE. Erro: " + e.getMessage(), e);
        }
    }

    /**
     * Gera PDF a partir de template DOCX (para TermoEncerramento)
     * Tenta múltiplas estratégias para lidar com templates do Google Docs
     */
    private byte[] gerarPdfDeTemplateDocxEncerramento(byte[] templateBytes, DadosTermoEncerramento dados) throws IOException {
        validarBytesDocx(templateBytes);
        ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
        Exception ultimoErro = null;
        
        // Estratégia 0: Criar novo documento limpo copiando apenas o conteúdo (mais agressiva, mas garante estilos)
        // Esta estratégia cria um documento completamente novo que sempre tem estilos
        try {
            XWPFDocument documentOriginal = new XWPFDocument(new ByteArrayInputStream(templateBytes));
            XWPFDocument documentNovo = new XWPFDocument();
            
            // Força criação de estilos no novo documento acessando-os
            // Um novo XWPFDocument deve ter estilos, mas acessá-los garante que sejam criados
            try {
                org.apache.poi.xwpf.usermodel.XWPFStyles styles = documentNovo.getStyles();
                if (styles == null) {
                    // Se não existir, tenta criar
                    try {
                        documentNovo.createStyles();
                    } catch (Exception e2) {
                        // Se createStyles não existir, tenta forçar criação criando um parágrafo com estilo
                        XWPFParagraph p = documentNovo.createParagraph();
                        p.setStyle("Normal");
                    }
                }
            } catch (Exception e) {
                // Se houver erro, continua - o documento pode ter estilos mesmo assim
            }
            
            // Copia parágrafos
            for (XWPFParagraph paragraph : documentOriginal.getParagraphs()) {
                String texto = paragraph.getText();
                if (texto != null && !texto.trim().isEmpty()) {
                    XWPFParagraph novoParagrafo = documentNovo.createParagraph();
                    // Substitui variáveis no texto
                    texto = substituirTodasVariaveisEncerramento(texto, dados);
                    novoParagrafo.createRun().setText(texto);
                }
            }
            
            // Copia tabelas
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : documentOriginal.getTables()) {
                org.apache.poi.xwpf.usermodel.XWPFTable novaTabela = documentNovo.createTable();
                boolean primeiraLinha = true;
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                    org.apache.poi.xwpf.usermodel.XWPFTableRow novaRow = primeiraLinha ? novaTabela.getRow(0) : novaTabela.createRow();
                    primeiraLinha = false;
                    
                    int cellIndex = 0;
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        org.apache.poi.xwpf.usermodel.XWPFTableCell novaCell;
                        if (cellIndex < novaRow.getTableCells().size()) {
                            novaCell = novaRow.getCell(cellIndex);
                        } else {
                            novaCell = novaRow.createCell();
                        }
                        cellIndex++;
                        
                        String texto = cell.getText();
                        if (texto != null && !texto.trim().isEmpty()) {
                            texto = substituirTodasVariaveisEncerramento(texto, dados);
                            if (novaCell.getParagraphs().isEmpty()) {
                                novaCell.addParagraph().createRun().setText(texto);
                            } else {
                                novaCell.getParagraphs().get(0).createRun().setText(texto);
                            }
                        }
                    }
                }
            }
            
            documentOriginal.close();
            
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(documentNovo, pdfOutputStream, options);
            documentNovo.close();
            
            if (pdfOutputStream.size() > 0) {
                return pdfOutputStream.toByteArray();
            }
        } catch (Exception e) {
            ultimoErro = e;
            pdfOutputStream.reset();
        }
        
        // Estratégia 1: Conversão direta (mais rápida, funciona para templates do Word)
        try {
            XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(templateBytes));
            substituirVariaveisEncerramento(document, dados);
            
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, pdfOutputStream, options);
            document.close();
            
            if (pdfOutputStream.size() > 0) {
                return pdfOutputStream.toByteArray();
            }
        } catch (Exception e) {
            ultimoErro = e;
            pdfOutputStream.reset();
        }
        
        // Estratégia 2: Salvar e recarregar para normalizar (remove problemas de XML)
        try {
            XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(templateBytes));
            substituirVariaveisEncerramento(document, dados);
            
            // Salva em memória e recarrega para normalizar o XML
            ByteArrayOutputStream docxNormalizado = new ByteArrayOutputStream();
            document.write(docxNormalizado);
            document.close();
            
            XWPFDocument documentNormalizado = new XWPFDocument(new ByteArrayInputStream(docxNormalizado.toByteArray()));
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(documentNormalizado, pdfOutputStream, options);
            documentNormalizado.close();
            
            if (pdfOutputStream.size() > 0) {
                return pdfOutputStream.toByteArray();
            }
        } catch (Exception e) {
            ultimoErro = e;
            pdfOutputStream.reset();
        }
        
        // Se todas as estratégias falharam, lança exceção com mensagem detalhada
        throw new RuntimeException(mensagemErroConversaoDocx(ultimoErro), ultimoErro);
    }

    /**
     * Substitui variáveis no documento PDF (para TermoPlanejamento)
     */
    private void substituirVariaveisNoPdfPlanejamento(PDDocument document, DadosTermoPlanejamento dados) throws IOException {
        // NOTA: Substituição de texto em PDFs é muito complexa
        // Esta implementação básica não faz substituição real
        // Por enquanto, apenas valida que o PDF pode ser carregado
    }
    
    /**
     * Substitui variáveis no documento PDF (para TermoEncerramento)
     */
    private void substituirVariaveisNoPdfEncerramento(PDDocument document, DadosTermoEncerramento dados) throws IOException {
        // NOTA: Substituição de texto em PDFs é muito complexa
        // Esta implementação básica não faz substituição real
        // Por enquanto, apenas valida que o PDF pode ser carregado
    }
    
    /**
     * Substitui todas as variáveis no texto (para TermoPlanejamento)
     */
    private String substituirTodasVariaveisPlanejamento(String texto, DadosTermoPlanejamento dados) {
        texto = substituirVariavel(texto, "${PROJETO_NOME}", dados.getProjetoNome());
        texto = substituirVariavel(texto, "${PROJETO_COD_TED}", dados.getProjetoCodTed());
        texto = substituirVariavel(texto, "${DEMANDA_CODIGO}", dados.getDemandaCodigo());
        texto = substituirVariavel(texto, "${DEMANDA_NOME}", dados.getDemandaNome());
        texto = substituirVariavel(texto, "${DEMANDA_DESCRICAO}", dados.getDemandaDescricao());
        texto = substituirVariavel(texto, "${ESPECIFICACAO}", dados.getEspecificacao());
        texto = substituirVariavel(texto, "${CRONOGRAMA}", dados.getCronograma());
        texto = substituirVariavel(texto, "${RESULTADO_ESPERADO}", dados.getResultadoEsperado());
        texto = substituirVariavel(texto, "${DATA_ABERTURA}", dados.getDataAbertura());
        texto = substituirVariavel(texto, "${DATA_ABERTURA_COMPLETA}", dados.getDataAberturaCompleta());
        texto = substituirVariavel(texto, "${USUARIO_DEMANDA_NOME}", dados.getUsuarioDemandaNome());
        texto = substituirVariavel(texto, "${USUARIO_DEMANDA_EMAIL}", dados.getUsuarioDemandaEmail());
        texto = substituirVariavel(texto, "${USUARIO_TERMO_NOME}", dados.getUsuarioTermoNome());
        texto = substituirVariavel(texto, "${USUARIO_TERMO_EMAIL}", dados.getUsuarioTermoEmail());
        texto = substituirVariavel(texto, "${DATA_ASSINATURA}", dados.getDataAssinatura());
        texto = substituirVariavel(texto, "${CUSTOS_DETALHADOS}", dados.getCustosDetalhados() != null ? dados.getCustosDetalhados() : "");
        texto = substituirVariavel(texto, "${CUSTO_TOTAL}", dados.getCustoTotal() != null ? dados.getCustoTotal() : "");
        return texto;
    }
    
    /**
     * Substitui todas as variáveis no texto
     */
    private String substituirTodasVariaveis(String texto, DadosTermoAbertura dados) {
        texto = substituirVariavel(texto, "${PROJETO_NOME}", dados.getProjetoNome());
        texto = substituirVariavel(texto, "${PROJETO_COD_TED}", dados.getProjetoCodTed());
        texto = substituirVariavel(texto, "${DEMANDA_CODIGO}", dados.getDemandaCodigo());
        texto = substituirVariavel(texto, "${DEMANDA_NOME}", dados.getDemandaNome());
        texto = substituirVariavel(texto, "${DEMANDA_DESCRICAO}", dados.getDemandaDescricao());
        texto = substituirVariavel(texto, "${TERMO_DESCRICAO}", dados.getTermoDescricao());
        texto = substituirVariavel(texto, "${DATA_ABERTURA}", dados.getDataAbertura());
        texto = substituirVariavel(texto, "${DATA_ABERTURA_COMPLETA}", dados.getDataAberturaCompleta());
        texto = substituirVariavel(texto, "${USUARIO_DEMANDA_NOME}", dados.getUsuarioDemandaNome());
        texto = substituirVariavel(texto, "${USUARIO_DEMANDA_EMAIL}", dados.getUsuarioDemandaEmail());
        texto = substituirVariavel(texto, "${USUARIO_TERMO_NOME}", dados.getUsuarioTermoNome());
        texto = substituirVariavel(texto, "${USUARIO_TERMO_EMAIL}", dados.getUsuarioTermoEmail());
        texto = substituirVariavel(texto, "${DATA_ASSINATURA}", dados.getDataAssinatura());
        return texto;
    }

    /**
     * Substitui variáveis no documento DOCX
     * Suporta variáveis no formato ${VARIAVEL}
     * Preserva a estrutura e formatação do documento
     * Processa: corpo do documento, cabeçalhos e rodapés
     */
    private void substituirVariaveis(XWPFDocument document, DadosTermoAbertura dados) {
        // Processa parágrafos do corpo do documento
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            substituirVariaveisNoParagrafo(paragraph, dados);
        }

        // Processa tabelas do corpo do documento
        for (org.apache.poi.xwpf.usermodel.XWPFTable table : document.getTables()) {
            for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        substituirVariaveisNoParagrafo(paragraph, dados);
                    }
                }
            }
        }
        
        // Processa CABEÇALHOS (headers) - onde ${PROJETO_COD_TED} e ${PROJETO_NOME} podem estar
        for (org.apache.poi.xwpf.usermodel.XWPFHeader header : document.getHeaderList()) {
            // Processa parágrafos do cabeçalho
            for (XWPFParagraph paragraph : header.getParagraphs()) {
                substituirVariaveisNoParagrafo(paragraph, dados);
            }
            
            // Processa tabelas do cabeçalho
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : header.getTables()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            substituirVariaveisNoParagrafo(paragraph, dados);
                        }
                    }
                }
            }
        }
        
        // Processa RODAPÉS (footers)
        for (org.apache.poi.xwpf.usermodel.XWPFFooter footer : document.getFooterList()) {
            // Processa parágrafos do rodapé
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                substituirVariaveisNoParagrafo(paragraph, dados);
            }
            
            // Processa tabelas do rodapé
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : footer.getTables()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            substituirVariaveisNoParagrafo(paragraph, dados);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Substitui variáveis no documento DOCX (para TermoPlanejamento)
     * Suporta variáveis no formato ${VARIAVEL}
     * Preserva a estrutura e formatação do documento
     * Processa: corpo do documento, cabeçalhos e rodapés
     */
    private void substituirVariaveisPlanejamento(XWPFDocument document, DadosTermoPlanejamento dados) {
        // Processa parágrafos do corpo do documento
        // Cria lista de parágrafos para processar (pode modificar durante iteração)
        List<XWPFParagraph> paragraphs = new ArrayList<>(document.getParagraphs());
        for (XWPFParagraph paragraph : paragraphs) {
            substituirVariaveisNoParagrafoPlanejamento(document, paragraph, dados);
        }

        // Processa tabelas do corpo do documento
        for (org.apache.poi.xwpf.usermodel.XWPFTable table : document.getTables()) {
            for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        substituirVariaveisNoParagrafoPlanejamento(document, paragraph, dados);
                    }
                }
            }
        }
        
        // Processa CABEÇALHOS (headers)
        for (org.apache.poi.xwpf.usermodel.XWPFHeader header : document.getHeaderList()) {
            // Processa parágrafos do cabeçalho
            for (XWPFParagraph paragraph : header.getParagraphs()) {
                substituirVariaveisNoParagrafoPlanejamento(document, paragraph, dados);
            }
            
            // Processa tabelas do cabeçalho
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : header.getTables()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            substituirVariaveisNoParagrafoPlanejamento(document, paragraph, dados);
                        }
                    }
                }
            }
        }
        
        // Processa RODAPÉS (footers)
        for (org.apache.poi.xwpf.usermodel.XWPFFooter footer : document.getFooterList()) {
            // Processa parágrafos do rodapé
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                substituirVariaveisNoParagrafoPlanejamento(document, paragraph, dados);
            }
            
            // Processa tabelas do rodapé
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : footer.getTables()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            substituirVariaveisNoParagrafoPlanejamento(document, paragraph, dados);
                        }
                    }
                }
            }
        }
    }

    /**
     * Substitui variáveis em um parágrafo específico
     * Abordagem melhorada para lidar com variáveis divididas entre múltiplos runs (comum no Google Docs)
     */
    private void substituirVariaveisNoParagrafo(XWPFParagraph paragraph, DadosTermoAbertura dados) {
        // Obtém o texto completo do parágrafo de duas formas para garantir detecção
        String textoCompleto = paragraph.getText();
        
        // Também acumula texto diretamente dos runs (mais confiável)
        List<XWPFRun> runs = paragraph.getRuns();
        StringBuilder textoDosRuns = new StringBuilder();
        for (XWPFRun run : runs) {
            try {
                String runText = run.getText(0);
                if (runText != null) {
                    textoDosRuns.append(runText);
                }
            } catch (Exception e) {
                // Ignora erros ao ler texto do run
            }
        }
        
        // Usa o texto dos runs se o getText() do parágrafo não funcionou bem
        String textoParaProcessar = textoCompleto;
        if ((textoCompleto == null || textoCompleto.trim().isEmpty()) && textoDosRuns.length() > 0) {
            textoParaProcessar = textoDosRuns.toString();
        } else if (textoDosRuns.length() > textoCompleto.length()) {
            // Se o texto dos runs é maior, pode ser mais completo
            textoParaProcessar = textoDosRuns.toString();
        }
        
        if (textoParaProcessar == null || textoParaProcessar.trim().isEmpty()) {
            return;
        }

        // Verifica se há variáveis no texto (com ou sem $)
        boolean temVariaveis = textoParaProcessar.contains("${") || 
                               textoParaProcessar.contains("{PROJETO") || 
                               textoParaProcessar.contains("{DEMANDA") || 
                               textoParaProcessar.contains("{TERMO") ||
                               textoParaProcessar.contains("{DATA") || 
                               textoParaProcessar.contains("{USUARIO");
        
        if (!temVariaveis) {
            return;
        }

        // Substitui todas as variáveis no texto completo
        String textoSubstituido = substituirTodasVariaveis(textoParaProcessar, dados);
        
        // Também tenta substituir variáveis sem o $ (caso o template tenha {PROJETO_COD_TED} sem $)
        textoSubstituido = substituirVariavelSemDolar(textoSubstituido, dados);

        // Sempre reconstrói os runs quando há variáveis para garantir substituição completa
        if (temVariaveis) {
            // Tenta encontrar o run que contém a variável para preservar sua formatação original
            int fontSize = -1;
            String fontFamily = null;
            boolean isBold = false;
            boolean isItalic = false;
            
            // Procura o run que contém a variável (preserva formatação original)
            // Prioriza runs que contêm a parte principal da variável (com ${ ou com o nome completo)
            XWPFRun runComVariavel = null;
            XWPFRun runComFormatoEspecial = null; // Run com formatação especial (negrito, itálico)
            
            for (XWPFRun run : runs) {
                try {
                    String runText = run.getText(0);
                    if (runText != null) {
                        boolean temFormatoEspecial = run.isBold() || run.isItalic();
                        
                        // Verifica se contém variáveis conhecidas
                        if (runText.contains("${PROJETO") || runText.contains("${DEMANDA") || 
                            runText.contains("${TERMO") || runText.contains("${DATA") || 
                            runText.contains("${USUARIO") || runText.contains("${")) {
                            // Este run contém a variável, preserva sua formatação
                            runComVariavel = run;
                            fontSize = run.getFontSize();
                            fontFamily = run.getFontFamily();
                            isBold = run.isBold();
                            isItalic = run.isItalic();
                            // Se tem formatação especial, já encontrou o melhor candidato
                            if (temFormatoEspecial) {
                                break;
                            }
                        } else if (runText.contains("{PROJETO") || runText.contains("{DEMANDA") || 
                                   runText.contains("{TERMO") || runText.contains("{DATA") || 
                                   runText.contains("{USUARIO")) {
                            // Variável sem $, também preserva formatação
                            if (runComVariavel == null) {
                                runComVariavel = run;
                                fontSize = run.getFontSize();
                                fontFamily = run.getFontFamily();
                                isBold = run.isBold();
                                isItalic = run.isItalic();
                            }
                        }
                        
                        // Guarda run com formatação especial para usar como fallback
                        if (temFormatoEspecial && runComFormatoEspecial == null) {
                            runComFormatoEspecial = run;
                        }
                    }
                } catch (Exception e) {
                    // Ignora erros
                }
            }
            
            // Se encontrou run com variável, mas sem formatação especial, e existe um run com formatação especial,
            // pode ser que a variável esteja em run separado - usa o run com formatação especial
            if (runComVariavel != null && !isBold && !isItalic && runComFormatoEspecial != null) {
                // Verifica se o run com formatação especial está próximo (últimos runs geralmente contêm a variável)
                int indiceVariavel = runs.indexOf(runComVariavel);
                int indiceFormato = runs.indexOf(runComFormatoEspecial);
                // Se o run formatado está próximo (dentro de 2 runs), usa sua formatação
                if (Math.abs(indiceVariavel - indiceFormato) <= 2) {
                    fontSize = runComFormatoEspecial.getFontSize();
                    fontFamily = runComFormatoEspecial.getFontFamily();
                    isBold = runComFormatoEspecial.isBold();
                    isItalic = runComFormatoEspecial.isItalic();
                }
            }
            
            // Se não encontrou run com variável, procura nos últimos runs (variáveis geralmente estão no final)
            // e prioriza runs com formatação especial (negrito, itálico)
            if (runComVariavel == null && !runs.isEmpty()) {
                // Verifica os últimos 3 runs (onde geralmente está a variável)
                int inicio = Math.max(0, runs.size() - 3);
                for (int i = runs.size() - 1; i >= inicio; i--) {
                    try {
                        XWPFRun run = runs.get(i);
                        if (run != null) {
                            boolean temFormatoEspecial = run.isBold() || run.isItalic();
                            // Se encontrou run com formatação especial nos últimos runs, usa ele
                            if (temFormatoEspecial) {
                                fontSize = run.getFontSize();
                                fontFamily = run.getFontFamily();
                                isBold = run.isBold();
                                isItalic = run.isItalic();
                                break;
                            } else if (fontSize == -1) {
                                // Guarda o último run como fallback
                                fontSize = run.getFontSize();
                                fontFamily = run.getFontFamily();
                                isBold = run.isBold();
                                isItalic = run.isItalic();
                            }
                        }
                    } catch (Exception e) {
                        // Continua procurando
                    }
                }
                
                // Se ainda não encontrou, usa o primeiro run como último recurso
                if (fontSize == -1) {
                    try {
                        XWPFRun primeiroRun = runs.get(0);
                        if (primeiroRun != null) {
                            fontSize = primeiroRun.getFontSize();
                            fontFamily = primeiroRun.getFontFamily();
                            isBold = primeiroRun.isBold();
                            isItalic = primeiroRun.isItalic();
                        }
                    } catch (Exception e2) {
                        // Ignora erros ao ler formatação
                    }
                }
            }
            
            // Remove TODOS os runs existentes para garantir substituição completa
            // Isso é necessário porque variáveis podem estar divididas entre múltiplos runs
            int tamanhoRuns = paragraph.getRuns().size();
            for (int i = tamanhoRuns - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }
            
            // Se o texto substituído não está vazio, cria runs com formatação apropriada
            if (textoSubstituido != null && !textoSubstituido.trim().isEmpty()) {
                // Verifica se o texto contém HTML/rich text
                if (contemHtml(textoSubstituido)) {
                    // Processa HTML e cria runs com formatação
                    processarTextoComHtml(paragraph, textoSubstituido, fontSize, fontFamily, isBold, isItalic);
                } else {
                    // Texto simples, cria um único run preservando formatação original
                    XWPFRun novoRun = paragraph.createRun();
                    novoRun.setText(textoSubstituido);
                    
                    // Restaura a formatação salva (sempre aplica, mesmo se valores padrão)
                    try {
                        // Aplica tamanho da fonte se foi detectado
                        if (fontSize > 0) {
                            novoRun.setFontSize(fontSize);
                        }
                        // Aplica família da fonte se foi detectada
                        if (fontFamily != null && !fontFamily.isEmpty()) {
                            novoRun.setFontFamily(fontFamily);
                        }
                        // Aplica negrito (importante preservar!)
                        novoRun.setBold(isBold);
                        // Aplica itálico
                        novoRun.setItalic(isItalic);
                    } catch (Exception e) {
                        // Se houver erro, tenta aplicar pelo menos negrito e itálico
                        try {
                            novoRun.setBold(isBold);
                            novoRun.setItalic(isItalic);
                        } catch (Exception e2) {
                            // Ignora erros de formatação
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Substitui variáveis em um parágrafo específico (para TermoPlanejamento)
     * Abordagem melhorada para lidar com variáveis divididas entre múltiplos runs (comum no Google Docs)
     */
    private void substituirVariaveisNoParagrafoPlanejamento(XWPFDocument document, XWPFParagraph paragraph, DadosTermoPlanejamento dados) {
        // Obtém o texto completo do parágrafo de duas formas para garantir detecção
        String textoCompleto = paragraph.getText();
        
        // Também acumula texto diretamente dos runs (mais confiável)
        List<XWPFRun> runs = paragraph.getRuns();
        StringBuilder textoDosRuns = new StringBuilder();
        for (XWPFRun run : runs) {
            try {
                String runText = run.getText(0);
                if (runText != null) {
                    textoDosRuns.append(runText);
                }
            } catch (Exception e) {
                // Ignora erros ao ler texto do run
            }
        }
        
        // Usa o texto dos runs se o getText() do parágrafo não funcionou bem
        String textoParaProcessar = textoCompleto;
        if ((textoCompleto == null || textoCompleto.trim().isEmpty()) && textoDosRuns.length() > 0) {
            textoParaProcessar = textoDosRuns.toString();
        } else if (textoDosRuns.length() > (textoCompleto != null ? textoCompleto.length() : 0)) {
            // Se o texto dos runs é maior, pode ser mais completo
            textoParaProcessar = textoDosRuns.toString();
        }
        
        if (textoParaProcessar == null || textoParaProcessar.trim().isEmpty()) {
            return;
        }

        // Verifica se há variáveis no texto (com ou sem $)
        boolean temVariaveis = textoParaProcessar.contains("${") || 
                               textoParaProcessar.contains("{PROJETO") || 
                               textoParaProcessar.contains("{DEMANDA") || 
                               textoParaProcessar.contains("{TERMO") ||
                               textoParaProcessar.contains("{ESPECIFICACAO") ||
                               textoParaProcessar.contains("{CRONOGRAMA") ||
                               textoParaProcessar.contains("{RESULTADO") ||
                               textoParaProcessar.contains("{CUSTOS") ||
                               textoParaProcessar.contains("{CUSTO_TOTAL") ||
                               textoParaProcessar.contains("{DATA") || 
                               textoParaProcessar.contains("{USUARIO");
        
        if (!temVariaveis) {
            return;
        }

        // Substitui todas as variáveis no texto completo
        String textoSubstituido = substituirTodasVariaveisPlanejamento(textoParaProcessar, dados);
        
        // Também tenta substituir variáveis sem o $ (caso o template tenha {PROJETO_COD_TED} sem $)
        textoSubstituido = substituirVariavelSemDolarPlanejamento(textoSubstituido, dados);

        // Sempre reconstrói os runs quando há variáveis para garantir substituição completa
        if (temVariaveis) {
            // Tenta encontrar o run que contém a variável para preservar sua formatação original
            int fontSize = -1;
            String fontFamily = null;
            boolean isBold = false;
            boolean isItalic = false;
            
            // Procura o run que contém a variável (preserva formatação original)
            XWPFRun runComVariavel = null;
            XWPFRun runComFormatoEspecial = null;
            
            for (XWPFRun run : runs) {
                try {
                    String runText = run.getText(0);
                    if (runText != null) {
                        boolean temFormatoEspecial = run.isBold() || run.isItalic();
                        
                        if (runText.contains("${PROJETO") || runText.contains("${DEMANDA") || 
                            runText.contains("${TERMO") || runText.contains("${ESPECIFICACAO") ||
                            runText.contains("${CRONOGRAMA") || runText.contains("${RESULTADO") ||
                            runText.contains("${DATA") || runText.contains("${USUARIO") || 
                            runText.contains("${")) {
                            runComVariavel = run;
                            fontSize = run.getFontSize();
                            fontFamily = run.getFontFamily();
                            isBold = run.isBold();
                            isItalic = run.isItalic();
                            if (temFormatoEspecial) {
                                break;
                            }
                        }
                        
                        if (temFormatoEspecial && runComFormatoEspecial == null) {
                            runComFormatoEspecial = run;
                        }
                    }
                } catch (Exception e) {
                    // Ignora erros
                }
            }
            
            // Se encontrou run com variável, mas sem formatação especial, e existe um run com formatação especial,
            // pode ser que a variável esteja em run separado - usa o run com formatação especial
            if (runComVariavel != null && !isBold && !isItalic && runComFormatoEspecial != null) {
                int indiceVariavel = runs.indexOf(runComVariavel);
                int indiceFormato = runs.indexOf(runComFormatoEspecial);
                if (Math.abs(indiceVariavel - indiceFormato) <= 2) {
                    fontSize = runComFormatoEspecial.getFontSize();
                    fontFamily = runComFormatoEspecial.getFontFamily();
                    isBold = runComFormatoEspecial.isBold();
                    isItalic = runComFormatoEspecial.isItalic();
                }
            }
            
            // Se não encontrou run com variável, procura nos últimos runs
            if (runComVariavel == null && !runs.isEmpty()) {
                int inicio = Math.max(0, runs.size() - 3);
                for (int i = runs.size() - 1; i >= inicio; i--) {
                    try {
                        XWPFRun run = runs.get(i);
                        if (run != null) {
                            boolean temFormatoEspecial = run.isBold() || run.isItalic();
                            if (temFormatoEspecial) {
                                fontSize = run.getFontSize();
                                fontFamily = run.getFontFamily();
                                isBold = run.isBold();
                                isItalic = run.isItalic();
                                break;
                            } else if (fontSize == -1) {
                                fontSize = run.getFontSize();
                                fontFamily = run.getFontFamily();
                                isBold = run.isBold();
                                isItalic = run.isItalic();
                            }
                        }
                    } catch (Exception e) {
                        // Continua procurando
                    }
                }
                
                if (fontSize == -1) {
                    try {
                        XWPFRun primeiroRun = runs.get(0);
                        if (primeiroRun != null) {
                            fontSize = primeiroRun.getFontSize();
                            fontFamily = primeiroRun.getFontFamily();
                            isBold = primeiroRun.isBold();
                            isItalic = primeiroRun.isItalic();
                        }
                    } catch (Exception e2) {
                        // Ignora erros
                    }
                }
            }
            
            // Remove TODOS os runs existentes
            int tamanhoRuns = paragraph.getRuns().size();
            for (int i = tamanhoRuns - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }
            
            // Se o texto substituído não está vazio, cria runs com formatação apropriada
            if (textoSubstituido != null && !textoSubstituido.trim().isEmpty()) {
                // Verifica se contém tabela HTML APÓS substituição das variáveis
                if (contemTabelaHtml(textoSubstituido)) {
                    // Cria tabela real no documento (passa o custo total para adicionar linha final)
                    criarTabelaAPartirDeHtml(document, paragraph, textoSubstituido, dados.getCustoTotal());
                } else if (contemHtml(textoSubstituido)) {
                    processarTextoComHtml(paragraph, textoSubstituido, fontSize, fontFamily, isBold, isItalic);
                } else {
                    XWPFRun novoRun = paragraph.createRun();
                    novoRun.setText(textoSubstituido);
                    
                    try {
                        if (fontSize > 0) {
                            novoRun.setFontSize(fontSize);
                        }
                        if (fontFamily != null && !fontFamily.isEmpty()) {
                            novoRun.setFontFamily(fontFamily);
                        }
                        novoRun.setBold(isBold);
                        novoRun.setItalic(isItalic);
                    } catch (Exception e) {
                        try {
                            novoRun.setBold(isBold);
                            novoRun.setItalic(isItalic);
                        } catch (Exception e2) {
                            // Ignora erros
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Substitui variáveis que podem estar sem o símbolo $ (ex: {PROJETO_COD_TED} em vez de ${PROJETO_COD_TED})
     */
    private String substituirVariavelSemDolar(String texto, DadosTermoAbertura dados) {
        if (texto == null) {
            return texto;
        }
        texto = substituirVariavel(texto, "{PROJETO_NOME}", dados.getProjetoNome());
        texto = substituirVariavel(texto, "{PROJETO_COD_TED}", dados.getProjetoCodTed());
        texto = substituirVariavel(texto, "{DEMANDA_CODIGO}", dados.getDemandaCodigo());
        texto = substituirVariavel(texto, "{DEMANDA_NOME}", dados.getDemandaNome());
        texto = substituirVariavel(texto, "{DEMANDA_DESCRICAO}", dados.getDemandaDescricao());
        texto = substituirVariavel(texto, "{TERMO_DESCRICAO}", dados.getTermoDescricao());
        texto = substituirVariavel(texto, "{DATA_ABERTURA}", dados.getDataAbertura());
        texto = substituirVariavel(texto, "{DATA_ABERTURA_COMPLETA}", dados.getDataAberturaCompleta());
        texto = substituirVariavel(texto, "{USUARIO_DEMANDA_NOME}", dados.getUsuarioDemandaNome());
        texto = substituirVariavel(texto, "{USUARIO_DEMANDA_EMAIL}", dados.getUsuarioDemandaEmail());
        texto = substituirVariavel(texto, "{USUARIO_TERMO_NOME}", dados.getUsuarioTermoNome());
        texto = substituirVariavel(texto, "{USUARIO_TERMO_EMAIL}", dados.getUsuarioTermoEmail());
        texto = substituirVariavel(texto, "{DATA_ASSINATURA}", dados.getDataAssinatura());
        return texto;
    }
    
    /**
     * Substitui variáveis que podem estar sem o símbolo $ (para TermoPlanejamento)
     */
    private String substituirVariavelSemDolarPlanejamento(String texto, DadosTermoPlanejamento dados) {
        if (texto == null) {
            return texto;
        }
        texto = substituirVariavel(texto, "{PROJETO_NOME}", dados.getProjetoNome());
        texto = substituirVariavel(texto, "{PROJETO_COD_TED}", dados.getProjetoCodTed());
        texto = substituirVariavel(texto, "{DEMANDA_CODIGO}", dados.getDemandaCodigo());
        texto = substituirVariavel(texto, "{DEMANDA_NOME}", dados.getDemandaNome());
        texto = substituirVariavel(texto, "{DEMANDA_DESCRICAO}", dados.getDemandaDescricao());
        texto = substituirVariavel(texto, "{ESPECIFICACAO}", dados.getEspecificacao());
        texto = substituirVariavel(texto, "{CRONOGRAMA}", dados.getCronograma());
        texto = substituirVariavel(texto, "{RESULTADO_ESPERADO}", dados.getResultadoEsperado());
        texto = substituirVariavel(texto, "{DATA_ABERTURA}", dados.getDataAbertura());
        texto = substituirVariavel(texto, "{DATA_ABERTURA_COMPLETA}", dados.getDataAberturaCompleta());
        texto = substituirVariavel(texto, "{USUARIO_DEMANDA_NOME}", dados.getUsuarioDemandaNome());
        texto = substituirVariavel(texto, "{USUARIO_DEMANDA_EMAIL}", dados.getUsuarioDemandaEmail());
        texto = substituirVariavel(texto, "{USUARIO_TERMO_NOME}", dados.getUsuarioTermoNome());
        texto = substituirVariavel(texto, "{USUARIO_TERMO_EMAIL}", dados.getUsuarioTermoEmail());
        texto = substituirVariavel(texto, "{DATA_ASSINATURA}", dados.getDataAssinatura());
        texto = substituirVariavel(texto, "{CUSTOS_DETALHADOS}", dados.getCustosDetalhados() != null ? dados.getCustosDetalhados() : "");
        texto = substituirVariavel(texto, "{CUSTO_TOTAL}", dados.getCustoTotal() != null ? dados.getCustoTotal() : "");
        return texto;
    }
    
    /**
     * Substitui variáveis no documento DOCX (para TermoEncerramento)
     */
    private void substituirVariaveisEncerramento(XWPFDocument document, DadosTermoEncerramento dados) {
        // Processa parágrafos do corpo do documento
        // Cria lista de parágrafos para processar (pode modificar durante iteração)
        List<XWPFParagraph> paragraphs = new ArrayList<>(document.getParagraphs());
        for (XWPFParagraph paragraph : paragraphs) {
            substituirVariaveisNoParagrafoEncerramento(document, paragraph, dados);
        }

        // Processa tabelas do corpo do documento
        for (org.apache.poi.xwpf.usermodel.XWPFTable table : document.getTables()) {
            for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph paragraph : cell.getParagraphs()) {
                        substituirVariaveisNoParagrafoEncerramento(document, paragraph, dados);
                    }
                }
            }
        }
        
        // Processa CABEÇALHOS (headers)
        for (org.apache.poi.xwpf.usermodel.XWPFHeader header : document.getHeaderList()) {
            // Processa parágrafos do cabeçalho
            for (XWPFParagraph paragraph : header.getParagraphs()) {
                substituirVariaveisNoParagrafoEncerramento(document, paragraph, dados);
            }
            
            // Processa tabelas do cabeçalho
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : header.getTables()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            substituirVariaveisNoParagrafoEncerramento(document, paragraph, dados);
                        }
                    }
                }
            }
        }
        
        // Processa RODAPÉS (footers)
        for (org.apache.poi.xwpf.usermodel.XWPFFooter footer : document.getFooterList()) {
            // Processa parágrafos do rodapé
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                substituirVariaveisNoParagrafoEncerramento(document, paragraph, dados);
            }
            
            // Processa tabelas do rodapé
            for (org.apache.poi.xwpf.usermodel.XWPFTable table : footer.getTables()) {
                for (org.apache.poi.xwpf.usermodel.XWPFTableRow row : table.getRows()) {
                    for (org.apache.poi.xwpf.usermodel.XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph paragraph : cell.getParagraphs()) {
                            substituirVariaveisNoParagrafoEncerramento(document, paragraph, dados);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Substitui variáveis em um parágrafo específico (para TermoEncerramento)
     * Abordagem melhorada para lidar com variáveis divididas entre múltiplos runs (comum no Google Docs)
     */
    private void substituirVariaveisNoParagrafoEncerramento(XWPFDocument document, XWPFParagraph paragraph, DadosTermoEncerramento dados) {
        // Obtém o texto completo do parágrafo de duas formas para garantir detecção
        String textoCompleto = paragraph.getText();
        
        // Também acumula texto diretamente dos runs (mais confiável)
        List<XWPFRun> runs = paragraph.getRuns();
        StringBuilder textoDosRuns = new StringBuilder();
        for (XWPFRun run : runs) {
            try {
                String runText = run.getText(0);
                if (runText != null) {
                    textoDosRuns.append(runText);
                }
            } catch (Exception e) {
                // Ignora erros ao ler texto do run
            }
        }
        
        // Usa o texto dos runs se o getText() do parágrafo não funcionou bem
        String textoParaProcessar = textoCompleto;
        if ((textoCompleto == null || textoCompleto.trim().isEmpty()) && textoDosRuns.length() > 0) {
            textoParaProcessar = textoDosRuns.toString();
        } else if (textoDosRuns.length() > textoCompleto.length()) {
            // Se o texto dos runs é maior, pode ser mais completo
            textoParaProcessar = textoDosRuns.toString();
        }
        
        if (textoParaProcessar == null || textoParaProcessar.trim().isEmpty()) {
            return;
        }

        // Verifica se há variáveis no texto (com ou sem $)
        boolean temVariaveis = textoParaProcessar.contains("${") || 
                               textoParaProcessar.contains("{PROJETO") || 
                               textoParaProcessar.contains("{DEMANDA") || 
                               textoParaProcessar.contains("{TERMO") ||
                               textoParaProcessar.contains("{RESULTADO") ||
                               textoParaProcessar.contains("{DATA") || 
                               textoParaProcessar.contains("{USUARIO") ||
                               textoParaProcessar.contains("{CUSTO");
        
        if (!temVariaveis) {
            return;
        }

        // Substitui todas as variáveis no texto completo
        String textoSubstituido = substituirTodasVariaveisEncerramento(textoParaProcessar, dados);
        
        // Também tenta substituir variáveis sem o $ (caso o template tenha {PROJETO_COD_TED} sem $)
        textoSubstituido = substituirVariavelSemDolarEncerramento(textoSubstituido, dados);
        
        // Sempre reconstrói os runs quando há variáveis para garantir substituição completa
        if (temVariaveis) {
            // Tenta encontrar o run que contém a variável para preservar sua formatação original
            int fontSize = -1;
            String fontFamily = null;
            boolean isBold = false;
            boolean isItalic = false;
            
            // Procura o run que contém a variável (preserva formatação original)
            // Prioriza runs que contêm a parte principal da variável (com ${ ou com o nome completo)
            XWPFRun runComVariavel = null;
            XWPFRun runComFormatoEspecial = null; // Run com formatação especial (negrito, itálico)
            
            for (XWPFRun run : runs) {
                try {
                    String runText = run.getText(0);
                    if (runText != null) {
                        boolean temFormatoEspecial = run.isBold() || run.isItalic();
                        
                        // Verifica se contém variáveis conhecidas
                        if (runText.contains("${PROJETO") || runText.contains("${DEMANDA") || 
                            runText.contains("${TERMO") || runText.contains("${RESULTADO") ||
                            runText.contains("${DATA") || runText.contains("${USUARIO") || 
                            runText.contains("${CUSTO") || runText.contains("${")) {
                            // Este run contém a variável, preserva sua formatação
                            runComVariavel = run;
                            fontSize = run.getFontSize();
                            fontFamily = run.getFontFamily();
                            isBold = run.isBold();
                            isItalic = run.isItalic();
                            // Se tem formatação especial, já encontrou o melhor candidato
                            if (temFormatoEspecial) {
                                break;
                            }
                        } else if (runText.contains("{PROJETO") || runText.contains("{DEMANDA") || 
                                   runText.contains("{TERMO") || runText.contains("{RESULTADO") ||
                                   runText.contains("{DATA") || runText.contains("{USUARIO") ||
                                   runText.contains("{CUSTO")) {
                            // Variável sem $, também preserva formatação
                            if (runComVariavel == null) {
                                runComVariavel = run;
                                fontSize = run.getFontSize();
                                fontFamily = run.getFontFamily();
                                isBold = run.isBold();
                                isItalic = run.isItalic();
                            }
                        }
                        
                        // Guarda run com formatação especial para usar como fallback
                        if (temFormatoEspecial && runComFormatoEspecial == null) {
                            runComFormatoEspecial = run;
                        }
                    }
                } catch (Exception e) {
                    // Ignora erros
                }
            }
            
            // Se encontrou run com variável, mas sem formatação especial, e existe um run com formatação especial,
            // pode ser que a variável esteja em run separado - usa o run com formatação especial
            if (runComVariavel != null && !isBold && !isItalic && runComFormatoEspecial != null) {
                // Verifica se o run com formatação especial está próximo (últimos runs geralmente contêm a variável)
                int indiceVariavel = runs.indexOf(runComVariavel);
                int indiceFormato = runs.indexOf(runComFormatoEspecial);
                // Se o run formatado está próximo (dentro de 2 runs), usa sua formatação
                if (Math.abs(indiceVariavel - indiceFormato) <= 2) {
                    fontSize = runComFormatoEspecial.getFontSize();
                    fontFamily = runComFormatoEspecial.getFontFamily();
                    isBold = runComFormatoEspecial.isBold();
                    isItalic = runComFormatoEspecial.isItalic();
                }
            }
            
            // Se não encontrou run com variável, procura nos últimos runs (variáveis geralmente estão no final)
            // e prioriza runs com formatação especial (negrito, itálico)
            if (runComVariavel == null && !runs.isEmpty()) {
                // Verifica os últimos 3 runs (onde geralmente está a variável)
                int inicio = Math.max(0, runs.size() - 3);
                for (int i = runs.size() - 1; i >= inicio; i--) {
                    try {
                        XWPFRun run = runs.get(i);
                        if (run != null) {
                            boolean temFormatoEspecial = run.isBold() || run.isItalic();
                            // Se encontrou run com formatação especial nos últimos runs, usa ele
                            if (temFormatoEspecial) {
                                fontSize = run.getFontSize();
                                fontFamily = run.getFontFamily();
                                isBold = run.isBold();
                                isItalic = run.isItalic();
                                break;
                            } else if (fontSize == -1) {
                                fontSize = run.getFontSize();
                                fontFamily = run.getFontFamily();
                                isBold = run.isBold();
                                isItalic = run.isItalic();
                            }
                        }
                    } catch (Exception e) {
                        // Continua procurando
                    }
                }
                
                if (fontSize == -1) {
                    try {
                        XWPFRun primeiroRun = runs.get(0);
                        if (primeiroRun != null) {
                            fontSize = primeiroRun.getFontSize();
                            fontFamily = primeiroRun.getFontFamily();
                            isBold = primeiroRun.isBold();
                            isItalic = primeiroRun.isItalic();
                        }
                    } catch (Exception e2) {
                        // Ignora erros
                    }
                }
            }
            
            // Remove TODOS os runs existentes
            int tamanhoRuns = paragraph.getRuns().size();
            for (int i = tamanhoRuns - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }
            
            // Se o texto substituído não está vazio, cria runs com formatação apropriada
            if (textoSubstituido != null && !textoSubstituido.trim().isEmpty()) {
                // Verifica se contém tabela HTML APÓS substituição das variáveis
                if (contemTabelaHtml(textoSubstituido)) {
                    // Cria tabela real no documento (passa o custo total para adicionar linha final)
                    criarTabelaAPartirDeHtml(document, paragraph, textoSubstituido, dados.getCustoTotal());
                } else if (contemHtml(textoSubstituido)) {
                    processarTextoComHtml(paragraph, textoSubstituido, fontSize, fontFamily, isBold, isItalic);
                } else {
                    XWPFRun novoRun = paragraph.createRun();
                    novoRun.setText(textoSubstituido);
                    
                    try {
                        // Aplica tamanho da fonte se foi detectado
                        if (fontSize > 0) {
                            novoRun.setFontSize(fontSize);
                        }
                        // Aplica família da fonte se foi detectada
                        if (fontFamily != null && !fontFamily.isEmpty()) {
                            novoRun.setFontFamily(fontFamily);
                        }
                        // Aplica negrito (importante preservar!)
                        novoRun.setBold(isBold);
                        // Aplica itálico
                        novoRun.setItalic(isItalic);
                    } catch (Exception e) {
                        // Se houver erro, tenta aplicar pelo menos negrito e itálico
                        try {
                            novoRun.setBold(isBold);
                            novoRun.setItalic(isItalic);
                        } catch (Exception e2) {
                            // Ignora erros de formatação
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Substitui todas as variáveis no texto (para TermoEncerramento)
     */
    private String substituirTodasVariaveisEncerramento(String texto, DadosTermoEncerramento dados) {
        texto = substituirVariavel(texto, "${PROJETO_NOME}", dados.getProjetoNome());
        texto = substituirVariavel(texto, "${PROJETO_COD_TED}", dados.getProjetoCodTed());
        texto = substituirVariavel(texto, "${DEMANDA_CODIGO}", dados.getDemandaCodigo());
        texto = substituirVariavel(texto, "${DEMANDA_NOME}", dados.getDemandaNome());
        texto = substituirVariavel(texto, "${DEMANDA_DESCRICAO}", dados.getDemandaDescricao());
        texto = substituirVariavel(texto, "${RESULTADO_ENTREGUE}", dados.getResultadoEntregue());
        texto = substituirVariavel(texto, "${DATA_TERMO}", dados.getDataTermo());
        texto = substituirVariavel(texto, "${DATA_TERMO_COMPLETA}", dados.getDataTermoCompleta());
        texto = substituirVariavel(texto, "${USUARIO_DEMANDA_NOME}", dados.getUsuarioDemandaNome());
        texto = substituirVariavel(texto, "${USUARIO_DEMANDA_EMAIL}", dados.getUsuarioDemandaEmail());
        texto = substituirVariavel(texto, "${USUARIO_TERMO_NOME}", dados.getUsuarioTermoNome());
        texto = substituirVariavel(texto, "${USUARIO_TERMO_EMAIL}", dados.getUsuarioTermoEmail());
        texto = substituirVariavel(texto, "${DATA_ASSINATURA}", dados.getDataAssinatura());
        texto = substituirVariavel(texto, "${CUSTOS_DETALHADOS}", dados.getCustosDetalhados() != null ? dados.getCustosDetalhados() : "");
        texto = substituirVariavel(texto, "${CUSTO_TOTAL}", dados.getCustoTotal() != null ? dados.getCustoTotal() : "");
        return texto;
    }
    
    /**
     * Substitui variáveis que podem estar sem o símbolo $ (para TermoEncerramento)
     */
    private String substituirVariavelSemDolarEncerramento(String texto, DadosTermoEncerramento dados) {
        if (texto == null) {
            return texto;
        }
        texto = substituirVariavel(texto, "{PROJETO_NOME}", dados.getProjetoNome());
        texto = substituirVariavel(texto, "{PROJETO_COD_TED}", dados.getProjetoCodTed());
        texto = substituirVariavel(texto, "{DEMANDA_CODIGO}", dados.getDemandaCodigo());
        texto = substituirVariavel(texto, "{DEMANDA_NOME}", dados.getDemandaNome());
        texto = substituirVariavel(texto, "{DEMANDA_DESCRICAO}", dados.getDemandaDescricao());
        texto = substituirVariavel(texto, "{RESULTADO_ENTREGUE}", dados.getResultadoEntregue());
        texto = substituirVariavel(texto, "{DATA_TERMO}", dados.getDataTermo());
        texto = substituirVariavel(texto, "{DATA_TERMO_COMPLETA}", dados.getDataTermoCompleta());
        texto = substituirVariavel(texto, "{USUARIO_DEMANDA_NOME}", dados.getUsuarioDemandaNome());
        texto = substituirVariavel(texto, "{USUARIO_DEMANDA_EMAIL}", dados.getUsuarioDemandaEmail());
        texto = substituirVariavel(texto, "{USUARIO_TERMO_NOME}", dados.getUsuarioTermoNome());
        texto = substituirVariavel(texto, "{USUARIO_TERMO_EMAIL}", dados.getUsuarioTermoEmail());
        texto = substituirVariavel(texto, "{DATA_ASSINATURA}", dados.getDataAssinatura());
        texto = substituirVariavel(texto, "{CUSTOS_DETALHADOS}", dados.getCustosDetalhados() != null ? dados.getCustosDetalhados() : "");
        texto = substituirVariavel(texto, "{CUSTO_TOTAL}", dados.getCustoTotal() != null ? dados.getCustoTotal() : "");
        return texto;
    }

    /**
     * Substitui uma variável no texto
     */
    private String substituirVariavel(String texto, String variavel, String valor) {
        if (valor == null) {
            valor = "";
        }
        return texto.replace(variavel, valor);
    }
    
    /**
     * Verifica se o texto contém tags HTML
     */
    private boolean contemHtml(String texto) {
        if (texto == null) {
            return false;
        }
        // Verifica tags HTML comuns
        return texto.contains("<font") || texto.contains("</font>") ||
               texto.contains("<b>") || texto.contains("</b>") ||
               texto.contains("<strong>") || texto.contains("</strong>") ||
               texto.contains("<i>") || texto.contains("</i>") ||
               texto.contains("<em>") || texto.contains("</em>") ||
               texto.contains("<u>") || texto.contains("</u>") ||
               texto.contains("<br") || texto.contains("<p>") || texto.contains("</p>") ||
               // Tags de tabela HTML
               texto.contains("<table") || texto.contains("</table>") ||
               texto.contains("<thead") || texto.contains("</thead>") ||
               texto.contains("<tbody") || texto.contains("</tbody>") ||
               texto.contains("<tfoot") || texto.contains("</tfoot>") ||
               texto.contains("<tr") || texto.contains("</tr>") ||
               texto.contains("<th") || texto.contains("</th>") ||
               texto.contains("<td") || texto.contains("</td>") ||
               texto.contains("<caption") || texto.contains("</caption>");
    }
    
    /**
     * Verifica se o texto contém uma tabela HTML
     */
    private boolean contemTabelaHtml(String texto) {
        if (texto == null) {
            return false;
        }
        return texto.contains("<table") && texto.contains("</table>");
    }
    
    /**
     * Cria uma tabela XWPFTable a partir de HTML e substitui o parágrafo
     */
    private void criarTabelaAPartirDeHtml(XWPFDocument document, XWPFParagraph paragraph, String textoHtml, String custoTotal) {
        try {
            // Extrai a tabela HTML do texto
            int inicioTabela = textoHtml.indexOf("<table");
            int fimTabela = textoHtml.lastIndexOf("</table>");
            
            if (inicioTabela == -1 || fimTabela == -1) {
                return;
            }
            
            String tabelaHtml = textoHtml.substring(inicioTabela, fimTabela + 8);
            String textoAntes = textoHtml.substring(0, inicioTabela).trim();
            String textoDepois = textoHtml.substring(fimTabela + 8).trim();
            
            // Extrai linhas da tabela HTML primeiro (antes de modificar o parágrafo)
            java.util.regex.Pattern rowPattern = java.util.regex.Pattern.compile("<tr[^>]*>(.*?)</tr>", 
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher rowMatcher = rowPattern.matcher(tabelaHtml);
            
            java.util.List<java.util.List<String>> linhas = new java.util.ArrayList<>();
            while (rowMatcher.find()) {
                String rowContent = rowMatcher.group(1);
                java.util.List<String> celulas = new java.util.ArrayList<>();
                
                // Extrai células (th ou td)
                java.util.regex.Pattern cellPattern = java.util.regex.Pattern.compile("<(th|td)[^>]*>(.*?)</(th|td)>", 
                    java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
                java.util.regex.Matcher cellMatcher = cellPattern.matcher(rowContent);
                
                while (cellMatcher.find()) {
                    String cellContent = cellMatcher.group(2);
                    // Remove tags HTML internas mas preserva texto
                    cellContent = cellContent.replaceAll("<[^>]+>", "");
                    cellContent = cellContent.replaceAll("&nbsp;", " ");
                    cellContent = cellContent.replaceAll("&amp;", "&");
                    cellContent = cellContent.replaceAll("&lt;", "<");
                    cellContent = cellContent.replaceAll("&gt;", ">");
                    cellContent = cellContent.replaceAll("&quot;", "\"");
                    celulas.add(cellContent.trim());
                }
                
                if (!celulas.isEmpty()) {
                    linhas.add(celulas);
                }
            }
            
            if (!linhas.isEmpty()) {
                // Determina o número de colunas baseado na primeira linha (cabeçalho)
                int numColunas = linhas.isEmpty() ? 1 : linhas.get(0).size();
                
                // Remove todo o conteúdo do parágrafo ANTES de criar a tabela
                List<XWPFRun> runs = paragraph.getRuns();
                for (int i = runs.size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }
                
                // Adiciona texto antes da tabela se houver
                if (!textoAntes.isEmpty()) {
                    XWPFRun run = paragraph.createRun();
                    run.setText(textoAntes);
                }
                
                // Usa XmlCursor para inserir a tabela logo após o parágrafo
                try {
                    // Cria cursor no final do parágrafo
                    XmlCursor cursor = paragraph.getCTP().newCursor();
                    cursor.toEndToken();
                    
                    // Move para o próximo token (após o parágrafo)
                    cursor.toNextToken();
                    
                    // Insere a tabela usando o método do documento
                    // Nota: insertNewTbl pode não existir em todas as versões do POI
                    // Se não existir, o catch vai usar o método alternativo
                    org.apache.poi.xwpf.usermodel.XWPFTable table = document.insertNewTbl(cursor);
                    
                    if (table != null) {
                        
                        // Configura o grid da tabela
                        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl tbl = table.getCTTbl();
                        if (tbl.getTblGrid() == null) {
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid grid = tbl.addNewTblGrid();
                            for (int col = 0; col < numColunas; col++) {
                                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol gridCol = grid.addNewGridCol();
                                gridCol.setW(java.math.BigInteger.valueOf(2000));
                            }
                        }
                        
                        // Remove a primeira linha padrão
                        if (table.getRows().size() > 0) {
                            table.removeRow(0);
                        }
                        
                        // Adiciona linhas e células
                        for (int i = 0; i < linhas.size(); i++) {
                            java.util.List<String> celulas = linhas.get(i);
                            org.apache.poi.xwpf.usermodel.XWPFTableRow row = table.createRow();
                            
                            // Adiciona margem bottom na linha (espaçamento após a linha)
                            try {
                                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr trPr = row.getCtRow().addNewTrPr();
                                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight height = trPr.addNewTrHeight();
                                height.setVal(java.math.BigInteger.valueOf(300)); // Altura mínima da linha em twips (aproximadamente 5.3mm)
                                height.setHRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule.AT_LEAST);
                            } catch (Exception e) {
                                // Se houver erro, continua sem margem
                            }
                            
                            while (row.getTableCells().size() < celulas.size()) {
                                row.createCell();
                            }
                            
                            for (int j = 0; j < celulas.size() && j < row.getTableCells().size(); j++) {
                                org.apache.poi.xwpf.usermodel.XWPFTableCell cell = row.getCell(j);
                                if (cell.getParagraphs().size() > 0) {
                                    cell.removeParagraph(0);
                                }
                                XWPFParagraph cellParagraph = cell.addParagraph();
                                XWPFRun cellRun = cellParagraph.createRun();
                                cellRun.setText(celulas.get(j));
                                
                                if (i == 0) {
                                    cellRun.setBold(true);
                                }
                            }
                        }
                        
                        // Adiciona linha final com custo total (se fornecido)
                        if (custoTotal != null && !custoTotal.trim().isEmpty() && numColunas > 0) {
                            org.apache.poi.xwpf.usermodel.XWPFTableRow rowTotal = table.createRow();
                            
                        // Adiciona margem bottom na linha total também
                        try {
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr trPr = rowTotal.getCtRow().addNewTrPr();
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight height = trPr.addNewTrHeight();
                            height.setVal(java.math.BigInteger.valueOf(300));
                            height.setHRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule.AT_LEAST);
                        } catch (Exception e5) {
                            // Ignora erro
                        }
                            
                            // Preenche células vazias até a penúltima coluna
                            for (int j = 0; j < numColunas - 1; j++) {
                                org.apache.poi.xwpf.usermodel.XWPFTableCell cell = rowTotal.createCell();
                                XWPFParagraph cellParagraph = cell.addParagraph();
                                XWPFRun cellRun = cellParagraph.createRun();
                                cellRun.setText("");
                            }
                            
                            // Última célula com o custo total em negrito
                            org.apache.poi.xwpf.usermodel.XWPFTableCell cellTotal = rowTotal.createCell();
                            XWPFParagraph cellTotalParagraph = cellTotal.addParagraph();
                            XWPFRun cellTotalRun = cellTotalParagraph.createRun();
                            cellTotalRun.setText(custoTotal);
                            cellTotalRun.setBold(true);
                        }
                    }
                    
                    if (cursor != null) {
                        cursor.dispose();
                    }
                    
                } catch (Exception e) {
                    // Se houver erro com XmlCursor, usa método alternativo: cria tabela no final
                    org.apache.poi.xwpf.usermodel.XWPFTable table = document.createTable();
                    
                    // Garante que a tabela tenha grid configurado
                    try {
                        org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl tbl = table.getCTTbl();
                        if (tbl.getTblGrid() == null) {
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid grid = tbl.addNewTblGrid();
                            for (int col = 0; col < numColunas; col++) {
                                org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol gridCol = grid.addNewGridCol();
                                gridCol.setW(java.math.BigInteger.valueOf(2000));
                            }
                        }
                    } catch (Exception e2) {
                        // Ignora erro
                    }
                    
                    // Remove a primeira linha padrão
                    if (table.getRows().size() > 0) {
                        table.removeRow(0);
                    }
                    
                    // Adiciona linhas e células
                    for (int i = 0; i < linhas.size(); i++) {
                        java.util.List<String> celulas = linhas.get(i);
                        org.apache.poi.xwpf.usermodel.XWPFTableRow row = table.createRow();
                        
                        // Adiciona margem bottom na linha (espaçamento após a linha)
                        try {
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr trPr = row.getCtRow().addNewTrPr();
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight height = trPr.addNewTrHeight();
                            height.setVal(java.math.BigInteger.valueOf(300)); // Altura mínima da linha em twips
                            height.setHRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule.AT_LEAST);
                        } catch (Exception e3) {
                            // Se houver erro, continua sem margem
                        }
                        
                        while (row.getTableCells().size() < celulas.size()) {
                            row.createCell();
                        }
                        
                        for (int j = 0; j < celulas.size() && j < row.getTableCells().size(); j++) {
                            org.apache.poi.xwpf.usermodel.XWPFTableCell cell = row.getCell(j);
                            if (cell.getParagraphs().size() > 0) {
                                cell.removeParagraph(0);
                            }
                            XWPFParagraph cellParagraph = cell.addParagraph();
                            XWPFRun cellRun = cellParagraph.createRun();
                            cellRun.setText(celulas.get(j));
                            
                            if (i == 0) {
                                cellRun.setBold(true);
                            }
                        }
                    }
                    
                    // Adiciona linha final com custo total (se fornecido)
                    if (custoTotal != null && !custoTotal.trim().isEmpty() && numColunas > 0) {
                        org.apache.poi.xwpf.usermodel.XWPFTableRow rowTotal = table.createRow();
                        
                        // Adiciona margem bottom na linha total também
                        try {
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTrPr trPr = rowTotal.getCtRow().addNewTrPr();
                            org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight height = trPr.addNewTrHeight();
                            height.setVal(java.math.BigInteger.valueOf(300));
                            height.setHRule(org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule.AT_LEAST);
                        } catch (Exception e4) {
                            // Ignora erro
                        }
                        
                        // Preenche células vazias até a penúltima coluna
                        for (int j = 0; j < numColunas - 1; j++) {
                            org.apache.poi.xwpf.usermodel.XWPFTableCell cell = rowTotal.createCell();
                            XWPFParagraph cellParagraph = cell.addParagraph();
                            XWPFRun cellRun = cellParagraph.createRun();
                            cellRun.setText("");
                        }
                        
                        // Última célula com o custo total em negrito
                        org.apache.poi.xwpf.usermodel.XWPFTableCell cellTotal = rowTotal.createCell();
                        XWPFParagraph cellTotalParagraph = cellTotal.addParagraph();
                        XWPFRun cellTotalRun = cellTotalParagraph.createRun();
                        cellTotalRun.setText(custoTotal);
                        cellTotalRun.setBold(true);
                    }
                }
                
                // Adiciona texto depois da tabela se houver (cria novo parágrafo)
                if (!textoDepois.isEmpty()) {
                    XWPFParagraph novoParagrafo = document.createParagraph();
                    XWPFRun run = novoParagrafo.createRun();
                    run.setText(textoDepois);
                }
            }
            
        } catch (Exception e) {
            // Se houver erro, apenas remove o conteúdo HTML e deixa texto simples
            String textoLimpo = textoHtml.replaceAll("<[^>]+>", "").replaceAll("&nbsp;", " ")
                       .replaceAll("&amp;", "&").replaceAll("&lt;", "<")
                       .replaceAll("&gt;", ">").replaceAll("&quot;", "\"");
            List<XWPFRun> runs = paragraph.getRuns();
            for (int i = runs.size() - 1; i >= 0; i--) {
                paragraph.removeRun(i);
            }
            if (!textoLimpo.trim().isEmpty()) {
                XWPFRun run = paragraph.createRun();
                run.setText(textoLimpo.trim());
            }
        }
    }
    
    /**
     * Processa texto com HTML e cria múltiplos runs preservando formatação original
     * Cada segmento com formatação diferente recebe seu próprio run
     */
    private void processarTextoComHtml(XWPFParagraph paragraph, String textoHtml, 
                                       int fontSizePadrao, String fontFamilyPadrao, 
                                       boolean boldPadrao, boolean italicPadrao) {
        try {
            // Classe auxiliar para representar um segmento de texto com formatação
            class SegmentoTexto {
                String texto;
                String fontFamily;
                int fontSize;
                boolean bold;
                boolean italic;
                boolean underline;
                
                SegmentoTexto(String texto) {
                    this.texto = texto;
                    this.fontFamily = fontFamilyPadrao;
                    this.fontSize = fontSizePadrao;
                    this.bold = boldPadrao;
                    this.italic = italicPadrao;
                    this.underline = false;
                }
            }
            
            // Parse do HTML preservando formatação
            java.util.List<SegmentoTexto> segmentos = new java.util.ArrayList<>();
            StringBuilder textoAtual = new StringBuilder();
            String fontFamilyAtual = fontFamilyPadrao;
            int fontSizeAtual = fontSizePadrao;
            boolean boldAtual = boldPadrao;
            boolean italicAtual = italicPadrao;
            boolean underlineAtual = false;
            
            int i = 0;
            while (i < textoHtml.length()) {
                if (textoHtml.charAt(i) == '<') {
                    // Encontrou uma tag HTML
                    int fimTag = textoHtml.indexOf('>', i);
                    if (fimTag == -1) break;
                    
                    String tag = textoHtml.substring(i, fimTag + 1).toLowerCase();
                    
                    // Se há texto acumulado, cria segmento antes de processar a tag
                    if (textoAtual.length() > 0) {
                        SegmentoTexto segmento = new SegmentoTexto(textoAtual.toString());
                        segmento.fontFamily = fontFamilyAtual;
                        segmento.fontSize = fontSizeAtual;
                        segmento.bold = boldAtual;
                        segmento.italic = italicAtual;
                        segmento.underline = underlineAtual;
                        segmentos.add(segmento);
                        textoAtual = new StringBuilder();
                    }
                    
                    // Processa tags de abertura
                    if (tag.startsWith("<font")) {
                        // Extrai face="..."
                        java.util.regex.Pattern fontPattern = java.util.regex.Pattern.compile(
                            "face\\s*=\\s*[\"']([^\"']+)[\"']", 
                            java.util.regex.Pattern.CASE_INSENSITIVE
                        );
                        java.util.regex.Matcher matcher = fontPattern.matcher(tag);
                        if (matcher.find()) {
                            String face = matcher.group(1);
                            if (face.contains(",")) {
                                fontFamilyAtual = face.split(",")[0].trim();
                            } else {
                                fontFamilyAtual = face.trim();
                            }
                        }
                    } else if (tag.equals("<b>") || tag.equals("<strong>")) {
                        boldAtual = true;
                    } else if (tag.equals("<i>") || tag.equals("<em>")) {
                        italicAtual = true;
                    } else if (tag.equals("<u>")) {
                        underlineAtual = true;
                    }
                    // Processa tags de fechamento
                    else if (tag.equals("</font>")) {
                        fontFamilyAtual = fontFamilyPadrao;
                        fontSizeAtual = fontSizePadrao;
                    } else if (tag.equals("</b>") || tag.equals("</strong>")) {
                        boldAtual = boldPadrao;
                    } else if (tag.equals("</i>") || tag.equals("</em>")) {
                        italicAtual = italicPadrao;
                    } else if (tag.equals("</u>")) {
                        underlineAtual = false;
                    } else if (tag.startsWith("<br")) {
                        // Quebra de linha - cria segmento vazio para forçar nova linha
                        if (textoAtual.length() > 0) {
                            SegmentoTexto segmento = new SegmentoTexto(textoAtual.toString());
                            segmento.fontFamily = fontFamilyAtual;
                            segmento.fontSize = fontSizeAtual;
                            segmento.bold = boldAtual;
                            segmento.italic = italicAtual;
                            segmento.underline = underlineAtual;
                            segmentos.add(segmento);
                            textoAtual = new StringBuilder();
                        }
                        // Adiciona quebra de linha como segmento especial
                        SegmentoTexto breakSegmento = new SegmentoTexto("\n");
                        breakSegmento.fontFamily = fontFamilyAtual;
                        breakSegmento.fontSize = fontSizeAtual;
                        breakSegmento.bold = boldAtual;
                        breakSegmento.italic = italicAtual;
                        breakSegmento.underline = underlineAtual;
                        segmentos.add(breakSegmento);
                    } else if (tag.equals("<p>")) {
                        // Início de parágrafo - adiciona quebra se houver texto anterior
                        if (textoAtual.length() > 0) {
                            SegmentoTexto segmento = new SegmentoTexto(textoAtual.toString());
                            segmento.fontFamily = fontFamilyAtual;
                            segmento.fontSize = fontSizeAtual;
                            segmento.bold = boldAtual;
                            segmento.italic = italicAtual;
                            segmento.underline = underlineAtual;
                            segmentos.add(segmento);
                            textoAtual = new StringBuilder();
                        }
                    } else if (tag.equals("</p>")) {
                        // Fim de parágrafo - adiciona quebra de linha
                        if (textoAtual.length() > 0) {
                            SegmentoTexto segmento = new SegmentoTexto(textoAtual.toString());
                            segmento.fontFamily = fontFamilyAtual;
                            segmento.fontSize = fontSizeAtual;
                            segmento.bold = boldAtual;
                            segmento.italic = italicAtual;
                            segmento.underline = underlineAtual;
                            segmentos.add(segmento);
                            textoAtual = new StringBuilder();
                        }
                        SegmentoTexto breakSegmento = new SegmentoTexto("\n");
                        breakSegmento.fontFamily = fontFamilyAtual;
                        breakSegmento.fontSize = fontSizeAtual;
                        breakSegmento.bold = boldAtual;
                        breakSegmento.italic = italicAtual;
                        breakSegmento.underline = underlineAtual;
                        segmentos.add(breakSegmento);
                    } else if (tag.startsWith("<table") || tag.startsWith("<thead") || tag.startsWith("<tbody") || 
                               tag.startsWith("<tfoot") || tag.startsWith("<caption")) {
                        // Tags de abertura de estrutura de tabela - apenas ignora
                    } else if (tag.equals("</table>") || tag.equals("</thead>") || tag.equals("</tbody>") || 
                               tag.equals("</tfoot>") || tag.equals("</caption>")) {
                        // Tags de fechamento de estrutura de tabela - apenas ignora
                    } else if (tag.startsWith("<tr")) {
                        // Início de linha da tabela - apenas ignora (o conteúdo será processado)
                    } else if (tag.equals("</tr>")) {
                        // Fim de linha da tabela - adiciona quebra de linha
                        if (textoAtual.length() > 0) {
                            SegmentoTexto segmento = new SegmentoTexto(textoAtual.toString());
                            segmento.fontFamily = fontFamilyAtual;
                            segmento.fontSize = fontSizeAtual;
                            segmento.bold = boldAtual;
                            segmento.italic = italicAtual;
                            segmento.underline = underlineAtual;
                            segmentos.add(segmento);
                            textoAtual = new StringBuilder();
                        }
                        // Adiciona quebra de linha para nova linha da tabela
                        SegmentoTexto breakSegmento = new SegmentoTexto("\n");
                        breakSegmento.fontFamily = fontFamilyAtual;
                        breakSegmento.fontSize = fontSizeAtual;
                        breakSegmento.bold = boldAtual;
                        breakSegmento.italic = italicAtual;
                        breakSegmento.underline = underlineAtual;
                        segmentos.add(breakSegmento);
                    } else if (tag.startsWith("<th") || tag.startsWith("<td")) {
                        // Início de célula (th ou td) - apenas ignora (o conteúdo será processado)
                        // Se houver texto acumulado antes da célula, adiciona como segmento
                        if (textoAtual.length() > 0) {
                            SegmentoTexto segmento = new SegmentoTexto(textoAtual.toString());
                            segmento.fontFamily = fontFamilyAtual;
                            segmento.fontSize = fontSizeAtual;
                            segmento.bold = boldAtual;
                            segmento.italic = italicAtual;
                            segmento.underline = underlineAtual;
                            segmentos.add(segmento);
                            textoAtual = new StringBuilder();
                        }
                    } else if (tag.equals("</th>") || tag.equals("</td>")) {
                        // Fim de célula - adiciona separador entre células
                        if (textoAtual.length() > 0) {
                            SegmentoTexto segmento = new SegmentoTexto(textoAtual.toString());
                            segmento.fontFamily = fontFamilyAtual;
                            segmento.fontSize = fontSizeAtual;
                            segmento.bold = boldAtual;
                            segmento.italic = italicAtual;
                            segmento.underline = underlineAtual;
                            segmentos.add(segmento);
                            textoAtual = new StringBuilder();
                        }
                        // Adiciona separador entre células (espaço duplo ou tab)
                        SegmentoTexto separatorSegmento = new SegmentoTexto("  ");
                        separatorSegmento.fontFamily = fontFamilyAtual;
                        separatorSegmento.fontSize = fontSizeAtual;
                        separatorSegmento.bold = boldAtual;
                        separatorSegmento.italic = italicAtual;
                        separatorSegmento.underline = underlineAtual;
                        segmentos.add(separatorSegmento);
                    }
                    
                    i = fimTag + 1;
                } else {
                    // Caractere normal, adiciona ao texto atual
                    textoAtual.append(textoHtml.charAt(i));
                    i++;
                }
            }
            
            // Adiciona último segmento se houver texto
            if (textoAtual.length() > 0) {
                SegmentoTexto segmento = new SegmentoTexto(textoAtual.toString());
                segmento.fontFamily = fontFamilyAtual;
                segmento.fontSize = fontSizeAtual;
                segmento.bold = boldAtual;
                segmento.italic = italicAtual;
                segmento.underline = underlineAtual;
                segmentos.add(segmento);
            }
            
            // Se não encontrou nenhum segmento, tenta remover tags e usar texto simples
            if (segmentos.isEmpty()) {
                String textoLimpo = textoHtml.replaceAll("<[^>]+>", "").trim();
                if (!textoLimpo.isEmpty()) {
                    SegmentoTexto segmento = new SegmentoTexto(textoLimpo);
                    segmentos.add(segmento);
                }
            }
            
            // Cria runs para cada segmento com sua formatação específica
            for (SegmentoTexto segmento : segmentos) {
                if (segmento.texto == null) {
                    continue;
                }
                
                // Se for quebra de linha, adiciona break ao invés de texto
                if (segmento.texto.equals("\n")) {
                    XWPFRun run = paragraph.createRun();
                    run.addBreak();
                    continue;
                }
                
                // Ignora segmentos vazios (mas não quebras de linha)
                if (segmento.texto.trim().isEmpty()) {
                    continue;
                }
                
                XWPFRun run = paragraph.createRun();
                run.setText(segmento.texto);
                
                try {
                    if (segmento.fontSize > 0) {
                        run.setFontSize(segmento.fontSize);
                    }
                    if (segmento.fontFamily != null && !segmento.fontFamily.isEmpty()) {
                        run.setFontFamily(segmento.fontFamily);
                    }
                    run.setBold(segmento.bold);
                    run.setItalic(segmento.italic);
                    // Nota: underline não é suportado diretamente no XWPFRun, mas pode ser aplicado via CT
                } catch (Exception e) {
                    // Ignora erros de formatação
                }
            }
        } catch (Exception e) {
            // Se houver erro ao processar HTML, insere como texto simples
            XWPFRun run = paragraph.createRun();
            String textoLimpo = textoHtml.replaceAll("<[^>]+>", "").trim();
            run.setText(textoLimpo);
        }
    }

    /**
     * Formata data no formato dd/MM/yyyy
     */
    private String formatarData(LocalDateTime data) {
        if (data == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return data.format(formatter);
    }

    /**
     * Formata data no formato dd/MM/yyyy HH:mm
     */
    private String formatarDataCompleta(LocalDateTime data) {
        if (data == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return data.format(formatter);
    }
    
    /**
     * Formata valor monetário no formato brasileiro (R$ 1.234,56)
     */
    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) {
            return "R$ 0,00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        symbols.setCurrencySymbol("R$ ");
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return "R$ " + format.format(valor);
    }
    
    /**
     * Gera tabela HTML com os custos detalhados
     */
    private String gerarTabelaCustos(List<CustoDetalhado> custos) {
        if (custos == null || custos.isEmpty()) {
            return "<p>Nenhum custo cadastrado.</p>";
        }
        
        StringBuilder html = new StringBuilder();
        html.append("<table border=\"1\" cellpadding=\"5\" cellspacing=\"0\" style=\"border-collapse: collapse; width: 100%;\">");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th style=\"background-color: #f0f0f0; text-align: left;\">Perfil</th>");
        html.append("<th style=\"background-color: #f0f0f0; text-align: right;\">Quantidade de Horas</th>");
        html.append("<th style=\"background-color: #f0f0f0; text-align: right;\">Valor por Hora</th>");
        html.append("<th style=\"background-color: #f0f0f0; text-align: right;\">Total</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");
        
        for (CustoDetalhado custo : custos) {
            html.append("<tr>");
            html.append("<td>").append(escapeHtml(custo.perfilNome)).append("</td>");
            html.append("<td style=\"text-align: right;\">").append(formatarDecimal(custo.qtdeHora)).append("</td>");
            html.append("<td style=\"text-align: right;\">").append(formatarMoeda(custo.valorHora)).append("</td>");
            html.append("<td style=\"text-align: right; font-weight: bold;\">").append(formatarMoeda(custo.total)).append("</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody>");
        html.append("</table>");
        
        return html.toString();
    }
    
    /**
     * Formata número decimal com 2 casas decimais
     */
    private String formatarDecimal(BigDecimal valor) {
        if (valor == null) {
            return "0,00";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("pt", "BR"));
        DecimalFormat format = new DecimalFormat("#,##0.00", symbols);
        return format.format(valor);
    }
    
    /**
     * Escapa caracteres HTML para evitar problemas de segurança
     */
    private String escapeHtml(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Classe auxiliar para representar um custo detalhado
     */
    private static class CustoDetalhado {
        String perfilNome;
        BigDecimal qtdeHora;
        BigDecimal valorHora;
        BigDecimal total;
    }

    /**
     * Carimba o PDF com os dados da assinatura digital na posição (x, y) da página.
     * Layout (fonte pequena estilo gov.br): (i) Assinado Digitalmente; (ii) usuário em negrito;
     * (iii) dataAssinatura; (iv) hash.
     */
    public byte[] carimbarAssinatura(
        byte[] pdfOriginal,
        String usuario,
        String hash,
        String ip,
        LocalDateTime dataAssinatura,
        Long pagina,
        Float x,
        Float y,
        Long width,
        Long height
    ) throws IOException {

        PDDocument document = PDDocument.load(pdfOriginal);
        int numberOfPages = document.getNumberOfPages();

        int pageIndex = (pagina != null && pagina >= 0) ? pagina.intValue() : 0;
        if (pageIndex < 0) {
            pageIndex = 0;
        } else if (pageIndex >= numberOfPages) {
            // Se vier uma página maior que o total, usamos a última página válida
            pageIndex = numberOfPages - 1;
        }

        PDPage page = document.getPage(pageIndex);
        PDRectangle mediaBox = page.getMediaBox();
        float pageWidth = mediaBox.getWidth();
        float pageHeight = mediaBox.getHeight();

        // Agora assumimos que x e y JÁ estão em coordenadas de PDF:
        // - origem no canto INFERIOR esquerdo
        // - unidade em pontos (1/72 in)
        float baseX = (x != null ? x.floatValue() : 50f);
        float baseY = (y != null ? y.floatValue() : 50f);

        // Apenas garante que o conteúdo não saia completamente fora da página.
        if (baseX < 0) baseX = 0;
        if (baseY < 0) baseY = 0;
        if (baseX > pageWidth) baseX = pageWidth;
        if (baseY > pageHeight) baseY = pageHeight;

        // Dimensões do bloco de texto.
        float fontSize = 5f;
        float lineHeight = 6f; // linhas mais próximas, combinando com fonte menor
        float textBlockHeight = lineHeight * 4; // 4 linhas
        float textBlockWidth = 160f;
        // Gap horizontal entre imagem e texto (menor, para aproximar mais).
        float gap = 2f;

        // Coordenadas iniciais do texto (caso a imagem não carregue, usamos o próprio baseX/baseY).
        float textX = baseX;
        float textY = baseY;

        java.awt.Color textColor = java.awt.Color.BLACK;

        String dataFormatada = dataAssinatura != null
                ? dataAssinatura.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "";

        try (PDPageContentStream cs = new PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true,
                true
        )) {
            // Desenha a imagem (carregada de recursos do classpath) e calcula posição do texto.
            try {
                // Coloque o arquivo em: src/main/resources/static/img/assinatura-julius.png
                PDImageXObject image = PDImageXObject.createFromByteArray(
                        document,
                        this.getClass().getResourceAsStream("/static/img/assinatura-julius.png").readAllBytes(),
                        "assinatura-julius"
                );

                // Mantém o aspecto original da imagem.
                float origW = image.getWidth();
                float origH = image.getHeight();
                // Altura levemente maior que o bloco de texto (4 linhas) para aproximar do visual anterior (~30pt).
                float imageHeight = textBlockHeight + lineHeight;
                float imageWidth = (origW / origH) * imageHeight;

                // Imagem ancorada próxima de baseX/baseY, com leve ajuste para baixo e para a direita.
                float imageX = baseX + 4f;   // ligeiro deslocamento para a direita
                float imageY = baseY - 2f;   // ligeiro deslocamento para baixo

                // Texto à direita da imagem, centralizado verticalmente em relação a ela.
                textX = imageX + imageWidth + gap;
                textY = imageY + (imageHeight / 2f) + (textBlockHeight / 2f) - lineHeight;

                // Se o texto extrapolar a largura da página, desloca o conjunto um pouco para a esquerda.
                if (textX + textBlockWidth > pageWidth) {
                    float overflow = (textX + textBlockWidth) - pageWidth;
                    textX -= overflow;
                    imageX -= overflow;
                    if (imageX < 0) {
                        // Garante que a imagem não vá para fora; reancora e recalc textX.
                        imageX = 0;
                        textX = imageX + imageWidth + gap;
                    }
                }

                cs.drawImage(image, imageX, imageY, imageWidth, imageHeight);
            } catch (Exception e) {
                // Se a imagem não existir ou falhar, seguimos apenas com o texto usando baseX/baseY.
                textX = baseX;
                textY = baseY + textBlockHeight; // bloco logo acima do ponto base
            }

            // Desenha o bloco de texto ao lado da imagem (ou sozinho, caso a imagem falhe).
            cs.saveGraphicsState();
            cs.beginText();

            // 1) "Assinado Digitalmente" em itálico
            cs.setFont(PDType1Font.HELVETICA_OBLIQUE, fontSize);
            cs.setNonStrokingColor(textColor);
            cs.newLineAtOffset(textX, textY);
            cs.showText("Assinado Digitalmente");
            cs.newLineAtOffset(0, -lineHeight);

            // 2) Usuário em negrito e cor navy
            cs.setFont(PDType1Font.HELVETICA_BOLD, fontSize);
            cs.setNonStrokingColor(new java.awt.Color(0, 0, 128)); // navy
            cs.showText(usuario != null ? usuario : "");

            // 3) Data + IP em regular
            cs.setFont(PDType1Font.HELVETICA, fontSize);
            cs.setNonStrokingColor(textColor);
            cs.newLineAtOffset(0, -lineHeight);
            cs.showText(dataFormatada + "-" + ip);

            // 4) Hash em itálico
            cs.newLineAtOffset(0, -lineHeight);
            cs.setFont(PDType1Font.HELVETICA_OBLIQUE, fontSize);
            cs.setNonStrokingColor(textColor);
            cs.showText(hash != null ? hash : "");

            cs.endText();
            cs.restoreGraphicsState();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();
        return out.toByteArray();
    }

    /**
     * Classe auxiliar para armazenar dados do termo de abertura
     */
    private static class DadosTermoAbertura {
        private String projetoNome;
        private String projetoCodTed;
        private String demandaCodigo;
        private String demandaNome;
        private String demandaDescricao;
        private String termoDescricao;
        private String dataAbertura;
        private String dataAberturaCompleta;
        private String usuarioDemandaNome;
        private String usuarioDemandaEmail;
        private String usuarioTermoNome;
        private String usuarioTermoEmail;
        private String dataAssinatura;

        // Getters e Setters
        public String getProjetoNome() { return projetoNome; }
        public void setProjetoNome(String projetoNome) { this.projetoNome = projetoNome; }
        public String getProjetoCodTed() { return projetoCodTed; }
        public void setProjetoCodTed(String projetoCodTed) { this.projetoCodTed = projetoCodTed; }
        public String getDemandaCodigo() { return demandaCodigo; }
        public void setDemandaCodigo(String demandaCodigo) { this.demandaCodigo = demandaCodigo; }
        public String getDemandaNome() { return demandaNome; }
        public void setDemandaNome(String demandaNome) { this.demandaNome = demandaNome; }
        public String getDemandaDescricao() { return demandaDescricao; }
        public void setDemandaDescricao(String demandaDescricao) { this.demandaDescricao = demandaDescricao; }
        public String getTermoDescricao() { return termoDescricao; }
        public void setTermoDescricao(String termoDescricao) { this.termoDescricao = termoDescricao; }
        public String getDataAbertura() { return dataAbertura; }
        public void setDataAbertura(String dataAbertura) { this.dataAbertura = dataAbertura; }
        public String getDataAberturaCompleta() { return dataAberturaCompleta; }
        public void setDataAberturaCompleta(String dataAberturaCompleta) { this.dataAberturaCompleta = dataAberturaCompleta; }
        public String getUsuarioDemandaNome() { return usuarioDemandaNome; }
        public void setUsuarioDemandaNome(String usuarioDemandaNome) { this.usuarioDemandaNome = usuarioDemandaNome; }
        public String getUsuarioDemandaEmail() { return usuarioDemandaEmail; }
        public void setUsuarioDemandaEmail(String usuarioDemandaEmail) { this.usuarioDemandaEmail = usuarioDemandaEmail; }
        public String getUsuarioTermoNome() { return usuarioTermoNome; }
        public void setUsuarioTermoNome(String usuarioTermoNome) { this.usuarioTermoNome = usuarioTermoNome; }
        public String getUsuarioTermoEmail() { return usuarioTermoEmail; }
        public void setUsuarioTermoEmail(String usuarioTermoEmail) { this.usuarioTermoEmail = usuarioTermoEmail; }
        public String getDataAssinatura() { return dataAssinatura; }
        public void setDataAssinatura(String dataAssinatura) { this.dataAssinatura = dataAssinatura; }
    }
    
    /**
     * Classe auxiliar para armazenar dados do termo de planejamento
     */
    private static class DadosTermoPlanejamento {
        private String projetoNome;
        private String projetoCodTed;
        private String demandaCodigo;
        private String demandaNome;
        private String demandaDescricao;
        private String especificacao;
        private String cronograma;
        private String resultadoEsperado;
        private String dataAbertura;
        private String dataAberturaCompleta;
        private String usuarioDemandaNome;
        private String usuarioDemandaEmail;
        private String usuarioTermoNome;
        private String usuarioTermoEmail;
        private String dataAssinatura;
        private String custosDetalhados;
        private String custoTotal;

        // Getters e Setters
        public String getProjetoNome() { return projetoNome; }
        public void setProjetoNome(String projetoNome) { this.projetoNome = projetoNome; }
        public String getProjetoCodTed() { return projetoCodTed; }
        public void setProjetoCodTed(String projetoCodTed) { this.projetoCodTed = projetoCodTed; }
        public String getDemandaCodigo() { return demandaCodigo; }
        public void setDemandaCodigo(String demandaCodigo) { this.demandaCodigo = demandaCodigo; }
        public String getDemandaNome() { return demandaNome; }
        public void setDemandaNome(String demandaNome) { this.demandaNome = demandaNome; }
        public String getDemandaDescricao() { return demandaDescricao; }
        public void setDemandaDescricao(String demandaDescricao) { this.demandaDescricao = demandaDescricao; }
        public String getEspecificacao() { return especificacao; }
        public void setEspecificacao(String especificacao) { this.especificacao = especificacao; }
        public String getCronograma() { return cronograma; }
        public void setCronograma(String cronograma) { this.cronograma = cronograma; }
        public String getResultadoEsperado() { return resultadoEsperado; }
        public void setResultadoEsperado(String resultadoEsperado) { this.resultadoEsperado = resultadoEsperado; }
        public String getDataAbertura() { return dataAbertura; }
        public void setDataAbertura(String dataAbertura) { this.dataAbertura = dataAbertura; }
        public String getDataAberturaCompleta() { return dataAberturaCompleta; }
        public void setDataAberturaCompleta(String dataAberturaCompleta) { this.dataAberturaCompleta = dataAberturaCompleta; }
        public String getUsuarioDemandaNome() { return usuarioDemandaNome; }
        public void setUsuarioDemandaNome(String usuarioDemandaNome) { this.usuarioDemandaNome = usuarioDemandaNome; }
        public String getUsuarioDemandaEmail() { return usuarioDemandaEmail; }
        public void setUsuarioDemandaEmail(String usuarioDemandaEmail) { this.usuarioDemandaEmail = usuarioDemandaEmail; }
        public String getUsuarioTermoNome() { return usuarioTermoNome; }
        public void setUsuarioTermoNome(String usuarioTermoNome) { this.usuarioTermoNome = usuarioTermoNome; }
        public String getUsuarioTermoEmail() { return usuarioTermoEmail; }
        public void setUsuarioTermoEmail(String usuarioTermoEmail) { this.usuarioTermoEmail = usuarioTermoEmail; }
        public String getDataAssinatura() { return dataAssinatura; }
        public void setDataAssinatura(String dataAssinatura) { this.dataAssinatura = dataAssinatura; }
        public String getCustosDetalhados() { return custosDetalhados; }
        public void setCustosDetalhados(String custosDetalhados) { this.custosDetalhados = custosDetalhados; }
        public String getCustoTotal() { return custoTotal; }
        public void setCustoTotal(String custoTotal) { this.custoTotal = custoTotal; }
    }
    
    /**
     * Classe auxiliar para armazenar dados do termo de encerramento
     */
    private static class DadosTermoEncerramento {
        private String projetoNome;
        private String projetoCodTed;
        private String demandaCodigo;
        private String demandaNome;
        private String demandaDescricao;
        private String resultadoEntregue;
        private String dataTermo;
        private String dataTermoCompleta;
        private String usuarioDemandaNome;
        private String usuarioDemandaEmail;
        private String usuarioTermoNome;
        private String usuarioTermoEmail;
        private String dataAssinatura;
        private String custosDetalhados;
        private String custoTotal;

        // Getters e Setters
        public String getProjetoNome() { return projetoNome; }
        public void setProjetoNome(String projetoNome) { this.projetoNome = projetoNome; }
        public String getProjetoCodTed() { return projetoCodTed; }
        public void setProjetoCodTed(String projetoCodTed) { this.projetoCodTed = projetoCodTed; }
        public String getDemandaCodigo() { return demandaCodigo; }
        public void setDemandaCodigo(String demandaCodigo) { this.demandaCodigo = demandaCodigo; }
        public String getDemandaNome() { return demandaNome; }
        public void setDemandaNome(String demandaNome) { this.demandaNome = demandaNome; }
        public String getDemandaDescricao() { return demandaDescricao; }
        public void setDemandaDescricao(String demandaDescricao) { this.demandaDescricao = demandaDescricao; }
        public String getResultadoEntregue() { return resultadoEntregue; }
        public void setResultadoEntregue(String resultadoEntregue) { this.resultadoEntregue = resultadoEntregue; }
        public String getDataTermo() { return dataTermo; }
        public void setDataTermo(String dataTermo) { this.dataTermo = dataTermo; }
        public String getDataTermoCompleta() { return dataTermoCompleta; }
        public void setDataTermoCompleta(String dataTermoCompleta) { this.dataTermoCompleta = dataTermoCompleta; }
        public String getUsuarioDemandaNome() { return usuarioDemandaNome; }
        public void setUsuarioDemandaNome(String usuarioDemandaNome) { this.usuarioDemandaNome = usuarioDemandaNome; }
        public String getUsuarioDemandaEmail() { return usuarioDemandaEmail; }
        public void setUsuarioDemandaEmail(String usuarioDemandaEmail) { this.usuarioDemandaEmail = usuarioDemandaEmail; }
        public String getUsuarioTermoNome() { return usuarioTermoNome; }
        public void setUsuarioTermoNome(String usuarioTermoNome) { this.usuarioTermoNome = usuarioTermoNome; }
        public String getUsuarioTermoEmail() { return usuarioTermoEmail; }
        public void setUsuarioTermoEmail(String usuarioTermoEmail) { this.usuarioTermoEmail = usuarioTermoEmail; }
        public String getDataAssinatura() { return dataAssinatura; }
        public void setDataAssinatura(String dataAssinatura) { this.dataAssinatura = dataAssinatura; }
        public String getCustosDetalhados() { return custosDetalhados; }
        public void setCustosDetalhados(String custosDetalhados) { this.custosDetalhados = custosDetalhados; }
        public String getCustoTotal() { return custoTotal; }
        public void setCustoTotal(String custoTotal) { this.custoTotal = custoTotal; }
    }
}
