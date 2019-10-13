package pl.jalokim.utils.file;

import org.junit.After;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.file.FileUtils.readFileFromPathToFileCursor;
import static pl.jalokim.utils.file.FileUtilsTest.PATH_TO_FILE;

public class FileCursorTest {

    private FileCursor tested = readFileFromPathToFileCursor(PATH_TO_FILE);

    @After
    public void tearDown() throws Exception {
        tested.close();
    }

    @Test
    public void correctlyReadLinesFromFileFromSystemPath() {
        assertNextLine(null, 0, true, "line first");
        assertNextLine("line first", 1, true, "second line");
        assertNextLine("second line", 2, true, "3 line");
        assertNextLine("3 line", 3, true, "end line");
        assertNextLine("end line", 4, false, null);
    }

    private void assertNextLine(String expectedCurrentLine, Integer currentLineNumber, boolean hasNextLine, String nextLine) {
        assertThat(expectedCurrentLine).isEqualTo(tested.getCurrentLine());
        assertThat(currentLineNumber).isEqualTo(tested.getLineNumber().intValue());
        assertThat(hasNextLine).isEqualTo(tested.hasNext());
        assertThat(nextLine).isEqualTo(tested.next());
    }
}