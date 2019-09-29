
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

    /*

    TODO
     * odczyt z pliku do stringa
     * odczyt z pliku do listy
     * odczyt z pliku z lambdą na każdą linijke
     * odczyt z pliku z lambdą na każdą linijke oraz z indexem
     * odczyt z pliku z jakimś iteratorem albo kursorem (jaki kursor?)

     * zapis string do pliku do początku (append)
     * zapis string do pliku na końcu (append)
     * zapis Collection<string> do pliku na końcu
     * zapis Collection<string> do początku

     */

    public static String loadFileFromClassPathAsText(String path) {
        try {
            URL url = Resources.getResource(path);
            return Resources.toString(url, Charsets.UTF_8);
        } catch(IOException e) {
            throw new FileException(e);
        }
    }

    public static String loadFileFromPathAsText(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), UTF_8);
        } catch(IOException e) {
            throw new FileException(e);
        }
    }
}
