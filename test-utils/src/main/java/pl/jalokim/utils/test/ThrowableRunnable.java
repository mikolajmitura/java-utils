package pl.jalokim.utils.test;

/**
 * This runnable can rise some Throwable instance.
 */
@FunctionalInterface
public interface ThrowableRunnable {
    void invoke() throws Throwable;
}
