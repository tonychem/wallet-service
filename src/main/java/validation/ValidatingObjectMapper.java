package validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.ConstraintViolationException;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Custom Jackson Object Mapper, определяющий единственный метод - validateValue
 */
public class ValidatingObjectMapper extends ObjectMapper {
    /**
     * Метод, транслирующий входящий JSON объект в Java объект, с последующей валидацией.
     *
     * @param content   входящий JSON объект в виде строки
     * @param valueType соответствующий JSON объекту Java тип
     * @return T валидированный POJO
     * @throws ConstraintViolationException при нарушении валидации
     */
    public <T> T validateValue(String content, Class<T> valueType)
            throws JsonProcessingException {
        T instance = super.readValue(content, valueType);
        validate(instance);
        return instance;
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
            if (value instanceof Collection s && s.isEmpty()) {
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
