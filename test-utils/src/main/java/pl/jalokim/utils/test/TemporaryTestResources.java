package pl.jalokim.utils.test;

import lombok.Data;
import lombok.SneakyThrows;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

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

    public TemporaryFolder getTempFolder() {
        return tempFolder;
    }

    @SneakyThrows
    public File newFile(String fileName) {
        return tempFolder.newFile(fileName);
    }

    @SneakyThrows
    public FolderWrapper newFolder(String fileName) {
        return new FolderWrapper(tempFolder.newFolder(fileName), new FolderWrapper(tempFolder.getRoot(), null));
    }

    /**
     * Wrapper for test folder.
     */
    @Data
    public static class FolderWrapper {

        private final File realFolder;
        private final FolderWrapper parent;

        @SneakyThrows
        public FolderWrapper newFolder(String folderName) {
            File file = realFolder;
            file = new File(file, folderName);
            if (!file.mkdir()) {
                throw new IOException(
                        "a folder with the name \'" + folderName + "\' already exists");
            }
            return new FolderWrapper(file, this);
        }

        @SneakyThrows
        public File newFile(String fileName) {
            File file = new File(realFolder, fileName);
            if (!file.createNewFile()) {
                throw new IOException(
                        "a file with the name \'" + fileName + "\' already exists in the test folder");
            }
            return file;
        }
    }
}
