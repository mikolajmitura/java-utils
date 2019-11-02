package pl.jalokim.utils.iteration;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class IterationUtilsTest {

    @Test
    public void repeatNTimesWithoutIndex() {
        // given
        AtomicInteger atomicInteger = new AtomicInteger();
        // when
        IterationUtils.repeatNTimes(5, atomicInteger::incrementAndGet);
        // then
        assertThat(atomicInteger.get()).isEqualTo(5);
    }

    @Test
    public void repeatNTimesWithIndex() {
        // given
        AtomicInteger atomicInteger = new AtomicInteger();
        // when
        IterationUtils.repeatNTimes(5, index ->
                assertThat(index).isEqualTo(atomicInteger.getAndIncrement()));
        // then
        assertThat(atomicInteger.get()).isEqualTo(5);
    }
}