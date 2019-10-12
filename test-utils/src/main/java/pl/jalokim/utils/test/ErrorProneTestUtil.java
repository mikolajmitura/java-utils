package pl.jalokim.utils.test;

import lombok.RequiredArgsConstructor;

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
        } catch(Throwable exception) {
            if(expectedExceptionType.isInstance(exception)) {
                caughtException = exception;
            } else {
                throw new AssertionError("Expected exception type: " + expectedExceptionType.getCanonicalName()
                                         + " but was caught another! " + exception, exception);
            }
        }
        if(instructionPassed.get()) {
            String restMsg = expectedMessage == null ? "" : WITH_MESSAGE + expectedMessage;
            throw new AssertionError("Nothing was thrown! Expected exception: " + expectedExceptionType.getCanonicalName()
                                     + restMsg);
        }
        return this;
    }

    private Throwable assertCaughtExceptionMessage(String expectedMessage) {
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

    private Throwable assertCaughtExceptionMessage(List<String> expectedLinesMessage) {
        assertExceptionAndMessageLines(caughtException, expectedLinesMessage);
        return caughtException;
    }

    private static void assertExceptionAndMessageLines(Throwable thrownThrowable, List<String> expectedLinesMessage) {
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

    private static String messageLines(String... lines) {
        return messageLines(asList(lines));
    }

    private static String messageLines(List<String> lines) {
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

        /**
         * This method checks that expected exception is the same type, and contains the same message type.
         *
         * @param expectedException instance of expected exception. (for verify exception will only check exception type and message, without nested exceptions and their messages)
         * @return instance of AfterAssertion on which you can check nested exception etc...
         */
        @TESTED
        public AfterAssertion thenException(Throwable expectedException) {
            return thenException(expectedException.getClass(), expectedException.getMessage());
        }

        @TESTED
        public AfterAssertion thenException(Class<? extends Throwable> exceptionType) {
            return thenException(exceptionType, (String) null);
        }

        @TESTED
        public AfterAssertion thenException(Class<? extends Throwable> exceptionType, String exactlyExpectedMessage) {
            this.expectedExceptionType = exceptionType;
            ErrorProneTestUtil errorProneTestUtil = new ErrorProneTestUtil(instruction, expectedExceptionType, exactlyExpectedMessage);
            return new AfterAssertion(errorProneTestUtil
                                              .invokeTest()
                                              .assertCaughtExceptionMessage(exactlyExpectedMessage));
        }

        @TESTED
        public AfterAssertion thenException(Class<? extends Throwable> exceptionType, String... expectedLinesInAnyOrder) {
            this.expectedExceptionType = exceptionType;
            ErrorProneTestUtil errorProneTestUtil = new ErrorProneTestUtil(instruction, expectedExceptionType, messageLines(expectedLinesInAnyOrder));
            return new AfterAssertion(errorProneTestUtil
                                              .invokeTest()
                                              .assertCaughtExceptionMessage(asList(expectedLinesInAnyOrder)));
        }


        // TODO nested exception on this level too 4 like below
        // TODO message contains on this level too for nested and firstException
        // TODO message to lambda on this level too for nested and firstException
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

        // TODO 5 methods for nested exception, but after nested exception assertion next "then" method wil return exception instance for nested exception.
        public AfterAssertion thenNestedException(Class<? extends Throwable> exceptionType, String... expectedLines) {
            Throwable currentEx = caughtException;
            while(currentEx != null) {
                if(exceptionType.isInstance(currentEx)) {
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
