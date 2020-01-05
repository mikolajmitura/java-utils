package pl.jalokim.utils.test;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

public class TemporaryTestResourcesTest extends TemporaryTestResources {

    @Test
    public void testGetPathForFileInTempFolder() {
        // when
        String result = getPathForFileInTempFolder("fileName");
        // then
        assertThat(result).contains("junit", result);
    }

    @Test
    public void testGetPathForTempFolder() {
        // when
        String result = getPathForTempFolder();
        // then
        assertThat(result).contains("junit");
    }

    @Test
    public void testNewFile() {
        // when
        File result = newFile("fileName");
        // then
        assertThat(result).isFile();
        assertThat(result.delete()).isTrue();
    }

    @Test
    public void testNewFolder() {
        // when
        FolderWrapper result = newFolder("fileName");
        // then
        assertThat(result.getRealFolder()).isEmptyDirectory();
        assertThat(result.getRealFolder().delete()).isTrue();
    }

    @Test
    public void cannotCreateNewFile() {
        // given
        newFile("fileName");
        // when
        when(() -> newFile("fileName"))
                .thenException(IOException.class);
    }

    @Test
    public void cannotCreateNewFolder() {
        // given
        newFolder("fileName");
        // when
        when(() -> newFolder("fileName"))
                .thenException(IOException.class);
    }

    @Test
    public void testNewFolderInFolderWrapper() {
        // given
        FolderWrapper tempFolder = newFolder("tempFolder");
        // when
        FolderWrapper nextFolder = tempFolder.newFolder("nextFolder");
        when(() -> tempFolder.newFolder("nextFolder"))
                .thenException(IOException.class);
        // then
        assertThat(nextFolder.getRealFolder()).isEmptyDirectory();
        assertThat(nextFolder.getRealFolder().delete()).isTrue();
    }

    @Test
    public void testNewFileInFolderWrapper() {
        // given
        FolderWrapper tempFolder = newFolder("tempFolder");
        // when
        File newFile = tempFolder.newFile("newFile");
        when(() -> tempFolder.newFile("newFile"))
                .thenException(IOException.class);
        // then
        assertThat(newFile).isFile();
        assertThat(newFile.delete()).isTrue();
    }
}
