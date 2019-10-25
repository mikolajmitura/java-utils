package pl.jalokim.utils.test;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

final class AssertionErrorUtils {

    static final Function<Object, String> EMPTY_MESSAGE_BUILDER = m -> "";
    static final BiConsumer<Throwable, Object> ASSERTION_NULL_MSG = (t, m) -> {
    };
    static final String WITH_MESSAGE = " with message: ";
    private static final String NEW_LINE = String.format("%n");

    private AssertionErrorUtils() {

    }

    static String buildExpectedExMessage(String msg) {
        return WITH_MESSAGE + msg;
    }

    static String buildExpectedExMessage(List<String> expectedLinesInAnyOrder) {
        return " with message lines: " + messageLines(expectedLinesInAnyOrder);
    }

    static String buildExpectedExContainsMessage(String msg) {
        return " which contains text: " + msg;
    }

    static void assertCaughtExceptionContainsMessage(Throwable caughtException, String containsText) {
        try {
            assertThat(caughtException.getMessage()).contains(containsText);
        } catch (AssertionError original) {
            throw new WrappedAssertionError(String.format("Caught expected exception type: %s but doesn't contain expected text",
                                                          caughtException.getClass().getCanonicalName()),
                                            original, caughtException);
        }
    }

    static void assertCaughtExceptionMessage(Throwable caughtException, String expectedMessage) {
        try {
            assertThat(expectedMessage).isEqualTo(caughtException.getMessage());
        } catch (AssertionError original) {
            throw new WrappedAssertionError(String.format("Caught expected exception type: %s but has another message than expected",
                                                          caughtException.getClass().getCanonicalName()),
                                            original, caughtException);
        }
    }

    static void assertExceptionAndMessageLines(Throwable thrownThrowable, List<String> expectedLinesMessage) {
        List<String> linesFromException = asList(thrownThrowable.getMessage().split(NEW_LINE));
        Collections.sort(linesFromException);
        Collections.sort(expectedLinesMessage);
        try {
            assertThat(linesFromException).isEqualTo(expectedLinesMessage);
        } catch (AssertionError original) {
            throw new WrappedAssertionError(String.format("Caught expected exception type: %s but has another message lines than expected",
                                                          thrownThrowable.getClass().getCanonicalName()),
                                            original, thrownThrowable);
        }
    }

    static String messageLines(List<String> lines) {
        return join(NEW_LINE + ",", lines);
    }
}
