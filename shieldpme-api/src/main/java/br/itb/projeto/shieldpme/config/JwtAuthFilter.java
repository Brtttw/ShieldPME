package br.itb.projeto.shieldpme.config;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Lê "Authorization: Bearer <token>" e, se o token for válido, identifica o usuário.
 *
 * Token ausente, inválido ou expirado NÃO gera erro aqui: a requisição segue como anônima.
 * Isso é importante porque o front guarda o token no navegador e o envia em TODAS as chamadas
 * (inclusive nas públicas, como GET /api/planos). Quem decide se a rota exige login é o SecurityConfig.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    public JwtAuthFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            try {
                Jwt jwt = jwtDecoder.decode(cabecalho.substring("Bearer ".length()));

                String nivelAcesso = jwt.getClaimAsString("role");
                List<GrantedAuthority> permissoes = nivelAcesso == null
                        ? List.of()
                        : List.of(new SimpleGrantedAuthority("ROLE_" + nivelAcesso));

                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(jwt.getSubject(), null, permissoes));
            } catch (JwtException e) {
                // token inválido/expirado: segue como anônimo
            }
        }

        chain.doFilter(request, response);
    }
}
