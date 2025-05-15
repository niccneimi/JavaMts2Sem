package com.example.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Aspect
@Component
public class ControllerLoggingAspect {

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void logMethodName(org.aspectj.lang.JoinPoint joinPoint) {
        System.out.println("Вызов метода: " + joinPoint.getSignature().toShortString());
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object measureExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        Instant start = Instant.now();
        Object result = pjp.proceed();
        Instant end = Instant.now();
        System.out.println("Время выполнения " + pjp.getSignature().toShortString() + ": " +
                Duration.between(start, end).toMillis() + " мс");
        return result;
    }
}
