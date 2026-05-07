package com.tunhan.micsu.utils;

import lombok.experimental.UtilityClass;

/**
 * Lua Scripts for Redis atomic operations.
 * Ensures atomicity of multi-step operations to prevent race conditions.
 */
@UtilityClass
public class LuaScripts {

    /**
     * Lua Script: Atomically add a like.
     * - SADD userId to song:{songId}:likes set
     * - ZADD songId to songs:likes_modified ZSET with current timestamp
     * - EXPIRE both keys to maintain TTL
     *
     * KEYS[1]: song:{songId}:likes (Redis Set)
     * KEYS[2]: songs:likes_modified (Redis ZSET - dirty flag)
     * ARGV[1]: userId (user ID to add)
     * ARGV[2]: Current timestamp in ms
     * ARGV[3]: TTL in seconds (default 7 days = 604800s)
     *
     * Returns: 1 if userId was added (new like), 0 if already liked
     */
    public static final String ADD_LIKE_SCRIPT = """
        local likes_key = KEYS[1]
        local modified_key = KEYS[2]
        local user_id = ARGV[1]
        local current_time = tonumber(ARGV[2])
        local ttl = tonumber(ARGV[3])
        
        -- Check if user already liked this song
        local is_member = redis.call('SISMEMBER', likes_key, user_id)
        if is_member == 1 then
            -- User already liked, just update TTL and return 0
            redis.call('EXPIRE', likes_key, ttl)
            return 0
        end
        
        -- Add user to likes set
        redis.call('SADD', likes_key, user_id)
        
        -- Mark song as modified (dirty) with current timestamp
        redis.call('ZADD', modified_key, current_time, likes_key)
        
        -- Set TTLs for both keys
        redis.call('EXPIRE', likes_key, ttl)
        redis.call('EXPIRE', modified_key, ttl)
        
        return 1
        """;

    /**
     * Lua Script: Atomically remove a like (unlike).
     * - SREM userId from song:{songId}:likes set
     * - ZADD songId to songs:likes_modified ZSET with current timestamp
     * - EXPIRE both keys
     *
     * KEYS[1]: song:{songId}:likes (Redis Set)
     * KEYS[2]: songs:likes_modified (Redis ZSET - dirty flag)
     * ARGV[1]: userId (user ID to remove)
     * ARGV[2]: Current timestamp in ms
     * ARGV[3]: TTL in seconds
     *
     * Returns: 1 if userId was removed, 0 if user didn't like the song
     */
    public static final String REMOVE_LIKE_SCRIPT = """
        local likes_key = KEYS[1]
        local modified_key = KEYS[2]
        local user_id = ARGV[1]
        local current_time = tonumber(ARGV[2])
        local ttl = tonumber(ARGV[3])
        
        -- Check if user liked this song
        local is_member = redis.call('SISMEMBER', likes_key, user_id)
        if is_member == 0 then
            -- User hasn't liked, return 0
            return 0
        end
        
        -- Remove user from likes set
        redis.call('SREM', likes_key, user_id)
        
        -- Mark song as modified (dirty) with current timestamp
        redis.call('ZADD', modified_key, current_time, likes_key)
        
        -- Set TTLs for both keys
        redis.call('EXPIRE', likes_key, ttl)
        redis.call('EXPIRE', modified_key, ttl)
        
        return 1
        """;

    /**
     * Lua Script: Atomically check if user liked and refresh TTL.
     * Used during reload to batch-check likes for efficiency.
     *
     * KEYS[1]: song:{songId}:likes (Redis Set)
     * ARGV[1]: userId
     * ARGV[2]: TTL in seconds
     *
     * Returns: 1 if user is in set, 0 otherwise
     */
    public static final String CHECK_LIKE_AND_REFRESH_TTL_SCRIPT = """
        local likes_key = KEYS[1]
        local user_id = ARGV[1]
        local ttl = tonumber(ARGV[2])
        
        -- Check membership
        local is_member = redis.call('SISMEMBER', likes_key, user_id)
        
        -- Refresh TTL regardless
        redis.call('EXPIRE', likes_key, ttl)
        
        return is_member
        """;
}
