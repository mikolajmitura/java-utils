package pl.jalokim.utils.test;


import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Useful for assert test which throws exception with certain text, with certain lines of test or exactly expected message.
 */
@RequiredArgsConstructor
class ExpectedErrorUtil {

    static final String WITH_MESSAGE = " with message: ";
    private static final String NEW_LINE = String.format("%n");

    private final ThrowableRunnable instruction;
    private final Class<? extends Throwable> expectedExceptionType;
    private final String expectedMessage;
    private final AtomicBoolean exceptionNotThrown = new AtomicBoolean(false);
    private Throwable caughtException;

    ExpectedErrorUtil invokeTest() {
        try {
            instruction.invoke();
            exceptionNotThrown.set(true);
        } catch(Throwable exception) {
            if(expectedExceptionType.isInstance(exception)) {
                caughtException = exception;
            } else {
                throw new AssertionError("Expected exception type: " + expectedExceptionType.getCanonicalName()
                                         + " but was caught another! " + exception, exception);
            }
        }
        if(exceptionNotThrown.get()) {
            String restMsg = expectedMessage == null ? "" : WITH_MESSAGE + expectedMessage;
            throw new AssertionError("Nothing was thrown! Expected exception: " + expectedExceptionType.getCanonicalName()
                                     + restMsg);
        }
        return this;
    }

    Throwable assertCaughtExceptionMessage(String expectedMessage) {
        if(expectedMessage != null) {
            try {
                assertThat(expectedMessage).isEqualTo(caughtException.getMessage());
            } catch(AssertionError original) {
                System.err.println("stacktrace for original caught exception:");
                caughtException.printStackTrace();
                throw new AssertionError(String.format("Caught expected exception type: %s but has another message than expected",
                                                       caughtException.getClass().getCanonicalName()),
                                         original);
            }
        }
        return caughtException;
    }

    Throwable assertCaughtExceptionMessage(List<String> expectedLinesMessage) {
        assertExceptionAndMessageLines(caughtException, expectedLinesMessage);
        return caughtException;
    }

    static void assertExceptionAndMessageLines(Throwable thrownThrowable, List<String> expectedLinesMessage) {
        List<String> linesFromException = asList(thrownThrowable.getMessage().split(NEW_LINE));
        Collections.sort(linesFromException);
        Collections.sort(expectedLinesMessage);
        try {
            assertThat(linesFromException).isEqualTo(expectedLinesMessage);
        } catch(AssertionError original) {
            System.err.println("stacktrace for original caught exception:");
            thrownThrowable.printStackTrace();
            throw new AssertionError(String.format("Caught expected exception type: %s but has another message lines than expected",
                                                   thrownThrowable.getClass().getCanonicalName()),
                                     original);
        }
    }

    static String messageLines(String... lines) {
        return messageLines(asList(lines));
    }

    static String messageLines(List<String> lines) {
        return join(NEW_LINE, lines);
    }
}
