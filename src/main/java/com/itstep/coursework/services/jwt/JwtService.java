package com.itstep.coursework.services.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;

import java.time.Instant;

public interface JwtService {
    public String createToken(String jti, String sub, Instant iat, Instant exp);
    public DecodedJWT verifyToken(String token);
    public boolean isTokenExpired(DecodedJWT decodedJWT);
}
