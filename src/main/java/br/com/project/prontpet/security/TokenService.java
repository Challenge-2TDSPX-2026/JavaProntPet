package br.com.project.prontpet.security;

import br.com.project.prontpet.models.Account;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Gera e valida o JWT. A chave e montada uma unica vez aqui
 * (antes era duplicada no TokenService e no JwtAuthenticationFilter).
 */
@Service
public class TokenService {

    private final SecretKey key;
    private final long expirationMs;

    public TokenService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:7200000}") long expirationMs
    ) {
        // falha no startup se a chave tiver menos de 32 bytes
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(Account account) {
        return Jwts.builder()
                .subject(account.getEmail())
                .claim("role", account.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /** Valida assinatura e expiracao e devolve o e-mail (subject) do token. */
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
