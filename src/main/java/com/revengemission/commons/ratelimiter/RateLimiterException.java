package com.revengemission.commons.ratelimiter;

public class RateLimiterException extends RuntimeException {
    public RateLimiterException() {
    }

    public RateLimiterException(String message) {
        super(message);
    }

    public RateLimiterException(String message, Throwable cause) {
        super(message, cause);
    }
}
