package br.itb.projeto.shieldpme.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Conteúdo público do site (GET). Os nomes dos campos seguem o que o front lê em src/data.
 */
public final class ConteudoDTO {

    private ConteudoDTO() {
    }

    /** GET /api/planos e /api/planos/{codigo} */
    public record PlanoDTO(Long id, String codigo, String nome, String descricao,
            BigDecimal precoMensal, BigDecimal precoAnual, boolean destaque, List<String> beneficios) {
    }

    /** GET /api/servicos ("icone" = nome do arquivo em /imagens) */
    public record ServicoDTO(Long id, String titulo, String descricao, String icone, String textoAlternativo) {
    }

    /** GET /api/blog/posts ("data" = yyyy-MM-dd; "imagem" = nome do arquivo em /imagens) */
    public record PostDTO(Long id, String titulo, String resumo, String data, String imagem, String textoAlternativo) {
    }

    /** GET /api/ferramentas */
    public record FerramentaDTO(Long id, String nome, String descricao, List<String> recursos) {
    }
}
