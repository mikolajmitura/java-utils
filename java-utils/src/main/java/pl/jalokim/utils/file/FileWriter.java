package pl.jalokim.utils.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newOutputStream;
import static pl.jalokim.utils.file.FileUtils.catchIoEx;
import static pl.jalokim.utils.file.FileUtils.catchIoExAndReturn;

import java.io.BufferedWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;

/**
 * Simply class for writing text to file, line by line and with append to file.
 */
public class FileWriter implements AutoCloseable, Flushable {

    private final BufferedWriter bufferedWriter;

    public FileWriter(String filePath) {
        try {
            OutputStream fos = newOutputStream(Paths.get(filePath));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos, UTF_8));
        } catch (IOException e) {
            throw new FileException(e);
        }
    }

    public void append(CharSequence text) {
        catchIoExAndReturn(() -> bufferedWriter.append(text));
    }

    public void appendAndNextLine(CharSequence text) {
        catchIoEx(() -> {
            bufferedWriter.append(text);
            bufferedWriter.newLine();
        });
    }

    public void appendNextLine() {
        catchIoEx(bufferedWriter::newLine);
    }

    @Override
    public void close() {
        catchIoEx(bufferedWriter::close);
    }

    @Override
    public void flush() {
        catchIoEx(bufferedWriter::flush);
    }
}
