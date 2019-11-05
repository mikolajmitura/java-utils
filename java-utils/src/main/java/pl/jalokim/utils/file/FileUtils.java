
package pl.jalokim.utils.file;

import com.google.common.io.Resources;
import pl.jalokim.utils.string.StringUtils;

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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Paths.get;
import static pl.jalokim.utils.collection.Elements.elements;

/**
 * Useful class for Files.
 */
public final class FileUtils {

    private FileUtils() {

    }

    /**
     * It read from file and put all content to String.
     *
     * @param path to file
     * @return text with file content
     */
    public static String loadFileFromPathAsText(String path) {
        return loadFileFromPathAsText(path, UTF_8);
    }

    /**
     * It read from file and put all content to String with certain encoding.
     *
     * @param path    to file
     * @param charset instance of java.nio.charset.Charset
     * @return text with file content
     */
    public static String loadFileFromPathAsText(String path, Charset charset) {
        return catchIoExAndReturn(() -> new String(Files.readAllBytes(get(path)), charset));
    }

    /**
     * It read from file from classpath and put all content to String.
     *
     * @param path to file
     * @return text with file content
     */
    public static String loadFileFromClassPathAsText(String path) {
        return loadFileFromClassPathAsText(path, UTF_8);
    }

    /**
     * It read from file from classpath and put all content to String with certain encoding.
     *
     * @param path    to file
     * @param charset instance of java.nio.charset.Charset
     * @return text with file content
     */
    public static String loadFileFromClassPathAsText(String path, Charset charset) {
        return catchIoExAndReturn(() -> {
            URL url = Resources.getResource(path);
            return Resources.toString(url, charset);
        });
    }

    /**
     * It reads all lines to list from certain path from system for file.
     * It read whole file content to memory.
     *
     * @param path system path to file to read.
     * @return list with all lines from file.
     */
    public static List<String> loadFileFromPathToList(String path) {
        List<String> lines = new ArrayList<>();
        consumeEveryLineFromFile(path, lines::add);
        return lines;
    }

    /**
     * It read all lines line by line and put line value to consumerLine argument.
     * It created for performance purpose.
     *
     * @param path         system path to file to read.
     * @param consumerLine which consume every line
     */
    public static void consumeEveryLineFromFile(String path, Consumer<String> consumerLine) {
        catchIoExAndReturn(() -> {
            try (BufferedReader br = newBufferedReader(get(path))) {
                String line;
                while ((line = br.readLine()) != null) {
                    consumerLine.accept(line);
                }
            }
            return null;
        });
    }

    /**
     * It read all lines line by line and put line value to consumerLine argument.
     * It created for performance purpose.
     *
     * @param path              system path to file to read.
     * @param consumerLineIndex which consume every line with index of them in file
     */
    public static void consumeEveryLineWitNumberFromFile(String path, BiConsumer<Long, String> consumerLineIndex) {
        catchIoExAndReturn(() -> {
            long index = 0;
            try (BufferedReader br = newBufferedReader(get(path))) {
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
     * It override current file content if exists.
     * it not create folders with not exist.
     *
     * @param filePath    system path to file.
     * @param fileContent as String to write to file
     */
    public static void writeToFile(String filePath, String fileContent) {
        Path path = get(filePath);
        byte[] strToBytes = fileContent.getBytes(UTF_8);
        catchIoExAndReturn(() -> Files.write(path, strToBytes));
    }

    /**
     * Append some text to file.
     *
     * @param filePath    system path to file
     * @param fileContent as String to append to file
     */
    public static void appendToFile(String filePath, String fileContent) {
        Path path = get(filePath);
        byte[] strToBytes = fileContent.getBytes(UTF_8);
        catchIoExAndReturn(() -> Files.write(path, strToBytes, StandardOpenOption.APPEND));
    }

    /**
     * It writes all list element to file, every as separated line.
     *
     * @param filePath       system path to file
     * @param elementToWrite text lines write to file
     */
    public static void writeAllElementsAsLinesToFile(String filePath, List<String> elementToWrite) {
        Path path = get(filePath);
        String fileContent = StringUtils.concatElementsAsLines(elementToWrite);
        byte[] strToBytes = fileContent.getBytes(UTF_8);
        catchIoExAndReturn(() -> Files.write(path, strToBytes));
    }

    /**
     * It append all list element to file, every as separated line.
     *
     * @param filePath       system path to file
     * @param elementToWrite text lines write to file
     */
    public static void appendAllElementsAsLinesToFile(String filePath, List<String> elementToWrite) {
        Path path = get(filePath);
        String fileContent = StringUtils.concatElementsAsLines(elementToWrite);
        byte[] strToBytes = fileContent.getBytes(UTF_8);
        catchIoExAndReturn(() -> Files.write(path, strToBytes, StandardOpenOption.APPEND));
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
    public static void createDirectories(String folderPath) {
        if (folderPath != null) {
            catchIoExAndReturn(() -> Files.createDirectories(get(folderPath)));
        }
    }

    public static List<File> listOfFiles(String pathToFile) {
        return listOfFiles(pathToFile, file -> true);
    }

    public static List<File> listOfFiles(String pathToFile, FileFilter fileFilter) {
        File rootFile = new File(pathToFile);
        File[] files = rootFile.listFiles();
        if (files == null) {
            throw new FileException("Provided path: " + pathToFile + " does not exist");
        }
        return elements(files)
                .filter(fileFilter::accept)
                .asList();
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
