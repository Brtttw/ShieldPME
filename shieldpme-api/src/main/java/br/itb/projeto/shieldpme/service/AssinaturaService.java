package br.itb.projeto.shieldpme.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.itb.projeto.shieldpme.dto.AssinaturaDTO.AssinaturaRequest;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.AssinaturaResponse;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.Cliente;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.Pagamento;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.PixResponse;
import br.itb.projeto.shieldpme.model.entity.Assinatura;
import br.itb.projeto.shieldpme.model.entity.Plano;
import br.itb.projeto.shieldpme.model.repository.AssinaturaRepository;
import br.itb.projeto.shieldpme.model.repository.PlanoRepository;

@Service
public class AssinaturaService {

    // chave fictícia, o pix é só simulação
    private static final String CHAVE_PIX = "shieldpme@pagamento.com";

    private final AssinaturaRepository assinaturaRepository;
    private final PlanoRepository planoRepository;

    public AssinaturaService(AssinaturaRepository assinaturaRepository, PlanoRepository planoRepository) {
        this.assinaturaRepository = assinaturaRepository;
        this.planoRepository = planoRepository;
    }

    @Transactional
    public AssinaturaResponse criar(AssinaturaRequest req) {
        Plano plano = buscarPlano(req.planoCodigo());
        Cliente cliente = req.cliente();
        Pagamento pagamento = req.pagamento();

        Assinatura assinatura = new Assinatura();
        assinatura.setPlano(plano);
        assinatura.setNome(cliente.nome().trim());
        assinatura.setEmail(cliente.email().trim());
        assinatura.setTelefone(cliente.telefone().trim());
        assinatura.setCpf(cliente.cpf().trim());
        assinatura.setEmpresa(cliente.empresa().trim());
        assinatura.setValorMensal(plano.getPrecoMensal()); // o preço vem sempre do banco
        assinatura.setMetodoPagamento(pagamento.metodo());
        assinatura.setParcelas("credito".equals(pagamento.metodo()) ? pagamento.parcelas() : 1);

        Assinatura salva = assinaturaRepository.save(assinatura);
        return new AssinaturaResponse(salva.getId(), plano.getCodigo(), salva.getValorMensal(), salva.getStatusAssinatura());
    }

    @Transactional(readOnly = true)
    public PixResponse gerarPix(String planoCodigo) {
        String codigo = brCode(buscarPlano(planoCodigo).getPrecoMensal());
        return new PixResponse(codigo, codigo);
    }

    private Plano buscarPlano(String codigo) {
        return planoRepository.findByCodigoAndAtivoTrue(codigo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano não encontrado."));
    }

    private static String brCode(BigDecimal valor) {
        String semCrc = campo("00", "01")
                + campo("26", campo("00", "BR.GOV.BCB.PIX") + campo("01", CHAVE_PIX))
                + campo("52", "0000")
                + campo("53", "986")
                + campo("54", valor.setScale(2, RoundingMode.HALF_UP).toPlainString())
                + campo("58", "BR")
                + campo("59", "ShieldPME")
                + campo("60", "SAOPAULO")
                + campo("62", campo("05", "***"))
                + "6304";
        return semCrc + crc16(semCrc);
    }

    // id + tamanho com 2 dígitos + valor
    private static String campo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    // crc-16/ccitt-false
    private static String crc16(String texto) {
        int crc = 0xFFFF;
        for (byte b : texto.getBytes(StandardCharsets.US_ASCII)) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                crc = (crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }
}
