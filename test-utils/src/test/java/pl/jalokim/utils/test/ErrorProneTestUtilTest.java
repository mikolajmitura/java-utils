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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static pl.jalokim.utils.test.ErrorProneTestUtil.ErrorProneTestUtilBuilder.when;

@RunWith(MockitoJUnitRunner.class)
public class ErrorProneTestUtilTest {

    private static final String WITH_MESSAGE = " with message: ";
    private static final String EXPECTED_MSG = "expected Message";
    private static final String EXPECTED_AS_SET_MSG = "expected as set!";
    private static final String COMPOSED_EXCEPTION_MESSAGE = "Composed Exception message";

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

    @Test
    public void willAssertExceptedErrorAfterExceptionWhenExpectedConcreteException() throws Exception {
        // given
        OwnException expectedException = new OwnException(EXPECTED_MSG);

        // when
        when(() -> someTestableObject.validate(true, EXPECTED_MSG))
                .thenExpectedException(expectedException)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isNotSameAs(expectedException);
                          assertThat(caughtEx).isInstanceOf(OwnException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(true), eq(EXPECTED_MSG));
    }

    @Test
    public void willAssertExceptedErrorAfterExceptionWhenExpectedClassException() throws Exception {
        // given
        OwnException expectedException = new OwnException(EXPECTED_MSG);

        // when
        when(() -> someTestableObject.validate(true, EXPECTED_MSG))
                .thenExpectedException(OwnException.class)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isNotSameAs(expectedException);
                          assertThat(caughtEx).isInstanceOf(OwnException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(true), eq(EXPECTED_MSG));
    }

    @Test
    public void willAssertExceptedErrorAfterExceptionWhenExpectedClassExceptionAndMessageLines() throws Exception {
        // given
        String messageWithLines = String.format("first line%nsecond line%nthird line");
        OwnException expectedException = new OwnException(messageWithLines);

        // when
        when(() -> someTestableObject.validate(true, messageWithLines))
                .thenExpectedException(OwnException.class, "first line", "third line", "second line")
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isNotSameAs(expectedException);
                          assertThat(caughtEx).isInstanceOf(OwnException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(true), eq(messageWithLines));
    }

    @Test
    public void willAssertExceptedErrorMsgAfterException() throws Exception {
        // when
        when(() -> someTestableObject.validate(true, EXPECTED_MSG))
                .thenExpectedException(OwnException.class, EXPECTED_MSG)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(OwnException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(true), eq(EXPECTED_MSG));
    }

    @Test
    public void assertNestedExceptionAfterThrownException() throws Exception {
        // when
        when(() -> someTestableObject.validate(true, true, EXPECTED_MSG))
                .thenExpectedException(MainException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(MainException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .assertNestedException(OwnException.class, EXPECTED_MSG);

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(true), eq(true), eq(EXPECTED_MSG));
    }

    @Test
    public void cannotFindExpectedNestedExceptionAfterThrownException() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(true, true, EXPECTED_MSG))
                    .thenExpectedException(MainException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(MainException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .assertNestedException(IllegalArgumentException.class, EXPECTED_MSG);
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find nested expected type : " + IllegalArgumentException.class.getCanonicalName() + WITH_MESSAGE + EXPECTED_MSG + " for caught exception: ");
        }
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(true), eq(true), eq(EXPECTED_MSG));
    }

    @Test
    public void exceptedThrownExceptionButTestedCasePassedWithoutProblems() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(false, EXPECTED_MSG))
                    .thenExpectedException(OwnException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + OwnException.class.getCanonicalName() + WITH_MESSAGE + EXPECTED_MSG);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(false), eq(EXPECTED_MSG));
        }
    }

    @Test
    public void thrownAnotherExceptionThanExpected() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(null, EXPECTED_MSG))
                    .thenExpectedException(OwnException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
        } catch(AssertionError assertionError) {

            RuntimeException runtimeException = new RuntimeException(EXPECTED_AS_SET_MSG);
            AssertionError expected = new AssertionError("Expected exception type: " + OwnException.class.getCanonicalName()
                                                         + " but was caught another! " + runtimeException, runtimeException);

            // then
            assertThat(assertionError.getMessage()).isEqualTo(expected.getMessage());
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(isNull(), eq(EXPECTED_MSG));
        }
    }

    private interface MockObject {
        void invokeMethod();
    }


    @RequiredArgsConstructor
    private static class TestableInstance {
        private final MockObject mockObject;

        void validate(Boolean throwEx, String message) throws OwnException, MainException {
            validate(throwEx, false, message);
        }

        void validate(Boolean throwEx, Boolean composedEx, String message) throws OwnException, MainException {
            if(throwEx == null) {
                throw new RuntimeException(EXPECTED_AS_SET_MSG);
            }
            if(throwEx) {
                if(composedEx) {
                    throw new MainException(new RuntimeException(new OwnException(message)));
                }
                throw new OwnException(message);
            }
            mockObject.invokeMethod();
        }
    }

    private static class OwnException extends Exception {

        private static final long serialVersionUID = -2263869279725601058L;

        public OwnException(String message) {
            super(message);
        }
    }

    private static class MainException extends Exception {

        private static final long serialVersionUID = -2263869279725601058L;

        public MainException(Throwable cause) {
            super(COMPOSED_EXCEPTION_MESSAGE, cause);
        }
    }
}