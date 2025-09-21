package com.eon.common.datasource.support;

import com.eon.common.datasource.annotation.UseDataSource;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

/**
 * 基于注解的切面，进入方法前推入目标数据源，执行完成后弹出。
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class DynamicDataSourceAspect {

    private final DataSourceContextHolder contextHolder;
    private final boolean strict;
    private final Set<String> availableKeys;
    private final Logger log = LoggerFactory.getLogger(DynamicDataSourceAspect.class);

    public DynamicDataSourceAspect(DataSourceContextHolder contextHolder, boolean strict, Set<String> availableKeys) {
        this.contextHolder = contextHolder;
        this.strict = strict;
        this.availableKeys = availableKeys;
    }

    @Around("@annotation(com.eon.common.datasource.annotation.UseDataSource) || @within(com.eon.common.datasource.annotation.UseDataSource)")
    public Object switchDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        UseDataSource useDataSource = resolveAnnotation(joinPoint);
        if (useDataSource == null) {
            return joinPoint.proceed();
        }
        String targetKey = useDataSource.value();
        if (!availableKeys.contains(targetKey)) {
            if (strict) {
                throw new IllegalStateException("未找到名称为 " + targetKey + " 的数据源");
            }
            log.warn("目标数据源 {} 未注册，将回退到默认数据源", targetKey);
            return joinPoint.proceed();
        }
        contextHolder.push(targetKey);
        try {
            return joinPoint.proceed();
        } finally {
            contextHolder.pop();
        }
    }

    private UseDataSource resolveAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        UseDataSource methodAnno = AnnotationUtils.findAnnotation(signature.getMethod(), UseDataSource.class);
        if (methodAnno != null) {
            return methodAnno;
        }
        Class<?> targetClass = joinPoint.getTarget() != null
                ? AopProxyUtils.ultimateTargetClass(joinPoint.getTarget())
                : signature.getDeclaringType();
        return AnnotationUtils.findAnnotation(targetClass, UseDataSource.class);
    }
}
