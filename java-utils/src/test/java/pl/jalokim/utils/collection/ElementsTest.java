package pl.jalokim.utils.collection;


import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.collection.Elements.elements;

public class ElementsTest {

    @Test
    public void arrayToList() {
        // when
        List<Integer> integers = elements(1, 2, 3).asList();
        // then
        assertThat(integers).hasSize(3);
        assertThat(integers).containsExactly(1, 2, 3);
    }

    @Test
    public void arrayToSet() {
        // when
        Set<Integer> integers = elements(1, 2, 3).asSet();
        // then
        assertThat(integers).hasSize(3);
        assertThat(integers).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    public void streamAsArray() {
        // given
        Stream<Integer> numberStream = Stream.of(1, 2, 3);
        // when
        Integer[] integers = elements(numberStream)
                .asArray(new Integer[0]);
        // then
        assertThat(integers).containsExactlyInAnyOrder(1, 2, 3);
    }

    @Test
    public void filterNumbersNextMapAsTextAndReturnsAsSet() {
        // given
        List<Integer> numbers = Arrays.asList(1, 2, 4, 5, 10, 20);
        // when
        Set<String> numberAsText = elements(numbers)
                .filter(number -> number > 9)
                .map(Object::toString)
                .asSet();
        // then
        assertThat(numberAsText).containsExactlyInAnyOrder("10", "20");
    }

    @Test
    public void filterSetMapToStringAndReturnAsArray() {
        // given
        Set<Integer> numbers = new HashSet<>(Arrays.asList(1, 2, 4, 5, 10, 20));
        // when
        String[] strings = elements(numbers)
                .filter(number -> number > 9)
                .map(Object::toString)
                .asArray(new String[0]);
        // then
        assertThat(strings).containsExactlyInAnyOrder("10", "20");
    }

    @Test
    public void filterArrayAndReturnStream() {
        // given
        Integer[] numbers = {1, 2, 3, 4, 5};
        // when
        int sum = elements(numbers)
                .filter(number -> number > 2)
                .asStream()
                .mapToInt(number -> number).sum();
        // then
        assertThat(sum).isEqualTo(12);
    }
}