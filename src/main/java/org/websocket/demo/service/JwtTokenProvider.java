package org.websocket.demo.service;


import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    // 토큰의 유효기간
    private long tokenValidMilisecond = 1000L*60*60;

    //1) 이름으로 JWT TOKEN을 생성한다.
    public String generateToken(String name) {
        Date now = new Date();

        return Jwts.builder()
                .setId(name)
                .setIssuedAt(now) // 발행 일자
                .setExpiration(new Date(now.getTime() + tokenValidMilisecond)) // 토큰의 수명
                .signWith(SignatureAlgorithm.HS256, secretKey) // 암호화값 -> secret값을 바꿔서 세팅
                .compact();
    }

    //2) Jwt Token을 복호화 하여 이름을 얻는다.
    public String getUserNameFromJwt(String jwt) {
        return getClaims(jwt).getBody().getId();
    }

    //3) Jwt Token 유효성 체크
    public boolean validateToken(String jwt) {
        return this.getClaims(jwt) != null;
    }

    //4) 유효성 검증을 하는 실질적인 로직 -> 외부 접근 못하게 private로 막음
    private Jws<Claims> getClaims(String jwt) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
        }catch (SignatureException ex){
            log.error("Invalid JWT signature");
            throw ex;
        }catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
            throw ex;
        }catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
            throw ex;
        }catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
            throw ex;
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty. ");
            throw ex;
        }
    }

}
