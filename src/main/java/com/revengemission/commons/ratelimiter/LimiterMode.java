package com.revengemission.commons.ratelimiter;

/**
 * @author wzhang
 */
public enum LimiterMode {
    /**
     * 根据IP限流
     */
    IP,
    /**
     * 根据指定的key限流
     */
    KEY,
    /**
     * 二者结合的方式限流
     */
    COMBINATION
}
