package com.revengemission.commons.ratelimiter;

import java.security.AccessControlException;

/**
 * @author wzhang
 */
public class RateLimiterException extends AccessControlException {
    private String ip;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public RateLimiterException(String message) {
        super(message);
    }

    public RateLimiterException(String message, String ip) {
        super(message);
        this.ip = ip;
    }
}
