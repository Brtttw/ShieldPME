package br.itb.projeto.shieldpme.model.entity;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Postagem do blog. Somente leitura.
 */
@Entity
@Table(name = "Postagem")
public class Postagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String resumo;

    private LocalDate publicadoEm;

    private String imagemArquivo;

    private String textoAlternativo;

    private boolean ativo;

    /** Exigido pelo JPA. */
    protected Postagem() {
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getResumo() {
        return resumo;
    }

    public LocalDate getPublicadoEm() {
        return publicadoEm;
    }

    public String getImagemArquivo() {
        return imagemArquivo;
    }

    public String getTextoAlternativo() {
        return textoAlternativo;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
