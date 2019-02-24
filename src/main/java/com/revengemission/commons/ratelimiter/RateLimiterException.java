package com.revengemission.commons.ratelimiter;

public class RateLimiterException extends RuntimeException {
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public RateLimiterException(String ip) {
        super();
        this.ip = ip;
    }

    public RateLimiterException(String message, String ip) {
        super(message);
        this.ip = ip;
    }
}
