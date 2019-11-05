package pl.jalokim.utils.file;


import org.junit.Test;
import pl.jalokim.utils.test.TemporaryTestResources;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.file.FileUtils.loadFileFromPathAsText;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

public class FileWriterTest extends TemporaryTestResources {

    @Test
    public void saveContentAsExpected() {
        // given
        String tempFile = getPathForFileInTempFolder("test-file");
        // when
        try (FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.append("text1");
            fileWriter.appendAndNextLine("text2");
            fileWriter.flush();
            String fileContent = loadFileFromPathAsText(tempFile);
            assertThat(fileContent).isEqualTo(String.format("text1text2%n"));
            fileWriter.appendNextLine();
            fileWriter.appendAndNextLine("text3");
        }
        // then
        String fileContent = loadFileFromPathAsText(tempFile);
        assertThat(fileContent).isEqualTo(String.format("text1text2%n%ntext3%n"));
    }

    @Test
    public void cannotCreateFileWriter() {
        when(() -> new FileWriter("/asdfakljaf/alkjsd"))
                .thenException(FileException.class)
                .thenNestedException(IOException.class);
    }
}