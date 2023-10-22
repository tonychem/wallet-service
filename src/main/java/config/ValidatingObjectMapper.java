package config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.*;

import java.util.Set;

/**
 * Custom Jackson Object Mapper, определяющий единственный метод - validateValue
 */
public class ValidatingObjectMapper extends ObjectMapper {
    private final Validator validator;

    public ValidatingObjectMapper() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.usingContext().getValidator();
        }
    }

    /**
     * Метод, транслирующий входящий JSON объект в Java объект, с последующей валидацией.
     *
     * @param content   входящий JSON объект в виде строки
     * @param valueType соответствующий JSON объекту Java тип
     * @return T валидированный POJO
     * @throws ConstraintViolationException при нарушении валидации
     */
    public <T> T validateValue(String content, Class<T> valueType)
            throws JsonProcessingException, JsonMappingException {
        T instance = super.readValue(content, valueType);
        validate(instance);
        return instance;
    }

    private <T> boolean validate(T instance) {
        Set<ConstraintViolation<T>> constraints = validator.validate(instance);

        if (constraints.isEmpty()) {
            return true;
        }

        throw new ConstraintViolationException(constraints);
    }
}
