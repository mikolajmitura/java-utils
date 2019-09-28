package pl.jalokim.utils.reflection;

public class ReflectionOperationException extends RuntimeException {
    public ReflectionOperationException(Throwable cause) {
        super(cause);
    }

    public ReflectionOperationException(String message) {
        super(message);
    }

    public ReflectionOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
