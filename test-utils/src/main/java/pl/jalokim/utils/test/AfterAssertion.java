package pl.jalokim.utils.test;

import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.WITH_MESSAGE;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.assertExceptionAndMessageLines;

/**
 * Useful after correct assertion of exception. In then you can get thrown exception and check others things in this exception.
 */
@SuppressWarnings("PMD.AccessorMethodGeneration")
@RequiredArgsConstructor
public class AfterAssertion {
    private final Throwable caughtException;

    public AfterAssertion then(Consumer<Throwable> exAssertion) {
        exAssertion.accept(caughtException);
        return this;
    }

    // TODO what with nested exception with the same type but with another message???

    // TODO 5 methods for nested exception, but after nested exception assertion next "then" method wil return exception instance for nested exception.
    public AfterAssertion thenNestedException(Class<? extends Throwable> exceptionType, String... expectedLines) {
        Throwable currentEx = caughtException;
        while(currentEx != null) {
            if(exceptionType.isInstance(currentEx)) {
                assertExceptionAndMessageLines(currentEx, asList(expectedLines));
                return this;
            }
            currentEx = currentEx.getCause();
        }
        throw new AssertionError("Cannot find nested expected type : " + exceptionType.getCanonicalName() + WITH_MESSAGE + join("", expectedLines) + " for caught exception: ", caughtException);
    }
}
