package br.itb.projeto.shieldpme.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Serviço exibido na home. Somente leitura.
 */
@Entity
@Table(name = "Servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;

    private String descricao;

    private String iconeArquivo;

    private String textoAlternativo;

    private boolean ativo;

    private int ordem;

    /** Exigido pelo JPA. */
    protected Servico() {
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getIconeArquivo() {
        return iconeArquivo;
    }

    public String getTextoAlternativo() {
        return textoAlternativo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getOrdem() {
        return ordem;
    }
}
