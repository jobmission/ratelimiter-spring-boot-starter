package com.revengemission.commons.ratelimiter;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * 解析SPEL 表达式
 *
 * @author wzhang
 */
public class SpelUtil {
    /**
     * 获取被拦截方法参数名列表(使用Spring支持类库)
     */
    private static LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();
    /**
     * 使用SPEL进行key的解析
     */
    private static ExpressionParser parser = new SpelExpressionParser();

    public static String parse(String spel, Method method, Object[] args) {
        // 获取被拦截方法参数名列表(使用Spring支持类库)
        String[] paraNameArr = discoverer.getParameterNames(method);

        // SPEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 把方法参数放入SPEL上下文中
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        try {
            return parser.parseExpression(spel).getValue(context, String.class);
        } catch (Exception e) {
            return spel;
        }
    }

    /**
     * 支持 #p0 参数索引的表达式解析
     *
     * @param rootObject 根对象,method 所在的对象
     * @param spel       表达式
     * @param method     ，目标方法
     * @param args       方法入参
     * @return 解析后的字符串
     */
    public static String parse(Object rootObject, String spel, Method method, Object[] args) {
        // 获取被拦截方法参数名列表(使用Spring支持类库)
        String[] paraNameArr = discoverer.getParameterNames(method);

        // SPEL上下文
        StandardEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, args, discoverer);
        // 把方法参数放入SPEL上下文中
        for (int i = 0; i < paraNameArr.length; i++) {
            context.setVariable(paraNameArr[i], args[i]);
        }
        try {
            return parser.parseExpression(spel).getValue(context, String.class);
        } catch (Exception e) {
            return spel;
        }
    }
}
