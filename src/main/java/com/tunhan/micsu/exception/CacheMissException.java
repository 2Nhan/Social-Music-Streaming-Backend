package com.tunhan.micsu.exception;

/**
 * Exception thrown when a cache miss requires reload from database.
 */
public class CacheMissException extends RuntimeException {
    public CacheMissException(String message) {
        super(message);
    }

    public CacheMissException(String message, Throwable cause) {
        super(message, cause);
    }
}
