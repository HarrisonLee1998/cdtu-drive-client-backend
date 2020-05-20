package cn.edu.cdtu.drive.util;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Map;

/**
 * @author HarrisonLee
 * @date 2020/5/17 17:10
 */
public class JWTUtil {

    private final static String issuer = "CDTU Drive";

    public static String generate(Map<String, Object>claims) {
        final JwtBuilder jwtBuilder = Jwts.builder();
        jwtBuilder.addClaims(claims)
                .setIssuer("Chengdu Technology University")
                .setIssuedAt(Date.from(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8))));
        jwtBuilder.signWith(SignatureAlgorithm.HS256, issuer);

        return  jwtBuilder.compact();
    }

    public static void parse(String token) {
        final JwtParser parser = Jwts.parser().setSigningKey(issuer);
        final var body = parser.parse(token).getBody();
    }
}
