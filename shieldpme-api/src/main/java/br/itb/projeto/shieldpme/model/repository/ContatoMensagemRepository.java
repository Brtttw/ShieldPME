package br.itb.projeto.shieldpme.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.itb.projeto.shieldpme.model.entity.ContatoMensagem;

@Repository
public interface ContatoMensagemRepository extends JpaRepository<ContatoMensagem, Long> {
}
