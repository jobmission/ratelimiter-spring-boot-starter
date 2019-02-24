package com.revengemission.commons.ratelimiter;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Aspect
public class RateLimiterAop {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAop.class);

    private static final String UNKNOWN = "unknown";

    private RedisTemplate<String, Serializable> intRedisTemplate;

    public RateLimiterAop() {
    }

    public RateLimiterAop(RedisTemplate<String, Serializable> intRedisTemplate) {
        this.intRedisTemplate = intRedisTemplate;
    }

    @Around("@annotation(com.revengemission.commons.ratelimiter.RateLimiter)")
    public Object interceptor(ProceedingJoinPoint pjp) throws Throwable, RateLimiterException {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        RateLimiter limitAnnotation = method.getAnnotation(RateLimiter.class);
        String project = limitAnnotation.project();
        String limitKey = limitAnnotation.key();
        String ip = "";
        int limitPeriod = limitAnnotation.period();
        int keyLimitCount = limitAnnotation.keyLimitCount();
        int ipLimitCount = limitAnnotation.ipLimitCount();
        if (limitAnnotation.limitType() == 1 || limitAnnotation.limitType() == 2) {
            ip = getIpAddress();
        }

        if (limitAnnotation.limitType() == 0) {
            List<String> keys = new ArrayList<>();
            keys.add(StringUtils.join(limitAnnotation.prefix(), project, limitKey));
            String luaScript = buildLuaScript();
            RedisScript<Number> redisScriptByKey = new DefaultRedisScript<>(luaScript, Number.class);
            Number keyCount = intRedisTemplate.execute(redisScriptByKey, keys, keyLimitCount, limitPeriod);
            logger.debug("Try to access: project={} , key = {}, count = {}", project, limitKey, keyCount);
            if (keyCount != null && keyCount.intValue() < keyLimitCount) {
                return pjp.proceed();
            } else {
                throw new RateLimiterException("Triggered an abuse detection mechanism. " +
                        "Please wait a few minutes before you try again. ", ip);
            }
        } else if (limitAnnotation.limitType() == 2) {
            List<String> ipKeys = new ArrayList<>();
            ipKeys.add(StringUtils.join(limitAnnotation.prefix(), project, limitKey, ip));
            List<String> keyKeys = new ArrayList<>();
            keyKeys.add(StringUtils.join(limitAnnotation.prefix(), project, limitKey));
            String luaScript = buildLuaScript();

            RedisScript<Number> redisScriptByKey = new DefaultRedisScript<>(luaScript, Number.class);
            Number keyCount = intRedisTemplate.execute(redisScriptByKey, keyKeys, keyLimitCount, limitPeriod);
            RedisScript<Number> redisScriptByIp = new DefaultRedisScript<>(luaScript, Number.class);
            Number ipCount = intRedisTemplate.execute(redisScriptByIp, ipKeys, ipLimitCount, limitPeriod);
            logger.debug("Try to access: project={} , key = {}, ip = {}, count = {}", project, limitKey, ip, ipCount);
            if (ipCount != null && ipCount.intValue() < ipLimitCount && keyCount != null && keyCount.intValue() < keyLimitCount) {
                return pjp.proceed();
            } else {
                throw new RateLimiterException("Triggered an abuse detection mechanism. " +
                        "Please wait a few minutes before you try again. ", ip);
            }
        } else {
            List<String> keys = new ArrayList<>();
            keys.add(StringUtils.join(limitAnnotation.prefix(), project, limitKey, ip));
            String luaScript = buildLuaScript();
            RedisScript<Number> redisScriptByIp = new DefaultRedisScript<>(luaScript, Number.class);
            Number ipCount = intRedisTemplate.execute(redisScriptByIp, keys, ipLimitCount, limitPeriod);
            logger.debug("Try to access: project={} , key = {}, ip = {}, count = {}", project, limitKey, ip, ipCount);
            if (ipCount != null && ipCount.intValue() < ipLimitCount) {
                return pjp.proceed();
            } else {
                throw new RateLimiterException("Triggered an abuse detection mechanism. " +
                        "Please wait a few minutes before you try again. ", ip);
            }
        }

    }

    /**
     * 限流 脚本 lua脚本
     *
     * @return String
     */
    private String buildLuaScript() {
        StringBuilder lua = new StringBuilder();
        lua.append("local c\n");
        lua.append("c = redis.call('get',KEYS[1])\n");
        // 调用不超过最大值，则直接返回
        lua.append("if c and tonumber(c) > tonumber(ARGV[1]) then\n");
        lua.append("return c;\n");
        lua.append("end\n");
        // 执行计算器自加
        lua.append("c = redis.call('incr',KEYS[1])\n");
        lua.append("if tonumber(c) == 1 then\n");
        // 从第一次调用开始限流，设置对应键值的过期
        lua.append("redis.call('expire',KEYS[1],ARGV[2])\n");
        lua.append("end\n");
        lua.append("return c;");
        return lua.toString();
    }

    private String getIpAddress() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
