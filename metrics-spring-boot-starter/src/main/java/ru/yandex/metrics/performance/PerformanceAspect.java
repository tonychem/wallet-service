package ru.yandex.metrics.performance;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Аспект, ответственный за замер времени исполнения методов контроллера. Вывод - в консоль.
 */
@Aspect
@Component
public class PerformanceAspect {
    @Pointcut("@within(ru.yandex.metrics.performance.Performance)")
    public void annotatedByPerformanceOnClassLevel() {
    }

    @Pointcut("execution(public * *(..))")
    public void anyPublicMethod() {
    }

    @Around("annotatedByPerformanceOnClassLevel() && anyPublicMethod()")
    public Object measurePerformanceTime(ProceedingJoinPoint pjp) throws Throwable {
        try {
            long millisBefore = System.currentTimeMillis();
            System.out.println("Entering method: " + pjp.getSignature().getName());

            Object result = pjp.proceed();
            long millisAfter = System.currentTimeMillis();

            System.out.println("Execution of method: " + pjp.getSignature().getName() + " has finished. " +
                    "Execution time is " + (millisAfter - millisBefore) + " ms.");
            return result;
        } catch (Throwable t) {
            System.out.println("Failed to execute method " + pjp.getSignature().getName());
            throw t;
        }
    }
}
