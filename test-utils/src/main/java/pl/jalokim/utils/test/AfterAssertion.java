package pl.jalokim.utils.test;

import lombok.RequiredArgsConstructor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static pl.jalokim.utils.test.AssertionErrorUtils.ASSERTION_NULL_MSG;
import static pl.jalokim.utils.test.AssertionErrorUtils.EMPTY_MESSAGE_BUILDER;

/**
 * Useful after correct assertion of exception. In then you can get thrown exception and check others things in this exception.
 */
@RequiredArgsConstructor
public class AfterAssertion {
    private final Throwable caughtException;

    public AfterAssertion then(Consumer<Throwable> exAssertion) {
        exAssertion.accept(caughtException);
        return this;
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
        return createExpectedNestedErrorUtil(exceptionType, null,
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
        return createExpectedNestedErrorUtil(exceptionType,
                                             exactlyExpectedMessage,
                                             AssertionErrorUtils::buildExpectedExMessage,
                                             AssertionErrorUtils::assertCaughtExceptionMessage);
    }

    /**
     * This method checks that thrown exception contains expected exception with the same type and contains all provided message lines.
     *
     * @param exceptionType           type of nested exception.
     * @param expectedLinesInAnyOrder message lines of nested expected in thrown exception.
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenNestedException(Class<? extends Throwable> exceptionType, String... expectedLinesInAnyOrder) {
        return createExpectedNestedErrorUtil(exceptionType, asList(expectedLinesInAnyOrder),
                                             AssertionErrorUtils::buildExpectedExMessage,
                                             AssertionErrorUtils::assertExceptionAndMessageLines);
    }

    /**
     * This method checks that thrown exception contains expected exception with the expected type and contains some text from containsText arg.
     *
     * @param exceptionType type of expected exception.
     * @param containsText  message which is expected in thrown exception.
     * @return instance of AfterAssertion on which you can check nested exception etc...
     */
    public AfterAssertion thenNestedExceptionContainsMsg(Class<? extends Throwable> exceptionType, String containsText) {
        return createExpectedNestedErrorUtil(exceptionType, containsText,
                                            AssertionErrorUtils::buildExpectedExContainsMessage,
                                            AssertionErrorUtils::assertCaughtExceptionContainsMessage);
    }

    private <T> AfterAssertion createExpectedNestedErrorUtil(Class<? extends Throwable> exceptionType,
                                                             T expectedMessage,
                                                             Function<T, String> messageBuilder,
                                                             BiConsumer<Throwable, T> assertionFunction) {
        ExpectedNestedErrorUtil<T> tExpectedNestedErrorUtil = new ExpectedNestedErrorUtil<>(null,
                                                                                            exceptionType,
                                                                                            expectedMessage,
                                                                                            messageBuilder,
                                                                                            assertionFunction);
        tExpectedNestedErrorUtil.assertCaughtException(caughtException);
        return new AfterAssertion(tExpectedNestedErrorUtil.getCaughtException());
    }

}
