package br.itb.projeto.shieldpme.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "FerramentaRecurso")
public class FerramentaRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ferramentaId")
    private Ferramenta ferramenta;

    private String descricao;

    private int ordem;

    protected FerramentaRecurso() {
    }

    public Long getId() {
        return id;
    }

    public Ferramenta getFerramenta() {
        return ferramenta;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getOrdem() {
        return ordem;
    }
}
