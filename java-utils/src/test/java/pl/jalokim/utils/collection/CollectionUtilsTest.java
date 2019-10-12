package pl.jalokim.utils.collection;


import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.collection.CollectionUtils.addWhenNotExist;
import static pl.jalokim.utils.collection.CollectionUtils.hasTheSameElements;
import static pl.jalokim.utils.collection.CollectionUtils.intersection;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

public class CollectionUtilsTest {

    private List<String> collection = initCollection();
    private String[] array = new String[]{"text4", "text2$", "text3$"};
    private List<String> empty = new ArrayList<>();

    private List<String> initCollection() {
        List<String> elements = new ArrayList<>();
        elements.add("text4");
        elements.add("text2$");
        elements.add("text3$");
        return elements;
    }

    @Test
    public void isLastIndexFromCollection() {
        // when
        boolean isLastIndex = CollectionUtils.isLastIndex(collection, 2);
        boolean notIsLastIndex = CollectionUtils.isLastIndex(collection, 1);
        // then
        assertThat(isLastIndex).isTrue();
        assertThat(notIsLastIndex).isFalse();
    }

    @Test
    public void isLastIndexFromArray() {
        // when
        boolean isLastIndex = CollectionUtils.isLastIndex(array, 2);
        boolean notIsLastIndex = CollectionUtils.isLastIndex(array, 1);
        // then
        assertThat(isLastIndex).isTrue();
        assertThat(notIsLastIndex).isFalse();
    }

    @Test
    public void getLastIndexNumberFromCollection() {
        // when
        int lastIndex = CollectionUtils.getLastIndex(collection);
        // then
        assertThat(lastIndex).isEqualTo(2);
    }

    @Test
    public void getLastIndexNumberFromArray() {
        // when
        int lastIndex = CollectionUtils.getLastIndex(array);
        // then
        assertThat(lastIndex).isEqualTo(2);
    }

    @Test
    public void newListSorted() {
        // given
        List<String> expected = Arrays.asList("text2$", "text3$", "text4");
        // when
        List<String> sortedList = CollectionUtils.sortAsNewList(collection);
        // then
        assertThat(sortedList).isEqualTo(expected);
    }

    @Test
    public void filterToNewSet() {
        // when
        Set<String> dollars = CollectionUtils.filterToSet(collection, text -> text.contains("$"));
        // then
        assertThat(dollars).containsExactlyInAnyOrder("text2$", "text3$");
    }

    @Test
    public void mapToSet() {
        // when
        Set<String> newSet = CollectionUtils.mapToSet(collection, text -> text + '_');
        // then
        assertThat(newSet).containsExactlyInAnyOrder("text2$_", "text3$_", "text4_");
    }

    @Test
    public void mapToSetWithFilter() {
        // given
        List<String> collection = Arrays.asList("1", "2x", "3x", "4", "15");

        // when
        Set<Integer> integers = CollectionUtils.mapToSet(collection, text -> {
            try {
                return Integer.valueOf(text);
            } catch(NumberFormatException ex) {
                return 0;
            }

        }, number -> number > 0);
        // then
        assertThat(integers).containsExactlyInAnyOrder(1, 4, 15);
    }

    @Test
    public void filterToSetWithMapFirst() {
        // given
        List<String> collection = Arrays.asList("1", "2x", "3x", "4", "15");

        // when
        Set<Integer> integers = CollectionUtils.filterToSet(collection,
                                                            text -> !text.contains("x"),
                                                            Integer::valueOf);
        // then
        assertThat(integers).containsExactlyInAnyOrder(1, 4, 15);
    }

    @Test
    public void mapToList() {
        // when
        List<String> newSet = CollectionUtils.mapToList(collection, text -> text + '_');
        // then
        assertThat(newSet).containsExactly("text4_", "text2$_", "text3$_");
    }


    @Test
    public void filterToNewList() {
        // given
        List<String> collection = Arrays.asList("1", "2x", "3x", "4", "15");
        // when
        List<String> onlyNumberAsText = CollectionUtils.filterToList(collection, text -> !text.contains("x"));
        // then
        assertThat(onlyNumberAsText).containsExactlyInAnyOrder("1", "4", "15");
    }

    @Test
    public void mapToListWithFilter() {
        // given
        List<String> collection = Arrays.asList("1", "2x", "3x", "4", "15");

        // when
        List<Integer> integers = CollectionUtils.mapToList(collection, text -> {
            try {
                return Integer.valueOf(text);
            } catch(NumberFormatException ex) {
                return 0;
            }

        }, number -> number > 0);
        // then
        assertThat(integers).containsExactly(1, 4, 15);
    }

    @Test
    public void filterToListWithMapFirst() {
        // given
        List<String> collection = Arrays.asList("1", "2x", "3x", "4", "15");

        // when
        List<Integer> integers = CollectionUtils.filterToList(collection,
                                                              text -> !text.contains("x"),
                                                              Integer::valueOf);
        // then
        assertThat(integers).containsExactly(1, 4, 15);
    }

    @Test
    public void mapArrayToList() {
        // given
        String[] array = {"1", "2", "3", "4"};
        // when
        List<Integer> integers = CollectionUtils.mapToList(Integer::valueOf, array);
        // then
        assertThat(integers).containsExactly(1, 2, 3, 4);
    }

    @Test
    public void mapAndFilterArrayToList() {
        // given
        String[] array = {"1", "2", "3", "4"};
        // when
        List<Integer> integers = CollectionUtils.mapToList(Integer::valueOf, number -> number < 4, array);
        // then
        assertThat(integers).containsExactly(1, 2, 3);
    }

    @Test
    public void filterArrayToList() {
        // given
        String[] array = {"1", "2", "3", "41"};
        // when
        List<String> numberAsText = CollectionUtils.filterToList(text -> text.length() == 1, array);
        // then
        assertThat(numberAsText).containsExactly("1", "2", "3");
    }

    @Test
    public void filterArrayAndMapToList() {
        // given
        String[] array = {"1", "2", "3", "41"};
        // when
        List<Integer> numberAsText = CollectionUtils.filterToList(text -> text.length() == 1, Integer::valueOf, array);
        // then
        assertThat(numberAsText).containsExactly(1, 2, 3);
    }

    @Test
    public void swapElementsAsNewList() {
        // when
        List<String> strings = CollectionUtils.swapElementsAsNewList(collection, 2, "2_text");
        // then
        assertThat(strings).containsExactly("text4", "text2$", "2_text");
    }

    @Test
    public void getFirstFromList() {
        // when
        String last = CollectionUtils.getFirst(collection);
        // then
        assertThat(last).isEqualTo("text4");
    }

    @Test
    public void getLastFromList() {
        // when
        String last = CollectionUtils.getLast(collection);
        // then
        assertThat(last).isEqualTo("text3$");
    }

    @Test
    public void cannotGetFirstFromList() {
        // when
        when(()-> CollectionUtils.getFirst(empty))
                .thenException(CollectionUtilsException.class, "cannot get first element from empty list: []");
    }

    @Test
    public void cannotGetLastFromList() {
        // when
        when(()-> CollectionUtils.getLast(empty))
                .thenException(CollectionUtilsException.class, "cannot get last element from empty list: []");
    }

    @Test
    public void isNotEmptyWhenHasElements() {
        // when
        boolean notEmpty = isNotEmpty(collection);
        // then
        assertThat(notEmpty).isTrue();
    }

    @Test
    public void isEmptyWhenHasZeroElements() {
        // when
        boolean notEmpty = isNotEmpty(empty);
        // then
        assertThat(notEmpty).isFalse();
    }

    @Test
    public void isEmptyWhenPassedNull() {
        // when
        boolean notEmpty = isNotEmpty(null);
        // then
        assertThat(notEmpty).isFalse();
    }

    @Test
    public void cannotAddElementToListWhenIsPresent() {
        // given
        List<String> elements = new ArrayList<>(Arrays.asList("0", "1", "2"));
        // when
        boolean added = addWhenNotExist(elements, new String("1"));
        // then
        assertThat(added).isFalse();
        assertThat(elements).hasSize(3);
    }

    @Test
    public void canAddElementToListWhenIsNotPresent() {
        // given
        List<String> elements = new ArrayList<>(Arrays.asList("0", "1", "2"));
        // when
        boolean added = addWhenNotExist(elements, "4");
        // then
        assertThat(added).isTrue();
        assertThat(elements).hasSize(4);
    }

    @Test
    public void hasTheSameElementsIsTrue() {
        // given
        List<String> someList = new ArrayList<>(collection);
        Set<String> someSet = new HashSet<>(collection);
        // when
        boolean hasTheSameElements = hasTheSameElements(someList, someSet);
        // then
        assertThat(hasTheSameElements).isTrue();
    }

    @Test
    public void hasTheSameElementsIsFalse() {
        // given
        List<String> someList = new ArrayList<>(collection);
        someList.add("1234");
        Set<String> someSet = new HashSet<>(collection);
        // when
        boolean hasTheSameElements = hasTheSameElements(someList, someSet);
        // then
        assertThat(hasTheSameElements).isFalse();
    }

    @Test
    public void returnEmptyIntersection() {
        // given
        List<Integer> elementsList = Arrays.asList(1, 2, 3, 4, 5);
        Set<Integer> elementsSet = new HashSet<>(Arrays.asList(6, 7, 8));
        // when
        List<Integer> intersection = intersection(elementsList, elementsSet);
        // then
        assertThat(intersection).isEmpty();
    }

    @Test
    public void returnIntersectionWhichIsNotEmpty() {
        // given
        List<Integer> elementsList = Arrays.asList(1, 2, 3, 4, 5);
        Set<Integer> elementsSet = new HashSet<>(Arrays.asList(5, 1, 2));
        // when
        List<Integer> intersection = intersection(elementsList, elementsSet);
        // then
        assertThat(intersection).containsExactly(1, 2, 5);
    }
}