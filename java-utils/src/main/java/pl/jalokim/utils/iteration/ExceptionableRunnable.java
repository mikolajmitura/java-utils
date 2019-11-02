package pl.jalokim.utils.iteration;

/**
 * This runnable can rise some Throwable instance.
 */
@FunctionalInterface
public interface ExceptionableRunnable {
    void invoke() throws Exception;
}
