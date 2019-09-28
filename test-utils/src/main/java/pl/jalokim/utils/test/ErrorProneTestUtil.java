package pl.jalokim.utils.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Useful for assert test which throws exception with certain text, with certain lines of test or exactly expected message.
 */
@RequiredArgsConstructor(access = PRIVATE)
@Slf4j
public class ErrorProneTestUtil {

    private static final String NEW_LINE = String.format("%n");
    private static final String WITH_MESSAGE = " with message: ";

    private final ThrowableRunnable instruction;
    private final Class<? extends Throwable> expectedExceptionType;
    private final String expectedMessage;
    private final AtomicBoolean instructionPassed = new AtomicBoolean(false);
    private Throwable caughtException;

    private ErrorProneTestUtil invokeTest() {
        try {
            instruction.invoke();
            instructionPassed.set(true);
        } catch (Throwable exception) {
            if (expectedExceptionType.isInstance(exception)) {
                caughtException = exception;
            } else {
                log.error("not expected exception", exception);
                throw new AssertionError("Expected exception type: " + expectedExceptionType.getCanonicalName()
                                         + " but was caught another! " + exception, exception);
            }
        }
        if (instructionPassed.get()) {
            throw new AssertionError("Nothing was thrown! Expected exception: " + expectedExceptionType.getCanonicalName()
                                     + WITH_MESSAGE + expectedMessage);
        }
        return this;
    }

    // TODO nested exception on this level too
    // TODO message contains on this level too
    // TODO message to lambda on this level too
    private Throwable assertCaughtException(Throwable expectedException) {
        return assertCaughtExceptionMessage(expectedException.getMessage());
    }

    private Throwable assertCaughtExceptionMessage(String expectedMessage) {
        assertThat(expectedMessage).isEqualTo(caughtException.getMessage());
        return caughtException;
    }

    private Throwable assertCaughtExceptionMessage(List<String> expectedLinesMessage) {
        assertExceptionAndMessageLines(caughtException, expectedLinesMessage);
        return caughtException;
    }

    public static void assertExceptionAndMessageLines(Throwable thrownThrowable, List<String> expectedLinesMessage) {
        List<String> linesFromException = asList(thrownThrowable.getMessage().split(NEW_LINE));
        Collections.sort(linesFromException);
        Collections.sort(expectedLinesMessage);
        assertThat(linesFromException).isEqualTo(expectedLinesMessage);
    }

    public static String messageLines(String... lines) {
        return messageLines(asList(lines));
    }

    public static String messageLines(List<String> lines) {
        return join(NEW_LINE, lines);
    }

    /**
     * Builder for ErrorProneTestUtil.
     */
    @SuppressWarnings("PMD.AccessorMethodGeneration")
    public static class ErrorProneTestUtilBuilder {

        private ThrowableRunnable instruction;
        private Class<? extends Throwable> expectedExceptionType;

        public static ErrorProneTestUtilBuilder when(ThrowableRunnable instruction) {
            ErrorProneTestUtilBuilder builder = new ErrorProneTestUtilBuilder();
            builder.instruction = instruction;
            return builder;
        }

        public AfterAssertion thenExpectedException(Throwable expectedException) {
            this.expectedExceptionType = expectedException.getClass();
            ErrorProneTestUtil errorProneTestUtil = new ErrorProneTestUtil(instruction, expectedExceptionType, expectedException.getMessage());
            return new AfterAssertion(errorProneTestUtil
                                              .invokeTest()
                                              .assertCaughtException(expectedException));
        }

        public AfterAssertion thenExpectedException(Class<? extends Throwable> exceptionType, String expectedMessage) {
            this.expectedExceptionType = exceptionType;
            ErrorProneTestUtil errorProneTestUtil = new ErrorProneTestUtil(instruction, expectedExceptionType, expectedMessage);
            return new AfterAssertion(errorProneTestUtil
                                              .invokeTest()
                                              .assertCaughtExceptionMessage(expectedMessage));
        }

        public AfterAssertion thenExpectedException(Class<? extends Throwable> exceptionType, String... expectedLines) {
            this.expectedExceptionType = exceptionType;
            ErrorProneTestUtil errorProneTestUtil = new ErrorProneTestUtil(instruction, expectedExceptionType, messageLines(expectedLines));
            return new AfterAssertion(errorProneTestUtil
                                              .invokeTest()
                                              .assertCaughtExceptionMessage(asList(expectedLines)));
        }

        public AfterAssertion thenExpectedException(Class<? extends Throwable> exceptionType) {
            this.expectedExceptionType = exceptionType;
            ErrorProneTestUtil errorProneTestUtil = new ErrorProneTestUtil(instruction, expectedExceptionType, null);
            return new AfterAssertion(errorProneTestUtil
                                              .invokeTest().caughtException);
        }
    }

    /**
     * Useful after correct assertion of exception. In then you can get thrown exception and check others things in this exception.
     */
    @RequiredArgsConstructor(access = PRIVATE)
    @SuppressWarnings("PMD.AccessorMethodGeneration")
    public static class AfterAssertion {
        private final Throwable caughtException;

        public AfterAssertion then(Consumer<Throwable> exAssertion) {
            exAssertion.accept(caughtException);
            return this;
        }

        // TODO add for text contains and text lambda
        public AfterAssertion assertNestedException(Class<? extends Throwable> exceptionType, String... expectedLines) {
            Throwable currentEx = caughtException;
            while (currentEx != null) {
                if (exceptionType.isInstance(currentEx)) {
                    assertExceptionAndMessageLines(currentEx, asList(expectedLines));
                    return this;
                }
                currentEx = currentEx.getCause();
            }
            throw new AssertionError("Cannot find nested expected type : " + exceptionType.getCanonicalName() + WITH_MESSAGE + join("", expectedLines) + " for caught exception: ", caughtException);
        }
    }

    /**
     * This runnable can rise some Throwable instance.
     */
    @FunctionalInterface
    @SuppressWarnings("IllegalThrows")
    public interface ThrowableRunnable {
        void invoke() throws Throwable;
    }
}
