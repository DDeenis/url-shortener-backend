package com.itstep.coursework.services.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@Primary
public class JwtServiceV1 implements JwtService {
    // not actually a secret
    private final String secret = "Sl=]*.8:1+63K98_O7Y/0}q/";
    private final String issuer = "step";

    @Override
    public String createToken(String jti, String sub, Instant iat, Instant exp) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withJWTId(jti)
                    .withIssuer(issuer)
                    .withIssuedAt(iat)
                    .withExpiresAt(exp)
                    .withSubject(sub)
                    .sign(algorithm);
        } catch (JWTCreationException exception){
            System.err.println(exception.getMessage());
            return null;
        }
    }

    @Override
    public DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build();
            return verifier.verify(token);
        } catch (JWTVerificationException exception){
            System.err.println(exception.getMessage());
            return null;
        }
    }

    @Override
    public boolean isTokenExpired(DecodedJWT decodedJWT) {
        return Instant.now().toEpochMilli() > decodedJWT.getExpiresAt().getTime();
    }
}
