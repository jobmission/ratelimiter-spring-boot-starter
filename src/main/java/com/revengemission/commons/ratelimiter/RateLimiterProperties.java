package com.revengemission.commons.ratelimiter;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author wzhang
 */
@ConfigurationProperties("spring.redis")
public class RateLimiterProperties {
    private String host = "localhost";
    private int port = 6379;
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
