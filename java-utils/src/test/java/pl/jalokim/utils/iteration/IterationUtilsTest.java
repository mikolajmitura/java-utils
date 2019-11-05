package pl.jalokim.utils.iteration;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.iteration.IterationUtils.repeatNTimes;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

public class IterationUtilsTest {

    @Test
    public void repeatNTimesWithoutIndex() {
        // given
        AtomicInteger atomicInteger = new AtomicInteger();
        // when
        repeatNTimes(5, atomicInteger::incrementAndGet);
        // then
        assertThat(atomicInteger.get()).isEqualTo(5);
    }

    @Test
    public void repeatNTimesWithIndex() {
        // given
        AtomicInteger atomicInteger = new AtomicInteger();
        // when
        repeatNTimes(5, index ->
                assertThat(index).isEqualTo(atomicInteger.getAndIncrement()));
        // then
        assertThat(atomicInteger.get()).isEqualTo(5);
    }

    @Test
    public void rethrowOriginalRuntimeException() {
        when(() ->
                     repeatNTimes(5, index -> {
                         assertThat(index).isEqualTo(0);
                         throw new SomeRuntimeException("unexpected Runtime");
                     })
            ).thenException(SomeRuntimeException.class, "unexpected Runtime");
    }

    @Test
    public void rethrowOriginalWrappedException() {
        when(() ->
                     repeatNTimes(5, index -> {
                         assertThat(index).isEqualTo(0);
                         throw new SomeException("unexpected Runtime");
                     })
            ).thenException(IterationException.class)
        .thenNestedException(SomeException.class, "unexpected Runtime");
    }

    private static class SomeRuntimeException extends RuntimeException {
        public SomeRuntimeException(String message) {
            super(message);
        }
    }

    private static class SomeException extends Exception {
        public SomeException(String message) {
            super(message);
        }
    }
}