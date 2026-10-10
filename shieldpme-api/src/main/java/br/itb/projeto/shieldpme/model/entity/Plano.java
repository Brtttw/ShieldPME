package br.itb.projeto.shieldpme.model.entity;

import java.math.BigDecimal;
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
@Table(name = "Plano")
public class Plano {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;

    private String nome;

    private String descricao;

    private BigDecimal precoMensal;

    private BigDecimal precoAnual;

    private boolean destaque;

    private boolean ativo;

    private int ordem;

    @OneToMany(mappedBy = "plano")
    @OrderBy("ordem ASC")
    private List<PlanoBeneficio> beneficios = new ArrayList<>();

    protected Plano() {
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPrecoMensal() {
        return precoMensal;
    }

    public BigDecimal getPrecoAnual() {
        return precoAnual;
    }

    public boolean isDestaque() {
        return destaque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getOrdem() {
        return ordem;
    }

    public List<PlanoBeneficio> getBeneficios() {
        return beneficios;
    }
}
