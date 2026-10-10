package br.itb.projeto.shieldpme.model.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "Ferramenta")
public class Ferramenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String descricao;

    private boolean ativo;

    private int ordem;

    @OneToMany(mappedBy = "ferramenta")
    @OrderBy("ordem ASC")
    private List<FerramentaRecurso> recursos = new ArrayList<>();

    protected Ferramenta() {
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getOrdem() {
        return ordem;
    }

    public List<FerramentaRecurso> getRecursos() {
        return recursos;
    }
}
