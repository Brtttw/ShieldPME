package br.itb.projeto.shieldpme.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.itb.projeto.shieldpme.dto.AuthDTO.CadastroRequest;
import br.itb.projeto.shieldpme.dto.AuthDTO.LoginRequest;
import br.itb.projeto.shieldpme.dto.AuthDTO.UsuarioResumo;
import br.itb.projeto.shieldpme.service.AuthService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public UsuarioResumo login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    // o front lê o corpo do 201 como json, então não pode ir vazio
    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResumo> cadastro(@Valid @RequestBody CadastroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(req));
    }
}
