package pl.jalokim.utils.iteration;

/**
 * Consumer which can throw some Exception.
 */
@FunctionalInterface
public interface ExceptionableConsumer {
    void consume(int iterationIndex) throws Exception;
}
