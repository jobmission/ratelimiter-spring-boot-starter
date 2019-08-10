package com.revengemission.commons.ratelimiter;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
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

/**
 * @author wzhang
 */
@Aspect
public class RateLimiterAop {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterAop.class);

    private static final String UNKNOWN = "unknown";

    private RedisTemplate<String, Serializable> intRedisTemplate;

    public RateLimiterAop(RedisTemplate<String, Serializable> intRedisTemplate) {
        this.intRedisTemplate = intRedisTemplate;
    }


    @Before("@annotation(com.revengemission.commons.ratelimiter.RateLimiter)")
    public void interceptor(JoinPoint joinPoint) throws RateLimiterException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        RateLimiter[] limitAnnotations = method.getAnnotationsByType(RateLimiter.class);

        boolean limitFlag = false;
        for (RateLimiter limitAnnotation : limitAnnotations) {
            String project = limitAnnotation.project();
            String limitKeySpel = limitAnnotation.key();
            String limitKey = SpelUtil.parse(limitKeySpel, method, args);
            String ip = "";
            int limitPeriod = limitAnnotation.period();
            int keyLimitCount = limitAnnotation.keyLimitCount();
            int ipLimitCount = limitAnnotation.ipLimitCount();
            if (limitAnnotation.limitMode().equals(LimiterMode.IP) || limitAnnotation.limitMode().equals(LimiterMode.COMBINATION)) {
                ip = getIpAddress();
            }

            if (limitAnnotation.limitMode().equals(LimiterMode.KEY)) {
                List<String> keys = new ArrayList<>();
                keys.add(limitAnnotation.prefix() + "" + project + "" + limitKey);
                String luaScript = buildLuaScript();
                RedisScript<Number> redisScriptByKey = new DefaultRedisScript<>(luaScript, Number.class);
                Number keyCount = intRedisTemplate.execute(redisScriptByKey, keys, limitPeriod);
                logger.debug("Try to access: project={} , key = {}, count = {}", project, limitKey, keyCount);
                if (keyCount != null && keyCount.intValue() > keyLimitCount) {
                    limitFlag = true;
                    break;
                }
            } else if (limitAnnotation.limitMode().equals(LimiterMode.COMBINATION)) {
                List<String> ipKeys = new ArrayList<>();
                ipKeys.add(limitAnnotation.prefix() + "" + project + "" + limitKey + ip);
                List<String> keyKeys = new ArrayList<>();
                keyKeys.add(limitAnnotation.prefix() + "" + project + "" + limitKey);
                String luaScript = buildLuaScript();

                RedisScript<Number> redisScriptByKey = new DefaultRedisScript<>(luaScript, Number.class);
                Number keyCount = intRedisTemplate.execute(redisScriptByKey, keyKeys, limitPeriod);
                RedisScript<Number> redisScriptByIp = new DefaultRedisScript<>(luaScript, Number.class);
                Number ipCount = intRedisTemplate.execute(redisScriptByIp, ipKeys, limitPeriod);
                logger.debug("Try to access: project={} , key = {}, ip = {}, count = {}", project, limitKey, ip,
                    ipCount);
                boolean checkFlag = (ipCount != null && ipCount.intValue() > ipLimitCount) || (keyCount != null && keyCount.intValue() > keyLimitCount);
                if (checkFlag) {
                    limitFlag = true;
                    break;
                }
            } else {
                List<String> keys = new ArrayList<>();
                keys.add(limitAnnotation.prefix() + "" + project + "" + limitKey + ip);
                String luaScript = buildLuaScript();
                RedisScript<Number> redisScriptByIp = new DefaultRedisScript<>(luaScript, Number.class);
                Number ipCount = intRedisTemplate.execute(redisScriptByIp, keys, limitPeriod);
                logger.info("Try to access: project={} , key = {}, ip = {}, count = {}", project, limitKey, ip,
                    ipCount);
                if (ipCount != null && ipCount.intValue() > ipLimitCount) {
                    System.out.println("ipCount:" + ipCount);
                    System.out.println("ipLimitCount:" + ipLimitCount);
                    limitFlag = true;
                    break;
                }
            }
        }

        if (limitFlag) {
            throw new RateLimiterException(
                "Triggered an abuse detection mechanism. " + "Please wait a few minutes before you try again. ");
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
        // 执行计算器自加
        lua.append("c = redis.call('incr',KEYS[1])\n");
        lua.append("if tonumber(c) == 1 then\n");
        // 从第一次调用开始限流，设置对应键值的过期
        lua.append("redis.call('expire',KEYS[1],ARGV[1])\n");
        lua.append("end\n");
        lua.append("return c;");
        return lua.toString();
    }

    private String getIpAddress() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
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
