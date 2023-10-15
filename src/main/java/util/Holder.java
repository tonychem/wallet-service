package util;

/**
 * Вспомогательный класс, который хранит единственное значение
 */
public class Holder<T> {
    private T value;

    public Holder(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public static <T> Holder<T> of(T value) {
        return new Holder<>(value);
    }
}
