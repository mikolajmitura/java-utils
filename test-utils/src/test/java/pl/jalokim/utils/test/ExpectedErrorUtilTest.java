package pl.jalokim.utils.test;

import lombok.RequiredArgsConstructor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

@RunWith(MockitoJUnitRunner.class)
public class ExpectedErrorUtilTest {

    private static final String EXPECTED_MSG = "expected Message";
    private static final String EXPECTED_AS_SET_MSG = "INVALID STATE OF METHOD";
    private static final String COMPOSED_EXCEPTION_MESSAGE = "Composed Exception message";
    private static final String ANOTHER_MESSAGE_NOT_EXPECTED = "another message (not expected)";

    @Mock
    private MockObject mockInTestableInstance;
    @Mock
    private MockObject assertionChecker;

    private TestableInstance someTestableObject;
    private PrintStream originalSystemErrStream;
    private SpyPrintStream spyPrintStream;

    @Before
    public void setUp() {
        someTestableObject = spy(new TestableInstance(mockInTestableInstance));
        originalSystemErrStream = System.err;
        spyPrintStream = new SpyPrintStream(originalSystemErrStream);
        System.setErr(spyPrintStream);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(assertionChecker, mockInTestableInstance);
        System.setErr(originalSystemErrStream);
        spyPrintStream.assertThatChecked();
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
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
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
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isTrue();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, ANOTHER_MESSAGE_NOT_EXPECTED)).isTrue();
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
            assertThat(assertionError.getCause().getMessage()).isEqualTo(EXPECTED_AS_SET_MSG);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(null), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
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
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + SimpleException.class.getCanonicalName() + " with message: " + EXPECTED_MSG);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
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
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
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
            assertThat(assertionError.getCause().getMessage()).isEqualTo(EXPECTED_AS_SET_MSG);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(null), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
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
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
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
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
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
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isTrue();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, ANOTHER_MESSAGE_NOT_EXPECTED)).isTrue();
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
            assertThat(assertionError.getCause().getMessage()).isEqualTo(EXPECTED_AS_SET_MSG);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(null), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
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
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + SimpleException.class.getCanonicalName() + " with message: " + EXPECTED_MSG);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
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
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, messageWithLines)).isFalse();
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
            assertThat(assertionError.getMessage()).isEqualTo("Caught expected exception type: pl.jalokim.utils.test.ExpectedErrorUtilTest.SimpleException but has another message lines than expected");
            assertThat(assertionError.getCause().getMessage()).isEqualTo("expected:<[\"first line\", \"[]third line\"]> but was:<[\"first line\", \"[second line\", \"]third line\"]>");
            assertThat(assertionError.getCause() instanceof AssertionError).isTrue();
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.SIMPLE_EX), eq(messageWithLines));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isTrue();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, messageWithLines)).isTrue();
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
            assertThat(assertionError.getCause().getMessage()).isEqualTo(EXPECTED_AS_SET_MSG);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(null), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
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
                                                              + " with message lines: " + String.format("first line%n,third line%n,second line"));
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
            throw new NotHappyPathException();
        }
    }


    /*
       tests for thenNestedException(with exception instance)
    */
    @Test
    public void thenNestedExceptionWithExceptionInstancePassCorrectlyWithoutNestedException() throws Exception {
        // given
        SimpleException expectedException = new SimpleException(EXPECTED_MSG);

        // when
        when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, EXPECTED_MSG))
                .thenNestedException(expectedException)
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
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test
    public void thenNestedExceptionWithExceptionInstancePassCorrectly() throws Exception {
        // given
        SimpleException expectedException = new SimpleException(EXPECTED_MSG);

        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                .thenNestedException(expectedException)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isNotSameAs(expectedException);
                          assertThat(caughtEx).isInstanceOf(SimpleException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test
    public void thenNestedExceptionWithExceptionInstancePassCorrectlyWithDeeperOne() throws Exception {
        // given
        SimpleException expectedException = new SimpleException(EXPECTED_MSG);

        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, EXPECTED_MSG))
                .thenNestedException(expectedException)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isNotSameAs(expectedException);
                          assertThat(caughtEx).isInstanceOf(SimpleException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SUPER_COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithExceptionInstanceButNotExpectedMessage() throws Exception {
        SimpleException expectedException = new SimpleException(EXPECTED_MSG);
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, ANOTHER_MESSAGE_NOT_EXPECTED))
                    .thenNestedException(expectedException)
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
            verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(ANOTHER_MESSAGE_NOT_EXPECTED));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isTrue();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, ANOTHER_MESSAGE_NOT_EXPECTED)).isTrue();
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithExceptionInstanceButCannotFindNestedExceptionInCaughtEx() throws Exception {
        IllegalArgumentException expectedException = new IllegalArgumentException(EXPECTED_MSG);
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                    .thenNestedException(expectedException)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getMessage()).contains(EXPECTED_MSG);
        }
        verify(assertionChecker, never()).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithExceptionInstanceButThrownNothing() throws Exception {
        SimpleException expectedException = new SimpleException(EXPECTED_MSG);
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenNestedException(expectedException)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch(AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + SimpleException.class.getCanonicalName() + " with message: expected Message");
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
            throw new NotHappyPathException();
        }
    }

    // TODO next here

    /*
       tests for thenNestedException(with Exception class)
    */

    // TODO nested second level etc
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
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
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
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find nested expected type : " + IllegalArgumentException.class.getCanonicalName() + " with message: " + EXPECTED_MSG + " for caught exception: ");
            assertThat(assertionError.getCause().getCause().getMessage()).contains(EXPECTED_MSG);
        }
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
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

            if(resultType == ResultType.SUPER_COMPOSED_EX) {
                throw new ComposedException(new RuntimeException(new SimpleException("problematic one!", new SimpleException(message))));
            }

            if(resultType == ResultType.SIMPLE_EX) {
                throw new SimpleException(message);

            }
            throw new RuntimeException(EXPECTED_AS_SET_MSG);
        }
    }

    private enum ResultType {
        SUPER_COMPOSED_EX, COMPOSED_EX, SIMPLE_EX, NONE
    }

    private static class NotHappyPathException extends RuntimeException {

    }

    private static class SimpleException extends Exception {

        private static final long serialVersionUID = -2263869279725601058L;

        public SimpleException(String message) {
            super(message);
        }

        public SimpleException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private static class ComposedException extends Exception {

        private static final long serialVersionUID = -2263869279725601058L;

        public ComposedException(Throwable cause) {
            super(COMPOSED_EXCEPTION_MESSAGE, cause);
        }
    }

    public class SpyPrintStream extends PrintStream {

        private List<String> caughtPrintln = new ArrayList<>();
        private List<String> caughtPrint = new ArrayList<>();
        private volatile boolean checkedPrintln = false;
        private volatile boolean checkedPrint = false;

        public SpyPrintStream(OutputStream out) {
            super(out);
        }

        @Override
        public void println(String x) {
            caughtPrintln.add(x);
            super.println(x);
        }

        @Override
        public void print(String x) {
            caughtPrint.add(x);
            super.print(x);
        }

        void assertThatChecked() {
            if(!(checkedPrintln && checkedPrint)) {
                throw new AssertionError("Should be checked, not invoked method containsExceptionAndMessage and containsInfoAboutStackTrace");
            }
        }

        boolean containsInfoAboutStackTrace() {
            checkedPrintln = true;
            return caughtPrintln.stream()
                                .anyMatch(text -> text.contains("stacktrace for original caught exception:"));
        }

        boolean containsExceptionAndMessage(Class<? extends Throwable> throwableClass, String msgText) {
            checkedPrint = true;
            return caughtPrint.stream()
                              .anyMatch(text -> text.contains(throwableClass.getName() + ": " + msgText));
        }

        boolean noProblemAboutExceptionLogged() {
            checkedPrint = true;
            return caughtPrint.isEmpty();
        }
    }
}