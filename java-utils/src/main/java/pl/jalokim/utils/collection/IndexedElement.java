package pl.jalokim.utils.collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Class which wraps element at certain index, over iterate you can check that this first or last element.
 *
 * @param <T> generic type of stored object at certain index in some collection.
 */
@RequiredArgsConstructor
@Getter
public class IndexedElement<T> {
    private final int index;
    private final T value;
    private final boolean isFirst;
    private final boolean isLast;
}
