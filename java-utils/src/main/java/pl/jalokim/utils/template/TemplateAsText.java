package pl.jalokim.utils.template;

import static pl.jalokim.utils.file.FileUtils.loadFileFromClassPathAsText;

/**
 * Utils class for some text with template, can override some placeholders.
 */
public class TemplateAsText {

    private static final String VAR_PATTERN = "\\$\\{%s}";
    private String templateText;

    private TemplateAsText(String templateText) {
        this.templateText = templateText;
    }

    public static TemplateAsText fromFile(String filePath) {
        return new TemplateAsText(loadFileFromClassPathAsText(filePath));
    }

    public static TemplateAsText fromText(String templateText) {
        return new TemplateAsText(templateText);
    }

    public void overrideVariable(String varName, String value) {
        if (value == null) {
            throw new NullPointerException("value for variable: " + varName + "cannot be null");
        }
        templateText = templateText.replaceAll(
                String.format(VAR_PATTERN, varName),
                value.replace("$", "\\$"));
    }

    public String getCurrentTemplateText() {
        return templateText;
    }
}
