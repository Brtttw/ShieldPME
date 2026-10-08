package br.itb.projeto.shieldpme.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Valida o ID token que o botão "Entrar com Google" entrega ao front.
 * Confere a assinatura (chaves públicas do Google), a validade, o emissor e se o token
 * foi emitido para o NOSSO Client ID. Precisa de internet só no momento do login com Google.
 */
@Service
public class GoogleTokenService {

    private static final String CHAVES_PUBLICAS_GOOGLE = "https://www.googleapis.com/oauth2/v3/certs";

    private final NimbusJwtDecoder decoder;

    public GoogleTokenService(@Value("${shieldpme.google.client-id}") String clientId) {
        this.decoder = NimbusJwtDecoder.withJwkSetUri(CHAVES_PUBLICAS_GOOGLE).build();

        OAuth2TokenValidator<Jwt> emissorEPublico = jwt -> {
            String emissor = jwt.getClaimAsString("iss");
            boolean emissorOk = "accounts.google.com".equals(emissor) || "https://accounts.google.com".equals(emissor);
            boolean publicoOk = jwt.getAudience() != null && jwt.getAudience().contains(clientId);

            return emissorOk && publicoOk
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token do Google inválido.", null));
        };

        this.decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), emissorEPublico));
    }

    /** Devolve o token já validado (claims: email, name, picture...) ou lança 401. */
    public Jwt validar(String credential) {
        Jwt jwt;
        try {
            jwt = decoder.decode(credential);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não foi possível validar o login com o Google.");
        }

        boolean emailVerificado = Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified"));
        if (jwt.getClaimAsString("email") == null || !emailVerificado) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "O email da conta Google não está verificado.");
        }
        return jwt;
    }
}
