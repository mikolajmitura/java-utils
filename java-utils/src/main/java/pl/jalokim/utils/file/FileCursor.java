package pl.jalokim.utils.file;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.util.Iterator;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newBufferedReader;
import static java.nio.file.Paths.get;
import static pl.jalokim.utils.file.FileUtils.catchIoExAndReturn;

/**
 * This class is useful for read next line, can check that file contains next line.
 * Can return current line number from file, can return current line a few times.
 */
public class FileCursor implements Iterator<String> {

    private final BufferedReader bufferedReader;
    private String currentLine;
    private String nextLine;
    private long currentLineNumber;


    /**
     * Create instance of FileCursor.
     * @param filePath from system path.
     */
    public FileCursor(String filePath) {
        this(filePath, UTF_8);
    }

    /**
     * Create instance of FileCursor.
     * @param filePath from system path.
     */
    public FileCursor(String filePath, Charset charset) {
        bufferedReader = catchIoExAndReturn(() -> newBufferedReader(get(filePath), charset));
        nextLine = catchIoExAndReturn(bufferedReader::readLine);
    }

    public String getCurrentLine() {
        return currentLine;
    }

    /**
     * First line number will be equals 1.
     * @return number of line.
     */
    public Long getLineNumber() {
        return currentLineNumber;
    }

    @Override
    public boolean hasNext() {
        return nextLine != null;
    }

    @Override
    public String next() {
        currentLine = nextLine;
        nextLine = catchIoExAndReturn(bufferedReader::readLine);
        currentLineNumber++;
        return currentLine;
    }

    public void close() {
        catchIoExAndReturn(() -> {
            bufferedReader.close();
            return null;
        });
    }
}
