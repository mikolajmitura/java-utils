package pl.jalokim.utils.test;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Useful for assert test which throws exception with certain text, with certain lines of test or exactly expected message.
 */
@RequiredArgsConstructor
@Getter
@Setter
class ExpectedErrorUtil<T> {

    protected final ThrowableRunnable instruction;
    protected final Class<? extends Throwable> expectedExceptionType;
    protected final T expectedMessage;
    protected final Function<T, String> messageBuilder;
    protected final BiConsumer<Throwable, T> assertionFunction;

    protected Throwable caughtException;
    private final AtomicBoolean exceptionNotThrown = new AtomicBoolean(false);

    Throwable invokeTest() {
        try {
            instruction.invoke();
            exceptionNotThrown.set(true);
        } catch(Throwable exception) {
            assertCaughtException(exception);
        }
        if(exceptionNotThrown.get()) {
            throw new AssertionError("Nothing was thrown! Expected exception: "
                                     + expectedExceptionType.getCanonicalName()
                                     + messageBuilder.apply(expectedMessage));
        }
        return caughtException;
    }

    void assertCaughtException(Throwable exception) {
        if(expectedExceptionType.isInstance(exception)) {
            caughtException = exception;
            try {
                assertionFunction.accept(caughtException, expectedMessage);
            } catch(WrappedAssertionError assertionError) {
                System.err.println("stacktrace for original caught exception:");
                assertionError.getOriginalCause().printStackTrace();
                throw assertionError;
            }
        } else {
            throw new AssertionError("Expected exception type: " + expectedExceptionType.getCanonicalName()
                                     + " but was caught another! " + exception, exception);
        }
    }
}
