package pl.jalokim.utils.iteration;

/**
 * Another way of iterate code.
 */
public final class IterationUtils {

    private IterationUtils() {

    }

    public static void repeatNTimes(int nTimes, ExceptionableRunnable runnable) {
        repeatNTimes(nTimes, (index) -> runnable.invoke());
    }

    @SuppressWarnings({"IllegalCatch", "PMD.AvoidRethrowingException"})
    public static void repeatNTimes(int nTimes, ExceptionableConsumer consumer) {
        for (int i = 0; i < nTimes; i++) {
            try {
                consumer.consume(i);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception throwable) {
                throw new IterationException(throwable);
            }
        }
    }
}
