package pl.jalokim.utils.test;

import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static pl.jalokim.utils.test.AssertionErrorUtils.ASSERTION_NULL_MSG;
import static pl.jalokim.utils.test.AssertionErrorUtils.EMPTY_MESSAGE_BUILDER;

/**
 * Builder for ExpectedErrorUtil.
 */
@SuppressWarnings("PMD.AccessorMethodGeneration")
@RequiredArgsConstructor
public class ExpectedErrorUtilBuilder {

    private final ThrowableRunnable instruction;

    /**
     * pass some code which will throw some exception.
     *
     * @param instruction some code which can throw exception
     * @return instance of ExpectedErrorUtilBuilder on which you can assert thrown exception, message of them or
     * assert nested exception of thrown exception with message of them.
     */
    public static ExpectedErrorUtilBuilder when(ThrowableRunnable instruction) {
        return new ExpectedErrorUtilBuilder(instruction);
    }

    /**
     * This method checks that expected exception is the same type, and contains the same message type.
     *
     * @param expectedException instance of expected exception. (for verify exception will only check exception type and message, without nested exceptions and their messages)
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenException(Throwable expectedException) {
        return thenException(expectedException.getClass(), expectedException.getMessage());
    }

    /**
     * This method checks that expected exception is the same type.
     *
     * @param exceptionType type of expected exception. Exception message is not verified.
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenException(Class<? extends Throwable> exceptionType) {
        return buildExpectedErrorUtil(exceptionType, null,
                                      EMPTY_MESSAGE_BUILDER,
                                      ASSERTION_NULL_MSG);
    }

    /**
     * This method checks that expected exception is the same type, and contains the expected message type.
     *
     * @param exceptionType          type of expected exception.
     * @param exactlyExpectedMessage message which is expected in thrown exception.
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenException(Class<? extends Throwable> exceptionType, String exactlyExpectedMessage) {
        return buildExpectedErrorUtil(exceptionType, exactlyExpectedMessage,
                                      AssertionErrorUtils::buildExpectedExMessage,
                                      AssertionErrorUtils::assertCaughtExceptionMessage);
    }

    /**
     * This method checks that expected exception is the same type, and contains some text from containsText arg.
     *
     * @param exceptionType type of expected exception.
     * @param containsText  message which is expected in thrown exception.
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenExceptionContainsMsg(Class<? extends Throwable> exceptionType, String containsText) {
        return buildExpectedErrorUtil(exceptionType, containsText,
                                      AssertionErrorUtils::buildExpectedExContainsMessage,
                                      AssertionErrorUtils::assertCaughtExceptionContainsMessage);
    }

    /**
     * This method checks that expected exception is the same type, and contains all provided message lines.
     *
     * @param exceptionType           type of expected exception.
     * @param expectedLinesInAnyOrder message lines which is expected in thrown exception.
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenException(Class<? extends Throwable> exceptionType, String... expectedLinesInAnyOrder) {
        return buildExpectedErrorUtil(exceptionType, asList(expectedLinesInAnyOrder),
                                      AssertionErrorUtils::buildExpectedExMessage,
                                      AssertionErrorUtils::assertExceptionAndMessageLines);
    }

    /**
     * This method checks that thrown exception contains expected exception with the same type, and contains the same message type.
     *
     * @param expectedException instance of nested expected exception. (for verify exception will only check exception type and message, without nested exceptions and their messages)
     * @return instance of AfterAssertion on which you can check deeper nested exception etc...
     */
    public AfterAssertion thenNestedException(Throwable expectedException) {
        return thenNestedException(expectedException.getClass(), expectedException.getMessage());
    }

    /**
     * This method checks that thrown exception contains expected exception with the same type.
     *
     * @param exceptionType type of nested expected exception. Exception message is not verified.
     * @return instance of AfterAssertion on which you can check deeper nested exception etc...
     */
    public AfterAssertion thenNestedException(Class<? extends Throwable> exceptionType) {
        return buildNestedExpectedErrorUtil(exceptionType, null,
                                            EMPTY_MESSAGE_BUILDER,
                                            ASSERTION_NULL_MSG);
    }

    /**
     * This method checks that thrown exception contains expected exception with the same type, and contains the expected message type.
     *
     * @param exceptionType          type of nested exception.
     * @param exactlyExpectedMessage expected message of nested expected in thrown exception.
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenNestedException(Class<? extends Throwable> exceptionType, String exactlyExpectedMessage) {
        return buildNestedExpectedErrorUtil(exceptionType, exactlyExpectedMessage,
                                            AssertionErrorUtils::buildExpectedExMessage,
                                            AssertionErrorUtils::assertCaughtExceptionMessage);
    }

    /**
     * This method checks that thrown exception contains expected exception with the expected type and contains some text from containsText arg.
     *
     * @param exceptionType type of expected exception.
     * @param containsText  message which is expected in thrown exception.
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenNestedExceptionContainsMsg(Class<? extends Throwable> exceptionType, String containsText) {
        return buildNestedExpectedErrorUtil(exceptionType, containsText,
                                            AssertionErrorUtils::buildExpectedExContainsMessage,
                                            AssertionErrorUtils::assertCaughtExceptionContainsMessage);
    }

    /**
     * This method checks that thrown exception contains expected exception with the same type and contains all provided message lines.
     *
     * @param exceptionType           type of nested exception.
     * @param expectedLinesInAnyOrder message lines of nested expected in thrown exception.
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenNestedException(Class<? extends Throwable> exceptionType, String... expectedLinesInAnyOrder) {
        return buildNestedExpectedErrorUtil(exceptionType, asList(expectedLinesInAnyOrder),
                                            AssertionErrorUtils::buildExpectedExMessage,
                                            AssertionErrorUtils::assertExceptionAndMessageLines);
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
        return new AfterAssertion(new ExpectedNestedErrorUtil<>(instruction,
                                                                expectedExceptionType,
                                                                expectedMessage,
                                                                messageBuilder,
                                                                assertionFunction).invokeTest());
    }

}

