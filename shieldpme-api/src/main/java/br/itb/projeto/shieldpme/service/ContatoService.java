package br.itb.projeto.shieldpme.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.itb.projeto.shieldpme.dto.FormularioDTO.ContatoRequest;
import br.itb.projeto.shieldpme.model.entity.ContatoMensagem;
import br.itb.projeto.shieldpme.model.repository.ContatoMensagemRepository;

@Service
public class ContatoService {

    private final ContatoMensagemRepository contatoMensagemRepository;

    public ContatoService(ContatoMensagemRepository contatoMensagemRepository) {
        this.contatoMensagemRepository = contatoMensagemRepository;
    }

    @Transactional
    public void registrar(ContatoRequest req) {
        ContatoMensagem contato = new ContatoMensagem();
        contato.setNome(req.nome().trim());
        contato.setEmail(req.email().trim());
        contato.setAssunto(req.assunto().trim());
        contato.setMensagem(req.mensagem().trim());
        contatoMensagemRepository.save(contato);
    }
}
