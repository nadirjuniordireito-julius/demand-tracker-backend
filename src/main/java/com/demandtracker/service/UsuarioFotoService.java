package com.demandtracker.service;

import com.demandtracker.dto.UsuarioFotoCreateDTO;
import com.demandtracker.dto.UsuarioFotoResponseDTO;
import com.demandtracker.dto.UsuarioFotoUpdateDTO;
import com.demandtracker.entity.Usuario;
import com.demandtracker.entity.UsuarioFoto;
import com.demandtracker.exception.BadRequestException;
import com.demandtracker.exception.ResourceNotFoundException;
import com.demandtracker.repository.UsuarioFotoRepository;
import com.demandtracker.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioFotoService {
    
    private final UsuarioFotoRepository usuarioFotoRepository;
    private final UsuarioRepository usuarioRepository;
    
    /**
     * Cria uma nova foto para um usuário
     */
    @Transactional
    public UsuarioFotoResponseDTO create(UsuarioFotoCreateDTO dto) throws IOException {
        // Valida se o usuário existe
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
        
        // Valida se já existe foto para este usuário
        if (usuarioFotoRepository.existsByUsuarioId(dto.getUsuarioId())) {
            throw new BadRequestException("Já existe uma foto para este usuário");
        }
        
        // Valida se o arquivo é uma imagem
        MultipartFile foto = dto.getFoto();
        if (foto == null || foto.isEmpty()) {
            throw new BadRequestException("Foto é obrigatória");
        }
        
        String contentType = foto.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("O arquivo deve ser uma imagem");
        }
        
        // Cria a entidade
        UsuarioFoto usuarioFoto = new UsuarioFoto();
        usuarioFoto.setUsuario(usuario);
        usuarioFoto.setFoto(foto.getBytes());
        
        // Salva a foto
        UsuarioFoto saved = usuarioFotoRepository.save(usuarioFoto);
        
        return toResponseDTO(saved);
    }
    
    /**
     * Busca foto por ID
     */
    @Transactional(readOnly = true)
    public UsuarioFotoResponseDTO findById(Long id) {
        UsuarioFoto usuarioFoto = usuarioFotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada com ID: " + id));
        return toResponseDTO(usuarioFoto);
    }
    
    /**
     * Busca foto pelo ID do usuário
     */
    @Transactional(readOnly = true)
    public UsuarioFotoResponseDTO findByUsuarioId(Long usuarioId) {
        UsuarioFoto usuarioFoto = usuarioFotoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada para o usuário com ID: " + usuarioId));
        return toResponseDTO(usuarioFoto);
    }
    
    /**
     * Lista todas as fotos
     */
    @Transactional(readOnly = true)
    public List<UsuarioFotoResponseDTO> findAll() {
        return usuarioFotoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Atualiza uma foto existente
     */
    @Transactional
    public UsuarioFotoResponseDTO update(Long id, UsuarioFotoUpdateDTO dto) throws IOException {
        UsuarioFoto usuarioFoto = usuarioFotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada com ID: " + id));
        
        if (dto.getFoto() != null && !dto.getFoto().isEmpty()) {
            String contentType = dto.getFoto().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("O arquivo deve ser uma imagem");
            }
            usuarioFoto.setFoto(dto.getFoto().getBytes());
        }
        
        UsuarioFoto saved = usuarioFotoRepository.save(usuarioFoto);
        return toResponseDTO(saved);
    }
    
    /**
     * Atualiza foto pelo ID do usuário (cria se não existir)
     */
    @Transactional
    public UsuarioFotoResponseDTO updateByUsuarioId(Long usuarioId, UsuarioFotoUpdateDTO dto) throws IOException {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + usuarioId));
        
        UsuarioFoto usuarioFoto = usuarioFotoRepository.findByUsuarioId(usuarioId).orElse(null);
        
        if (usuarioFoto == null) {
            // Cria nova foto se não existir
            if (dto.getFoto() == null || dto.getFoto().isEmpty()) {
                throw new BadRequestException("Foto é obrigatória");
            }
            
            String contentType = dto.getFoto().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("O arquivo deve ser uma imagem");
            }
            
            usuarioFoto = new UsuarioFoto();
            usuarioFoto.setUsuario(usuario);
            usuarioFoto.setFoto(dto.getFoto().getBytes());
        } else {
            // Atualiza foto existente
            if (dto.getFoto() != null && !dto.getFoto().isEmpty()) {
                String contentType = dto.getFoto().getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new BadRequestException("O arquivo deve ser uma imagem");
                }
                usuarioFoto.setFoto(dto.getFoto().getBytes());
            }
        }
        
        UsuarioFoto saved = usuarioFotoRepository.save(usuarioFoto);
        return toResponseDTO(saved);
    }
    
    /**
     * Deleta uma foto por ID
     */
    @Transactional
    public void delete(Long id) {
        if (!usuarioFotoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Foto não encontrada com ID: " + id);
        }
        usuarioFotoRepository.deleteById(id);
    }
    
    /**
     * Deleta foto pelo ID do usuário
     */
    @Transactional
    public void deleteByUsuarioId(Long usuarioId) {
        UsuarioFoto usuarioFoto = usuarioFotoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada para o usuário com ID: " + usuarioId));
        usuarioFotoRepository.deleteById(usuarioFoto.getId());
    }
    
    /**
     * Retorna o arquivo da foto por ID
     */
    @Transactional(readOnly = true)
    public byte[] getFoto(Long id) {
        UsuarioFoto usuarioFoto = usuarioFotoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada com ID: " + id));
        return usuarioFoto.getFoto();
    }
    
    /**
     * Retorna o arquivo da foto pelo ID do usuário
     */
    @Transactional(readOnly = true)
    public byte[] getFotoByUsuarioId(Long usuarioId) {
        UsuarioFoto usuarioFoto = usuarioFotoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Foto não encontrada para o usuário com ID: " + usuarioId));
        return usuarioFoto.getFoto();
    }
    
    /**
     * Verifica se existe foto para um usuário
     */
    @Transactional(readOnly = true)
    public boolean existsByUsuarioId(Long usuarioId) {
        return usuarioFotoRepository.existsByUsuarioId(usuarioId);
    }
    
    /**
     * Converte entidade para DTO de resposta
     */
    private UsuarioFotoResponseDTO toResponseDTO(UsuarioFoto usuarioFoto) {
        UsuarioFotoResponseDTO dto = new UsuarioFotoResponseDTO();
        dto.setId(usuarioFoto.getId());
        dto.setUsuarioId(usuarioFoto.getUsuario().getId());
        dto.setTamanhoFoto(usuarioFoto.getFoto() != null ? (long) usuarioFoto.getFoto().length : 0L);
        return dto;
    }
}
