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
import java.util.Arrays;
import java.util.List;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;
import static pl.jalokim.utils.test.TemporaryTestResources.isWindows;

@RunWith(MockitoJUnitRunner.class)
public class ExpectedErrorUtilTest {

    private static final String PROBLEMATIC_ONE = "problematic one!";
    private static final String EXPECTED_MSG = "expected Message";
    private static final String EXPECTED_MSG_PART = "cted Mess";
    private static final String EXPECTED_AS_SET_MSG = "INVALID STATE OF METHOD";
    private static final String COMPOSED_EXCEPTION_MESSAGE = "Composed Exception message";
    private static final String ANOTHER_MESSAGE_NOT_EXPECTED = "another message (not expected)";
    public static final String ANOTHER_MSG = "Another MSG";

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
        } catch (AssertionError assertionError) {
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
        } catch (AssertionError assertionError) {

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
        } catch (AssertionError assertionError) {
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
        } catch (AssertionError assertionError) {

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
        } catch (AssertionError assertionError) {
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
        } catch (AssertionError assertionError) {

            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but has another message than expected",
                                                                            SimpleException.class.getCanonicalName()));

            assertThat(assertionError.getCause().getMessage())
                    .isEqualTo("expected:<\"[another message (not expected)]\"> but was:<\"[expected Message]\">");

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
        } catch (AssertionError assertionError) {

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
        } catch (AssertionError assertionError) {
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
      tests for thenException(with Exception class and contains message)
    */
    @Test
    public void thenExpectedExceptionTypeAndContainsMsgPassCorrectly() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, EXPECTED_MSG))
                .thenExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(SimpleException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SIMPLE_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenExpectedExceptionTypeAndContainsMsgButNotExpectedMessage() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.SIMPLE_EX, ANOTHER_MESSAGE_NOT_EXPECTED))
                    .thenExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {

            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but doesn't contain expected text",
                                                                            SimpleException.class.getCanonicalName()));

            assertThat(assertionError.getCause().getMessage()).isEqualTo(String.format("%n" +
                                                                                       "Expecting:%n" +
                                                                                       " <\"another message (not expected)\">%n" +
                                                                                       "to contain:%n" +
                                                                                       " <\"cted Mess\"> "));
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
    public void thenExpectedExceptionTypeAndContainsMsgButThrownAnotherExceptionThanExpected() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(null, EXPECTED_MSG))
                    .thenExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {

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
    public void thenExpectedExceptionTypeAndContainsMsgButThrownNothing() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + SimpleException.class.getCanonicalName() + " which contains text: " + EXPECTED_MSG_PART);
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
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Caught expected exception type: pl.jalokim.utils.test.ExpectedErrorUtilTest.SimpleException but has another message lines than expected");

            assertThat(assertionError.getCause().getMessage())
                    .isEqualTo(concatAsNewLines("expected:<\"first line",
                                                "[]third line\"> but was:<\"first line",
                                                "[second line",
                                                "]third line\">"));

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
        } catch (AssertionError assertionError) {

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
        } catch (AssertionError assertionError) {
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
        } catch (AssertionError assertionError) {
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
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(EXPECTED_MSG);
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
        } catch (AssertionError assertionError) {
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


    /*
       tests for thenNestedException(with Exception class)
    */

    @Test
    public void thenNestedExceptionWithTypeOnlyPassCorrectly() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                .thenNestedException(SimpleException.class)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
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
    public void thenNestedExceptionWithTypeOnlyPassCorrectlyWithNoDeeperOne() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, EXPECTED_MSG))
                .thenNestedException(SimpleException.class)
                .then(caughtEx ->
                      {
                          assertThat(caughtEx.getMessage()).isEqualTo(PROBLEMATIC_ONE);
                          assertionChecker.invokeMethod();
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
    public void thenNestedExceptionWithExceptionTypeOnlyButCannotFindNestedExceptionInCaughtEx() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                    .thenNestedException(IllegalArgumentException.class)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(EXPECTED_MSG);
        }
        verify(assertionChecker, never()).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithExceptionTypeOnlyButThrownNothing() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenNestedException(SimpleException.class)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
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
    tests for thenNestedException(with Exception class and expected message)
     */
    @Test
    public void thenNestedExceptionWithExceptionTypeAndMsgPassCorrectly() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                .thenNestedException(SimpleException.class, EXPECTED_MSG)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
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
    public void thenNestedExceptionWithExceptionTypeAndMsgCorrectlyWithDeeperOne() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, EXPECTED_MSG))
                .thenNestedException(SimpleException.class, EXPECTED_MSG)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
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
    public void thenNestedExceptionWithExceptionTypeAndMsgButNotExpectedMessage() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, ANOTHER_MESSAGE_NOT_EXPECTED))
                    .thenNestedException(SimpleException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but has another message than expected",
                                                                            SimpleException.class.getCanonicalName()));

            assertThat(assertionError.getCause().getMessage())
                    .isEqualTo("expected:<\"[another message (not expected)]\"> but was:<\"[expected Message]\">");

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
    public void thenNestedExceptionWithExceptionTypeAndMsgButCannotFindNestedExceptionInCaughtEx() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                    .thenNestedException(IllegalArgumentException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(EXPECTED_MSG);
        }
        verify(assertionChecker, never()).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithExceptionTypeAndMsgButThrownNothing() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenNestedException(SimpleException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
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

    /*
    tests for thenNestedExceptionContainsMsg(with Exception class and contains message)
     */
    @Test
    public void thenNestedExceptionWithExceptionTypeAndContainsMsgPassCorrectly() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                .thenNestedExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
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
    public void thenNestedExceptionWithExceptionTypeAndContainsMsgCorrectlyWithDeeperOne() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, EXPECTED_MSG))
                .thenNestedExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
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
    public void thenNestedExceptionWithExceptionTypeAndContainsMsgButNotExpectedMessage() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, ANOTHER_MESSAGE_NOT_EXPECTED))
                    .thenNestedExceptionContainsMsg(SimpleException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but doesn't contain expected text",
                                                                            SimpleException.class.getCanonicalName()));
            assertThat(assertionError.getCause().getMessage()).isEqualTo(String.format("%n"
                                                                                       + "Expecting:%n"
                                                                                       + " <\"another message (not expected)\">%n"
                                                                                       + "to contain:%n"
                                                                                       + " <\"expected Message\"> "));
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
    public void thenNestedExceptionWithExceptionTypeAndContainsMsgButCannotFindNestedExceptionInCaughtEx() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                    .thenNestedExceptionContainsMsg(IllegalArgumentException.class, EXPECTED_MSG_PART)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(EXPECTED_MSG);
        }
        verify(assertionChecker, never()).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithExceptionTypeAndContainsMsgButThrownNothing() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenNestedExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + SimpleException.class.getCanonicalName() + " which contains text: " + EXPECTED_MSG_PART);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
            throw new NotHappyPathException();
        }
    }

    /*
     tests for thenNestedException(with Exception class and lines)
    */
    @Test
    public void thenNestedExceptionWithExceptionTypeAndMsgLinesPassCorrectly() throws Exception {
        // given
        String messageWithLines = String.format("first line%nsecond line%nthird line");
        SimpleException expectedException = new SimpleException(messageWithLines);

        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, messageWithLines))
                .thenNestedException(SimpleException.class, "first line", "third line", "second line")
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isNotSameAs(expectedException);
                          assertThat(caughtEx).isInstanceOf(SimpleException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(messageWithLines));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test
    public void thenNestedExceptionWithExceptionTypeAndMsgLinesPassCorrectlyWithDeeperOne() throws Exception {
        // given
        String messageWithLines = String.format("first line%nsecond line%nthird line");

        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, messageWithLines))
                .thenNestedException(SimpleException.class, "first line", "third line", "second line")
                .then(caughtEx ->
                      {
                          assertionChecker.invokeMethod();
                          assertThat(caughtEx).isInstanceOf(SimpleException.class);
                          verify(mockInTestableInstance, never()).invokeMethod();
                      });

        // then
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SUPER_COMPOSED_EX), eq(messageWithLines));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithExceptionTypeAndMsgLinesButNotExpectedMessage() throws Exception {
        String messageWithLines = String.format("first line%nsecond line%nthird line");
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, messageWithLines))
                    .thenNestedException(SimpleException.class, "first line", "third line")
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but has another message lines than expected",
                                                                            SimpleException.class.getCanonicalName()));

            assertThat(assertionError.getCause().getMessage()).isEqualTo(concatAsNewLines("expected:<\"first line",
                                                                                          "[]third line\"> but was:<\"first line",
                                                                                          "[second line",
                                                                                          "]third line\">"));

            assertThat(assertionError.getCause() instanceof AssertionError).isTrue();
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(messageWithLines));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isTrue();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, messageWithLines)).isTrue();
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithExceptionTypeAndMsgLinesButCannotFindNestedExceptionInCaughtEx() throws Exception {
        String messageWithLines = String.format("first line%nsecond line%nthird line");
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, messageWithLines))
                    .thenNestedException(IllegalArgumentException.class, "first line", "third line", "second line")
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(messageWithLines);
        }
        verify(assertionChecker, never()).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(messageWithLines));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithExceptionTypeAndMsgLinesButThrownNothing() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenNestedException(SimpleException.class, EXPECTED_MSG)
                    .then(caughtEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
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

    /*
     tests for thenNestedException(with Exception instance) second level
   */
    @Test
    public void thenNestedExceptionInstanceInSecondLevelPositivePath() throws Exception {
        SimpleException expectedSimpleException = new SimpleException(EXPECTED_MSG);
        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(expectedSimpleException)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(expectedSimpleException).isNotSameAs(nestedEx);
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test
    public void thenNestedExceptionInstanceInSecondLevelForDeeperExceptionPositivePath() throws Exception {
        SimpleException expectedSimpleException = new SimpleException(EXPECTED_MSG);
        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(expectedSimpleException)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(expectedSimpleException).isNotSameAs(nestedEx);
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SUPER_COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionInstanceInSecondLevelButNotExpectedMessage() throws Exception {
        // given
        SimpleException expectedSimpleException = new SimpleException(EXPECTED_MSG);
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, ANOTHER_MESSAGE_NOT_EXPECTED))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedException(expectedSimpleException)
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but has another message than expected",
                                                                            SimpleException.class.getCanonicalName()));

            assertThat(assertionError.getCause().getMessage()).isEqualTo("expected:<\"[another message (not expected)]\"> but was:<\"[expected Message]\">");


            assertThat(assertionError.getCause() instanceof AssertionError).isTrue();
            verify(assertionChecker).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(ANOTHER_MESSAGE_NOT_EXPECTED));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isTrue();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, ANOTHER_MESSAGE_NOT_EXPECTED)).isTrue();
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionInstanceInSecondLevelButCannotFindNestedExceptionInCaughtEx() throws Exception {
        // given
        IllegalArgumentException expectedException = new IllegalArgumentException(EXPECTED_MSG);
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedException(expectedException)
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(EXPECTED_MSG);
        }
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }

    /*
      tests for thenNestedException(with Exception type) second level
    */
    @Test
    public void thenNestedExceptionTypeInSecondLevelPositivePath() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(SimpleException.class)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx.getMessage()).isEqualTo(EXPECTED_MSG);
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test
    public void thenNestedExceptionTypeInSecondLevelForDeeperExceptionPositivePath() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(SimpleException.class)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx.getMessage()).isEqualTo(PROBLEMATIC_ONE);
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SUPER_COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionTypeInSecondLevelButCannotFindNestedExceptionInCaughtEx() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedException(IllegalArgumentException.class)
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(EXPECTED_MSG);
        }
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }

    /*
      tests for thenNestedException(with Exception class and msg text) second level
    */
    @Test
    public void thenNestedExceptionWithMessageInSecondLevelPositivePath() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(SimpleException.class, EXPECTED_MSG)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test
    public void thenNestedExceptionWithMessageInSecondLevelForDeeperExceptionPositivePath() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(SimpleException.class, EXPECTED_MSG)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SUPER_COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithMessageInSecondLevelButNotExpectedMessage() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, ANOTHER_MESSAGE_NOT_EXPECTED))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedException(SimpleException.class, EXPECTED_MSG)
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but has another message than expected",
                                                                            SimpleException.class.getCanonicalName()));

            assertThat(assertionError.getCause().getMessage()).isEqualTo("expected:<\"[another message (not expected)]\"> but was:<\"[expected Message]\">");
            assertThat(assertionError.getCause() instanceof AssertionError).isTrue();
            verify(assertionChecker).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(ANOTHER_MESSAGE_NOT_EXPECTED));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isTrue();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, ANOTHER_MESSAGE_NOT_EXPECTED)).isTrue();
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithMessageInSecondLevelButCannotFindNestedExceptionInCaughtEx() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedException(IllegalArgumentException.class, EXPECTED_MSG)
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(EXPECTED_MSG);
        }
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }


    /*
      tests for thenNestedExceptionContainsMsg second level
    */
    @Test
    public void thenNestedExceptionContainsMsgInSecondLevelPositivePath() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test
    public void thenNestedExceptionContainsMsgInSecondLevelForDeeperExceptionPositivePath() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SUPER_COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionContainsMsgInSecondLevelButNotExpectedMessage() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, ANOTHER_MESSAGE_NOT_EXPECTED))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedExceptionContainsMsg(SimpleException.class, EXPECTED_MSG_PART)
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but doesn't contain expected text",
                                                                            SimpleException.class.getCanonicalName()));
            assertThat(assertionError.getCause().getMessage()).isEqualTo(String.format("%n" +
                                                                                       "Expecting:%n" +
                                                                                       " <\"another message (not expected)\">%n" +
                                                                                       "to contain:%n" +
                                                                                       " <\"cted Mess\"> "));
            assertThat(assertionError.getCause() instanceof AssertionError).isTrue();
            verify(assertionChecker).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(ANOTHER_MESSAGE_NOT_EXPECTED));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isTrue();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, ANOTHER_MESSAGE_NOT_EXPECTED)).isTrue();
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionContainsMsgInSecondLevelButCannotFindNestedExceptionInCaughtEx() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, EXPECTED_MSG))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedExceptionContainsMsg(IllegalArgumentException.class, EXPECTED_MSG)
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(EXPECTED_MSG);
        }
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }

    /*
      tests for thenNestedException(exception type and message lines) second level
    */
    @Test
    public void thenNestedExceptionTypeAndMsgLinesInSecondLevelPositivePath() throws Exception {
        // given
        String messageWithLines = String.format("first line%nsecond line%nthird line");

        // when
        when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, messageWithLines))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(SimpleException.class, "second line", "first line", "third line")
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(messageWithLines));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test
    public void thenNestedExceptionTypeAndMsgLinesInSecondLevelForDeeperExceptionPositivePath() throws Exception {
        // given
        String messageWithLines = String.format("first line%nsecond line%nthird line");

        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, messageWithLines))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(SimpleException.class, "second line", "first line", "third line")
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(2)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SUPER_COMPOSED_EX), eq(messageWithLines));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionTypeAndMsgLinesInSecondLevelButNotExpectedMessage() throws Exception {
        // given
        String messageWithLines = String.format("first line%nsecond line%nthird line");
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, messageWithLines))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedException(SimpleException.class, "second line", "first line")
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo(String.format("Caught expected exception type: %s but has another message lines than expected",
                                                                            SimpleException.class.getCanonicalName()));

            String stPart = isWindows() ? "st" : "rst";
            assertThat(assertionError.getCause().getMessage()).isEqualTo(concatAsNewLines("expected:<..." + stPart +" line",
                                                                                          "second line[]\"> but was:<..." + stPart +" line",
                                                                                          "second line[",
                                                                                          "third line]\">"));

            assertThat(assertionError.getCause() instanceof AssertionError).isTrue();
            verify(assertionChecker).invokeMethod();
            verify(mockInTestableInstance, never()).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(messageWithLines));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isTrue();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, messageWithLines)).isTrue();
            throw new NotHappyPathException();
        }
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionTypeAndMsgLinesInSecondLevelButCannotFindNestedExceptionInCaughtEx() throws Exception {
        // given
        String messageWithLines = String.format("first line%nsecond line%nthird line");
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.COMPOSED_EX, messageWithLines))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedException(IllegalArgumentException.class, messageWithLines)
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );
            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Cannot find any nested expected type : java.lang.IllegalArgumentException for caught exception: ");
            assertThat(assertionError.getCause() instanceof ComposedException).isTrue();
            assertThat(assertionError.getCause().getCause().getCause().getMessage()).contains(messageWithLines);
        }
        verify(assertionChecker).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.COMPOSED_EX), eq(messageWithLines));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
        throw new NotHappyPathException();
    }

    @Test
    public void thenAlwaysReturnNextLevelOfException() throws Exception {
        // when
        when(() -> someTestableObject.validate(ResultType.SUPER_COMPOSED_EX, EXPECTED_MSG))
                .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                .then(caughtEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(caughtEx).isInstanceOf(ComposedException.class);
                    verify(mockInTestableInstance, never()).invokeMethod();
                })
                .thenNestedException(AnotherException.class, ANOTHER_MSG)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx).isInstanceOf(AnotherException.class);
                })
                .thenNestedException(SimpleException.class, PROBLEMATIC_ONE)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                })
                .thenNestedException(SimpleException.class, EXPECTED_MSG)
                .then(nestedEx -> {
                    assertionChecker.invokeMethod();
                    assertThat(nestedEx).isInstanceOf(SimpleException.class);
                });

        // then
        verify(assertionChecker, times(4)).invokeMethod();
        verify(someTestableObject).validate(eq(ResultType.SUPER_COMPOSED_EX), eq(EXPECTED_MSG));
        assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
        assertThat(spyPrintStream.noProblemAboutExceptionLogged()).isTrue();
    }

    @Test(expected = NotHappyPathException.class)
    public void thenNestedExceptionWithMessageIn2LevelButThrownNothingAt1Level() throws Exception {
        try {
            // when
            when(() -> someTestableObject.validate(ResultType.NONE, EXPECTED_MSG))
                    .thenException(ComposedException.class, COMPOSED_EXCEPTION_MESSAGE)
                    .then(caughtEx -> {
                        assertionChecker.invokeMethod();
                        assertThat(caughtEx).isInstanceOf(ComposedException.class);
                        verify(mockInTestableInstance, never()).invokeMethod();
                    })
                    .thenNestedException(SimpleException.class, EXPECTED_MSG)
                    .then(nestedEx ->
                                  assertionChecker.invokeMethod()
                         );

            fail("should not occurred");
        } catch (AssertionError assertionError) {
            // then
            assertThat(assertionError.getMessage()).isEqualTo("Nothing was thrown! Expected exception: " + ComposedException.class.getCanonicalName() + " with message: " + COMPOSED_EXCEPTION_MESSAGE);
            verify(assertionChecker, never()).invokeMethod();
            verify(mockInTestableInstance).invokeMethod();
            verify(someTestableObject).validate(eq(ResultType.NONE), eq(EXPECTED_MSG));
            assertThat(spyPrintStream.containsInfoAboutStackTrace()).isFalse();
            assertThat(spyPrintStream.containsExceptionAndMessage(SimpleException.class, EXPECTED_MSG)).isFalse();
            throw new NotHappyPathException();
        }
    }

    private interface MockObject {

        void invokeMethod();
    }


    @RequiredArgsConstructor
    private static class TestableInstance {
        private final MockObject mockObject;

        void validate(ResultType resultType, String message) throws SimpleException, ComposedException {
            if (resultType == ResultType.NONE) {
                mockObject.invokeMethod();
                return;
            }

            if (resultType == ResultType.COMPOSED_EX) {
                throw new ComposedException(new AnotherException(ANOTHER_MSG, new SimpleException(message)));
            }

            if (resultType == ResultType.SUPER_COMPOSED_EX) {
                throw new ComposedException(new AnotherException(ANOTHER_MSG, new SimpleException(PROBLEMATIC_ONE, new SimpleException(message))));
            }

            if (resultType == ResultType.SIMPLE_EX) {
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

    private static class AnotherException extends Exception {
        public AnotherException(String message, Throwable cause) {
            super(message, cause);
        }
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
            if (!(checkedPrintln && checkedPrint)) {
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

    String concatAsNewLines(String... lines) {
        return join(String.format("%n"), Arrays.asList(lines));
    }
}