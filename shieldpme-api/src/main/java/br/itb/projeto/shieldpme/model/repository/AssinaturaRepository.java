package br.itb.projeto.shieldpme.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.itb.projeto.shieldpme.model.entity.Assinatura;

@Repository
public interface AssinaturaRepository extends JpaRepository<Assinatura, Long> {
}
