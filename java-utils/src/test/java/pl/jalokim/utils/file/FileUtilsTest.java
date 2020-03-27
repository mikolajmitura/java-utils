package pl.jalokim.utils.file;

import org.junit.Test;
import pl.jalokim.utils.test.TemporaryTestResources;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.file.FileUtils.catchIoEx;
import static pl.jalokim.utils.file.FileUtils.consumeEveryLineFromFile;
import static pl.jalokim.utils.file.FileUtils.consumeEveryLineWitNumberFromFile;
import static pl.jalokim.utils.file.FileUtils.createDirectories;
import static pl.jalokim.utils.file.FileUtils.createDirectoriesForFile;
import static pl.jalokim.utils.file.FileUtils.deleteFileOrDirectory;
import static pl.jalokim.utils.file.FileUtils.listOfFiles;
import static pl.jalokim.utils.file.FileUtils.readAsText;
import static pl.jalokim.utils.file.FileUtils.readAsTextFromClassPath;
import static pl.jalokim.utils.file.FileUtils.readAsList;
import static pl.jalokim.utils.file.FileUtils.writeToFile;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

public class FileUtilsTest extends TemporaryTestResources {

    static final String PATH_TO_FILE = "src/test/resources/filesTest/someFile";

    @Test
    public void loadFileFromPath() {
        // when
        String text = FileUtils.readAsText(PATH_TO_FILE);
        // then
        assertThat(text).isEqualTo(String.format("line first%n" +
                                                 "second line%n" +
                                                 "3 line%n" +
                                                 "end line"));
    }

    @Test
    public void loadFileFromPathWithEncoding() {
        // when
        String text = FileUtils.readAsText(PATH_TO_FILE, StandardCharsets.UTF_8);
        // then
        assertThat(text).isEqualTo(String.format("line first%n" +
                                                 "second line%n" +
                                                 "3 line%n" +
                                                 "end line"));
    }

    @Test
    public void loadFileFromClassPath() {
        // when
        String text = FileUtils.readAsTextFromClassPath("filesTest/someFile");
        // then
        assertThat(text).isEqualTo(String.format("line first%n" +
                                                 "second line%n" +
                                                 "3 line%n" +
                                                 "end line"));
    }

    @Test
    public void loadFileFromClassPathWithEncoding() {
        // when
        String text = readAsTextFromClassPath("filesTest/someFile", StandardCharsets.UTF_8);
        // then
        assertThat(text).isEqualTo(String.format("line first%n" +
                                                 "second line%n" +
                                                 "3 line%n" +
                                                 "end line"));
    }

    @Test
    public void testForLoadFileFromPathToList() {
        // when
        List<String> lines = FileUtils.readAsList(PATH_TO_FILE);
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
    public void testWriteStringToFile() {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        // when
        writeToFile(newFile.getAbsolutePath(), "old value");
        writeToFile(newFile.getAbsolutePath(), contentToSave);
        // then
        String readContent = FileUtils.readAsText(newFile.getAbsolutePath());
        assertThat(readContent).isEqualTo(contentToSave);
    }

    @Test
    public void testWriteStringToFileWithAnotherEncoding() {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        // when
        writeToFile(newFile.getAbsolutePath(), "old value", ISO_8859_1);
        writeToFile(newFile.getAbsolutePath(), contentToSave, ISO_8859_1);
        // then
        String readContent = FileUtils.readAsText(newFile.getAbsolutePath(), ISO_8859_1);
        assertThat(readContent).isEqualTo(contentToSave);
    }

    @Test
    public void testAppendAtEndOfFile() {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.appendToFile(newFile.getAbsolutePath(), String.format("%nnext line%nlast line..."));
        // then
        List<String> lines = FileUtils.readAsList(newFile.getAbsolutePath());
        assertThat(lines).containsExactly("line first",
                                          "second Line__",
                                          "end line.......",
                                          "next line",
                                          "last line...");
    }

    @Test
    public void testAppendToFileWhenNotExist() throws IOException {
        // given
        File newFile = newFile("new_file_to_save");
        // when
        FileUtils.appendToFile(newFile.getAbsolutePath(), String.format("next line%nlast line..."));
        // then
        List<String> lines = FileUtils.readAsList(newFile.getAbsolutePath());
        assertThat(lines).containsExactly("next line",
                                          "last line...");
    }

    @Test
    public void testWriteAllElementsAsLinesToFile() throws IOException {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.writeAllElementsAsLinesToFile(newFile.getAbsolutePath(), Arrays.asList("line first",
                                                                                         "second Line__",
                                                                                         "end line.......",
                                                                                         "next line",
                                                                                         "last line..."));
        // then
        List<String> lines = FileUtils.readAsList(newFile.getAbsolutePath());
        assertThat(lines).containsExactly("line first",
                                          "second Line__",
                                          "end line.......",
                                          "next line",
                                          "last line...");
    }

    @Test
    public void testAppendAllElementsAsLinesToFile() {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......%n");
        writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.appendAllElementsAsLinesToFile(newFile.getAbsolutePath(), Arrays.asList("line first",
                                                                                          "second Line__",
                                                                                          "end2 line.......",
                                                                                          "next line",
                                                                                          "last line..."));
        // then
        List<String> lines = FileUtils.readAsList(newFile.getAbsolutePath());
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
                     FileUtils.readAsList("/some_folder_which_not_exist"))
                .thenException(FileException.class)
                .thenNestedException(NoSuchFileException.class);
    }

    @Test
    public void createFoldersForFile() {
        // given
        String pathForFileInTempFolder = getPathForFileInTempFolder("folder/folder2/fileName");
        // when
        createDirectoriesForFile(pathForFileInTempFolder);
        String fileContent = "some_value1";
        writeToFile(pathForFileInTempFolder, fileContent);
        // then
        File file = new File(pathForFileInTempFolder);
        assertThat(file.isDirectory()).isFalse();
        String readValue = FileUtils.readAsText(pathForFileInTempFolder);
        assertThat(readValue).isEqualTo(fileContent);
    }

    @Test
    public void willNotThrowException() {
        createDirectoriesForFile("test");
    }

    @Test
    public void catchIoExReturnFileException() {
        when(
                () -> catchIoEx(
                        () -> {
                            throw new NoSuchFileException("some text");
                        })
            )
                .thenException(FileException.class)
                .thenNestedException(new NoSuchFileException("some text"));
    }

    @Test
    public void createFoldersAsExpected() {
        // given
        String pathForFolder = getPathForFileInTempFolder("folder/folder2");
        // when
        createDirectories(pathForFolder);
        // then
        File file = new File(pathForFolder);
        assertThat(file.isDirectory()).isTrue();
    }

    @Test
    public void listOfFilesTest() throws IOException {
        // given
        getTempFolder().newFile("file1");
        tempFolder.newFile("file2");
        tempFolder.newFile("file3");
        tempFolder.newFolder("folder1");
        tempFolder.newFolder("folder2");
        // when
        List<File> fileList = listOfFiles(getPathForTempFolder());
        List<File> fileListOnlyFolders = listOfFiles(getPathForTempFolder(), File::isDirectory);
        // then
        assertThat(fileList).hasSize(5);
        assertThat(fileListOnlyFolders).hasSize(2);
    }

    @Test
    public void listOf() {
        // given
        newFile("file1");
        newFile("file2");
        newFile("file3");
        newFolder("folder1");
        newFolder("folder2");
        // when
        List<File> fileList = listOfFiles(Paths.get(getPathForTempFolder()));
        List<File> fileListOnlyFolders = listOfFiles(Paths.get(getPathForTempFolder()), File::isDirectory);
        // then
        assertThat(fileList).hasSize(5);
        assertThat(fileListOnlyFolders).hasSize(2);
    }

    @Test
    public void cannotGetListOfInvalidFile() {
        when(() -> listOfFiles("notExist"))
                .thenException(FileException.class, "Provided path: " + new File("notExist").getAbsolutePath() + " does not exist");
    }

    @Test
    public void removeNonEmptyFolderByPathAsText() {
        removeNonEmptyFolder(file -> deleteFileOrDirectory(getPathForFileInTempFolder("notEmptyFolder")));
    }

    @Test
    public void removeNonEmptyFolderByPath() {
        removeNonEmptyFolder(file -> deleteFileOrDirectory(file.toPath()));
    }

    @Test
    public void removeNonEmptyFolderByFile() {
        removeNonEmptyFolder(FileUtils::deleteFileOrDirectory);
    }

    @Test
    public void removeNormalFile() {
        // given
        File normalFile = newFile("normalFile");
        // when
        deleteFileOrDirectory(normalFile);
        // then
        assertThat(listOfFiles(getTempFolder().getRoot())).isEmpty();
    }

    @Test
    public void cannotRemoveFileWhenNotExist() {
        when(() -> deleteFileOrDirectory("notExist"))
                .thenException(FileException.class, "cannot delete file: " + new File("notExist").getAbsolutePath());
    }

    public void removeNonEmptyFolder(Consumer<File> fileConsumer) {
        // given
        FolderWrapper notEmptyFolder = newFolder("notEmptyFolder");
        notEmptyFolder.newFile("file1");
        notEmptyFolder.newFile("file2");
        FolderWrapper notEmptyL1 = notEmptyFolder.newFolder("notEmptyL1");
        notEmptyL1.newFile("someFile");
        notEmptyFolder.newFolder("emptyFolderL1");
        FolderWrapper someFolder = newFolder("someFolder");
        someFolder.newFile("test1");
        newFile("someFile1");
        newFile("someFile2");
        assertThat(elements(listOfFiles(getTempFolder().getRoot()))
                           .map(File::getName)
                           .asList())
                .containsExactlyInAnyOrder("someFile1", "someFile2", "notEmptyFolder", "someFolder");
        // when
        fileConsumer.accept(notEmptyFolder.getRealFolder());
        // then
        assertThat(elements(listOfFiles(getTempFolder().getRoot()))
                           .map(File::getName)
                           .asList())
                .containsExactlyInAnyOrder("someFile1", "someFile2", "someFolder");
    }
}