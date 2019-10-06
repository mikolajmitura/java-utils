package pl.jalokim.utils.template;

import pl.jalokim.utils.collection.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.file.FileUtils.loadFileFromClassPathAsText;
import static pl.jalokim.utils.file.FileUtils.loadFileFromPathAsText;

/**
 * Utils class for some text with template, can override some placeholders. It can throw exception when some
 * placeholder is not resolved.
 * But to enable this please create instance of TemplateAsText by one of method which contains throwExceptionForNotResolved flag
 * and set it to true.
 * When throw exception is set to true then Every text like ${name_of_placeholder}
 * will be recognized as placeholder when name of place holder contains [a-zA-Z_0-9]
 * when contains all letters from 'a' to 'z' and from 'A' to 'Z' and contains number, or sign '_' and '-'
 */
public class TemplateAsText {

    private static final String VAR_PATTERN = "\\$\\{%s}";

    private final boolean throwExceptionForNotResolved;
    private String templateText;

    public TemplateAsText(String templateText, boolean throwExceptionForNotResolved) {
        this.throwExceptionForNotResolved = throwExceptionForNotResolved;
        this.templateText = templateText;
    }

    private TemplateAsText(String templateText) {
        this(templateText, false);
    }

    public static TemplateAsText fromClassPath(String resourcePath) {
        return new TemplateAsText(loadFileFromClassPathAsText(resourcePath));
    }

    public static TemplateAsText fromClassPath(String resourcePath, boolean throwExceptionForNotResolved) {
        return new TemplateAsText(loadFileFromClassPathAsText(resourcePath), throwExceptionForNotResolved);
    }

    public static TemplateAsText fromFile(String filePath) {
        return new TemplateAsText(loadFileFromPathAsText(filePath));
    }

    public static TemplateAsText fromFile(String filePath, boolean throwExceptionForNotResolved) {
        return new TemplateAsText(loadFileFromPathAsText(filePath), throwExceptionForNotResolved);
    }

    public static TemplateAsText fromText(String templateText) {
        return new TemplateAsText(templateText);
    }

    public static TemplateAsText fromText(String templateText, boolean throwExceptionForNotResolved) {
        return new TemplateAsText(templateText, throwExceptionForNotResolved);
    }

    public void overrideVariable(String varName, String value) {
        if (value == null) {
            throw new NullPointerException("Value for variable: '" + varName + "' cannot be null");
        }
        templateText = templateText.replaceAll(
                String.format(VAR_PATTERN, varName),
                value.replace("$", "\\$"));
    }

    public String getCurrentTemplateText() {
        if (throwExceptionForNotResolved) {
            Pattern pattern = Pattern.compile(String.format(VAR_PATTERN, "(\\w|-)+"));
            Matcher matcher = pattern.matcher(templateText);

            List<String> notResolved = new ArrayList<>();

            while(matcher.find()) {
                notResolved.add(matcher.group(0));
            }
            if (isNotEmpty(notResolved)) {
                throw new IllegalArgumentException("Not resolved placeholders: " + notResolved);
            }
        }
        return templateText;
    }
}
