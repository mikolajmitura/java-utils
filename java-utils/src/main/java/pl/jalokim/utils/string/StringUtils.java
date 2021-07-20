package pl.jalokim.utils.string;

import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static pl.jalokim.utils.constants.Constants.EMPTY;
import static pl.jalokim.utils.constants.Constants.NEW_LINE;
import static pl.jalokim.utils.constants.Constants.TAB;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import pl.jalokim.utils.collection.Elements;

/**
 * Utils methods for String.
 */
public final class StringUtils {

    private StringUtils() {

    }

    /**
     * Check that text is empty or null.
     *
     * @param text to check.
     * @return boolean value
     */
    public static boolean isEmpty(String text) {
        return text == null || text.isEmpty();
    }

    /**
     * Check that text is not empty.
     *
     * @param text to check.
     * @return boolean value
     */
    public static boolean isNotEmpty(String text) {
        return !isEmpty(text);
    }

    /**
     * If whole text contains only white signs then return true.
     *
     * @param text to check.
     * @return boolean value
     */
    public static boolean isBlank(String text) {
        return text == null || isBlank(text.toCharArray());
    }

    private static boolean isBlank(char... chars) {
        boolean result = true;
        for (char currentChar : chars) {
            result = result && Character.isWhitespace(currentChar);
            if (!result) {
                break;
            }
        }
        return result;
    }

    /**
     * If whole text doesn't contain only white signs then return true.
     *
     * @param text to check.
     * @return boolean value
     */
    public static boolean isNotBlank(String text) {
        return !isBlank(text);
    }

    /**
     * Return concatenated text all list elements as new lines.
     *
     * @param elements to concatenate
     * @return concatenated text.
     */
    public static <E> String concatElementsAsLines(List<E> elements) {
        return concatElements(elements, NEW_LINE);
    }

    /**
     * Return concatenated text all array elements as new lines.
     *
     * @param elements to concatenate
     * @return concatenated text.
     */
    public static String concatElementsAsLines(Object... elements) {
        return concatElements(asList(elements), NEW_LINE);
    }

    /**
     * Return concatenated text from mapped all array elements to text as new lines.
     *
     * @param elements to concatenate
     * @return concatenated text.
     */
    public static <E> String concatElementsAsLines(Function<E, String> mapper, E... elements) {
        return concatElements(asList(elements), mapper, NEW_LINE);
    }

    /**
     * Return concatenated text all list elements as new lines.
     *
     * @param elements to concatenate
     * @param mapper   for from E type to string
     * @param <E>      generic type for
     * @return concatenated text.
     */
    public static <E> String concatElementsAsLines(List<E> elements, Function<E, String> mapper) {
        return concatElements(elements,
                              mapper,
                              NEW_LINE);
    }

    /**
     * It return N times of tabulator.
     *
     * @param tabsNumber number of Tabs
     * @return created n times tabs.
     */
    public static String tabsNTimes(int tabsNumber) {
        return repeatTextNTimes(tabsNumber, TAB);
    }

    /**
     * It create text with n times repeated text.
     *
     * @param nTimes number to repeat
     * @param text   value to repeat
     * @return created n times text.
     */
    public static String repeatTextNTimes(int nTimes, String text) {
        return String.join(EMPTY, nCopies(nTimes, text));
    }

    /**
     * Concatenate elements with empty text. On every object will be called toString().
     *
     * @param collection elements to concatenate
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElements(Collection<E> collection) {
        return concatElements(collection, EMPTY);
    }

    /**
     * Concatenate elements with empty text. On every object will be called toString().
     *
     * @param elements   elements to concatenate
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElements(Elements<E> elements) {
        return concatElements(elements.asList(), EMPTY);
    }

    /**
     * Concatenate elements with empty text with mapper from E type to String.
     *
     * @param collection elements to concatenate
     * @param mapper     from some type to String
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElements(Collection<E> collection, Function<E, String> mapper) {
        return concatElements(collection, mapper, EMPTY);
    }

    /**
     * Concatenate elements with joinText value.
     *
     * @param elements   elements to concatenate
     * @param joinText   value between all texts.
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElements(Elements<E> elements, String joinText) {
        return concatElements(elements.asList(), Object::toString, joinText);
    }

    /**
     * Concatenate elements with joinText value.
     *
     * @param collection elements to concatenate
     * @param joinText   value between all texts.
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElements(Collection<E> collection, String joinText) {
        return concatElements(collection, Object::toString, joinText);
    }

    /**
     * Concatenate elements with joinText value with mapper from E type to String.
     *
     * @param collection elements to concatenate
     * @param mapper     from some type to String
     * @param joinText   value between all texts.
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElements(Collection<E> collection, Function<E, String> mapper, String joinText) {
        return concatElements(EMPTY, collection, (t) -> true,  mapper, joinText, EMPTY);
    }

    /**
     * Concatenate elements with prefix, join text between all elements and with suffix value with mapper from E type to String.
     *
     * @param textPrefix text before all concatenated text
     * @param collection elements to concatenate
     * @param mapper     from some type to String
     * @param joinText   value between all texts.
     * @param textSuffix text after all concatenated text
     * @param <E>        generic type of collection.
     * @return concatenated text with empty text.
     */
    public static <E> String concatElements(String textPrefix, Collection<E> collection, Function<E, String> mapper,
        String joinText, String textSuffix) {
        return concatElements(textPrefix, collection, (e) -> true, mapper, joinText, textSuffix);
    }

    /**
     * Concatenate elements with prefix, join text between all elements and with suffix value skip by filter and map by mapper from E type to String.
     *
     * @param textPrefix text before all concatenated text
     * @param collection elements to concatenate
     * @param filter     predicate filter
     * @param mapper     from some type to String
     * @param joinText   value between all texts.
     * @param textSuffix text after all concatenated text
     * @param <E>        generic type of collection.
     * @return concatenated text with empty text.
     */
    public static <E> String concatElements(String textPrefix, Collection<E> collection, Predicate<E> filter, Function<E, String> mapper,
        String joinText, String textSuffix) {
        return textPrefix.concat(collection.stream()
            .filter(filter)
            .map(mapper)
            .collect(Collectors.joining(joinText))
        ).concat(textSuffix);
    }

    /**
     * Concatenate elements with prefix, join text between all elements and with suffix value with mapper from E type to String.
     *
     * @param textPrefix text before all concatenated text
     * @param collection elements to concatenate
     * @param joinText   value between all texts.
     * @param textSuffix text after all concatenated text
     * @param <E>        generic type of collection.
     * @return concatenated text with empty text.
     */
    public static <E> String concatElements(String textPrefix, Collection<E> collection,
        String joinText, String textSuffix) {
        return concatElements(textPrefix, collection, (t) -> true, Objects::toString, joinText, textSuffix);
    }

    /**
     * This method concatenate all texts from array with join Text.
     *
     * @param joinText between all varargs.
     * @param texts    varargs for text.
     * @return concatenated text
     */
    public static String concatElements(String joinText, String... texts) {
        return concatElements(asList(texts), joinText);
    }

    /**
     * Concatenate elements with empty text but skip null objects. On every object will be called toString().
     *
     * @param elements   elements to concatenate
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElementsSkipNulls(Elements<E> elements) {
        return concatElementsSkipNulls(elements.asList(), EMPTY);
    }

    /**
     * Concatenate elements with empty text but skip null objects. On every object will be called toString().
     *
     * @param collection elements to concatenate
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElementsSkipNulls(Collection<E> collection) {
        return concatElementsSkipNulls(collection, EMPTY);
    }

    /**
     * Concatenate elements with joinText value but skip null objects.
     *
     * @param collection elements to concatenate
     * @param joinText   value between all texts.
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElementsSkipNulls(Collection<E> collection, String joinText) {
        return concatElementsSkipNulls(collection, Object::toString, joinText);
    }

    /**
     * Concatenate elements with joinText value but skip null objects.
     *
     * @param eElements  elements to concatenate
     * @param joinText   value between all texts.
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElementsSkipNulls(Elements<E> eElements, String joinText) {
        return concatElementsSkipNulls(eElements.asList(), Object::toString, joinText);
    }

    /**
     * Concatenate elements with joinText value with mapper from E type to String but skip null objects.
     *
     * @param collection elements to concatenate
     * @param mapper     from some type to String
     * @param joinText   value between all texts.
     * @param <E>        generic type of collection.
     * @return concatenated text.
     */
    public static <E> String concatElementsSkipNulls(Collection<E> collection, Function<E, String> mapper, String joinText) {
        return concatElements(EMPTY, collection, Objects::nonNull,  mapper, joinText, EMPTY);
    }

    /**
     * concatenate all elements with empty string value.
     *
     * @param texts to concatenate
     * @return concatenated text
     */
    public static String concat(String... texts) {
        return concatElements(asList(texts), EMPTY);
    }

    /**
     * concatenate all object with empty string value.
     *
     * @param texts to concatenate
     * @return concatenated text
     */
    public static String concatObjects(Object... texts) {
        return concatElements(asList(texts), EMPTY);
    }

    /**
     * It returns number of searched char in provided text.
     *
     * @param text         for search
     * @param searchedChar char which is searched
     * @return number how many times it is in this text
     */
    public static int countSearchedChar(String text, char searchedChar) {
        char[] chars = text.toCharArray();
        int counter = 0;
        for (char currentChar : chars) {
            if (currentChar == searchedChar) {
                counter++;
            }
        }
        return counter;
    }
}
