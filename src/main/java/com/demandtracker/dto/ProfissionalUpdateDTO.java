package com.demandtracker.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalUpdateDTO {

    @Size(max = 500, message = "Nome deve ter no máximo 500 caracteres")
    private String nome;

    private LocalDate dataInicioAtividade;

    private BigDecimal valorHora;

    @Size(max = 1, message = "Tipo de pessoa deve ter no máximo 1 caractere")
    private String tipoPessoa;

    @Size(max = 100, message = "Documento deve ter no máximo 100 caracteres")
    private String documento;

    @Size(max = 255, message = "Função deve ter no máximo 255 caracteres")
    private String funcao;

    private Long projetoId;

    private Long perfilId;
}
