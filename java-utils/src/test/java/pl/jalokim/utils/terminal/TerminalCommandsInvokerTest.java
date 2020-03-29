package pl.jalokim.utils.terminal;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import pl.jalokim.utils.test.TemporaryTestResources;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.terminal.TerminalCommandsInvoker.invokeCommand;

public class TerminalCommandsInvokerTest extends TemporaryTestResources {

    private static final String FOLDER_1 = "folder1";
    private static final String NEXT_NEW_FOLDER = "next new_folder";

    @Test
    public void invokeCmdBasedOnSystem() throws IOException {
        TemporaryFolder tempFolder = getTempFolder();
        tempFolder.newFolder(FOLDER_1);
        tempFolder.newFolder(NEXT_NEW_FOLDER);
        // when
        String dirResult = invokeCommand(getPathForTempFolder(), isWindows() ? "dir" : "ls");
        // then
        assertThat(dirResult).contains(FOLDER_1, NEXT_NEW_FOLDER);
    }
}