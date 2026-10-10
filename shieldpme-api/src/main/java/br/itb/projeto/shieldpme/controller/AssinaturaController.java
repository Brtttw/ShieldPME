package br.itb.projeto.shieldpme.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.itb.projeto.shieldpme.dto.AssinaturaDTO.AssinaturaRequest;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.AssinaturaResponse;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.PixRequest;
import br.itb.projeto.shieldpme.dto.AssinaturaDTO.PixResponse;
import br.itb.projeto.shieldpme.service.AssinaturaService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class AssinaturaController {

    private final AssinaturaService assinaturaService;

    public AssinaturaController(AssinaturaService assinaturaService) {
        this.assinaturaService = assinaturaService;
    }

    @PostMapping("/pagamentos/pix")
    public PixResponse pix(@Valid @RequestBody PixRequest req) {
        return assinaturaService.gerarPix(req.planoCodigo());
    }

    // 201 com corpo, mesmo motivo do cadastro
    @PostMapping("/assinaturas")
    public ResponseEntity<AssinaturaResponse> criar(@Valid @RequestBody AssinaturaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assinaturaService.criar(req));
    }
}
