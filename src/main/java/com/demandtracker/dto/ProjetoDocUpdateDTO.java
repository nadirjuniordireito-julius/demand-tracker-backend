package com.demandtracker.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO para atualização de ProjetoDoc
 */
@Data
public class ProjetoDocUpdateDTO {
    
    private String nome;
    
    private MultipartFile documento;
}
