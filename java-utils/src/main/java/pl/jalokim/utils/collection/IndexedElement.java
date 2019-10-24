package pl.jalokim.utils.collection;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class IndexedElement<T> {
    private final int index;
    private final T value;
    private final boolean isFirst;
    private final boolean isLast;
}
