package pl.jalokim.utils.test;

import lombok.Getter;

/**
 * Wrapper class for AssertionError, it contains original Exception thrown by test and AssertionError
 * which is thrown from test utils api.
 */
@Getter
public class WrappedAssertionError extends AssertionError {

    private final Throwable originalCause;

    public WrappedAssertionError(String message, AssertionError cause, Throwable originalCause) {
        super(message, cause);
        this.originalCause = originalCause;
    }

}
