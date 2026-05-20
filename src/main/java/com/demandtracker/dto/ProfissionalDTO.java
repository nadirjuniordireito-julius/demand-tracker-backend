package com.demandtracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.demandtracker.entity.Profissional;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalDTO {

    private Long id;
    private String nome;
    private String tipoPessoa;
    private String documento;
    private BigDecimal valorHora;
    private BigDecimal custoTotalMensal;
    private LocalDate dataInicioAtividade;
    private String funcao;
    private Long projetoId;
    private ProjetoDTO projeto;
    private Long perfilId;
    private String perfilNome;

    public static ProfissionalDTO fromEntity(Profissional profissional) {
        ProfissionalDTO dto = new ProfissionalDTO();
        dto.setId(profissional.getId());
        dto.setTipoPessoa(profissional.getTipoPessoa());
        dto.setDocumento(profissional.getDocumento());
        dto.setNome(profissional.getNome());
        dto.setValorHora(profissional.getValorHora());
        dto.setCustoTotalMensal(profissional.getCustoTotalMensal());
        dto.setDataInicioAtividade(profissional.getDataInicioAtividade());
        dto.setFuncao(profissional.getFuncao());
        if (profissional.getProjeto() != null) {
            dto.setProjetoId(profissional.getProjeto().getId());
            dto.setProjeto(ProjetoDTO.fromEntity(profissional.getProjeto()));
        }
        if (profissional.getPerfil() != null) {
            dto.setPerfilId(profissional.getPerfil().getId());
            dto.setPerfilNome(profissional.getPerfil().getNome());
        }
        return dto;
    }
}
