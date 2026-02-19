package com.demandtracker.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

/**
 * DTO para atualização de DemandaAvaliacaoDoc
 */
@Data
public class DemandaAvaliacaoDocUpdateDTO {

    private LocalDateTime dataUpload;

    private MultipartFile arquivoPdf;
}
