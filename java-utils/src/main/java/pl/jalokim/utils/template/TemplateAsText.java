package pl.jalokim.utils.template;

import static java.util.Objects.requireNonNull;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pl.jalokim.utils.file.FileUtils;

/**
 * Utils class for some text with template, can override some placeholders. It can throw exception when some placeholder is not resolved. But to enable this
 * please create instance of TemplateAsText by one of method which contains throwExceptionForNotResolved flag and set it to true. When throw exception is set to
 * true then Every text like ${name_of_placeholder} will be recognized as placeholder when name of place holder contains [a-zA-Z_0-9] when contains all letters
 * from 'a' to 'z' and from 'A' to 'Z' and contains number, or sign '_' and '-'
 */
public final class TemplateAsText {

    private static final String VAR_PATTERN = "\\$\\{%s}";

    private final boolean throwExceptionForNotResolved;
    private String templateText;

    private TemplateAsText(String templateText, boolean throwExceptionForNotResolved) {
        this.throwExceptionForNotResolved = throwExceptionForNotResolved;
        this.templateText = templateText;
    }

    private TemplateAsText(String templateText) {
        this(templateText, false);
    }

    /**
     * Load text with placeholders from classpath.
     *
     * @param resourcePath name of resource
     * @return instance of TemplateAsText
     */
    public static TemplateAsText fromClassPath(String resourcePath) {
        return new TemplateAsText(FileUtils.readAsTextFromClassPath(resourcePath));
    }

    /**
     * Load text with placeholders from classpath and set flag for throwExceptionForNotResolved.
     *
     * @param resourcePath name of resource
     * @param throwExceptionForNotResolved it setup that will be throw exception when will not resolved all placeholder during invoke {@link
     * #getCurrentTemplateText()}
     * @return instance of TemplateAsText
     */
    public static TemplateAsText fromClassPath(String resourcePath, boolean throwExceptionForNotResolved) {
        return new TemplateAsText(FileUtils.readAsTextFromClassPath(resourcePath), throwExceptionForNotResolved);
    }

    /**
     * Load text with placeholders from file from certain file path.
     *
     * @param filePath path for file
     * @return instance of TemplateAsText
     */
    public static TemplateAsText fromFile(String filePath) {
        return new TemplateAsText(FileUtils.readAsText(filePath));
    }

    /**
     * Load text with placeholders from file from certain file path and set flag for throwExceptionForNotResolved.
     *
     * @param filePath path for file
     * @param throwExceptionForNotResolved it setup that will be throw exception when will not resolved all placeholder during invoke {@link
     * #getCurrentTemplateText()}
     * @return instance of TemplateAsText
     */
    public static TemplateAsText fromFile(String filePath, boolean throwExceptionForNotResolved) {
        return new TemplateAsText(FileUtils.readAsText(filePath), throwExceptionForNotResolved);
    }

    /**
     * Load text with placeholders from simple String object.
     *
     * @param templateText text with placeholders
     * @return instance of TemplateAsText
     */
    public static TemplateAsText fromText(String templateText) {
        return new TemplateAsText(templateText);
    }

    /**
     * * Load text with placeholders from simple String object and set flag for throwExceptionForNotResolved.
     *
     * @param templateText text with placeholders
     * @param throwExceptionForNotResolved it setup that will be throw exception when will not resolved all placeholder during invoke {@link
     * #getCurrentTemplateText()}
     * @return instance of TemplateAsText
     */
    public static TemplateAsText fromText(String templateText, boolean throwExceptionForNotResolved) {
        return new TemplateAsText(templateText, throwExceptionForNotResolved);
    }

    /**
     * Override some placeholder.
     *
     * @param varName value of placeholder name
     * @param value value for setup.
     */
    public TemplateAsText overrideVariable(String varName, String value) {
        requireNonNull(value, "Value for variable: '" + varName + "' cannot be null");

        templateText = templateText.replaceAll(
            String.format(VAR_PATTERN, varName),
            value.replace("$", "\\$"));
        return this;
    }

    /**
     * Override some variables given in arguemnt valuesByArgNames
     *
     * @param valuesByArgNames map with placeholders name and values
     * @return instance of TemplateAsText
     */
    public TemplateAsText overrideVariables(Map<String, ?> valuesByArgNames) {
        valuesByArgNames.forEach((name, value) -> overrideVariable(name, value.toString()));
        return this;
    }

    /**
     * Returns value for already resolved placeholders.
     *
     * @return text with already resolved placeholders or throw exception when some placeholder not resolved only when throwExceptionForNotResolved is set to
     * true.
     */
    public String getCurrentTemplateText() {
        if (throwExceptionForNotResolved) {
            Pattern pattern = Pattern.compile(String.format(VAR_PATTERN, "(\\w|-)+"));
            Matcher matcher = pattern.matcher(templateText);

            List<String> notResolved = new ArrayList<>();

            while (matcher.find()) {
                notResolved.add(matcher.group(0));
            }
            if (isNotEmpty(notResolved)) {
                throw new IllegalArgumentException("Not resolved placeholders: " + notResolved);
            }
        }
        return templateText;
    }
}
