package br.itb.projeto.shieldpme.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.itb.projeto.shieldpme.dto.AuthDTO.CadastroRequest;
import br.itb.projeto.shieldpme.dto.AuthDTO.GoogleRequest;
import br.itb.projeto.shieldpme.dto.AuthDTO.LoginRequest;
import br.itb.projeto.shieldpme.dto.AuthDTO.LoginResponse;
import br.itb.projeto.shieldpme.dto.AuthDTO.UsuarioResumo;
import br.itb.projeto.shieldpme.model.entity.Usuario;
import br.itb.projeto.shieldpme.model.repository.UsuarioRepository;

@Service
public class AuthService {

    /** O BCrypt só considera os primeiros 72 bytes da senha. */
    private static final int LIMITE_BYTES_BCRYPT = 72;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenService googleTokenService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, GoogleTokenService googleTokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleTokenService = googleTokenService;
    }

    /* ================= CADASTRO ================= */
    @Transactional
    public UsuarioResumo cadastrar(CadastroRequest req) {
        String email = normalizar(req.email());

        if (excedeLimiteDoBcrypt(req.senha())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha é muito longa.");
        }
        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com este email.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(req.senha())); // BCrypt: a senha em texto nunca é gravada

        return paraResumo(usuarioRepository.save(usuario));
    }

    /* ================= LOGIN ================= */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(normalizar(req.email())).orElse(null);

        boolean credenciaisOk = usuario != null
                && usuario.getSenhaHash() != null // conta criada só pelo Google não tem senha
                && !excedeLimiteDoBcrypt(req.senha())
                && passwordEncoder.matches(req.senha(), usuario.getSenhaHash());

        if (!credenciaisOk) {
            // Mesma mensagem para "email não existe" e "senha errada": não revela quais emails têm conta
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha inválidos.");
        }
        return montarLogin(usuario);
    }

    /* ================= LOGIN COM GOOGLE ================= */
    @Transactional
    public LoginResponse loginComGoogle(GoogleRequest req) {
        Jwt google = googleTokenService.validar(req.credential());
        String email = normalizar(google.getClaimAsString("email"));

        // Já existe conta com esse email? Entra nela. Senão, cria uma nova (sem senha).
        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
            String nome = google.getClaimAsString("name");

            Usuario novo = new Usuario();
            novo.setNome(limitar(nome == null || nome.isBlank() ? email : nome.trim(), 150));
            novo.setEmail(email);
            novo.setFotoUrl(limitar(google.getClaimAsString("picture"), 500));
            return usuarioRepository.save(novo);
        });

        return montarLogin(usuario);
    }

    /* ================= AUXILIARES ================= */
    private LoginResponse montarLogin(Usuario usuario) {
        if (!"ATIVO".equals(usuario.getStatusUsuario())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta conta está inativa.");
        }
        return new LoginResponse(jwtService.gerarToken(usuario), paraResumo(usuario));
    }

    private UsuarioResumo paraResumo(Usuario usuario) {
        return new UsuarioResumo(usuario.getNome(), usuario.getEmail(), usuario.getFotoUrl());
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean excedeLimiteDoBcrypt(String senha) {
        return senha.getBytes(StandardCharsets.UTF_8).length > LIMITE_BYTES_BCRYPT;
    }

    private static String limitar(String texto, int maximo) {
        if (texto == null) {
            return null;
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo);
    }
}
