package com.tunhan.micsu.controller;

import com.tunhan.micsu.dto.request.LoginRequest;
import com.tunhan.micsu.dto.request.RefreshTokenRequest;
import com.tunhan.micsu.dto.request.RegisterRequest;
import com.tunhan.micsu.dto.response.ApiResponse;
import com.tunhan.micsu.dto.response.AuthResponse;
import com.tunhan.micsu.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Registered successfully",
                authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully",
                authService.login(request)));
    }

    /**
     * Issues a new access + refresh token pair using a valid refresh token.
     * The old refresh token is rotated (deleted from Redis, replaced with a new
     * one).
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Token refreshed",
                authService.refresh(request.getRefreshToken())));
    }

    /**
     * Logs out the user by:
     * 1. Revoking the refresh token from Redis.
     * 2. Blacklisting the current access token in Redis for its remaining TTL.
     *
     * The access token is read from the Authorization header (Bearer <token>).
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        authService.logout(request.getRefreshToken(), accessToken);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }
}
