package ru.yandex.metrics.performance;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация включает использование замеров времени исполнения всех публичных методов, аннотированных @Performance
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(PerformanceConfig.class)
public @interface EnablePerformance {
}
