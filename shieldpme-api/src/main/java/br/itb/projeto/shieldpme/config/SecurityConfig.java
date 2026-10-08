package br.itb.projeto.shieldpme.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtDecoder jwtDecoder)
            throws Exception {

        http
            // ================= CSRF / CORS / SESSÃO =================
            // Sem cookies de sessão: a autenticação é o token JWT enviado em cada requisição
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ================= AUTORIZAÇÃO =================
            .authorizeHttpRequests(auth -> auth
                // Conteúdo público do site
                .requestMatchers(HttpMethod.GET, "/api/planos/**", "/api/servicos",
                        "/api/blog/posts", "/api/ferramentas").permitAll()
                // Login, cadastro, formulários e checkout (o checkout não exige login)
                .requestMatchers(HttpMethod.POST, "/api/auth/**", "/api/contatos", "/api/newsletter",
                        "/api/pagamentos/pix", "/api/assinaturas").permitAll()
                // Ler as mensagens do formulário de contato: só ADMIN
                .requestMatchers(HttpMethod.GET, "/api/contatos").hasRole("ADMIN")
                // Página de erro padrão do Spring (senão um erro 500 viraria 401)
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )

            // ================= RESPOSTAS DE ERRO EM JSON =================
            .exceptionHandling(erro -> erro
                .authenticationEntryPoint((request, response, e) ->
                        escreverErro(response, HttpServletResponse.SC_UNAUTHORIZED, "Faça login para continuar."))
                .accessDeniedHandler((request, response, e) ->
                        escreverErro(response, HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar este recurso."))
            )

            // ================= JWT =================
            .addFilterBefore(new JwtAuthFilter(jwtDecoder), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ================= PASSWORD ENCODER =================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ================= JWT: chave, geração e validação =================
    @Bean
    public SecretKey jwtSecretKey(@Value("${shieldpme.jwt.secret:}") String segredo) {
        byte[] bytes;

        if (segredo.isBlank()) {
            // Sem segredo configurado: gera um aleatório (vale até reiniciar a API)
            bytes = new byte[32];
            new SecureRandom().nextBytes(bytes);
            log.warn("shieldpme.jwt.secret nao configurado: usando um segredo aleatorio. "
                    + "Os logins deixam de valer quando a API reiniciar.");
        } else {
            bytes = segredo.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 32) {
                throw new IllegalStateException("shieldpme.jwt.secret precisa ter pelo menos 32 caracteres.");
            }
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    private static void escreverErro(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensagem\":\"" + mensagem + "\"}");
    }
}
