package pl.jalokim.utils.iteration;

/**
 * Wrapper exception for thrown exception from ExceptionableConsumer and ExceptionableRunnable.
 */
public class IterationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IterationException(Throwable cause) {
        super(cause);
    }
}
