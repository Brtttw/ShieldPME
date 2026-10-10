package br.itb.projeto.shieldpme.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.itb.projeto.shieldpme.dto.AuthDTO.CadastroRequest;
import br.itb.projeto.shieldpme.dto.AuthDTO.LoginRequest;
import br.itb.projeto.shieldpme.dto.AuthDTO.UsuarioResumo;
import br.itb.projeto.shieldpme.model.entity.Usuario;
import br.itb.projeto.shieldpme.model.repository.UsuarioRepository;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public UsuarioResumo cadastrar(CadastroRequest req) {
        String email = normalizar(req.email());

        if (maiorQueBcrypt(req.senha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha é muito longa.");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com este email.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(encoder.encode(req.senha()));
        usuarioRepository.save(usuario);

        return new UsuarioResumo(usuario.getNome(), usuario.getEmail());
    }

    @Transactional(readOnly = true)
    public UsuarioResumo login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(normalizar(req.email())).orElse(null);

        boolean senhaOk = usuario != null
                && !maiorQueBcrypt(req.senha())
                && encoder.matches(req.senha(), usuario.getSenhaHash());

        // mesma mensagem para email inexistente e senha errada
        if (!senhaOk) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos.");
        }
        if (!"ATIVO".equals(usuario.getStatusUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta conta está inativa.");
        }

        return new UsuarioResumo(usuario.getNome(), usuario.getEmail());
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // bcrypt só usa os 72 primeiros bytes
    private static boolean maiorQueBcrypt(String senha) {
        return senha.getBytes(StandardCharsets.UTF_8).length > 72;
    }
}
