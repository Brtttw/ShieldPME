package br.itb.projeto.shieldpme.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.itb.projeto.shieldpme.model.entity.Usuario;

/**
 * Gera o token (JWT) devolvido no login. Quem VALIDA o token é o JwtAuthFilter.
 */
@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long horasDeValidade;

    public JwtService(JwtEncoder jwtEncoder, @Value("${shieldpme.jwt.horas:8}") long horasDeValidade) {
        this.jwtEncoder = jwtEncoder;
        this.horasDeValidade = horasDeValidade;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("shieldpme")
                .subject(usuario.getEmail())
                .issuedAt(agora)
                .expiresAt(agora.plus(horasDeValidade, ChronoUnit.HOURS))
                .claim("role", usuario.getNivelAcesso())   // ADMIN ou USER
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
