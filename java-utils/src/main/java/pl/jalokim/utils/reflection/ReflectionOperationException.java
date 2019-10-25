package pl.jalokim.utils.reflection;

/**
 * Exception which can be thrown during invoke operations on reflection API.
 * Mostly this is wrapper for exception throw by native java reflection API.
 */
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
