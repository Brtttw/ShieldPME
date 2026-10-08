package br.itb.projeto.shieldpme.model.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.itb.projeto.shieldpme.model.entity.Plano;

@Repository
public interface PlanoRepository extends JpaRepository<Plano, Long> {

    List<Plano> findByAtivoTrueOrderByOrdemAsc();

    Optional<Plano> findByCodigoAndAtivoTrue(String codigo);
}
