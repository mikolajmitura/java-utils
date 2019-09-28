package pl.jalokim.utils.string;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.nCopies;
import static pl.jalokim.utils.constants.Constants.EMPTY;
import static pl.jalokim.utils.constants.Constants.NEW_LINE;
import static pl.jalokim.utils.constants.Constants.TAB;

public class StringUtils {

    public boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    public boolean isNotEmpty(String text) {
        return !isEmpty(text);
    }

    public boolean isBlank(String text) {
        return text == null || isBlank(text.toCharArray());
    }

    public boolean isNotBlank(String text) {
        return !isBlank(text);
    }

    private boolean isBlank(char... chars) {
        boolean result = true;
        for(char aChar : chars) {
            result = result && Character.isWhitespace(aChar);
            if(!result) {
                break;
            }
        }
        return result;
    }

    public static String concatElementsAsLines(List<String> elements) {
        return concatElements(elements,
                              NEW_LINE);
    }

    public static <E> String concatElementsAsLines(List<E> elements, Function<E, String> mapper) {
        return concatElements(elements,
                              mapper,
                              NEW_LINE);
    }

    public static String tabsNTimes(int tabsNumber) {
        return repeatTextNTimes(tabsNumber, TAB);
    }

    public static String repeatTextNTimes(int nTimes, String text) {
        return String.join(EMPTY, nCopies(nTimes, text));
    }

    public static <E> String concatElements(Collection<E> collection) {
        return concatElements(collection, EMPTY);
    }

    public static <E> String concatElements(Collection<E> collection, Function<E, String> mapper) {
        return concatElements(collection, mapper, EMPTY);
    }

    public static <E> String concatElements(Collection<E> collection, String joinText) {
        return concatElements(collection, Object::toString, joinText);
    }

    public static <E> String concatElements(Collection<E> collection, Function<E, String> mapper, String joinText) {
        return concatElements(EMPTY, collection, mapper, joinText, EMPTY);
    }

    public static <E> String concatElements(String textPrefix, Collection<E> collection, Function<E, String> mapper,
                                                 String joinText, String textSuffix) {
        return textPrefix.concat(collection.stream()
                                           .map(mapper)
                                           .collect(Collectors.joining(joinText))
                                ).concat(textSuffix);
    }

    public static String concatElements(String joinText, String... texts) {
        return concatElements(Arrays.asList(texts), joinText);
    }

    public static String concat(String... texts) {
        return concatElements(Arrays.asList(texts), EMPTY);
    }
}
