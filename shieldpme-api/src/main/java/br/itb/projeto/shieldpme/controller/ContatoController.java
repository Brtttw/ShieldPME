package br.itb.projeto.shieldpme.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.itb.projeto.shieldpme.dto.FormularioDTO.ContatoRequest;
import br.itb.projeto.shieldpme.dto.FormularioDTO.ContatoResposta;
import br.itb.projeto.shieldpme.service.ContatoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contatos")
public class ContatoController {

    private final ContatoService contatoService;

    public ContatoController(ContatoService contatoService) {
        this.contatoService = contatoService;
    }

    // Formulário da página Contato (público). O front espera 204, sem corpo.
    @PostMapping
    public ResponseEntity<Void> enviar(@Valid @RequestBody ContatoRequest req) {
        contatoService.registrar(req);
        return ResponseEntity.noContent().build();
    }

    // Leitura das mensagens recebidas: somente ADMIN (regra em SecurityConfig).
    @GetMapping
    public List<ContatoResposta> listar() {
        return contatoService.listar();
    }
}
