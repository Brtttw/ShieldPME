package br.itb.projeto.shieldpme.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.itb.projeto.shieldpme.dto.AssinaturaDTO.AssinaturaRequest;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.AssinaturaResponse;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.PixRequest;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.PixResponse;
import br.itb.projeto.shieldpme.service.AssinaturaService;
import br.itb.projeto.shieldpme.service.PixService;
import jakarta.validation.Valid;

/**
 * Checkout: gerar o PIX e finalizar a assinatura. Ambos são públicos (o checkout não exige login).
 */
@RestController
@RequestMapping("/api")
public class AssinaturaController {

    private final AssinaturaService assinaturaService;
    private final PixService pixService;

    public AssinaturaController(AssinaturaService assinaturaService, PixService pixService) {
        this.assinaturaService = assinaturaService;
        this.pixService = pixService;
    }

    @PostMapping("/pagamentos/pix")
    public PixResponse pix(@Valid @RequestBody PixRequest req) {
        return pixService.gerar(req.planoCodigo());
    }

    // O front lê o corpo da resposta como JSON, então o 201 precisa vir COM corpo.
    @PostMapping("/assinaturas")
    public ResponseEntity<AssinaturaResponse> criar(@Valid @RequestBody AssinaturaRequest req,
            Authentication authentication) {

        // Se o cliente estiver logado (token válido), a assinatura é ligada ao usuário
        String emailLogado = authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
                ? authentication.getName()
                : null;

        return ResponseEntity.status(HttpStatus.CREATED).body(assinaturaService.criar(req, emailLogado));
    }
}
