package pl.jalokim.utils.test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Useful for assert test which throws exception with certain text, with certain lines of test or exactly expected message.
 */
@RequiredArgsConstructor
@Setter
class ExpectedErrorUtil<T> {

    private final AtomicBoolean exceptionNotThrown = new AtomicBoolean(false);
    private final ThrowableRunnable instruction;
    @Getter
    private final Class<? extends Throwable> expectedExceptionType;
    @Getter
    private final T expectedMessage;
    private final Function<T, String> messageBuilder;
    @Getter
    private final BiConsumer<Throwable, T> assertionFunction;
    private Throwable caughtException;

    Throwable getCaughtException() {
        return caughtException;
    }

    Throwable invokeTest() {
        try {
            instruction.invoke();
            exceptionNotThrown.set(true);
        } catch (Throwable exception) {
            assertCaughtException(exception);
        }
        if (exceptionNotThrown.get()) {
            throw new AssertionError("Nothing was thrown! Expected exception: " +
                expectedExceptionType.getCanonicalName() +
                messageBuilder.apply(expectedMessage));
        }
        return caughtException;
    }

    @SuppressWarnings({"PMD.SystemPrintln", "PMD.AvoidPrintStackTrace"})
    void assertCaughtException(Throwable exception) {
        if (expectedExceptionType.isInstance(exception)) {
            caughtException = exception;
            try {
                assertionFunction.accept(caughtException, expectedMessage);
            } catch (WrappedAssertionError assertionError) {
                System.err.println("stacktrace for original caught exception:");
                assertionError.getOriginalCause().printStackTrace();
                throw assertionError;
            }
        } else {
            throw new AssertionError("Expected exception type: " + expectedExceptionType.getCanonicalName() +
                " but was caught another! " + exception, exception);
        }
    }
}
