
package pl.jalokim.utils.file;

import com.google.common.io.Resources;
import pl.jalokim.utils.string.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Useful class for Files.
 */
public final class FileUtils {

    private FileUtils() {

    }

    /**
     * It read from file and put all content to String
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
        return catchIoEx(() -> new String(Files.readAllBytes(Paths.get(path)), charset));
    }

    /**
     * It read from file from classpath and put all content to String
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
        return catchIoEx(() -> {
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
        catchIoEx(() -> {
            try(BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while((line = br.readLine()) != null) {
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
        catchIoEx(() -> {
            long index = 0;
            try(BufferedReader br = new BufferedReader(new FileReader(path))) {
                String line;
                while((line = br.readLine()) != null) {
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
        Path path = Paths.get(filePath);
        byte[] strToBytes = fileContent.getBytes(UTF_8);
        catchIoEx(() -> Files.write(path, strToBytes));
    }

    /**
     * Append some text to file.
     * @param filePath system path to file
     * @param fileContent as String to append to file
     */
    public static void appendToFile(String filePath, String fileContent) {
        Path path = Paths.get(filePath);
        byte[] strToBytes = fileContent.getBytes(UTF_8);
        catchIoEx(() -> Files.write(path, strToBytes, StandardOpenOption.APPEND));
    }

    /**
     * It writes all list element to file, every as separated line.
     * @param filePath system path to file
     * @param elementToWrite text lines write to file
     */
    public static void writeAllElementsAsLinesToFile(String filePath, List<String> elementToWrite) {
        Path path = Paths.get(filePath);
        String fileContent = StringUtils.concatElementsAsLines(elementToWrite);
        byte[] strToBytes = fileContent.getBytes(UTF_8);
        catchIoEx(() -> Files.write(path, strToBytes));
    }

    /**
     * It append all list element to file, every as separated line.
     * @param filePath system path to file
     * @param elementToWrite text lines write to file
     */
    public static void appendAllElementsAsLinesToFile(String filePath, List<String> elementToWrite) {
        Path path = Paths.get(filePath);
        String fileContent = StringUtils.concatElementsAsLines(elementToWrite);
        byte[] strToBytes = fileContent.getBytes(UTF_8);
        catchIoEx(() -> Files.write(path, strToBytes, StandardOpenOption.APPEND));
    }

    static <T> T catchIoEx(IOExceptionSupplier<T> throwableSupplier) {
        try {
            return throwableSupplier.get();
        } catch(IOException ex) {
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

}
