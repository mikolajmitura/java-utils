package pl.jalokim.utils.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils {

    public static boolean isLastIndex(List<?> list, int index) {
        return getLastIndex(list) == index;
    }

    public static boolean isLastIndex(Object[] array, int index) {
        return getLastIndex(array) == index;
    }

    public static int getLastIndex(Collection<?> list) {
        return list.size() - 1;
    }

    public static int getLastIndex(Object[] array) {
        return array.length - 1;
    }

    public static <T extends Comparable<? super T>> List<T> sortAsNewList(Collection<T> list) {
        List<T> copyList = new ArrayList<>(list);
        Collections.sort(copyList);
        return copyList;
    }

    public static <T> Set<T> filterToSet(Collection<T> collection, Predicate<T> filter) {
        return collection.stream()
                         .filter(filter)
                         .collect(Collectors.toSet());
    }

    public static <T, R> Set<R> mapToSet(Collection<T> collection, Function<T, R> mapFunc) {
        return collection.stream()
                         .map(mapFunc)
                         .collect(Collectors.toSet());
    }

    public static <T, R> Set<R> collectToSet(Collection<T> collection, Function<T, R> mapFunc, Predicate<R> filter) {
        return collection.stream()
                         .map(mapFunc)
                         .filter(filter)
                         .collect(Collectors.toSet());
    }

    public static <T, R> Set<R> collectToSet(Collection<T> collection, Predicate<T> filter, Function<T, R> mapFunc) {
        return collection.stream()
                         .filter(filter)
                         .map(mapFunc)
                         .collect(Collectors.toSet());
    }

    public static <T, R> List<R> mapToList(Collection<T> collection, Function<T, R> mapFunc) {
        return collection.stream()
                         .map(mapFunc)
                         .collect(Collectors.toList());
    }

    public static <T> List<T> filerToList(Collection<T> collection, Predicate<T> filter) {
        return collection.stream()
                         .filter(filter)
                         .collect(Collectors.toList());
    }

    public static <T, R> List<R> collectToList(Collection<T> collection, Function<T, R> mapFunc, Predicate<R> filter) {
        return collection.stream()
                         .map(mapFunc)
                         .filter(filter)
                         .collect(Collectors.toList());
    }

    public static <T, R> List<R> collectToList(Collection<T> collection, Predicate<T> filter, Function<T, R> mapFunc) {
        return collection.stream()
                         .filter(filter)
                         .map(mapFunc)
                         .collect(Collectors.toList());
    }

    public static <T, R> List<R> mapToList(Function<T, R> mapFunc, T[] array) {
        return Stream.of(array)
                     .map(mapFunc)
                     .collect(Collectors.toList());
    }

    public static <T> List<T> swapElementsAsNewList(List<T> list, int indexToSwap, T newValue) {
        List<T> copyList = new ArrayList<>(list);
        copyList.set(indexToSwap, newValue);
        return copyList;
    }

    public static <T> T getLast(List<T> list) {
        if(list.isEmpty()) {
            throw new CollectionUtilsException("cannot get last element from empty list: " + list);
        }
        return list.get(list.size() - 1);
    }

    public static <T> T getFirst(List<T> list) {
        if(list.isEmpty()) {
            throw new CollectionUtilsException("cannot get first element from empty list: " + list);
        }
        return list.get(0);
    }

    public static <T> boolean addWhenNotExist(List<T> list, T element) {
        if(!list.contains(element)) {
            return list.add(element);
        }
        return false;
    }

    public static <T extends Comparable<? super T>> boolean hasTheSameElements(Collection<T> first, Collection<T> second) {
        return new HashSet<>(first).equals(new HashSet<>(second));
    }

    public static <T> boolean isNotEmpty(List<T> list) {
        return list != null && !list.isEmpty();
    }

    public static <E> List<E> intersection(Collection<E> first, Collection<E> second) {
        List<E> list = new ArrayList<>();
        for(E element : first) {
            if(second.contains(element)) {
                list.add(element);
            }
        }
        return list;
    }
}
