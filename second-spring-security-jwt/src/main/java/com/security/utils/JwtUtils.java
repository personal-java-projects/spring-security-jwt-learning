package com.security.utils;

import com.security.constant.RedisConstant;
import com.security.enums.CodeEnum;
import com.security.exception.BizException;
import com.security.model.SecurityUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author  不才陈某
 * JWT的工具类
 */
@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtUtils {

    //秘钥
    private String secret;

    // 过期时间 毫秒
    private Long expiration;

    @Autowired
    private RedisUtil redisUtil;

    public String createToken(String userName){
        return Jwts.builder()
                .setSubject(userName)
                //生成时间
                .setIssuedAt(new Date())
                //过期时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String createToken(SecurityUser securityUser){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", securityUser.getId());
        claims.put("username", securityUser.getUsername());
        claims.put("roles", securityUser.getAuthorities());

        // 过期时间
        Date expireTime = new Date(System.currentTimeMillis() + expiration);

        // 生成token
        String accessToken = Jwts.builder()
                .addClaims(claims)
                //生成时间
                .setIssuedAt(new Date())
                //过期时间
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        redisUtil.set(RedisConstant.UNIQUE_USER_PREFIX+securityUser.getUsername(), accessToken, expiration);

        return accessToken;
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims,Long expiration) {
        Date expirationDate = new Date(System.currentTimeMillis() + expiration);
        return Jwts.builder().setClaims(claims).setExpiration(expirationDate).signWith(SignatureAlgorithm.HS512, secret).compact();
    }


    /**
     * 从token中解析出数据
     * @param token 令牌
     * @return
     */
    private Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            claims = null;
        }
        return claims;
    }

    /**
     * 从令牌中获取用户名
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            username = (String) claims.get("username");
        } catch (Exception e) {
            e.printStackTrace();
            username = null;
        }
        return username;
    }

    /**
     * 判断令牌是否过期
     *
     * @param token 令牌
     * @return 是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();

            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 刷新令牌
     *
     * @param token 原令牌
     * @return 新令牌
     */
    public String refreshToken(String token) {
        String refreshedToken;
        try {
            Claims claims = getClaimsFromToken(token);
            claims.put(Claims.ISSUED_AT, new Date());
            refreshedToken = generateToken(claims,2*expiration);
        } catch (Exception e) {
            refreshedToken = null;
        }
        return refreshedToken;
    }

    /**
     * 验证令牌
     *
     * @param token       令牌
     * @param userDetails 用户
     * @return 是否有效
     */
    public Boolean validateToken(String token, UserDetails userDetails) {

        SecurityUser user = (SecurityUser) userDetails;
        String username = getUsernameFromToken(token);

        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }


}
