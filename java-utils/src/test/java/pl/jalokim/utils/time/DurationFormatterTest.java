package pl.jalokim.utils.time;

import org.junit.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.time.DurationFormatter.formatDuration;

public class DurationFormatterTest {

    @Test
    public void prettyTimeTestAsDays() {
        // given
        long time = (11 * 24 * 60 * 60 * 1000) + 342 + (5 * 1000) + (1000 * 60 * 11);
        String result = formatDuration(time);
        // then
        assertThat("11 days 0 hours 11 minutes 5 seconds 342 milliseconds").isEqualTo(result);
    }

    @Test
    public void prettyTimeTestAsMinutes() {
        // given
        long time = 64 * 1000 + 121;
        String result = formatDuration(time);
        // then
        assertThat("1 minutes 4 seconds 121 milliseconds").isEqualTo(result);
    }

    @Test
    public void prettyTimeTestAsOneMinute() {
        // given
        long time = 60 * 1000 + 345;
        String result = formatDuration(time);
        // then
        assertThat("1 minutes 0 seconds 345 milliseconds").isEqualTo(result);
    }

    @Test
    public void formatNativeDuration() {
        // given
        Duration duration = Duration.ofDays(12).plusMillis(20);
        // when
        String result = formatDuration(duration);
        // then
        assertThat("12 days 0 hours 0 minutes 0 seconds 20 milliseconds").isEqualTo(result);
    }
}