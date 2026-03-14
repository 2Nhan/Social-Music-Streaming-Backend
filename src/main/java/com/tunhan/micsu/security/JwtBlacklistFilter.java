package com.tunhan.micsu.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tunhan.micsu.service.auth.TokenRedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Runs once per request BEFORE the JWT resource-server filter.
 * If the access token's jti is on the Redis blacklist → reject with 401.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRedisService tokenRedis;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String jti = jwtUtil.extractJti(token);
                if (jti != null && tokenRedis.isBlacklisted(jti)) {
                    log.warn("[JwtBlacklistFilter] Rejected blacklisted token jti={}", jti);
                    sendUnauthorized(response, "Token has been revoked. Please log in again.");
                    return;
                }
            } catch (Exception e) {
                // Malformed token — let the oauth2 resource server handle it
                log.debug("[JwtBlacklistFilter] Could not extract jti: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of(
                "status", 401,
                "error", "Unauthorized",
                "message", message));
    }
}
