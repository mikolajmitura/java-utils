package pl.jalokim.utils.test;

import lombok.RequiredArgsConstructor;

import static java.util.Arrays.asList;
import static pl.jalokim.utils.test.ExpectedErrorUtil.messageLines;

/**
 * Builder for ExpectedErrorUtil.
 */
@SuppressWarnings("PMD.AccessorMethodGeneration")
@RequiredArgsConstructor
public class ExpectedErrorUtilBuilder {

    private final ThrowableRunnable instruction;
    private Class<? extends Throwable> expectedExceptionType;

    public static ExpectedErrorUtilBuilder when(ThrowableRunnable instruction) {
        return new ExpectedErrorUtilBuilder(instruction);
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
        ExpectedErrorUtil expectedErrorUtil = new ExpectedErrorUtil(instruction, expectedExceptionType, exactlyExpectedMessage);
        return new AfterAssertion(expectedErrorUtil
                                          .invokeTest()
                                          .assertCaughtExceptionMessage(exactlyExpectedMessage));
    }

    @TESTED
    public AfterAssertion thenException(Class<? extends Throwable> exceptionType, String... expectedLinesInAnyOrder) {
        this.expectedExceptionType = exceptionType;
        ExpectedErrorUtil expectedErrorUtil = new ExpectedErrorUtil(instruction, expectedExceptionType, messageLines(expectedLinesInAnyOrder));
        return new AfterAssertion(expectedErrorUtil
                                          .invokeTest()
                                          .assertCaughtExceptionMessage(asList(expectedLinesInAnyOrder)));
    }

//        private AfterAssertion


    // TODO nested exception on this level too 4 like below
    // TODO message contains on this level too for nested and firstException
    // TODO message to lambda on this level too for nested and firstException
}

