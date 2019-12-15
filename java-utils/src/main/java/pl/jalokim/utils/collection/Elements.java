package pl.jalokim.utils.collection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static pl.jalokim.utils.collection.CollectionUtils.isLastIndex;
import static pl.jalokim.utils.constants.Constants.COMMA;
import static pl.jalokim.utils.constants.Constants.SPACE;
import static pl.jalokim.utils.string.StringUtils.concat;
import static pl.jalokim.utils.string.StringUtils.concatElements;

/**
 * Simpler API than native java Stream API.
 * Contains some shortcut methods for return some types.
 *
 * @param <T> type of elements.
 */
public final class Elements<T> {

    private final Stream<T> stream;

    private Elements(Stream<T> stream) {
        this.stream = stream;
    }

    public static <T> Elements<T> elements(Iterable<T> iterable) {
        return new Elements<>(StreamSupport.stream(iterable.spliterator(), false));
    }

    public static <T> Elements<T> elements(T... array) {
        return new Elements<>(Stream.of(array));
    }

    public static <T> Elements<T> elements(Stream<T> stream) {
        return new Elements<>(stream);
    }

    public Elements<T> filter(Predicate<T> predicate) {
        return new Elements<>(stream.filter(predicate));
    }

    public <R> Elements<R> map(Function<? super T, ? extends R> mapper) {
        return new Elements<>(this.stream.map(mapper));
    }

    public <R> Elements<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return new Elements<>(this.stream.flatMap(mapper));
    }

    public T getFirst() {
        return stream.findFirst().get();
    }

    public T getLast() {
        return CollectionUtils.getLast(asList());
    }

    public List<T> asList() {
        return unmodifiableList(new ArrayList<>(stream.collect(toList())));
    }

    public Set<T> asSet() {
        return unmodifiableSet(new HashSet<>(stream.collect(toSet())));
    }

    public <K> Map<K, T> asMap(Function<? super T, ? extends K> keyMapper) {
        return stream.collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    public <K, V> Map<K, V> asMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends V> valueMapper) {
        return stream.collect(Collectors.toMap(keyMapper, valueMapper));
    }

    @SuppressWarnings("PMD.UseVarargs")
    public T[] asArray(T[] array) {
        return stream.collect(toList()).toArray(array);
    }

    public Stream<T> asStream() {
        return stream;
    }

    /**
     * Concatenate all elements in stream to String.
     *
     * @return elements separated ', ' (comma and space) argument within '[element[0], element[1]...]'
     */
    @Override
    public String toString() {
        return asText();
    }

    /**
     * Concatenate all elements in stream to String with join text.
     *
     * @param joinText text between every element
     * @return elements separated join text argument within '[element[0]${JOIN_TEXT}element[1]...]'
     */
    public String asText(String joinText) {
        return concatElements("[", asList(), joinText, "]");
    }

    /**
     * Concatenate all elements in stream to String.
     *
     * @return elements separated ', ' (comma and space) argument within '[element[0], element[1]...]'
     */
    public String asText() {
        return asText(concat(COMMA, SPACE));
    }

    /**
     * For each with index and element.
     *
     * @param consumer consumer of index and element.
     */
    public void forEach(BiConsumer<Integer, T> consumer) {
        AtomicInteger currentIndex = new AtomicInteger();
        stream.forEach(element -> consumer.accept(currentIndex.getAndIncrement(), element));
    }

    /**
     * For each with index and element and useful method isFirst, isLast.
     *
     * @param consumer for IndexedElement which holds element with T type and index
     */
    public void forEach(Consumer<IndexedElement<T>> consumer) {
        List<T> elements = asList();
        int index = 0;
        for (T element : elements) {
            consumer.accept(new IndexedElement<>(index, element, index == 0, isLastIndex(elements, index)));
            index++;
        }
    }
}
