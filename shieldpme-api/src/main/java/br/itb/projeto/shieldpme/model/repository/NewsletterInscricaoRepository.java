package br.itb.projeto.shieldpme.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.itb.projeto.shieldpme.model.entity.NewsletterInscricao;

@Repository
public interface NewsletterInscricaoRepository extends JpaRepository<NewsletterInscricao, Long> {

    boolean existsByEmail(String email);
}
