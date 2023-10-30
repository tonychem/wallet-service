package ru.tonychem.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import ru.tonychem.exception.model.ConstraintViolationException;
import ru.tonychem.aop.annotations.validation.NotBlank;
import ru.tonychem.aop.annotations.validation.NotEmpty;
import ru.tonychem.aop.annotations.validation.NotNull;

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

    @Pointcut("@args(ru.tonychem.aop.annotations.validation.Validated)")
    public void entityAnnotatedWithValidated() {
    }

    @Before("isControllerLayer() && entityAnnotatedWithValidated()")
    public void validateValue(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();

        for (Object obj : args) {
            validate(obj);
        }
    }

    private <T> boolean validate(T instance) {
        Field[] fields = instance.getClass().getDeclaredFields();

        try {
            for (Field field : fields) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(NotBlank.class)) {
                    validateNotBlank(instance, field);
                }

                if (field.isAnnotationPresent(NotEmpty.class)) {
                    validateNotEmpty(instance, field);
                }

                if (field.isAnnotationPresent(NotNull.class)) {
                    validateNotNull(instance, field);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    private <T> boolean validateNotBlank(T instance, Field field) throws IllegalAccessException {
        Object value = field.get(instance);

        if (value != null) {
            if (value instanceof String s && (s.isEmpty() || s.isBlank())) {
                throw new ConstraintViolationException(field.getName() + " is either empty or blank!");
            }
        } else {
            throw new ConstraintViolationException(field.getName() + " is null!");
        }

        return true;
    }

    private <T> boolean validateNotEmpty(T instance, Field field) throws IllegalAccessException {
        Object value = field.get(instance);

        if (value != null) {
            if (value instanceof Collection c && c.isEmpty()) {
                throw new ConstraintViolationException(field.getName() + " is empty!");
            } else if (value instanceof String s && s.isEmpty()) {
                throw new ConstraintViolationException(field.getName() + " is empty!");
            }
        } else {
            throw new ConstraintViolationException(field.getName() + " is null!");
        }

        return true;
    }

    private <T> boolean validateNotNull(T instance, Field field) throws IllegalAccessException {
        Object value = field.get(instance);

        if (value == null) {
            throw new ConstraintViolationException(field.getName() + " is null!");
        }

        return true;
    }
}
