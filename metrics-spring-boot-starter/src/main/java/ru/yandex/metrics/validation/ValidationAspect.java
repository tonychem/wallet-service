package ru.yandex.metrics.validation;

import model.dto.exception.ConstraintViolationException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Аспект, ответственный за валидацию сущностей в контроллерах, приходящих от клиента
 */
@Aspect
@Component
public class ValidationAspect {

    @Pointcut("@within(org.springframework.web.bind.annotation.RestController)")
    public void isControllerLayer() {
    }

    @Before("isControllerLayer()")
    public void validateValue(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        for (Object obj : args) {
            validate(obj);
        }
    }

    /**
     * Валидирует DTO объекты, приходящие от клиента. Для упрощения, считается, что все поля этих сущностей не должны
     * быть null, коллекции - не должны быть пустыми, а строки - пустыми или состоящими из пробелов.
     */
    private <T> boolean validate(T instance) {
        Field[] fields = instance.getClass().getDeclaredFields();

        try {
            for (Field field : fields) {
                field.setAccessible(true);

                Object value = field.get(instance);

                if (value != null) {
                    if (value instanceof String s && (s.isEmpty() || s.isBlank())) {
                        throw new ConstraintViolationException(field.getName() + " is either empty or blank!");
                    } else if (value instanceof Collection c && c.isEmpty()) {
                        throw new ConstraintViolationException(field.getName() + " is empty!");
                    }
                } else {
                    throw new ConstraintViolationException(field.getName() + " is null!");
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
