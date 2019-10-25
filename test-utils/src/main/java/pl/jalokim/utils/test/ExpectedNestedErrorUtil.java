package pl.jalokim.utils.test;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

class ExpectedNestedErrorUtil<T> extends ExpectedErrorUtil<T> {

    ExpectedNestedErrorUtil(ThrowableRunnable instruction,
                            Class<? extends Throwable> expectedExceptionType,
                            T expectedMessage,
                            Function<T, String> messageBuilder,
                            BiConsumer<Throwable, T> assertionFunction) {
        super(instruction, expectedExceptionType, expectedMessage, messageBuilder, assertionFunction);
    }

    @SuppressWarnings("PMD.SystemPrintln")
    void assertCaughtException(Throwable exception) {
        Throwable currentEx = exception;
        WrappedAssertionError assertionErrorForMessage = null;
        while (currentEx != null) {
            if (getExpectedExceptionType().isInstance(currentEx)) {
                try {
                    getAssertionFunction().accept(currentEx, getExpectedMessage());
                    setCaughtException(currentEx);
                    return;
                } catch (WrappedAssertionError original) {
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

        throw new AssertionError("Cannot find any nested expected type : " + getExpectedExceptionType().getCanonicalName()
                                 + " for caught exception: ", exception);
    }
}
