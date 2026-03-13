package com.demandtracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalCreateDTO {
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 500, message = "Nome deve ter no máximo 500 caracteres")
    private String nome;
    
    @NotNull(message = "Data inicial é obrigatória")
    private LocalDate dataInicioAtividade;
    
    @NotNull(message = "ID do projeto é obrigatório")
    private Long projetoId;

    @NotNull(message = "Valor hora é obrigatório")
    private BigDecimal valorHora;   

    @NotBlank(message = "Tipo de pessoa é obrigatório")
    @Size(max = 1, message = "Tipo de pessoa deve ter no máximo 1 caractere")
    private String tipoPessoa;

    @NotBlank(message = "Documento é obrigatório")
    @Size(max = 100, message = "Documento deve ter no máximo 100 caracteres")
    private String documento;

    @Size(max = 255, message = "Funcao deve ter no maximo 255 caracteres")
    @JsonProperty("funcao")
    private String funcaoProfissional;

    @NotNull(message = "ID do perfil é obrigatório")
    private Long perfilId;
}

