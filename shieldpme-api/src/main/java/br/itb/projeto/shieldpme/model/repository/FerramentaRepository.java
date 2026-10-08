package br.itb.projeto.shieldpme.model.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.itb.projeto.shieldpme.model.entity.Ferramenta;

@Repository
public interface FerramentaRepository extends JpaRepository<Ferramenta, Long> {

    List<Ferramenta> findByAtivoTrueOrderByOrdemAsc();
}
