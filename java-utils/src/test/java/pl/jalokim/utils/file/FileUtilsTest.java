package pl.jalokim.utils.file;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.file.FileUtils.consumeEveryLineFromFile;
import static pl.jalokim.utils.file.FileUtils.consumeEveryLineWitNumberFromFile;
import static pl.jalokim.utils.file.FileUtils.loadFileFromClassPathAsText;
import static pl.jalokim.utils.file.FileUtils.loadFileFromPathAsText;
import static pl.jalokim.utils.file.FileUtils.loadFileFromPathToList;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

public class FileUtilsTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    static final String PATH_TO_FILE = "src/test/resources/filesTest/someFile";

    @Test
    public void loadFileFromPath() {
        // when
        String text = loadFileFromPathAsText(PATH_TO_FILE);
        // then
        assertThat(text).isEqualTo(String.format("line first%n" +
                                                 "second line%n" +
                                                 "3 line%n" +
                                                 "end line"));
    }

    @Test
    public void loadFileFromPathWithEncoding() {
        // when
        String text = loadFileFromPathAsText(PATH_TO_FILE, StandardCharsets.UTF_8);
        // then
        assertThat(text).isEqualTo(String.format("line first%n" +
                                                 "second line%n" +
                                                 "3 line%n" +
                                                 "end line"));
    }

    @Test
    public void loadFileFromClassPath() {
        // when
        String text = loadFileFromClassPathAsText("filesTest/someFile");
        // then
        assertThat(text).isEqualTo(String.format("line first%n" +
                                                 "second line%n" +
                                                 "3 line%n" +
                                                 "end line"));
    }

    @Test
    public void loadFileFromClassPathWithEncoding() {
        // when
        String text = loadFileFromClassPathAsText("filesTest/someFile", StandardCharsets.UTF_8);
        // then
        assertThat(text).isEqualTo(String.format("line first%n" +
                                                 "second line%n" +
                                                 "3 line%n" +
                                                 "end line"));
    }

    @Test
    public void testForLoadFileFromPathToList() {
        // when
        List<String> lines = loadFileFromPathToList(PATH_TO_FILE);
        // then
        assertThat(lines).containsExactly("line first", "second line", "3 line", "end line");
    }

    @Test
    public void testReadEveryLineToConsumer() {
        List<String> lines = new ArrayList<>();
        // when
        consumeEveryLineFromFile(PATH_TO_FILE, lines::add);
        // then
        assertThat(lines).containsExactly("line first", "second line", "3 line", "end line");
    }

    @Test
    public void testReadEveryLineToConsumeIndexAndLine() {
        Map<Long, String> map = new HashMap<>();
        // when
        consumeEveryLineWitNumberFromFile(PATH_TO_FILE, map::put);
        // then
        assertThat(map.get(1L)).isEqualTo("line first");
        assertThat(map.get(2L)).isEqualTo("second line");
        assertThat(map.get(3L)).isEqualTo("3 line");
        assertThat(map.get(4L)).isEqualTo("end line");
    }

    @Test
    public void testWriteStringToFile() throws IOException {
        // given
        File newFile = testFolder.newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        // when
        FileUtils.writeToFile(newFile.getAbsolutePath(), "old value");
        FileUtils.writeToFile(newFile.getAbsolutePath(), contentToSave);
        // then
        String readContent = loadFileFromPathAsText(newFile.getAbsolutePath());
        assertThat(readContent).isEqualTo(contentToSave);
    }

    @Test
    public void testAppendAtEndOfFile() throws IOException {
        // given
        File newFile = testFolder.newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        FileUtils.writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.appendToFile(newFile.getAbsolutePath(), String.format("%nnext line%nlast line..."));
        // then
        List<String> lines = loadFileFromPathToList(newFile.getAbsolutePath());
        assertThat(lines).containsExactly("line first",
                                          "second Line__",
                                          "end line.......",
                                          "next line",
                                          "last line...");
    }

    @Test
    public void testAppendToFileWhenNotExist() throws IOException {
        // given
        File newFile = testFolder.newFile("new_file_to_save");
        // when
        FileUtils.appendToFile(newFile.getAbsolutePath(), String.format("next line%nlast line..."));
        // then
        List<String> lines = loadFileFromPathToList(newFile.getAbsolutePath());
        assertThat(lines).containsExactly("next line",
                                          "last line...");
    }

    @Test
    public void testWriteAllElementsAsLinesToFile() throws IOException {
        // given
        File newFile = testFolder.newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        FileUtils.writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.writeAllElementsAsLinesToFile(newFile.getAbsolutePath(), Arrays.asList("line first",
                                                                                         "second Line__",
                                                                                         "end line.......",
                                                                                         "next line",
                                                                                         "last line..."));
        // then
        List<String> lines = loadFileFromPathToList(newFile.getAbsolutePath());
        assertThat(lines).containsExactly("line first",
                                          "second Line__",
                                          "end line.......",
                                          "next line",
                                          "last line...");
    }

    @Test
    public void testAppendAllElementsAsLinesToFile() throws IOException {
        // given
        File newFile = testFolder.newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......%n");
        FileUtils.writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.appendAllElementsAsLinesToFile(newFile.getAbsolutePath(), Arrays.asList("line first",
                                                                                          "second Line__",
                                                                                          "end2 line.......",
                                                                                          "next line",
                                                                                          "last line..."));
        // then
        List<String> lines = loadFileFromPathToList(newFile.getAbsolutePath());
        assertThat(lines).containsExactly("line first",
                                          "second Line__",
                                          "end line.......",
                                          "line first",
                                          "second Line__",
                                          "end2 line.......",
                                          "next line",
                                          "last line...");
    }

    @Test
    public void cannotReadFromFile() {
        when(() ->
                     FileUtils.loadFileFromPathAsText("/some_folder_which_not_exist"))
                .thenException(FileException.class)
                .thenNestedException(NoSuchFileException.class);
    }
}