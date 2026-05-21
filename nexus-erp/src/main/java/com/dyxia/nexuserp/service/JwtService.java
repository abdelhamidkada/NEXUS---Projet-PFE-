package com.dyxia.nexuserp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service pour la gestion des tokens JWT (JSON Web Tokens).
 * Gère la génération, le décodage et la validation des tokens d'accès.
 */
@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Expiration stricte fixée à exactement 15 minutes (15 min * 60 s * 1000 ms)
    private static final long JWT_EXPIRATION = 15 * 60 * 1000;

    /**
     * Génère un token d'accès JWT pour un utilisateur spécifique.
     *
     * @param userDetails Les détails de l'utilisateur (email, rôles, etc.).
     * @return Le token JWT généré sous forme de String.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Génère un token d'accès JWT avec des claims (revendications) supplémentaires.
     *
     * @param extraClaims Des revendications supplémentaires à inclure dans le token.
     * @param userDetails Les détails de l'utilisateur.
     * @return Le token JWT généré.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extrait l'adresse e-mail (username) contenue dans le token JWT.
     *
     * @param token Le token JWT.
     * @return L'email de l'utilisateur.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait un claim spécifique du token en utilisant une fonction de résolution.
     *
     * @param token Le token JWT.
     * @param claimsResolver La fonction de rappel pour récupérer le claim.
     * @param <T> Le type du claim.
     * @return La valeur du claim extrait.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Valide la légitimité du token JWT par rapport aux détails de l'utilisateur.
     *
     * @param token Le token JWT.
     * @param userDetails Les détails de l'utilisateur à comparer.
     * @return true si le token est valide et non expiré, sinon false.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Vérifie si le token JWT a expiré.
     *
     * @param token Le token JWT.
     * @return true si le token est expiré, sinon false.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrait la date d'expiration du token JWT.
     *
     * @param token Le token JWT.
     * @return La date d'expiration.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait tous les claims signés du token JWT en utilisant la clé de signature.
     *
     * @param token Le token JWT.
     * @return Les claims extraits.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Décode la clé secrète encodée en Base64 et renvoie une instance de {@link SecretKey}.
     *
     * @return La clé secrète de signature.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
