package com.dahiramafatihulbichriisepat.plateform_dahira_mafatihoul_bichri_isepat.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${jwt.secret:change-me-to-a-long-secret-key-for-dahira-project-12345678901234567890}")
    private String secretKey;

    /*
     * Access Token : 15 minutes
     */
    @Value("${jwt.expiration:900000}")
    private long jwtExpiration;

    /*
     * Refresh Token : 30 jours
     */
    @Value("${jwt.refresh-expiration:2592000000}")
    private long refreshExpiration;

    /**
     * Génère un Access Token.
     */
    public String generateToken(String email, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Génère un Refresh Token.
     */
    public String generateRefreshToken(String email) {

        return Jwts.builder()
                .setSubject(email)
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrait l'email du JWT.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait le rôle.
     */
    public String extractRole(String token) {
        return extractClaim(
                token,
                claims -> claims.get("role", String.class));
    }

    /**
     * Extrait le type du token.
     */
    public String extractTokenType(String token) {
        return extractClaim(
                token,
                claims -> claims.get("type", String.class));
    }

    /**
     * Vérifie un Access Token.
     */
    public boolean isTokenValid(String token, String username) {

        final String extractedUsername = extractUsername(token);

        return extractedUsername.equals(username)
                && !isTokenExpired(token)
                && "access".equals(extractTokenType(token));
    }

    /**
     * Vérifie un Refresh Token.
     */
    public boolean isRefreshTokenValid(String token) {

        try {

            String type = extractTokenType(token);

            return "refresh".equals(type)
                    && !isTokenExpired(token);

        } catch (Exception e) {

            return false;
        }
    }

    /**
     * Extrait une information du JWT.
     */
    private <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver) {

        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claimsResolver.apply(claims);
    }

    /**
     * Vérifie si le token est expiré.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Récupère la date d'expiration.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Clé de signature.
     */
    private Key getSigningKey() {

        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8));
    }
}