package br.itb.projeto.shieldpme.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.itb.projeto.shieldpme.dto.ConteudoDTO.FerramentaDTO;
import br.itb.projeto.shieldpme.dto.ConteudoDTO.PlanoDTO;
import br.itb.projeto.shieldpme.dto.ConteudoDTO.PostDTO;
import br.itb.projeto.shieldpme.dto.ConteudoDTO.ServicoDTO;
import br.itb.projeto.shieldpme.service.ConteudoService;

/**
 * Conteúdo público do site, somente leitura: planos, serviços, blog e ferramentas.
 */
@RestController
@RequestMapping("/api")
public class ConteudoController {

    private final ConteudoService conteudoService;

    public ConteudoController(ConteudoService conteudoService) {
        this.conteudoService = conteudoService;
    }

    @GetMapping("/planos")
    public List<PlanoDTO> planos() {
        return conteudoService.listarPlanos();
    }

    @GetMapping("/planos/{codigo}")
    public PlanoDTO plano(@PathVariable String codigo) {
        return conteudoService.buscarPlano(codigo);
    }

    @GetMapping("/servicos")
    public List<ServicoDTO> servicos() {
        return conteudoService.listarServicos();
    }

    @GetMapping("/blog/posts")
    public List<PostDTO> posts() {
        return conteudoService.listarPosts();
    }

    @GetMapping("/ferramentas")
    public List<FerramentaDTO> ferramentas() {
        return conteudoService.listarFerramentas();
    }
}
