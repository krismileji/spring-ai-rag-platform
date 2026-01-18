package cn.krismile.ai.agent.configuration.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT Token Provider
 *
 * @author JiYinchuan
 * @since 1.0.0
 */
@Component
public class JwtTokenProvider {

    private static final String DEFAULT_SECRET_KEY = "krismile666krismile666krismile666krismile666";
    private static final String DEFAULT_VALIDITY_SECONDS = "2592000";

    @Value("${project.jwt.secret-key:" + DEFAULT_SECRET_KEY + "}")
    private String secretKey;

    @Value("${project.jwt.timeout:" + DEFAULT_VALIDITY_SECONDS + "}")
    private long validityInSeconds;

    private Key key;

    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String createToken(Long userId) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInSeconds * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        String userId = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
        return Long.parseLong(userId);
    }
}
