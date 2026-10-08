package br.itb.projeto.shieldpme.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.itb.projeto.shieldpme.dto.FormularioDTO.ContatoRequest;
import br.itb.projeto.shieldpme.dto.FormularioDTO.ContatoResposta;
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

    /** Usado só pelo ADMIN (GET /api/contatos). */
    @Transactional(readOnly = true)
    public List<ContatoResposta> listar() {
        return contatoMensagemRepository.findAllByOrderByCriadoEmDesc().stream()
                .map(c -> new ContatoResposta(c.getId(), c.getNome(), c.getEmail(), c.getAssunto(),
                        c.getMensagem(), c.getCriadoEm().toString()))
                .toList();
    }
}
