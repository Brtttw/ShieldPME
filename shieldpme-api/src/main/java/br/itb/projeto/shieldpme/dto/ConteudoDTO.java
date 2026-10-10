package br.itb.projeto.shieldpme.dto;

import java.math.BigDecimal;
import java.util.List;

public final class ConteudoDTO {

    private ConteudoDTO() {
    }

    public record PlanoDTO(Long id, String codigo, String nome, String descricao,
            BigDecimal precoMensal, BigDecimal precoAnual, boolean destaque, List<String> beneficios) {
    }

    public record ServicoDTO(Long id, String titulo, String descricao, String icone, String textoAlternativo) {
    }

    public record PostDTO(Long id, String titulo, String resumo, String data, String imagem, String textoAlternativo) {
    }

    public record FerramentaDTO(Long id, String nome, String descricao, List<String> recursos) {
    }
}
