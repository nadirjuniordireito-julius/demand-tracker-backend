package com.demandtracker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * DTO para criação de DemandaAvaliacaoDoc
 */
@Data
public class DemandaAvaliacaoDocCreateDTO {

    @NotNull(message = "ID da Avaliação é obrigatório")
    private Long avaliacaoId;

    private LocalDateTime dataUpload;

    @NotNull(message = "Arquivo PDF é obrigatório")
    private MultipartFile arquivoPdf;
}
