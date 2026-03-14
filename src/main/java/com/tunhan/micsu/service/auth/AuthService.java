package com.tunhan.micsu.service.auth;

import com.tunhan.micsu.dto.request.LoginRequest;
import com.tunhan.micsu.dto.request.RegisterRequest;
import com.tunhan.micsu.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    /**
     * Validates the refresh token in Redis, issues a new access + refresh token
     * (token rotation — old refresh token is deleted).
     */
    AuthResponse refresh(String refreshToken);

    /**
     * Revokes the refresh token from Redis AND blacklists the access token
     * for its remaining lifetime.
     *
     * @param refreshToken the refresh token to delete from Redis
     * @param accessToken  the current JWT access token to put on the blacklist
     */
    void logout(String refreshToken, String accessToken);
}
