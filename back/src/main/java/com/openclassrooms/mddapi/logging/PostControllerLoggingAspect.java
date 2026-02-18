package com.openclassrooms.mddapi.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Principal;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class PostControllerLoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(PostControllerLoggingAspect.class);

    @Around("within(com.openclassrooms.mddapi.controller..*)")
    public Object logPostControllerCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        if (request.getMethod() == null || !"POST".equalsIgnoreCase(request.getMethod())) {
            return joinPoint.proceed();
        }

        HttpServletResponse response = attributes.getResponse();
        String requestUri = request.getRequestURI();
        String handler = joinPoint.getSignature().toShortString();

        Principal principal = request.getUserPrincipal();
        String user = (principal != null && principal.getName() != null) ? principal.getName() : "anonymous";

        long startNanos = System.nanoTime();
        try {
            logger.info("POST {} -> {} user={}", requestUri, handler, user);
            Object result = joinPoint.proceed();

            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            Integer status = (response != null) ? response.getStatus() : null;
            if (status != null) {
                logger.info("POST {} <- {} status={} durationMs={}", requestUri, handler, status, durationMs);
            } else {
                logger.info("POST {} <- {} durationMs={}", requestUri, handler, durationMs);
            }

            return result;
        } catch (Throwable ex) {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            logger.warn("POST {} !! {} durationMs={} errorType={} message={}", requestUri, handler, durationMs,
                    ex.getClass().getSimpleName(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
