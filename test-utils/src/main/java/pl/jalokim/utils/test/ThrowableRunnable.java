package pl.jalokim.utils.test;

/**
 * This runnable can rise some Throwable instance.
 */
@FunctionalInterface
@SuppressWarnings("IllegalThrows")
public interface ThrowableRunnable {
    void invoke() throws Throwable;
}
