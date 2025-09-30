package com.bin.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JWTUtil {
    // 定义令牌秘钥
    @Value("${jwt.secret}")
    private String secret;
    // 定义令牌过期时间
    @Value("${jwt.expiration}")
    private Long expiration;

    private static final String JWT_ISS = "UniBrain"; // JWT 签发者

    /**
     * 生成JWT令牌
     */
    public String generateToken(String name, Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration); //创建过期时间
        return Jwts.builder()
                .claims(claims) // 添加自定义负载
                .subject(name) // 设置用户名
                .issuedAt(new Date()) // 设置签发时间
                .expiration(expirationDate) // 设置过期时间
                .issuer(JWT_ISS) // 设置签发者
                .signWith(getSecretKey(),Jwts.SIG.HS256) // 设置签名算法和秘钥
                .compact();
    }
    /**
     * 生成JWT令牌(无自定义负载)
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(String username) {
        return generateToken(username, Map.of());
    }
    /**
     * 从JWT令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 从JWT令牌中获取负载信息
     * @param token JWT令牌
     * @return 负载信息
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    /**
     * 验证JWT令牌是否有效
     * @param token JWT令牌
     * @param username 用户名
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    /**
     * 检查JWT令牌是否过期
     * @param token JWT令牌
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 刷新JWT令牌(生成新的令牌，保持用户名和自定义负载不变)
     * @param token 旧令牌
     * @return 新令牌
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return generateToken(claims.getSubject(), claims);
    }
    /**
     * 生成签名秘钥
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }



}
