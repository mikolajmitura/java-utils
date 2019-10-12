package pl.jalokim.utils.test;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

public class ExpectedNestedErrorUtil<T> extends ExpectedErrorUtil<T> {

    public ExpectedNestedErrorUtil(ThrowableRunnable instruction,
                                   Class<? extends Throwable> expectedExceptionType,
                                   T expectedMessage,
                                   Function<T, String> messageBuilder,
                                   BiConsumer<Throwable, T> assertionFunction) {
        super(instruction, expectedExceptionType, expectedMessage, messageBuilder, assertionFunction);
    }

    void assertCaughtException(Throwable exception) {
        Throwable currentEx = exception;
        WrappedAssertionError assertionErrorForMessage = null;
        while(currentEx != null) {
            if(expectedExceptionType.isInstance(currentEx)) {
                try {
                    assertionFunction.accept(currentEx, expectedMessage);
                    caughtException = currentEx;
                    return;
                } catch(WrappedAssertionError original) {
                    assertionErrorForMessage = original;
                }
            }
            currentEx = currentEx.getCause();
        }

        ofNullable(assertionErrorForMessage)
                .ifPresent(assertionError -> {
                    System.err.println("stacktrace for original caught exception:");
                    assertionError.getOriginalCause().printStackTrace();
                    throw assertionError;
                });

        throw new AssertionError("Cannot find any nested expected type : " + expectedExceptionType.getCanonicalName()
                                 + " for caught exception: ", exception);
    }
}
