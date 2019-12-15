package pl.jalokim.utils.reflection;

/**
 * Exception thrown when cannot resolve some generic type to real class.
 */
public class UnresolvedRealClassException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnresolvedRealClassException(String message, UnresolvedRealClassException cause) {
        super(message, cause);
    }

    public UnresolvedRealClassException(Throwable cause) {
        super(cause);
    }
}
