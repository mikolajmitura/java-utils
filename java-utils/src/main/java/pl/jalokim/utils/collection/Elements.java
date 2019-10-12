package pl.jalokim.utils.collection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Simpler API than native java Stream API.
 * Contains some shortcut methods for return some types.
 * @param <T>
 */
public class Elements<T> {

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

    public List<T> asList() {
        return unmodifiableList(new ArrayList<>(stream.collect(toList())));
    }

    public Set<T> asSet() {
        return unmodifiableSet(new HashSet<>(stream.collect(toSet())));
    }

    public T[] asArray(T[] array) {
        return stream.collect(Collectors.toList()).toArray(array);
    }

    public Stream<T> asStream() {
        return stream;
    }
}
