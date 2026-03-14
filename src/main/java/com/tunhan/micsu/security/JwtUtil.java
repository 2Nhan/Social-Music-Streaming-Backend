package com.tunhan.micsu.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    /** Access token TTL in ms — default 15 minutes */
    @Value("${jwt.expiration:900000}")
    private long expiration;

    /** Generates a short-lived access token (sub = userId) */
    public String generateAccessToken(String userId) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .jwtID(UUID.randomUUID().toString()) // unique jti for blacklisting
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expiration))
                    .build();

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(new MACSigner(secret.getBytes()));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error("[JwtUtil] Failed to generate access token: {}", e.getMessage());
            throw new RuntimeException("Could not generate access token", e);
        }
    }

    /** Generates a random opaque refresh token (UUID-based) */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "");
    }

    /** Kept for backward compatibility — delegates to generateAccessToken */
    public String generateToken(String userId) {
        return generateAccessToken(userId);
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            MACVerifier verifier = new MACVerifier(secret.getBytes());
            if (!jwt.verify(verifier)) {
                return false;
            }
            return jwt.getJWTClaimsSet().getExpirationTime().after(new Date());
        } catch (JOSEException | ParseException e) {
            log.warn("[JwtUtil] Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String extractUserId(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            return jwt.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            log.error("[JwtUtil] Failed to extract userId from token: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /** Returns the jti (JWT ID) claim — used as the blacklist key. */
    public String extractJti(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getJWTID();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /** Returns the expiration timestamp in epoch milliseconds. */
    public long extractExpirationMs(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet()
                    .getExpirationTime().getTime();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}
