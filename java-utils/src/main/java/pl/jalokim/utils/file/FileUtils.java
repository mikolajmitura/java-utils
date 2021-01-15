package pl.jalokim.utils.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Paths.get;
import static pl.jalokim.utils.collection.Elements.elements;

import com.google.common.io.Resources;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.string.StringUtils;

/**
 * Useful class for Files.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.ExcessivePublicCount"})
public final class FileUtils {

    private FileUtils() {

    }

    /**
     * It read from file and put all content to String.
     *
     * @param path to file
     * @return text with file content
     */
    public static String readAsText(String path) {
        return readAsText(new File(path));
    }

    /**
     * It read from file and put all content to String with certain encoding.
     *
     * @param path to file
     * @param charset instance of java.nio.charset.Charset
     * @return text with file content
     */
    public static String readAsText(String path, Charset charset) {
        return readAsText(new File(path), charset);
    }

    /**
     * It read from file and put all content to String.
     *
     * @param file path for File
     * @return text with file content
     */
    public static String readAsText(File file) {
        return readAsText(file.toPath());
    }

    /**
     * It read from file and put all content to String with certain encoding.
     *
     * @param file path for File
     * @param charset instance of java.nio.charset.Charset
     * @return text with file content
     */
    public static String readAsText(File file, Charset charset) {
        return readAsText(file.toPath(), charset);
    }


    /**
     * It read from file and put all content to String with certain encoding.
     *
     * @param path to file
     * @return text with file content
     */
    public static String readAsText(Path path) {
        return readAsText(path, UTF_8);
    }

    /**
     * It read from file and put all content to String with certain encoding.
     *
     * @param path to file
     * @param charset instance of java.nio.charset.Charset
     * @return text with file content
     */
    public static String readAsText(Path path, Charset charset) {
        return catchIoExAndReturn(() -> new String(Files.readAllBytes(path), charset));
    }

    /**
     * It read from file from classpath and put all content to String.
     *
     * @param path to file
     * @return text with file content
     */
    public static String readAsTextFromClassPath(String path) {
        return readAsTextFromClassPath(path, UTF_8);
    }

    /**
     * It read from file from classpath and put all content to String with certain encoding.
     *
     * @param path to file
     * @param charset instance of java.nio.charset.Charset
     * @return text with file content
     */
    public static String readAsTextFromClassPath(String path, Charset charset) {
        return catchIoExAndReturn(() -> {
            URL url = Resources.getResource(path);
            return Resources.toString(url, charset);
        });
    }

    /**
     * It reads all lines to list from certain path from system for file. It not read whole file content to memory. After that you need to close stream in
     * elements.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static Elements<String> readAsElements(String path) {
        return readAsElements(path, UTF_8);
    }

    /**
     * It reads all lines to list from certain path from system for file. It not read whole file content to memory. After that you need to close stream in
     * elements.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static Elements<String> readAsElements(Path path) {
        return readAsElements(path, UTF_8);
    }

    /**
     * It reads all lines to list from certain path from system for file. It not read whole file content to memory. After that you need to close stream in
     * elements.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static Elements<String> readAsElements(File path) {
        return readAsElements(path, UTF_8);
    }

    /**
     * It reads all lines to Elements from certain path from system for file. It read whole file content to memory.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static Elements<String> readAsElements(File path, Charset charset) {
        return readAsElements(path.toString(), charset);
    }

    /**
     * It reads all lines to Elements from certain path from system for file. It read whole file content to memory.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static Elements<String> readAsElements(Path path, Charset charset) {
        return readAsElements(path.toString(), charset);
    }

    /**
     * It reads all lines to Elements from certain path from system for file. It read whole file content to memory.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static Elements<String> readAsElements(String path, Charset charset) {
        return elements(catchIoExAndReturn(() -> Files.lines(get(path), charset)));
    }

    /**
     * It reads all lines to list from certain path from system for file. It read whole file content to memory.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static List<String> readAsList(String path) {
        return readAsList(path, UTF_8);
    }

    /**
     * It reads all lines to list from certain path from system for file. It read whole file content to memory.
     *
     * @param file system path to file to read.
     * @return list with all lines from file.
     */
    public static List<String> readAsList(File file) {
        return readAsList(file, UTF_8);
    }

    /**
     * It reads all lines to list from certain path from system for file. It read whole file content to memory.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static List<String> readAsList(Path path) {
        return readAsList(path, UTF_8);
    }

    /**
     * It reads all lines to list from certain path from system for file. It read whole file content to memory.
     *
     * @param file system path to file to read.
     * @return list with all lines from file.
     */
    public static List<String> readAsList(File file, Charset charset) {
        return readAsList(file.toString(), charset);
    }

    /**
     * It reads all lines to list from certain path from system for file. It read whole file content to memory.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static List<String> readAsList(Path path, Charset charset) {
        return readAsList(path.toString(), charset);
    }

    /**
     * It reads all lines to list from certain path from system for file. It read whole file content to memory.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static List<String> readAsList(String path, Charset charset) {
        List<String> lines = new ArrayList<>();
        consumeEveryLineFromFile(path, lines::add, charset);
        return lines;
    }

    /**
     * It read all lines line by line and put line value to consumerLine argument. It created for performance purpose.
     *
     * @param path system path to file to read.
     * @param consumerLine which consume every line
     */
    public static void consumeEveryLineFromFile(String path, Consumer<String> consumerLine) {
        consumeEveryLineFromFile(path, consumerLine, UTF_8);
    }

    /**
     * It read all lines line by line and put line value to consumerLine argument. It created for performance purpose.
     *
     * @param path system path to file to read.
     * @param consumerLine which consume every line
     */
    public static void consumeEveryLineFromFile(String path, Consumer<String> consumerLine, Charset charset) {
        catchIoExAndReturn(() -> {
            try (BufferedReader br = newBufferedReader(get(path), charset)) {
                String line;
                while ((line = br.readLine()) != null) {
                    consumerLine.accept(line);
                }
            }
            return null;
        });
    }

    /**
     * It read all lines line by line and put line value to consumerLine argument. It created for performance purpose.
     *
     * @param path system path to file to read.
     * @param consumerLineIndex which consume every line with index of them in file
     */
    public static void consumeEveryLineWitNumberFromFile(String path, BiConsumer<Long, String> consumerLineIndex) {
        consumeEveryLineWitNumberFromFile(path, consumerLineIndex, UTF_8);
    }

    /**
     * It read all lines line by line and put line value to consumerLine argument. It created for performance purpose.
     *
     * @param path system path to file to read.
     * @param consumerLineIndex which consume every line with index of them in file
     */
    public static void consumeEveryLineWitNumberFromFile(String path, BiConsumer<Long, String> consumerLineIndex, Charset charset) {
        catchIoExAndReturn(() -> {
            long index = 0;
            try (BufferedReader br = newBufferedReader(get(path), charset)) {
                String line;
                while ((line = br.readLine()) != null) {
                    index++;
                    consumerLineIndex.accept(index, line);
                }
            }
            return null;
        });
    }

    /**
     * It create instance of FileCursor.
     *
     * @param path system path to file.
     * @return instance of FileCursor.
     */
    public static FileCursor readFileFromPathToFileCursor(String path) {
        return new FileCursor(path);
    }

    /**
     * It create instance of FileCursor.
     *
     * @param path system path to file.
     * @return instance of FileCursor.
     */
    public static FileCursor readFileFromPathToFileCursor(String path, Charset charset) {
        return new FileCursor(path, charset);
    }

    /**
     * It override current file content if exists. it not create folders with not exist.
     *
     * @param filePath system path to file.
     * @param fileContent as String to write to file
     */
    public static void writeToFile(String filePath, String fileContent) {
        writeToFile(new File(filePath), fileContent);
    }

    /**
     * It override current file content if exists. it not create folders with not exist.
     *
     * @param filePath system path to file.
     * @param fileContent as String to write to file
     */
    public static void writeToFile(String filePath, String fileContent, Charset charset) {
        writeToFile(new File(filePath), fileContent, charset);
    }

    /**
     * It override current file content if exists. it not create folders with not exist.
     *
     * @param filePath system path to file.
     * @param fileContent as String to write to file
     */
    public static void writeToFile(File filePath, String fileContent) {
        writeToFile(filePath.toPath(), fileContent);
    }

    /**
     * It override current file content if exists. it not create folders with not exist.
     *
     * @param filePath system path to file.
     * @param fileContent as String to write to file
     */
    public static void writeToFile(File filePath, String fileContent, Charset charset) {
        writeToFile(filePath.toPath(), fileContent, charset);
    }

    /**
     * It override current file content if exists. it not create folders with not exist.
     *
     * @param filePath system path to file.
     * @param fileContent as String to write to file
     */
    public static void writeToFile(Path filePath, String fileContent) {
        writeToFile(filePath, fileContent, UTF_8);
    }

    /**
     * It override current file content if exists. it not create folders with not exist.
     *
     * @param filePath system path to file.
     * @param fileContent as String to write to file
     */
    public static void writeToFile(Path filePath, String fileContent, Charset charset) {
        byte[] strToBytes = fileContent.getBytes(charset);
        catchIoExAndReturn(() -> Files.write(filePath, strToBytes));
    }


    /**
     * It writes all list element to file, every as separated line.
     *
     * @param filePath system path to file
     * @param elementsToWrite text lines write to file
     */
    public static void writeToFile(String filePath, List<String> elementsToWrite) {
        writeToFile(filePath, elementsToWrite, UTF_8);
    }

    /**
     * It writes all list element to file, every as separated line.
     *
     * @param filePath system path to file
     * @param elementsToWrite text lines write to file
     */
    public static void writeToFile(String filePath, Elements<String> elementsToWrite) {
        writeToFile(filePath, elementsToWrite, UTF_8);
    }

    /**
     * It writes all list element to file, every as separated line.
     *
     * @param filePath system path to file
     * @param elementsToWrite text lines write to file
     */
    public static void writeToFile(String filePath, Elements<String> elementsToWrite, Charset charset) {
        writeToFile(filePath, elementsToWrite.asList(), charset);
    }

    /**
     * It writes all list element to file, every as separated line.
     *
     * @param filePath system path to file
     * @param elementToWrite text lines write to file
     */
    public static void writeToFile(String filePath, List<String> elementToWrite, Charset charset) {
        Path path = get(filePath);
        String fileContent = StringUtils.concatElementsAsLines(elementToWrite);
        byte[] strToBytes = fileContent.getBytes(charset);
        catchIoExAndReturn(() -> Files.write(path, strToBytes));
    }

    /**
     * It append all list element to file, every as separated line.
     *
     * @param filePath system path to file
     * @param elementToWrite text lines write to file
     */
    public static void appendToFile(File filePath, List<String> elementToWrite) {
        appendToFile(filePath.toString(), elementToWrite, UTF_8);
    }

    /**
     * It append all list element to file, every as separated line.
     *
     * @param filePath system path to file
     * @param elementToWrite text lines write to file
     */
    public static void appendToFile(Path filePath, List<String> elementToWrite) {
        appendToFile(filePath.toString(), elementToWrite, UTF_8);
    }

    /**
     * It append all list element to file, every as separated line.
     *
     * @param filePath system path to file
     * @param elementToWrite text lines write to file
     */
    public static void appendToFile(String filePath, List<String> elementToWrite) {
        appendToFile(filePath, elementToWrite, UTF_8);
    }

    /**
     * It append all list element to file, every as separated line.
     *
     * @param filePath system path to file
     * @param elementToWrite text lines write to file
     */
    public static void appendToFile(String filePath, List<String> elementToWrite, Charset charset) {
        Path path = get(filePath);
        String fileContent = StringUtils.concatElementsAsLines(elementToWrite);
        byte[] strToBytes = fileContent.getBytes(charset);
        catchIoExAndReturn(() -> Files.write(path, strToBytes, StandardOpenOption.APPEND, StandardOpenOption.CREATE));
    }

    /**
     * Append some text to file.
     *
     * @param filePath system path to file
     * @param fileContent as String to append to file
     */
    public static void appendToFile(File filePath, String fileContent) {
        appendToFile(filePath, fileContent, UTF_8);
    }

    /**
     * Append some text to file.
     *
     * @param filePath system path to file
     * @param fileContent as String to append to file
     */
    public static void appendToFile(Path filePath, String fileContent) {
        appendToFile(filePath, fileContent, UTF_8);
    }

    /**
     * Append some text to file.
     *
     * @param filePath system path to file
     * @param fileContent as String to append to file
     */
    public static void appendToFile(String filePath, String fileContent) {
        appendToFile(filePath, fileContent, UTF_8);
    }

    /**
     * Append some text to file.
     *
     * @param filePath system path to file
     * @param fileContent as String to append to file
     */
    public static void appendToFile(Path filePath, String fileContent, Charset charset) {
        appendToFile(filePath.toString(), fileContent, charset);
    }

    /**
     * Append some text to file.
     *
     * @param filePath system path to file
     * @param fileContent as String to append to file
     */
    public static void appendToFile(File filePath, String fileContent, Charset charset) {
        appendToFile(filePath.toString(), fileContent, charset);
    }

    /**
     * Append some text to file.
     *
     * @param filePath system path to file
     * @param fileContent as String to append to file
     */
    public static void appendToFile(String filePath, String fileContent, Charset charset) {
        Path path = get(filePath);
        byte[] strToBytes = fileContent.getBytes(charset);
        catchIoExAndReturn(() -> Files.write(path, strToBytes, StandardOpenOption.APPEND, StandardOpenOption.CREATE));
    }

    /**
     * It creates directories recursively, leaf of provided path is expected as file name.
     *
     * @param pathToFile as String.
     */
    public static void createDirectoriesForFile(File pathToFile) {
        createDirectoriesForFile(pathToFile.toString());
    }

    /**
     * It creates directories recursively, leaf of provided path is expected as file name.
     *
     * @param pathToFile as String.
     */
    public static void createDirectoriesForFile(Path pathToFile) {
        createDirectoriesForFile(pathToFile.toString());
    }

    /**
     * It creates directories recursively, leaf of provided path is expected as file name.
     *
     * @param pathToFile as String.
     */
    public static void createDirectoriesForFile(String pathToFile) {
        Path folderPath = get(pathToFile).getParent();
        if (folderPath != null) {
            catchIoExAndReturn(() -> Files.createDirectories(folderPath));
        }
    }

    /**
     * It creates directories recursively, all path part will be a folder type.
     *
     * @param folderPath as String.
     */
    public static void createDirectories(File folderPath) {
        createDirectories(folderPath.toString());
    }

    /**
     * It creates directories recursively, all path part will be a folder type.
     *
     * @param folderPath as String.
     */
    public static void createDirectories(Path folderPath) {
        createDirectories(folderPath.toString());
    }

    /**
     * It creates directories recursively, all path part will be a folder type.
     *
     * @param folderPath as String.
     */
    public static void createDirectories(String folderPath) {
        catchIoExAndReturn(() -> Files.createDirectories(get(folderPath)));
    }

    /**
     * It returns simple list of files.
     *
     * @param pathToFile path to file as simple text
     * @return list of files
     */
    public static List<File> listOfFiles(String pathToFile) {
        return listOfFiles(new File(pathToFile));
    }

    /**
     * It returns simple list of files.
     *
     * @param path by java Path
     * @return list of files
     */
    public static List<File> listOfFiles(Path path) {
        return listOfFiles(path.toFile());
    }

    /**
     * It returns simple list of files.
     *
     * @param rootFile as simple java file
     * @return list of files
     */
    public static List<File> listOfFiles(File rootFile) {
        return listOfFiles(rootFile, file -> true);
    }

    /**
     * It returns list of files which were filtered by fileFilter.
     *
     * @param pathToFile path to file as simple text
     * @return list of files filtered by FileFilter instance
     */
    public static List<File> listOfFiles(String pathToFile, FileFilter fileFilter) {
        return listOfFiles(new File(pathToFile), fileFilter);
    }

    /**
     * It returns list of files which were filtered by fileFilter.
     *
     * @param path by java Path
     * @return list of files filtered by FileFilter instance
     */
    public static List<File> listOfFiles(Path path, FileFilter fileFilter) {
        return listOfFiles(path.toFile(), fileFilter);
    }

    /**
     * It returns list of files which were filtered by fileFilter.
     *
     * @param rootFile as simple java file
     * @return list of files filtered by FileFilter instance
     */
    public static List<File> listOfFiles(File rootFile, FileFilter fileFilter) {
        File[] files = rootFile.listFiles();
        if (files == null) {
            throw new FileException("Provided path: " + rootFile.getAbsolutePath() + " does not exist");
        }
        return elements(files)
            .filter(fileFilter::accept)
            .asList();
    }

    public static List<Path> listOfFilesRecursively(File rootFile) {
        return listOfFilesRecursively(rootFile, (f) -> true);
    }

    public static List<Path> listOfFilesRecursively(Path rootFile) {
        return listOfFilesRecursively(rootFile, (f) -> true);
    }

    public static List<Path> listOfFilesRecursively(String rootFile) {
        return listOfFilesRecursively(rootFile, (f) -> true);
    }

    public static List<Path> listOfFilesRecursively(File rootFile, Predicate<Path> fileFilter) {
        return listOfFilesRecursively(rootFile.toPath(), fileFilter);
    }

    public static List<Path> listOfFilesRecursively(String rootFile, Predicate<Path> fileFilter) {
        return listOfFilesRecursively(get(rootFile), fileFilter);
    }

    public static List<Path> listOfFilesRecursively(Path rootFile, Predicate<Path> fileFilter) {
        return elements(
            catchIoExAndReturn(() ->
                Files.walk(rootFile)
                    .filter(fileFilter)
            )
        ).asList();
    }

    /**
     * It removes whole directory or file.
     *
     * @param pathAsText path to file as simple text
     */
    public static void deleteFileOrDirectory(String pathAsText) {
        deleteFileOrDirectory(new File(pathAsText));
    }

    /**
     * It removes whole directory or file.
     *
     * @param file as simple java file
     */
    public static void deleteFileOrDirectory(File file) {
        if (file.isDirectory()) {
            List<File> files = listOfFiles(file);
            for (File childField : files) {
                deleteFileOrDirectory(childField);
            }
        }
        boolean deleted = file.delete();
        if (!deleted) {
            throw new FileException("cannot delete file: " + file.getAbsolutePath());
        }
    }

    /**
     * It removes whole directory or file.
     *
     * @param path by java Path
     */
    public static void deleteFileOrDirectory(Path path) {
        deleteFileOrDirectory(path.toFile());
    }

    static <T> T catchIoExAndReturn(IOExceptionSupplier<T> throwableSupplier) {
        try {
            return throwableSupplier.get();
        } catch (IOException ex) {
            throw new FileException(ex);
        }
    }

    static void catchIoEx(IOExceptionRunnable ioExceptionRunnable) {
        try {
            ioExceptionRunnable.run();
        } catch (IOException ex) {
            throw new FileException(ex);
        }
    }

    @FunctionalInterface
    interface IOExceptionSupplier<T> {

        /**
         * Gets a result.
         *
         * @return a result
         */
        T get() throws IOException;
    }

    @FunctionalInterface
    interface IOExceptionRunnable {

        void run() throws IOException;
    }
}
