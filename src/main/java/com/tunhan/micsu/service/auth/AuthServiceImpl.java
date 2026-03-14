package com.tunhan.micsu.service.auth;

import com.tunhan.micsu.dto.request.LoginRequest;
import com.tunhan.micsu.dto.request.RegisterRequest;
import com.tunhan.micsu.dto.response.AuthResponse;
import com.tunhan.micsu.entity.User;
import com.tunhan.micsu.exception.DuplicateResourceException;
import com.tunhan.micsu.exception.ResourceNotFoundException;
import com.tunhan.micsu.repository.UserRepository;
import com.tunhan.micsu.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenRedisService tokenRedis;

    /** Access token TTL in ms — must match jwt.expiration in config */
    @Value("${jwt.expiration:900000}")
    private long accessTokenTtlMs;

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);
        log.info("[AuthService] Registered new user: {}", user.getId());
        return buildAuthResponse(user.getId());
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid email or password");
        }

        log.info("[AuthService] User logged in: {}", user.getId());
        return buildAuthResponse(user.getId());
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Override
    public AuthResponse refresh(String refreshToken) {
        String userId = tokenRedis.getUserIdByRefreshToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("Invalid or expired refresh token. Please log in again.");
        }

        // Rotate: delete old refresh token, issue new pair
        tokenRedis.deleteRefreshToken(refreshToken);
        log.info("[AuthService] Rotating refresh token for user: {}", userId);
        return buildAuthResponse(userId);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    /**
     * @param refreshToken the refresh token to revoke
     * @param accessToken  the current access token to blacklist (may be null if
     *                     already expired)
     */
    @Override
    public void logout(String refreshToken, String accessToken) {
        // 1. Revoke refresh token
        if (refreshToken != null && !refreshToken.isBlank()) {
            tokenRedis.deleteRefreshToken(refreshToken);
            log.info("[AuthService] Refresh token revoked");
        }

        // 2. Blacklist access token for its remaining lifetime
        if (accessToken != null && !accessToken.isBlank()) {
            try {
                // extract jti (or use the raw token as key) and compute remaining TTL
                String jti = jwtUtil.extractJti(accessToken);
                long expiresAt = jwtUtil.extractExpirationMs(accessToken);
                long remainingMs = expiresAt - Instant.now().toEpochMilli();
                long remainingSeconds = remainingMs / 1000;
                tokenRedis.blacklistToken(jti, remainingSeconds);
                log.info("[AuthService] Access token blacklisted (jti={})", jti);
            } catch (Exception e) {
                log.warn("[AuthService] Could not blacklist access token: {}", e.getMessage());
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(String userId) {
        String accessToken = jwtUtil.generateAccessToken(userId);
        String refreshToken = jwtUtil.generateRefreshToken();
        tokenRedis.saveRefreshToken(refreshToken, userId);
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
