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
    String prefix() default "ratelimiter_";

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
    int period() default 30;

    /**
     * 最多访问限制次数
     *
     * @return int
     */
    int keyLimitCount() default 900;

    /**
     * 最多访问限制次数
     *
     * @return int
     */
    int ipLimitCount() default 15;

    /**
     * 限速模式 0：key limit ,1:ip limit ,2:ip and key limit
     *
     * @return int
     */
    int limitType() default 1;
}
