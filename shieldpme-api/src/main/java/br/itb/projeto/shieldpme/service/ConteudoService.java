package br.itb.projeto.shieldpme.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.itb.projeto.shieldpme.dto.ConteudoDTO.FerramentaDTO;
import br.itb.projeto.shieldpme.dto.ConteudoDTO.PlanoDTO;
import br.itb.projeto.shieldpme.dto.ConteudoDTO.PostDTO;
import br.itb.projeto.shieldpme.dto.ConteudoDTO.ServicoDTO;
import br.itb.projeto.shieldpme.model.entity.Ferramenta;
import br.itb.projeto.shieldpme.model.entity.FerramentaRecurso;
import br.itb.projeto.shieldpme.model.entity.Plano;
import br.itb.projeto.shieldpme.model.entity.PlanoBeneficio;
import br.itb.projeto.shieldpme.model.entity.Postagem;
import br.itb.projeto.shieldpme.model.entity.Servico;
import br.itb.projeto.shieldpme.model.repository.FerramentaRepository;
import br.itb.projeto.shieldpme.model.repository.PlanoRepository;
import br.itb.projeto.shieldpme.model.repository.PostagemRepository;
import br.itb.projeto.shieldpme.model.repository.ServicoRepository;

/**
 * Conteúdo público do site (planos, serviços, blog, ferramentas). Só leitura.
 * O @Transactional é necessário porque as listas (benefícios/recursos) são carregadas sob demanda.
 */
@Service
@Transactional(readOnly = true)
public class ConteudoService {

    private final PlanoRepository planoRepository;
    private final ServicoRepository servicoRepository;
    private final PostagemRepository postagemRepository;
    private final FerramentaRepository ferramentaRepository;

    public ConteudoService(PlanoRepository planoRepository, ServicoRepository servicoRepository,
            PostagemRepository postagemRepository, FerramentaRepository ferramentaRepository) {
        this.planoRepository = planoRepository;
        this.servicoRepository = servicoRepository;
        this.postagemRepository = postagemRepository;
        this.ferramentaRepository = ferramentaRepository;
    }

    public List<PlanoDTO> listarPlanos() {
        return planoRepository.findByAtivoTrueOrderByOrdemAsc().stream().map(this::paraPlano).toList();
    }

    public PlanoDTO buscarPlano(String codigo) {
        return planoRepository.findByCodigoAndAtivoTrue(codigo)
                .map(this::paraPlano)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado."));
    }

    public List<ServicoDTO> listarServicos() {
        return servicoRepository.findByAtivoTrueOrderByOrdemAsc().stream().map(this::paraServico).toList();
    }

    public List<PostDTO> listarPosts() {
        return postagemRepository.findByAtivoTrueOrderByPublicadoEmDesc().stream().map(this::paraPost).toList();
    }

    public List<FerramentaDTO> listarFerramentas() {
        return ferramentaRepository.findByAtivoTrueOrderByOrdemAsc().stream().map(this::paraFerramenta).toList();
    }

    /* ================= entidade -> JSON do front ================= */

    private PlanoDTO paraPlano(Plano p) {
        List<String> beneficios = p.getBeneficios().stream().map(PlanoBeneficio::getDescricao).toList();
        return new PlanoDTO(p.getId(), p.getCodigo(), p.getNome(), p.getDescricao(),
                p.getPrecoMensal(), p.getPrecoAnual(), p.isDestaque(), beneficios);
    }

    private ServicoDTO paraServico(Servico s) {
        return new ServicoDTO(s.getId(), s.getTitulo(), s.getDescricao(), s.getIconeArquivo(), s.getTextoAlternativo());
    }

    private PostDTO paraPost(Postagem p) {
        // LocalDate.toString() = yyyy-MM-dd, exatamente o que o front espera
        return new PostDTO(p.getId(), p.getTitulo(), p.getResumo(), p.getPublicadoEm().toString(),
                p.getImagemArquivo(), p.getTextoAlternativo());
    }

    private FerramentaDTO paraFerramenta(Ferramenta f) {
        List<String> recursos = f.getRecursos().stream().map(FerramentaRecurso::getDescricao).toList();
        return new FerramentaDTO(f.getId(), f.getNome(), f.getDescricao(), recursos);
    }
}
