package com.tunhan.micsu.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final StringRedisTemplate redis;

    @Value("${jwt.refresh-expiration-days:30}")
    private int refreshExpirationDays;

    public void saveRefreshToken(String refreshToken, String userId) {
        redis.opsForValue().set(
                REFRESH_PREFIX + refreshToken,
                userId,
                Duration.ofDays(refreshExpirationDays));
        log.debug("[TokenRedis] Saved refresh token for user {}", userId);
    }

    public String getUserIdByRefreshToken(String refreshToken) {
        return redis.opsForValue().get(REFRESH_PREFIX + refreshToken);
    }

    public void deleteRefreshToken(String refreshToken) {
        redis.delete(REFRESH_PREFIX + refreshToken);
    }

    public void blacklistToken(String jti, long ttlSeconds) {
        if (ttlSeconds <= 0)
            return;
        redis.opsForValue().set(
                BLACKLIST_PREFIX + jti,
                "1",
                Duration.ofSeconds(ttlSeconds));
        log.debug("[TokenRedis] Blacklisted token jti={} for {}s", jti, ttlSeconds);
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(BLACKLIST_PREFIX + jti));
    }
}
