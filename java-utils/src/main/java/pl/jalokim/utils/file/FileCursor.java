package pl.jalokim.utils.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;

import static pl.jalokim.utils.file.FileUtils.catchIoEx;

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
        bufferedReader = catchIoEx(() -> new BufferedReader(new FileReader(filePath)));
        nextLine = catchIoEx(bufferedReader::readLine);
    }

    public String getCurrentLine() {
        return currentLine;
    }

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
        nextLine = catchIoEx(bufferedReader::readLine);
        currentLineNumber++;
        return currentLine;
    }

    public void close() {
        catchIoEx(() -> {
            bufferedReader.close();
            return null;
        });
    }
}
