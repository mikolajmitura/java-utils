package pl.jalokim.utils.test;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Builder for ExpectedErrorUtil.
 */
@SuppressWarnings("PMD.AccessorMethodGeneration")
@RequiredArgsConstructor
public class ExpectedErrorUtilBuilder {

    static final String WITH_MESSAGE = " with message: ";
    private static final String NEW_LINE = String.format("%n");

    private static final Function<Object, String> EMPTY_MESSAGE_BUILDER = m -> "";
    private static final BiConsumer<Throwable, Object> ASSERTION_NULL_MSG = (t, m) -> {
    };


    private final ThrowableRunnable instruction;

    public static ExpectedErrorUtilBuilder when(ThrowableRunnable instruction) {
        return new ExpectedErrorUtilBuilder(instruction);
    }

    /**
     * This method checks that expected exception is the same type, and contains the same message type.
     *
     * @param expectedException instance of expected exception. (for verify exception will only check exception type and message, without nested exceptions and their messages)
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    @FULLY_TESTED
    public AfterAssertion thenException(Throwable expectedException) {
        return thenException(expectedException.getClass(), expectedException.getMessage());
    }

    @FULLY_TESTED
    public AfterAssertion thenException(Class<? extends Throwable> exceptionType) {
        return buildExpectedErrorUtil(exceptionType, null,
                                      EMPTY_MESSAGE_BUILDER,
                                      ASSERTION_NULL_MSG);
    }

    @FULLY_TESTED
    public AfterAssertion thenException(Class<? extends Throwable> exceptionType, String exactlyExpectedMessage) {
        return buildExpectedErrorUtil(exceptionType, exactlyExpectedMessage,
                                      ExpectedErrorUtilBuilder::buildExpectedExMessage,
                                      ExpectedErrorUtilBuilder::assertCaughtExceptionMessage);
    }

    @FULLY_TESTED
    public AfterAssertion thenException(Class<? extends Throwable> exceptionType, String... expectedLinesInAnyOrder) {
        return buildExpectedErrorUtil(exceptionType, asList(expectedLinesInAnyOrder),
                                      ExpectedErrorUtilBuilder::buildExpectedExMessage,
                                      ExpectedErrorUtilBuilder::assertExceptionAndMessageLines);
    }

    @FULLY_TESTED
    public AfterAssertion thenNestedException(Throwable expectedException) {
        return thenNestedException(expectedException.getClass(), expectedException.getMessage());
    }

    public AfterAssertion thenNestedException(Class<? extends Throwable> exceptionType) {
        return buildNestedExpectedErrorUtil(exceptionType, null,
                                      EMPTY_MESSAGE_BUILDER,
                                      ASSERTION_NULL_MSG);
    }

    public AfterAssertion thenNestedException(Class<? extends Throwable> exceptionType, String exactlyExpectedMessage) {
        return buildNestedExpectedErrorUtil(exceptionType, exactlyExpectedMessage,
                                            ExpectedErrorUtilBuilder::buildExpectedExMessage,
                                            ExpectedErrorUtilBuilder::assertCaughtExceptionMessage);
    }

    public AfterAssertion thenNestedException(Class<? extends Throwable> exceptionType, String... expectedLinesInAnyOrder) {
        return buildNestedExpectedErrorUtil(exceptionType, asList(expectedLinesInAnyOrder),
                                      ExpectedErrorUtilBuilder::buildExpectedExMessage,
                                      ExpectedErrorUtilBuilder::assertExceptionAndMessageLines);
    }

    private <T> AfterAssertion buildExpectedErrorUtil(Class<? extends Throwable> expectedExceptionType, T expectedMessage,
                                                      Function<T, String> messageBuilder,
                                                      BiConsumer<Throwable, T> assertionFunction) {
        return new AfterAssertion(new ExpectedErrorUtil<>(instruction, expectedExceptionType,
                                                          expectedMessage, messageBuilder,
                                                          assertionFunction).invokeTest());
    }

    private <T> AfterAssertion buildNestedExpectedErrorUtil(Class<? extends Throwable> expectedExceptionType, T expectedMessage,
                                                            Function<T, String> messageBuilder,
                                                            BiConsumer<Throwable, T> assertionFunction) {
        return new AfterAssertion(new ExpectedNestedErrorUtil<>(instruction, expectedExceptionType,
                                                                expectedMessage, messageBuilder,
                                                                assertionFunction).invokeTest());
    }

    static String buildExpectedExMessage(String msg) {
        return WITH_MESSAGE + msg;
    }

    static String buildExpectedExMessage(List<String> expectedLinesInAnyOrder) {
        return " with message lines: " + messageLines(expectedLinesInAnyOrder);
    }

    static void assertCaughtExceptionMessage(Throwable caughtException, String expectedMessage) {
        try {
            assertThat(expectedMessage).isEqualTo(caughtException.getMessage());
        } catch(AssertionError original) {
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
        } catch(AssertionError original) {
            throw new WrappedAssertionError(String.format("Caught expected exception type: %s but has another message lines than expected",
                                                   thrownThrowable.getClass().getCanonicalName()),
                                     original, thrownThrowable);
        }
    }

    static String messageLines(List<String> lines) {
        return join(NEW_LINE + ",", lines);
    }

    // TODO nested exception on this level too 4 like below
    // TODO message contains on this level too for nested and firstException
    // TODO message to lambda on this level too for nested and firstException
}

