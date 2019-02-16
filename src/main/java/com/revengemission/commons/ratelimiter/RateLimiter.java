package com.revengemission.commons.ratelimiter;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RateLimiter {

    /**
     * 前缀
     *
     * @return String
     */
    String prefix() default "limit_";

    /**
     * 模块的名字
     *
     * @return String
     */
    String project() default "project_";

    /**
     * 资源的key
     *
     * @return String
     */
    String key() default "api_";

    /**
     * 给定的时间段
     * 单位秒
     *
     * @return int
     */
    int period() default 60;

    /**
     * 最多的访问限制次数
     *
     * @return int
     */
    int count() default 10;

    /**
     * key是否包含IP
     *
     * @return boolean
     */
    boolean keyWithIP() default true;
}
