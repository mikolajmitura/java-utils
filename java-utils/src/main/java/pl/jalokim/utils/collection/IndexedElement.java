package pl.jalokim.utils.collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class IndexedElement<T> {
    private final int index;
    private final T value;
    private final boolean isFirst;
    private final boolean isLast;
}
