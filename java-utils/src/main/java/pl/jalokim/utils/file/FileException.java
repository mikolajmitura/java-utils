package pl.jalokim.utils.file;

import java.io.IOException;

/**
 * Wrapper class for IOException types.
 */
public class FileException extends RuntimeException {

    private static final long serialVersionUID = -2267356725601058L;

    public FileException(IOException cause) {
        super(cause);
    }
}
