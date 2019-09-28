
package pl.jalokim.utils.file;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Useful class for Files.
 */
public final class FileUtils {

    private FileUtils() {

    }

    public static String loadFileFromClassPathAsText(String path) {
        try {
            URL url = Resources.getResource(path);
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new FileException(e);
        }
    }

    public static String loadFileFromPathAsText(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), UTF_8);
        } catch (IOException e) {
            throw new FileException(e);
        }
    }
}
