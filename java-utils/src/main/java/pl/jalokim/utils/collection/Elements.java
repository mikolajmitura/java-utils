package pl.jalokim.utils.collection;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static pl.jalokim.utils.collection.CollectionUtils.isLastIndex;
import static pl.jalokim.utils.constants.Constants.COMMA;
import static pl.jalokim.utils.constants.Constants.SPACE;
import static pl.jalokim.utils.string.StringUtils.concatElements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import pl.jalokim.utils.file.FileUtils;
import pl.jalokim.utils.string.StringUtils;

/**
 * Simpler API than native java Stream API. Contains some shortcut methods for return some types.
 *
 * @param <T> type of elements.
 */
@SuppressWarnings("PMD.ExcessivePublicCount")
public final class Elements<T> implements Stream<T> {

    private final Stream<T> stream;

    private Elements(@Nullable Stream<T> stream) {
        this.stream = Optional.ofNullable(stream)
            .orElse(Stream.empty());
    }

    public static <T> Elements<T> elements(@Nullable Iterable<T> iterable) {
        return new Elements<>(Optional.ofNullable(iterable)
            .map(nonNullIterable -> StreamSupport.stream(nonNullIterable.spliterator(), false))
            .orElse(Stream.empty()));
    }

    public static <T> Elements<T> elements(@Nullable Iterator<T> iterator) {
        return Optional.ofNullable(iterator)
            .map(nonNullIterator -> elements(() -> iterator))
            .orElse(Elements.empty());
    }

    public static <T> Elements<T> elements(@Nullable T... array) {
        return new Elements<>(Optional.ofNullable(array)
            .map(Stream::of)
            .orElse(Elements.empty()));
    }

    public static <T> Elements<T> elements(@Nullable Stream<T> stream) {
        return new Elements<>(stream);
    }

    @Override
    public Elements<T> filter(Predicate<? super T> predicate) {
        return new Elements<>(stream.filter(predicate));
    }

    @Override
    public <R> Elements<R> map(Function<? super T, ? extends R> mapper) {
        return new Elements<>(this.stream.map(mapper));
    }

    public <R> Elements<R> mapWithIndex(BiFunction<Integer, ? super T, ? extends R> mapper) {
        List<R> resultList = new ArrayList<>();
        List<T> sourceList = new Elements<>(this.stream).asList();
        for (int index = 0; index < sourceList.size(); index++) {
            resultList.add(mapper.apply(index, sourceList.get(index)));
        }
        return elements(resultList);
    }

    @Override
    public <R> Elements<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return new Elements<>(this.stream.flatMap(mapper));
    }

    public <R> Elements<R> flatMapByElements(Function<? super T, ? extends Elements<? extends R>> mapper) {
        return new Elements<>(asStream().flatMap(element -> {
            Elements<? extends R> elements = mapper.apply(element);
            return elements.asStream();
        }));
    }

    public <R> Elements<R> flatMapByIterables(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return new Elements<>(asStream().flatMap(element -> {
            Iterable<? extends R> newIterable = mapper.apply(element);
            return elements(newIterable).asStream();
        }));
    }

    public <R> Elements<R> flatMapByArray(Function<? super T, R[]> mapper) {
        return new Elements<>(asStream().flatMap(element -> {
            R[] array = mapper.apply(element);
            return elements(array).asStream();
        }));
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

    public <K> Map<K, List<T>> asMapGroupedBy(Function<? super T, ? extends K> keyMapper) {
        return stream.collect(Collectors.groupingBy(keyMapper));
    }

    @SuppressWarnings("PMD.UseVarargs")
    public T[] asArray(T[] array) {
        return stream.collect(toList()).toArray(array);
    }

    public Stream<T> asStream() {
        return stream;
    }

    public void writeToFile(String filePath) {
        FileUtils.writeToFile(filePath, this.map(Objects::toString));
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
     * Concatenate all elements in stream to String.
     *
     * @return elements separated ', ' (comma and space) argument within '[element[0], element[1]...]'
     */
    public String asText() {
        return asText(StringUtils.concat(COMMA, SPACE));
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

    public String asConcatText(String joinText) {
        return concatElements(asList(), joinText);
    }

    public String concatWithNewLines() {
        return asConcatText(System.lineSeparator());
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

    @Override
    public void forEach(Consumer<? super T> action) {
        stream.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        stream.forEachOrdered(action);
    }

    /**
     * For each with index and element and useful method isFirst, isLast.
     *
     * @param consumer for IndexedElement which holds element with T type and index
     */
    public void forEachWithIndexed(Consumer<IndexedElement<T>> consumer) {
        List<T> elements = asList();
        int index = 0;
        for (T element : elements) {
            consumer.accept(new IndexedElement<>(index, element, index == 0, isLastIndex(elements, index)));
            index++;
        }
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream.mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream.mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream.mapToDouble(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    @Override
    public Elements<T> distinct() {
        return Elements.elements(stream.distinct());
    }

    @Override
    public Elements<T> sorted() {
        return Elements.elements(stream.sorted());
    }

    @Override
    public Elements<T> sorted(Comparator<? super T> comparator) {
        return Elements.elements(stream.sorted(comparator));
    }

    @Override
    public Elements<T> peek(Consumer<? super T> action) {
        return Elements.elements(stream.peek(action));
    }

    @Override
    public Elements<T> limit(long maxSize) {
        return Elements.elements(stream.limit(maxSize));
    }

    @Override
    public Elements<T> skip(long n) {
        return Elements.elements(stream.skip(n));
    }

    @Override
    public Object[] toArray() {
        return stream.toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream.reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator,
        BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream.collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return stream.min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return stream.max(comparator);
    }

    @Override
    public long count() {
        return stream.count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return stream.anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return stream.allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return stream.noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return stream.findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return stream.findAny();
    }

    public Elements<T> concat(Stream<? extends T> toConcat) {
        return Elements.concat(this, toConcat);
    }

    public static <T> Elements<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        return Elements.elements(Stream.concat(a, b));
    }

    public static <T> Elements<T> empty() {
        return Elements.elements(Stream.empty());
    }

    public static <T> Elements<T> of(T t1) {
        return Elements.elements(Stream.of(t1));
    }

    @SafeVarargs
    public static <T> Elements<T> of(@Nullable T... values) {
        return Elements.elements(values);
    }

    public static <T> Elements<T> iterate(T seed, UnaryOperator<T> f) {
        return Elements.elements(Stream.iterate(seed, f));
    }

    public static <T> Elements<T> generate(Supplier<? extends T> s) {
        Stream<T> generate = Stream.generate((Supplier<T>) s);
        return Elements.elements(generate);
    }

    @Override
    public Iterator<T> iterator() {
        return stream.iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return stream.spliterator();
    }

    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    @Override
    public Elements<T> sequential() {
        return Elements.elements(stream.sequential());
    }

    @Override
    public Elements<T> parallel() {
        return Elements.elements(stream.parallel());
    }

    @Override
    public Elements<T> unordered() {
        return Elements.elements(stream.unordered());
    }

    @Override
    public Elements<T> onClose(Runnable closeHandler) {
        return Elements.elements(stream.onClose(closeHandler));
    }

    @Override
    public void close() {
        stream.close();
    }
}
