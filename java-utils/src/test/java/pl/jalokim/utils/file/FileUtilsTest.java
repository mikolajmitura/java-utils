package pl.jalokim.utils.file;

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
import static pl.jalokim.utils.file.FileUtils.readAsTextFromClassPath;
import static pl.jalokim.utils.file.FileUtils.writeToFile;
import static pl.jalokim.utils.string.StringUtils.equalsIgnoreLineEndings;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.Test;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.test.TemporaryTestResources;

public class FileUtilsTest extends TemporaryTestResources {

    static final String PATH_TO_FILE = "src/test/resources/filesTest/someFile";
    static final String PATH_TO_FILE_NOT_UTF8 = "src/test/resources/filesTest/not_utf_8.txt";

    @Test
    public void loadFileFromPath() {
        // when
        String text = FileUtils.readAsText(PATH_TO_FILE);
        // then
        assertThat(equalsIgnoreLineEndings(text, String.format("line first%n" +
            "second line%n" +
            "3 line%n" +
            "end line"))).isTrue();
    }

    @Test
    public void loadFileFromPathWithEncoding() {
        // when
        String text = FileUtils.readAsText(PATH_TO_FILE, StandardCharsets.UTF_8);
        // then
        assertThat(equalsIgnoreLineEndings(text, String.format("line first%n" +
            "second line%n" +
            "3 line%n" +
            "end line"))).isTrue();
    }

    @Test
    public void loadFileFromClassPath() {
        // when
        String text = FileUtils.readAsTextFromClassPath("filesTest/someFile");
        // then
        assertThat(equalsIgnoreLineEndings(text, String.format("line first%n" +
            "second line%n" +
            "3 line%n" +
            "end line"))).isTrue();
    }

    @Test
    public void loadFileFromClassPathWithEncoding() {
        // when
        String text = readAsTextFromClassPath("filesTest/someFile", StandardCharsets.UTF_8);
        // then
        assertThat(equalsIgnoreLineEndings(text, String.format("line first%n" +
            "second line%n" +
            "3 line%n" +
            "end line"))).isTrue();
    }

    @Test
    public void testForLoadFileByStringPathToList() {
        // when
        List<String> lines = FileUtils.readAsList(PATH_TO_FILE);
        // then
        assertThat(lines).containsExactly("line first", "second line", "3 line", "end line");
    }

    @Test
    public void testForLoadFileByStringPathNotUtf8ToList() {
        // when
        List<String> lines = FileUtils.readAsList(PATH_TO_FILE_NOT_UTF8, ISO_8859_1);
        // then
        assertThat(lines).hasSize(2);
    }

    @Test
    public void testForLoadFileByPathToList() {
        // when
        List<String> lines = FileUtils.readAsList(Paths.get(PATH_TO_FILE));
        // then
        assertThat(lines).containsExactly("line first", "second line", "3 line", "end line");
    }

    @Test
    public void testForLoadFileByPathToNotUtf8List() {
        // when
        List<String> lines = FileUtils.readAsList(Paths.get(PATH_TO_FILE_NOT_UTF8), ISO_8859_1);
        // then
        assertThat(lines).hasSize(2);
    }

    @Test
    public void testForLoadFileByFileToList() {
        // when
        List<String> lines = FileUtils.readAsList(new File(PATH_TO_FILE));
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
    public void testAppendAtEndOfFileByFile() {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.appendToFile(newFile, String.format("%nnext line%nlast line..."));
        // then
        List<String> lines = FileUtils.readAsList(newFile.getAbsolutePath());
        assertThat(lines).containsExactly("line first",
            "second Line__",
            "end line.......",
            "next line",
            "last line...");
    }

    @Test
    public void testAppendAtEndOfFileByPath() {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.appendToFile(newFile.toPath(), String.format("%nnext line%nlast line..."));
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
    public void testAppendToFileShouldCreateWhenNotExist() {
        // given
        String filePath = "new_file_to_save2";
        if (Files.exists(Paths.get(filePath))) {
            FileUtils.deleteFileOrDirectory(filePath);
        }
        // when
        FileUtils.appendToFile(filePath, String.format("next line%nlast line..."));
        // then
        List<String> lines = FileUtils.readAsList(filePath);
        assertThat(lines).containsExactly("next line",
            "last line...");
        FileUtils.deleteFileOrDirectory(filePath);
    }

    @Test
    public void testWriteAllElementsAsLinesToFile() throws IOException {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......");
        writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.writeToFile(newFile.getAbsolutePath(), Arrays.asList("line first",
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
        FileUtils.appendToFile(newFile.getAbsolutePath(), Arrays.asList("line first",
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
    public void testAppendAllElementsAsLinesToFileByFile() {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......%n");
        writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.appendToFile(newFile, Arrays.asList("line first",
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
    public void testAppendAllElementsAsLinesToFileByPath() {
        // given
        File newFile = newFile("new_file_to_save");
        String contentToSave = String.format("line first%nsecond Line__%nend line.......%n");
        writeToFile(newFile.getAbsolutePath(), contentToSave);
        // when
        FileUtils.appendToFile(newFile.toPath(), Arrays.asList("line first",
            "second Line__",
            "end2 line.......",
            "next line",
            "last line..."));
        // then
        List<String> lines = FileUtils.readAsList(newFile.toPath());
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
    public void createFoldersByStringPath() {
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
    public void createFoldersByPath() {
        // given
        String pathForFileInTempFolder = getPathForFileInTempFolder("folder/folder2/fileName");
        // when
        createDirectoriesForFile(Paths.get(pathForFileInTempFolder));
        String fileContent = "some_value1";
        writeToFile(pathForFileInTempFolder, fileContent);
        // then
        File file = new File(pathForFileInTempFolder);
        assertThat(file.isDirectory()).isFalse();
        String readValue = FileUtils.readAsText(pathForFileInTempFolder);
        assertThat(readValue).isEqualTo(fileContent);
    }

    @Test
    public void createFoldersByFile() {
        // given
        String pathForFileInTempFolder = getPathForFileInTempFolder("folder/folder2/fileName");
        // when
        createDirectoriesForFile(new File(pathForFileInTempFolder));
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
    public void createFoldersByStringPathAsExpected() {
        // given
        String pathForFolder = getPathForFileInTempFolder("folder/folder2");
        // when
        createDirectories(pathForFolder);
        // then
        File file = new File(pathForFolder);
        assertThat(file.isDirectory()).isTrue();
    }

    @Test
    public void createFoldersByPathAsExpected() {
        // given
        Path pathForFolder = Paths.get(getPathForFileInTempFolder("folder/folder2"));
        // when
        createDirectories(pathForFolder);
        // then
        assertThat(pathForFolder.toFile().isDirectory()).isTrue();
    }

    @Test
    public void createFoldersByFileAsExpected() {
        // given
        File pathForFolder = new File(getPathForFileInTempFolder("folder/folder2"));
        // when
        createDirectories(pathForFolder);
        // then
        assertThat(pathForFolder.isDirectory()).isTrue();
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

    @Test
    public void readAsElementsByStringPath() {
        // given
        // when
        Elements<String> lines = FileUtils.readAsElements(PATH_TO_FILE);
        // then
        assertThat(lines.asList()).containsExactly("line first", "second line", "3 line", "end line");
    }

    @Test
    public void readAsElementsByPath() {
        // given
        // when
        Elements<String> lines = FileUtils.readAsElements(Paths.get(PATH_TO_FILE));
        // then
        assertThat(lines.asList()).containsExactly("line first", "second line", "3 line", "end line");
    }

    @Test
    public void readAsElementsByFile() {
        // given
        // when
        Elements<String> lines = FileUtils.readAsElements(new File(PATH_TO_FILE));
        // then
        assertThat(lines.asList()).containsExactly("line first", "second line", "3 line", "end line");
    }

    @Test
    public void readAsElementsByFileNotUtf8() {
        // given
        // when
        Elements<String> lines = FileUtils.readAsElements(new File(PATH_TO_FILE_NOT_UTF8), ISO_8859_1);
        // then
        assertThat(lines.asList()).hasSize(2);
    }

    @Test
    public void writeToFileByElements() {
        // given
        File normalFile = newFile("fileWithElements");
        Elements<String> elements = elements("1", "2", "3");
        // when
        FileUtils.writeToFile(normalFile.toString(), elements);
        // then
        List<String> lines = FileUtils.readAsList(normalFile);
        assertThat(lines).containsExactly("1", "2", "3");
    }

    @Test
    public void listOfFilesRecursivelyByPath() {
        // given
        FolderWrapper testPath = newFolder("test_path")
            .newFolder("1")
            .newFolder("1_1")
            .getParent()
            .newFolder("1_2")
            .getParent()
            .getParent()
            .newFolder("2")
            .getParent()
            .newFolder("3")
            .getParent();
        // when
        List<Path> paths = FileUtils.listOfFilesRecursively(testPath.getRealFolder().toPath());
        // then
        assertThat(paths).hasSize(6);
    }

    @Test
    public void listOfFilesRecursivelyByFile() {
        // given
        FolderWrapper testPath = newFolder("test_path")
            .newFolder("1")
            .newFolder("1_1")
            .getParent()
            .newFolder("1_2")
            .getParent()
            .getParent()
            .newFolder("2")
            .getParent()
            .newFolder("3")
            .getParent();
        // when
        List<Path> paths = FileUtils.listOfFilesRecursively(testPath.getRealFolder());
        // then
        assertThat(paths).hasSize(6);
    }

    @Test
    public void listOfFilesRecursivelyByStringPath() {
        // given
        FolderWrapper testPath = newFolder("test_path")
            .newFolder("1")
            .newFolder("1_1")
            .getParent()
            .newFolder("1_2")
            .getParent()
            .getParent()
            .newFolder("2")
            .getParent()
            .newFolder("3")
            .getParent();
        // when
        List<Path> paths = FileUtils.listOfFilesRecursively(testPath.getRealFolder().toString());
        // then
        assertThat(paths).hasSize(6);
    }

    @Test
    public void listOfFilesRecursivelyFilterByPredicateByPath() {
        // given
        FolderWrapper rootPath = newFolder("test_path");
        FolderWrapper folder1 = rootPath.newFolder("1");
        folder1.newFolder("1_1").newFile("file3");
        folder1.newFolder("1_2");
        folder1.newFile("file1");
        rootPath.newFolder("2").newFile("file2");
        rootPath.newFolder("3");
        // when
        List<Path> paths = FileUtils.listOfFilesRecursively(rootPath.getRealFolder().toPath(), p -> Files.isRegularFile(p));
        // then
        assertThat(paths).hasSize(3);
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
