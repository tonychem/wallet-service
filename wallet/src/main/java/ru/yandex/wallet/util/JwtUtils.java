package ru.yandex.wallet.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Map;
import java.util.function.Function;

/**
 * Утилитарный класс для работы с JWT
 */
public class JwtUtils {
    public static <T> T extractClaim(String token, Function<Claims, T> mapper, String secret) {
        Claims claims = extractClaims(token, secret);
        return mapper.apply(claims);
    }

    /**
     * Генерирует JWT по заданным claims, используя алгоритм подписи HS256
     *
     * @param claims карта payload
     */
    public static String generateToken(String secret, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSigningKey(secret), SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean isTokenValid(String token, String secret) {
        return extractClaims(token, secret) != null;
    }

    /**
     * Извлекает Claims из данного токена
     *
     * @param token токен, закодированный в HS256
     * @return
     */
    private static Claims extractClaims(String token, String secret) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static Key getSigningKey(String secret) {
        byte[] encodedSecretArray = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(encodedSecretArray);
    }
}
