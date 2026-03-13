package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para criação de anexo do Termo de Encerramento.
 */
@Data
public class TermoEncerramentoAnexoCreateDTO {

    @NotNull(message = "ID do Termo de Encerramento é obrigatório")
    private Long termoEncerramentoId;

    @NotNull(message = "Arquivo é obrigatório")
    private MultipartFile arquivo;

    /**
     * ID do usuário que está fazendo o upload (opcional).
     */
    private Long usuarioId;
}
