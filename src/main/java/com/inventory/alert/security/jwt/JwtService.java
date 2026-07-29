package com.inventory.alert.security.jwt;

import com.inventory.alert.enums.Role;
import com.inventory.alert.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Issues and validates HS256 JWTs. Stateless auth alternative to server sessions —
 * chosen for a simple REST API without sticky sessions. Trade-off: no server-side revoke
 * until expiry (refresh-token / denylist would be needed for instant logout).
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLE = "role";

    private final JwtProperties jwtProperties;

    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.expirationMs());
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(CLAIM_USER_ID, userDetails.getId())
                .claim(CLAIM_ROLE, userDetails.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    public long getExpirationSeconds() {
        return jwtProperties.expirationMs() / 1000L;
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        return parseClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    public Role extractRole(String token) {
        return Role.valueOf(parseClaims(token).get(CLAIM_ROLE, String.class));
    }

    public boolean isTokenValid(String token, CustomUserDetails userDetails) {
        Claims claims = parseClaims(token);
        String email = claims.getSubject();
        return email.equals(userDetails.getUsername()) && claims.getExpiration().after(new Date());
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw ex;
        } catch (JwtException | IllegalArgumentException ex) {
            throw new JwtException("Invalid JWT token", ex);
        }
    }

    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
