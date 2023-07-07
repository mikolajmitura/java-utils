package pl.jalokim.utils.template;

import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.Test;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.string.StringUtils.equalsIgnoreLineEndings;
import static pl.jalokim.utils.template.TemplateAsText.fromClassPath;
import static pl.jalokim.utils.template.TemplateAsText.fromFile;
import static pl.jalokim.utils.template.TemplateAsText.fromText;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

public class TemplateAsTextTest {

    private static final String FILE_NAME = "some_placeholders";
    private static final String RESOURCES_PATH = "src/test/resources";

    private static final String TEMPLATE_AS_TEXT = String.format("some text with: ${placeholder_name}%n" +
        "some text with: ${another-placeholder}%n" +
        "this one will not throw exception: ${test^test}%n" +
        "the same text like in first with: '${placeholder_name}' end text.%n" +
        "other text with: ${placeholder_name_5}");

    @Test
    public void fromClassPathNotExceptionWhileNotResolvedPlaceholders() {
        // given
        TemplateAsText templateAsText = fromClassPath(FILE_NAME);
        // when
        templateAsText.overrideVariable("placeholder_name", "SOME TEXT");
        String currentTemplateText = templateAsText.getCurrentTemplateText();
        // then
        assertThat(equalsIgnoreLineEndings(currentTemplateText, format("some text with: SOME TEXT%n" +
            "some text with: ${another-placeholder}%n" +
            "this one will not throw exception: ${test^test}%n" +
            "the same text like in first with: 'SOME TEXT' end text.%n" +
            "other text with: ${placeholder_name_5}"))).isTrue();
    }

    @Test
    public void fromClassPathExceptionWhileNotResolvedPlaceholders() {
        // given
        TemplateAsText templateAsText = fromClassPath(FILE_NAME, true);
        // when
        templateAsText.overrideVariable("placeholder_name", "SOME TEXT");
        when(templateAsText::getCurrentTemplateText)
            .thenException(
                new IllegalArgumentException("Not resolved placeholders: [${another-placeholder}, ${placeholder_name_5}]"));
    }

    @Test
    public void fromFileNotExceptionWhileNotResolvedPlaceholders() {
        // given
        TemplateAsText templateAsText = fromFile(RESOURCES_PATH + "/" + FILE_NAME);
        // when
        templateAsText.overrideVariable("placeholder_name", "SOME TEXT");
        String currentTemplateText = templateAsText.getCurrentTemplateText();
        // then
        assertThat(equalsIgnoreLineEndings(currentTemplateText, format("some text with: SOME TEXT%n" +
            "some text with: ${another-placeholder}%n" +
            "this one will not throw exception: ${test^test}%n" +
            "the same text like in first with: 'SOME TEXT' end text.%n" +
            "other text with: ${placeholder_name_5}"))).isTrue();
    }

    @Test
    public void fromFileExceptionWhileNotResolvedPlaceholders() {
        // given
        TemplateAsText templateAsText = fromFile(RESOURCES_PATH + "/" + FILE_NAME, true);
        // when
        templateAsText.overrideVariable("placeholder_name", "SOME TEXT");
        when(templateAsText::getCurrentTemplateText)
            .thenException(
                new IllegalArgumentException("Not resolved placeholders: [${another-placeholder}, ${placeholder_name_5}]"));
    }

    @Test
    public void fromTextNotExceptionWhileNotResolvedPlaceholders() {
        // when
        TemplateAsText templateAsText = fromText(TEMPLATE_AS_TEXT)
            .overrideVariable("placeholder_name", "SOME TEXT");
        String currentTemplateText = templateAsText.getCurrentTemplateText();
        // then
        assertThat(currentTemplateText).isEqualTo(format("some text with: SOME TEXT%n" +
            "some text with: ${another-placeholder}%n" +
            "this one will not throw exception: ${test^test}%n" +
            "the same text like in first with: 'SOME TEXT' end text.%n" +
            "other text with: ${placeholder_name_5}"));
    }

    @Test
    public void fromTextExceptionWhileNotResolvedPlaceholders() {
        // given
        TemplateAsText templateAsText = fromText(TEMPLATE_AS_TEXT, true);
        // when
        templateAsText.overrideVariable("placeholder_name", "SOME TEXT");
        when(templateAsText::getCurrentTemplateText)
            .thenException(
                new IllegalArgumentException("Not resolved placeholders: [${another-placeholder}, ${placeholder_name_5}]"));
    }

    @Test
    public void allPlaceholdersResolvedWhenCanThrowException() {
        // when
        TemplateAsText templateAsText = fromText(TEMPLATE_AS_TEXT, true)
            .overrideVariable("placeholder_name", "SOME TEXT")
            .overrideVariable("another-placeholder", "another")
            .overrideVariable("placeholder_name_5", "TEST_1_2");
        String currentTemplateText = templateAsText.getCurrentTemplateText();
        // then
        assertThat(currentTemplateText).isEqualTo(format("some text with: SOME TEXT%n" +
            "some text with: another%n" +
            "this one will not throw exception: ${test^test}%n" +
            "the same text like in first with: 'SOME TEXT' end text.%n" +
            "other text with: TEST_1_2"));
    }

    @Test
    public void allPlaceholdersByMapResolvedWhenCanThrowException() {
        // given
        Map<String, Object> valuesByArgName = Maps.of(
            "placeholder_name", "SOME TEXT",
            "another-placeholder", "another",
        "placeholder_name_5", "TEST_1_2"
        );
        // when
        TemplateAsText templateAsText = fromText(TEMPLATE_AS_TEXT, true)
            .overrideVariables(valuesByArgName);
        String currentTemplateText = templateAsText.getCurrentTemplateText();
        // then
        assertThat(currentTemplateText).isEqualTo(format("some text with: SOME TEXT%n" +
            "some text with: another%n" +
            "this one will not throw exception: ${test^test}%n" +
            "the same text like in first with: 'SOME TEXT' end text.%n" +
            "other text with: TEST_1_2"));
    }

    @Test
    public void valueForVariableCannotBeNull() {
        // given
        TemplateAsText templateAsText = fromText(TEMPLATE_AS_TEXT, true);
        when(() ->
            templateAsText.overrideVariable("placeholder_name", null))
            .thenException(
                NullPointerException.class,
                "Value for variable: 'placeholder_name' cannot be null");
    }
}
