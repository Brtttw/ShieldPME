package br.itb.projeto.shieldpme.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.itb.projeto.shieldpme.dto.AssinaturaDTO.AssinaturaRequest;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.AssinaturaResponse;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.Cliente;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.Pagamento;
import br.itb.projeto.shieldpme.model.entity.Assinatura;
import br.itb.projeto.shieldpme.model.entity.Plano;
import br.itb.projeto.shieldpme.model.repository.AssinaturaRepository;
import br.itb.projeto.shieldpme.model.repository.PlanoRepository;
import br.itb.projeto.shieldpme.model.repository.UsuarioRepository;

@Service
public class AssinaturaService {

    private final AssinaturaRepository assinaturaRepository;
    private final PlanoRepository planoRepository;
    private final UsuarioRepository usuarioRepository;

    public AssinaturaService(AssinaturaRepository assinaturaRepository, PlanoRepository planoRepository,
            UsuarioRepository usuarioRepository) {
        this.assinaturaRepository = assinaturaRepository;
        this.planoRepository = planoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Cria a assinatura. Pagamento SIMULADO: não há gateway, então a assinatura já nasce ATIVA.
     * Dados de cartão (pagamento.dados) são ignorados de propósito e nunca gravados.
     *
     * @param emailDoUsuarioLogado email do token JWT, ou null se o cliente não estiver logado
     */
    @Transactional
    public AssinaturaResponse criar(AssinaturaRequest req, String emailDoUsuarioLogado) {
        Plano plano = planoRepository.findByCodigoAndAtivoTrue(req.planoCodigo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado."));

        Cliente cliente = req.cliente();
        Pagamento pagamento = req.pagamento();

        Assinatura assinatura = new Assinatura();
        assinatura.setPlano(plano);
        if (emailDoUsuarioLogado != null) {
            usuarioRepository.findByEmail(emailDoUsuarioLogado).ifPresent(assinatura::setUsuario);
        }
        assinatura.setNome(cliente.nome().trim());
        assinatura.setEmail(cliente.email().trim());
        assinatura.setTelefone(cliente.telefone().trim());
        assinatura.setCpf(cliente.cpf().trim());
        assinatura.setEmpresa(cliente.empresa().trim());

        // O preço SEMPRE vem do banco, nunca do front.
        assinatura.setValorMensal(plano.getPrecoMensal());

        assinatura.setMetodoPagamento(pagamento.metodo());
        assinatura.setParcelas("credito".equals(pagamento.metodo()) ? pagamento.parcelas() : 1);

        Assinatura salva = assinaturaRepository.save(assinatura);
        return new AssinaturaResponse(salva.getId(), plano.getCodigo(), salva.getValorMensal(), salva.getStatusAssinatura());
    }
}
