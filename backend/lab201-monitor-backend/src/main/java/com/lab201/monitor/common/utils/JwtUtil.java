package com.lab201.monitor.common.utils;

import com.lab201.monitor.common.exception.LabException;
import com.lab201.monitor.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    // 7天过期，可根据需求调整
    private static long tokenExpiration = 7 * 24 * 60 * 60 * 1000L;
    // 签名密钥，长度需符合算法要求
    private static SecretKey tokenSignKey = Keys.hmacShaKeyFor("M0PKKI6pYGVWWfDZw90a0lTpGYX1d4AQ".getBytes());

    // 生成token
    public static String createToken(Long userId, String username,String role) {
        String token = Jwts.builder()
                .setSubject("USER_INFO")
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
                .setIssuedAt(new Date())
                .claim("userId", userId)
                .claim("username", username)
                .claim("role",role)
                .signWith(tokenSignKey)
                .compact();
        return token;
    }

    // 解析token
    public static Claims parseToken(String token) {
        if (token == null) {
            throw new LabException(ResultCodeEnum.Token_Empty);
        }
        try {
            JwtParser jwtParser = Jwts.parserBuilder().setSigningKey(tokenSignKey).build();
            return jwtParser.parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new LabException(ResultCodeEnum.UNAUTHORIZED);
        } catch (JwtException e) {
            throw new LabException(ResultCodeEnum.Token_Error);
        }
    }
}