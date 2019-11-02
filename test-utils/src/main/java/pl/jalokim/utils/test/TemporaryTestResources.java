package pl.jalokim.utils.test;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 * Useful class for test with temporary folder.
 */
public class TemporaryTestResources {

    public static final String SLASH = "/";
    public static final String BACK_SLASH = "\\";

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected String getPathForFileInTempFolder(String fileName) {
        return tempFolder.getRoot().toString().replace(BACK_SLASH, SLASH)
                         .concat(SLASH)
                         .concat(fileName);
    }

    protected String getPathForTempFolder() {
        return tempFolder.getRoot().toString().replace(BACK_SLASH, SLASH);
    }
}
