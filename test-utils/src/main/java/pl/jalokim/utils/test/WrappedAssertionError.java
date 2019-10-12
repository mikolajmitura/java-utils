package pl.jalokim.utils.test;

import lombok.Getter;

@Getter
public class WrappedAssertionError extends AssertionError {

    private Throwable originalCause;

    public WrappedAssertionError(String message, AssertionError cause, Throwable originalCause) {
        super(message, cause);
        this.originalCause = originalCause;
    }

}
