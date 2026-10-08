package br.itb.projeto.shieldpme.service;

import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.itb.projeto.shieldpme.dto.FormularioDTO.NewsletterRequest;
import br.itb.projeto.shieldpme.model.entity.NewsletterInscricao;
import br.itb.projeto.shieldpme.model.repository.NewsletterInscricaoRepository;

@Service
public class NewsletterService {

    private final NewsletterInscricaoRepository newsletterInscricaoRepository;

    public NewsletterService(NewsletterInscricaoRepository newsletterInscricaoRepository) {
        this.newsletterInscricaoRepository = newsletterInscricaoRepository;
    }

    @Transactional
    public void inscrever(NewsletterRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);

        // Email já inscrito não é erro: o resultado para o visitante é o mesmo ("cadastro realizado")
        if (newsletterInscricaoRepository.existsByEmail(email)) {
            return;
        }

        NewsletterInscricao inscricao = new NewsletterInscricao();
        inscricao.setEmail(email);
        inscricao.setDataNascimento(req.dataNascimento());
        newsletterInscricaoRepository.save(inscricao);
    }
}
