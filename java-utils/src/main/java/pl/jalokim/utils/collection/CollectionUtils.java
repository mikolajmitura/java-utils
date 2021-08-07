package pl.jalokim.utils.collection;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Some utils methods for collections.
 */
public final class CollectionUtils {

    private CollectionUtils() {

    }

    /**
     * check that given index is last in certain list.
     *
     * @param list list to check.
     * @param index index to check
     * @return boolean value.
     */
    public static boolean isLastIndex(List<?> list, int index) {
        return getLastIndex(list) == index;
    }

    /**
     * check that given index is last in certain array.
     *
     * @param array list to check.
     * @param index index to check
     * @return boolean value.
     */
    public static boolean isLastIndex(Object[] array, int index) {
        return getLastIndex(array) == index;
    }

    /**
     * It returns last index from list.
     *
     * @param list for get last index.
     * @return last index for certain list.
     */
    public static int getLastIndex(Collection<?> list) {
        return list.size() - 1;
    }

    /**
     * It returns last index from array.
     *
     * @param array for get last index.
     * @return last index for certain array.
     */
    public static int getLastIndex(Object... array) {
        return array.length - 1;
    }

    /**
     * This method get some collection and filter expected elements and put them to new set.
     *
     * @param collection source collection.
     * @param filter predicate for filter.
     * @param <T> generic type for collection and predicate.
     * @return new Set of filtered element from source collection.
     */
    public static <T> Set<T> filterToSet(Collection<T> collection, Predicate<T> filter) {
        return getEmptyWhenIsNull(collection).stream()
            .filter(filter)
            .collect(Collectors.toSet());
    }

    /**
     * This method get some collection and filter expected elements and map them  to new type and put results to new Set.
     *
     * @param collection source collection.
     * @param filter predicate for filter by source elements type.
     * @param mapFunc map to new type filtered result.
     * @param <T> type for source collection.
     * @param <R> type for result Set.
     * @return Set with filtered source and another types than source collection.
     */
    public static <T, R> Set<R> filterToSet(Collection<T> collection,
        Predicate<T> filter,
        Function<T, R> mapFunc) {
        return getEmptyWhenIsNull(collection).stream()
            .filter(filter)
            .map(mapFunc)
            .collect(Collectors.toSet());
    }

    /**
     * This method return new Set with mapped values from source collection.
     *
     * @param collection source elements.
     * @param mapFunc function for map from collection to new Set with another types.
     * @param <T> type for source collection.
     * @param <R> type for result Set.
     * @return new Set with another type elements than source elements type.
     */
    public static <T, R> Set<R> mapToSet(Collection<T> collection, Function<T, R> mapFunc) {
        return getEmptyWhenIsNull(collection).stream()
            .map(mapFunc)
            .collect(Collectors.toSet());
    }

    /**
     * This method return new Set with mapped values from source collection. But after map it can filter them by last method argument.
     *
     * @param collection source elements.
     * @param mapFunc function for map from collection to new Set with another types.
     * @param filter predicate for mapped elements.
     * @param <T> type for source collection.
     * @param <R> type for result Set.
     * @return set with another type of filtered elements than from source.
     */
    public static <T, R> Set<R> mapToSet(Collection<T> collection,
        Function<T, R> mapFunc,
        Predicate<R> filter) {
        return getEmptyWhenIsNull(collection).stream()
            .map(mapFunc)
            .filter(filter)
            .collect(Collectors.toSet());
    }

    /**
     * This method return new List with mapped values from source collection.
     *
     * @param collection source elements.
     * @param mapFunc function for map from collection to new List with another types.
     * @param <T> type for source collection.
     * @param <R> type for result List.
     * @return new List with another type elements than source elements type.
     */
    public static <T, R> List<R> mapToList(Collection<T> collection, Function<T, R> mapFunc) {
        return getEmptyWhenIsNull(collection).stream()
            .map(mapFunc)
            .collect(Collectors.toList());
    }

    /**
     * This method return new List with mapped values from source collection. But after map it can filter them by last method argument.
     *
     * @param collection source elements.
     * @param mapFunc function for map from collection to new List with another types.
     * @param filter predicate for mapped elements.
     * @param <T> type for source collection.
     * @param <R> type for result List.
     * @return new List with another type of filtered elements than source elements type.
     */
    public static <T, R> List<R> mapToList(Collection<T> collection,
        Function<T, R> mapFunc,
        Predicate<R> filter) {
        return getEmptyWhenIsNull(collection).stream()
            .map(mapFunc)
            .filter(filter)
            .collect(Collectors.toList());
    }

    /**
     * This method return new List with mapped values from source array.
     *
     * @param mapFunc function for map from collection to new List with another types.
     * @param array source elements.
     * @param <T> type for source array.
     * @param <R> type for result List.
     * @return new List with another type elements than source elements type.
     */
    public static <T, R> List<R> mapToList(Function<T, R> mapFunc, T... array) {
        return mapToList(asList(array), mapFunc);
    }

    /**
     * This method return new List with mapped values from source array. But after map it can filter them by second method argument.
     *
     * @param mapFunc function for map from collection to new List with another types.
     * @param predicate predicate for filter.
     * @param array source elements.
     * @param <T> type for source array.
     * @param <R> type for result List.
     * @return new List with another type elements than source elements type.
     */
    public static <T, R> List<R> mapToList(Function<T, R> mapFunc, Predicate<R> predicate, T... array) {
        return mapToList(asList(array), mapFunc, predicate);
    }

    /**
     * This method get some collection and filter expected elements and put them to new List.
     *
     * @param collection source collection.
     * @param filter predicate for filter.
     * @param <T> generic type for collection and predicate.
     * @return new List of filtered element from source collection.
     */
    public static <T> List<T> filterToList(Collection<T> collection, Predicate<T> filter) {
        return getEmptyWhenIsNull(collection).stream()
            .filter(filter)
            .collect(Collectors.toList());
    }

    /**
     * This method get some collection and filter expected elements and map them  to new type and put results to new List.
     *
     * @param collection source collection.
     * @param filter predicate for filter by source elements type.
     * @param mapFunc map to new type filtered result.
     * @param <T> type for source collection.
     * @param <R> type for result List.
     * @return new List with filtered source and another types than source collection.
     */
    public static <T, R> List<R> filterToList(Collection<T> collection,
        Predicate<T> filter,
        Function<T, R> mapFunc) {
        return getEmptyWhenIsNull(collection).stream()
            .filter(filter)
            .map(mapFunc)
            .collect(Collectors.toList());
    }

    /**
     * This method get some collection and filter expected elements and put them to new List.
     *
     * @param predicate predicate for filter.
     * @param array source collection.
     * @param <T> generic type for collection and predicate.
     * @return new List of filtered element from source array.
     */
    public static <T> List<T> filterToList(Predicate<T> predicate, T... array) {
        return filterToList(asList(array), predicate);
    }

    /**
     * This method get some array and filter expected elements and map them to new type and put results to new List.
     *
     * @param predicate predicate for filter by source elements type.
     * @param mapFunc map to new type filtered result.
     * @param array source array.
     * @param <T> type for source array.
     * @param <R> type for result List.
     * @return new List with filtered source and another types than source array.
     */
    public static <T, R> List<R> filterToList(Predicate<T> predicate, Function<T, R> mapFunc, T... array) {
        return filterToList(asList(array), predicate, mapFunc);
    }

    /**
     * It get list and sort elements but not change source list.
     *
     * @param list to sort.
     * @param <T> generic type in list.
     * @return returns copy of source list but with sorted values.
     */
    public static <T extends Comparable<? super T>> List<T> sortAsNewList(Collection<T> list) {
        List<T> copyList = new ArrayList<>(list);
        Collections.sort(copyList);
        return copyList;
    }

    /**
     * Swap element in copy of sourceList and returns it.
     *
     * @param sourceList source for swap.
     * @param indexToSwap index element to swap in copy of source list
     * @param newValue new value in certain index.
     * @param <T> generic type for list.
     * @return copy of source list with swapped value.
     */
    public static <T> List<T> swapElementsAsNewList(List<T> sourceList, int indexToSwap, T newValue) {
        List<T> copyList = new ArrayList<>(sourceList);
        copyList.set(indexToSwap, newValue);
        return copyList;
    }

    /**
     * It returns first element from list if present.
     *
     * @param list as source
     * @param <T> generic type
     * @return returns first element if present.
     */
    public static <T> T getFirst(List<T> list) {
        if (list.isEmpty()) {
            throw new CollectionUtilsException("cannot get first element from empty list: " + list);
        }
        return list.get(0);
    }

    /**
     * It returns last element from list if present.
     *
     * @param list as source
     * @param <T> generic type
     * @return returns last element if present.
     */
    public static <T> T getLast(List<T> list) {
        if (list.isEmpty()) {
            throw new CollectionUtilsException("cannot get last element from empty list: " + list);
        }
        return list.get(list.size() - 1);
    }

    /**
     * It add to collection if is not present already.
     *
     * @param list where will be added element.
     * @param element to add.
     * @param <T> generic type of collection and tried add element.
     * @return return true when element was added.
     */
    public static <T> boolean addWhenNotExist(Collection<T> list, T element) {
        if (!list.contains(element)) {
            return list.add(element);
        }
        return false;
    }

    /**
     * It return true when provided collections have the same elements. Order or elements is not important here.
     *
     * @param first collection
     * @param second collection
     * @param <T> generic type of collections.
     * @return true when has the same elements.
     */
    public static <T extends Comparable<? super T>> boolean hasTheSameElements(Collection<T> first, Collection<T> second) {
        return new HashSet<>(first).equals(new HashSet<>(second));
    }

    /**
     * It returns true when collection is null or empty.
     *
     * @param list to verify.
     * @param <T> generic type.
     * @return boolean value.
     */
    public static <T> boolean isEmpty(Collection<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * It returns true only when collection is not null, and if not empty.
     *
     * @param list to verify.
     * @param <T> generic type.
     * @return boolean value.
     */
    public static <T> boolean isNotEmpty(Collection<T> list) {
        return !isEmpty(list);
    }

    /**
     * It return intersection for two given collections.
     *
     * @param first collection.
     * @param second collection.
     * @param <E> generic type.
     * @return return intersected elements.
     */
    public static <E> List<E> intersection(Collection<E> first, Collection<E> second) {
        List<E> list = new ArrayList<>();
        for (E element : first) {
            if (second.contains(element)) {
                list.add(element);
            }
        }
        return unmodifiableList(list);
    }

    @SuppressWarnings("unchecked")
    public static <E> Collection<E> getEmptyWhenIsNull(@Nullable Collection<E> nullableCollection) {
        return Optional.ofNullable(nullableCollection)
            .orElseGet(() -> Collections.EMPTY_LIST);
    }
}
