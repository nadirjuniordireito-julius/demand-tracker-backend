package com.demandtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfilCreateDTO {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;
    
    @NotNull(message = "Data inicial é obrigatória")
    private LocalDate termoInicial;
    
    @NotNull(message = "Data final é obrigatória")
    private LocalDate termoFinal;
    
    @NotNull(message = "ID do usuário é obrigatório")
    private Long usuarioId;
    
    @NotNull(message = "ID do projeto é obrigatório")
    private Long projetoId;

    @NotNull(message = "Valor é obrigatório")
    private BigDecimal valor;   

}

