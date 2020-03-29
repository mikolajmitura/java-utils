package pl.jalokim.utils.time;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static pl.jalokim.utils.collection.CollectionUtils.isLastIndex;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.SPACE;
import static pl.jalokim.utils.string.StringUtils.concatElements;

/**
 * Class for format time duration.
 */
public final class DurationFormatter {

    private DurationFormatter() {

    }

    /**
     * It format time duration in millis as human readable format.
     *
     * @param duration time as java duration
     * @return returns duration in format 1 days, 2 minutes, 12 seconds, 112 milliseconds
     */
    public static String formatDuration(Duration duration) {
        return formatDuration(duration.toMillis());
    }

    /**
     * It format time duration in millis as human readable format.
     *
     * @param durationInMs time as long
     * @return returns duration in format 1 days, 2 minutes, 12 seconds, 112 milliseconds
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public static String formatDuration(long durationInMs) {
        List<Long> timeSequence = new ArrayList<>();
        List<Integer> timeDividers = new ArrayList<>(asList(1000, 60, 60, 24));
        List<String> timeLabels = asList("milliseconds", "seconds", "minutes", "hours", "days");

        timeDividers.add(1);
        long currentTime = durationInMs;
        for (int i = 0; i < timeDividers.size(); i++) {
            Integer timeDivider = timeDividers.get(i);
            if (currentTime >= timeDivider && !isLastIndex(timeDividers, i)) {
                long earlierTime = currentTime;
                currentTime = currentTime / timeDivider;
                long restOfTime = earlierTime - (currentTime * timeDivider);
                if (restOfTime == timeDivider) {
                    currentTime = 1;
                } else {
                    timeSequence.add(restOfTime);
                }
            } else {
                timeSequence.add(currentTime);
                break;
            }
        }

        List<String> timeAsList = new ArrayList<>();
        elements(timeSequence).forEach((index, aLong) -> {
            timeAsList.add(aLong.toString() + SPACE + timeLabels.get(index));
        });
        Collections.reverse(timeAsList);
        return concatElements(timeAsList, SPACE);
    }
}
