package pl.jalokim.utils.test;

import lombok.RequiredArgsConstructor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static pl.jalokim.utils.test.ErrorProneTestUtil.ErrorProneTestUtilBuilder.when;

@RunWith(MockitoJUnitRunner.class)
public class ErrorProneTestUtilTest {

    private static final String WITH_MESSAGE = " with message: ";
    private static final String EXPECTED_MSG = "expected Message";
    private static final String EXPECTED_AS_SET_MSG = "INVALID STATE OF METHOD";
    private static final String COMPOSED_EXCEPTION_MESSAGE = "Composed Exception message";
    private static final String ANOTHER_MESSAGE_NOT_EXPECTED = "another message (not expected)";

    @Mock
    private MockObject mockInTestableInstance;
    @Mock
    private MockObject assertionChecker;

    private TestableInstance someTestableObject;

    @Before
    public void setUp() {
        someTestableObject = spy(new TestableInstance(mockInTestableInstance));
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(assertionChecker, mockInTestableInstance);
    }

    /*

       test format
       - positive test
       - thrown another message
       - thrown another exception
       - nothing thrown

     */

    /*
        tests for thenException(with exception instance)
     */
    @Test
    public void thenExpectedExceptionWithExceptionInstancePassCorrectly() throws Exception {
        // given
        SimpleException expectedException = new SimpleException(EXPECTED_MSG);

        // when
        when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, EXPECTED_MSG))
                .thenException(expectedException)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isNotSameAs(expectedException);
                          assertThat(caughtEx).isInstanceOf(SimpleException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SIMPLE_EX), eq(EXPECTED_MSG));
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionWithExceptionInstanceButNotExpectedMessage() throws Exception {
        SimpleException expectedException = new SimpleException(EXPECTED_MSG);
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, ANOTHER_MESSAGE_NOT_EXPECTED))
                    .thenException(expectedException)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but has another message than expected",
                                                                            SimpleException.class.getCanonicalName()));
            assertThat(assertionError.getCause().getMessage()).isEqualTo("expected:<\"[another message (not expected)]\"> but was:<\"[expected Message]\">");
            assertThat(assertionError.getCause() instanceof AssertionError).isTrue();
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.SIMPLE_EX), eq(ANOTHER_MESSAGE_NOT_EXPECTED));
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionWithExceptionInstanceButThrownAnotherExceptionThanExpected() throws Exception {
        SimpleException expectedException = new SimpleException(EXPECTED_MSG);
        try {
            // when
            when(() -> someTestableObject.validate(null, EXPECTED_MSG))
                    .thenException(expectedException)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {

            RuntimeException runtimeException = new RuntimeException(EXPECTED_AS_SET_MSG);
            AssertionError expected = new AssertionError("Expected exception type: " + SimpleException.class.getCanonicalName()
                                                         + " but was caught another! " + runtimeException, runtimeException);

            // then
            assertThat(assertionError.getMessage()).isEqualTo(expected.getMessage());
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(null), eq(EXPECTED_MSG));
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionWithExceptionInstanceButThrownNothing() throws Exception {
        SimpleException expectedException = new SimpleException(EXPECTED_MSG);
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenException(expectedException)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + SimpleException.class.getCanonicalName() + WITH_MESSAGE + EXPECTED_MSG);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            throw new NotHappyPathException();
        }
    }

    /*
       tests for thenException(with Exception class)
    */
    @Test
    public void thenExpectedExceptionTypeOnlyPassCorrectly() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, EXPECTED_MSG))
                .thenException(SimpleException.class)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isInstanceOf(SimpleException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SIMPLE_EX), eq(EXPECTED_MSG));
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionTypeButThrownAnotherExceptionThanExpected() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(null, EXPECTED_MSG))
                    .thenException(SimpleException.class)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {

            RuntimeException runtimeException = new RuntimeException(EXPECTED_AS_SET_MSG);
            AssertionError expected = new AssertionError("Expected exception type: " + SimpleException.class.getCanonicalName()
                                                         + " but was caught another! " + runtimeException, runtimeException);

            // then
            assertThat(assertionError.getMessage()).isEqualTo(expected.getMessage());
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(null), eq(EXPECTED_MSG));
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionTypeButThrownNothing() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenException(SimpleException.class)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + SimpleException.class.getCanonicalName());
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            throw new NotHappyPathException();
        }
    }

    /*
         tests for thenException(with Exception class and expected message)
      */
    @Test
    public void thenExpectedExceptionTypeAndMsgPassCorrectly() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, EXPECTED_MSG))
                .thenException(SimpleException.class, EXPECTED_MSG)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(SimpleException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SIMPLE_EX), eq(EXPECTED_MSG));
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionTypeAndMsgButNotExpectedMessage() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, ANOTHER_MESSAGE_NOT_EXPECTED))
                    .thenException(SimpleException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {

            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but has another message than expected",
                                                                            SimpleException.class.getCanonicalName()));
            assertThat(assertionError.getCause().getMessage()).isEqualTo("expected:<\"[another message (not expected)]\"> but was:<\"[expected Message]\">");
            assertThat(assertionError.getCause() instanceof AssertionError).isTrue();
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.SIMPLE_EX), eq(ANOTHER_MESSAGE_NOT_EXPECTED));
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionTypeAndMsgButThrownAnotherExceptionThanExpected() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(null, EXPECTED_MSG))
                    .thenException(SimpleException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {

            RuntimeException runtimeException = new RuntimeException(EXPECTED_AS_SET_MSG);
            AssertionError expected = new AssertionError("Expected exception type: " + SimpleException.class.getCanonicalName()
                                                         + " but was caught another! " + runtimeException, runtimeException);

            // then
            assertThat(assertionError.getMessage()).isEqualTo(expected.getMessage());
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(null), eq(EXPECTED_MSG));
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionTypeAndMsgButThrownNothing() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenException(SimpleException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + SimpleException.class.getCanonicalName() + WITH_MESSAGE + EXPECTED_MSG);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            throw new NotHappyPathException();
        }
    }

    /*
      tests for thenException(with Exception class and lines)
   */
    @Test
    public void thenExpectedExceptionTypeAndMsgLinesPassCorrectly() throws Exception {
        // given
        String messageWithLines = String.format("first line%nsecond line%nthird line");
        SimpleException expectedException = new SimpleException(messageWithLines);

        // when
        when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, messageWithLines))
                .thenException(SimpleException.class, "first line", "third line", "second line")
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isNotSameAs(expectedException);
                          assertThat(caughtEx).isInstanceOf(SimpleException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SIMPLE_EX), eq(messageWithLines));
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionTypeAndMsgLinesButNotExpectedMessagesLines() throws Exception {
        // given
        String messageWithLines = String.format("first line%nsecond line%nthird line");
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, messageWithLines))
                    .thenException(SimpleException.class, "first line", "third line")
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Caught expected exception type: pl.jalokim.utils.test.ErrorProneTestUtilTest.SimpleException but has another message lines than expected");
            assertThat(assertionError.getCause().getMessage()).isEqualTo("expected:<[\"first line\", \"[]third line\"]> but was:<[\"first line\", \"[second line\", \"]third line\"]>");
            assertThat(assertionError.getCause() instanceof AssertionError).isTrue();
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.SIMPLE_EX), eq(messageWithLines));
            throw new NotHappyPathException();
        }
    }


    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionTypeAndMsgLinesButThrownAnotherExceptionThanExpected() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(null, EXPECTED_MSG))
                    .thenException(SimpleException.class, "first line", "third line", "second line")
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {

            RuntimeException runtimeException = new RuntimeException(EXPECTED_AS_SET_MSG);
            AssertionError expected = new AssertionError("Expected exception type: " + SimpleException.class.getCanonicalName()
                                                         + " but was caught another! " + runtimeException, runtimeException);

            // then
            assertThat(assertionError.getMessage()).isEqualTo(expected.getMessage());
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(null), eq(EXPECTED_MSG));
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionTypeAndMsgLinesButThrownNothing() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenException(SimpleException.class, "first line", "third line", "second line")
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + SimpleException.class.getCanonicalName()
                                                              + WITH_MESSAGE + String.format("first line%nthird line%nsecond line"));
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            throw new NotHappyPathException();
        }
    }

    // TODO nested etc
    @Test
    public void assertNestedExceptionAfterThrownException() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(SimpleException.class, EXPECTED_MSG);

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
    }

    @Test(expected = NotHappyPathException.class)
    public void cannotFindExpectedNestedExceptionAfterThrownException() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedException(IllegalArgumentException.class, EXPECTED_MSG);
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find nested expected type : " + IllegalArgumentException.class.getCanonicalName() + WITH_MESSAGE + EXPECTED_MSG + " for caught exception: ");
        }
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        throw new NotHappyPathException();
    }

    private interface MockObject {
        void invokeMethod();
    }


    @RequiredArgsConstructor
    private static class TestableInstance {
        private final MockObject mockObject;

        void validate(ResultType resultType, String message) throws SimpleException, ComposedException {
            if(resultType == ResultType.NONE) {
                mockObject.invokeMethod();
                return;
            }

            if(resultType == ResultType.COMPOSED_EX) {
                throw new ComposedException(new RuntimeException(new SimpleException(message)));
            }

            if(resultType == ResultType.SIMPLE_EX) {
                throw new SimpleException(message);

            }
            throw new RuntimeException(EXPECTED_AS_SET_MSG);
        }
    }

    private enum ResultType {
        COMPOSED_EX, SIMPLE_EX, NONE
    }

    private static class NotHappyPathException extends RuntimeException {

    }

    private static class SimpleException extends Exception {

        private static final long serialVersionUID = -2263869279725601058L;

        public SimpleException(String message) {
            super(message);
        }
    }

    private static class ComposedException extends Exception {

        private static final long serialVersionUID = -2263869279725601058L;

        public ComposedException(Throwable cause) {
            super(COMPOSED_EXCEPTION_MESSAGE, cause);
        }
    }
}