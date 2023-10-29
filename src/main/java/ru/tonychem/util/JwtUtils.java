package ru.tonychem.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Map;
import java.util.function.Function;

/**
 * Утилитарный класс для работы с JWT
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private static String secret;

    public static <T> T extractClaim(String token, Function<Claims, T> mapper) {
        Claims claims = extractClaims(token);
        return mapper.apply(claims);
    }

    /**
     * Генерирует JWT по заданным claims, используя алгоритм подписи HS256
     *
     * @param claims карта payload
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean isTokenValid(String token) {
        return extractClaims(token) != null;
    }

    /**
     * Извлекает Claims из данного токена
     *
     * @param token токен, закодированный в HS256
     * @return
     */
    private static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static Key getSigningKey() {
        byte[] encodedSecretArray = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(encodedSecretArray);
    }

    public static void setSecret(String secret) {
        JwtUtils.secret = secret;
    }
}
