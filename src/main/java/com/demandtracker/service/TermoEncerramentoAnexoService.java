package com.demandtracker.service;

import com.demandtracker.dto.TermoEncerramentoAnexoCreateDTO;
import com.demandtracker.dto.TermoEncerramentoAnexoResponseDTO;
import com.demandtracker.entity.TermoEncerramento;
import com.demandtracker.entity.TermoEncerramentoAnexo;
import com.demandtracker.entity.Usuario;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.TermoEncerramentoAnexoRepository;
import com.demandtracker.repository.TermoEncerramentoRepository;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço para anexos do Termo de Encerramento.
 * Inclusão e exclusão só são permitidas quando o status da demanda técnica for "E" (Planejado e assinado) ou "F" (Em encerramento).
 */
@Service
@RequiredArgsConstructor
public class TermoEncerramentoAnexoService {

    private static final Set<String> STATUS_PERMITIDOS = Set.of("E", "F");

    private final TermoEncerramentoAnexoRepository repository;
    private final TermoEncerramentoRepository termoEncerramentoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Verifica se a demanda técnica do termo de encerramento está com status "E" ou "F".
     */
    private void validarStatusPermitidoParaAlteracao(Long termoEncerramentoId) {
        TermoEncerramento termo = termoEncerramentoRepository.findById(termoEncerramentoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Termo de Encerramento não encontrado com ID: " + termoEncerramentoId));
        String status = termo.getDemandaTecnica() != null ? termo.getDemandaTecnica().getStatus() : null;
        if (status == null || !STATUS_PERMITIDOS.contains(status)) {
            throw new BadRequestException(
                    "Inclusão e exclusão de anexos só são permitidas quando o status da demanda técnica for 'E' (Planejado e assinado) ou 'F' (Em encerramento). Status atual: " + status);
        }
    }

    @Transactional
    public TermoEncerramentoAnexoResponseDTO create(TermoEncerramentoAnexoCreateDTO dto) throws IOException {
        validarStatusPermitidoParaAlteracao(dto.getTermoEncerramentoId());

        TermoEncerramento termo = termoEncerramentoRepository.findById(dto.getTermoEncerramentoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Termo de Encerramento não encontrado com ID: " + dto.getTermoEncerramentoId()));

        MultipartFile arquivo = dto.getArquivo();
        if (arquivo == null || arquivo.isEmpty()) {
            throw new BadRequestException("Arquivo é obrigatório");
        }

        String nomeOriginal = arquivo.getOriginalFilename();
        if (nomeOriginal != null && repository.existsByNomeArquivo(nomeOriginal)) {
            throw new BadRequestException("Já existe um anexo com o nome de arquivo: " + nomeOriginal);
        }

        TermoEncerramentoAnexo anexo = new TermoEncerramentoAnexo();
        anexo.setTermoEncerramento(termo);
        anexo.setArquivo(arquivo.getBytes());
        anexo.setNomeArquivo(nomeOriginal);
        anexo.setTipoConteudo(arquivo.getContentType());
        anexo.setTamanhoArquivo(arquivo.getSize());

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                    .orElse(null);
            anexo.setUsuario(usuario);
        }

        TermoEncerramentoAnexo saved = repository.save(anexo);
        return TermoEncerramentoAnexoResponseDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public TermoEncerramentoAnexoResponseDTO findById(Long id) {
        TermoEncerramentoAnexo anexo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo não encontrado com ID: " + id));
        return TermoEncerramentoAnexoResponseDTO.fromEntity(anexo);
    }

    @Transactional(readOnly = true)
    public List<TermoEncerramentoAnexoResponseDTO> findByTermoEncerramentoId(Long termoEncerramentoId) {
        return repository.findByTermoEncerramentoIdOrderByIdAsc(termoEncerramentoId).stream()
                .map(TermoEncerramentoAnexoResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public byte[] getArquivo(Long id) {
        TermoEncerramentoAnexo anexo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo não encontrado com ID: " + id));
        return anexo.getArquivo();
    }

    @Transactional
    public void delete(Long id) {
        TermoEncerramentoAnexo anexo = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo não encontrado com ID: " + id));
        validarStatusPermitidoParaAlteracao(anexo.getTermoEncerramento().getId());
        repository.deleteById(id);
    }
}
