package com.revengemission.commons.ratelimiter;

import java.lang.annotation.*;

/**
 * @author wzhang
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiters {
    RateLimiter[] value();
}
