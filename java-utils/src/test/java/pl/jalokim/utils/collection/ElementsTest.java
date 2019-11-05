package pl.jalokim.utils.collection;


import org.junit.Test;
import pl.jalokim.utils.reflection.beans.inheritiance.ClassForTest;
import pl.jalokim.utils.reflection.beans.inheritiance.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
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
        List<Integer> numbers = asList(1, 2, 4, 5, 10, 20);
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
        Set<Integer> numbers = new HashSet<>(asList(1, 2, 4, 5, 10, 20));
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

    @Test
    public void forEachReturnsElementsAsExpected() {
        // given
        List<String> sourceList = asList("element1", "element2", "element3", "element4");
        // when
        List<String> collectedElements = new ArrayList<>();
        List<Integer> collectedIndexes = new ArrayList<>();
        elements(sourceList).forEach((index, element) -> {
            collectedElements.add(element);
            collectedIndexes.add(index);
        });
        // then
        assertThat(sourceList).containsExactlyElementsOf(collectedElements);
        assertThat(collectedIndexes).containsExactlyElementsOf(asList(0, 1, 2, 3));
    }

    @Test
    public void forEachWithIndexedElements() {
        // given
        List<String> sourceList = asList("element1", "element2", "element3", "element4");
        // when
        List<String> collectedElements = new ArrayList<>();
        List<Integer> collectedIndexes = new ArrayList<>();
        AtomicInteger index = new AtomicInteger();
        elements(sourceList).forEach(element -> {
            collectedElements.add(element.getValue());
            collectedIndexes.add(element.getIndex());
            int currentIndex = index.getAndIncrement();
            if (currentIndex == 0) {
                assertThat(element.isFirst()).isTrue();
            } else {
                assertThat(element.isFirst()).isFalse();
            }
            if (currentIndex == 3) {
                assertThat(element.isLast()).isTrue();
            } else {
                assertThat(element.isLast()).isFalse();
            }
        });
        // then
        assertThat(sourceList).containsExactlyElementsOf(collectedElements);
        assertThat(collectedIndexes).containsExactlyElementsOf(asList(0, 1, 2, 3));
    }

    @Test
    public void flatMapTest() {
        // given
        List<ClassForTest> classesForTest = new ArrayList<>();
        classesForTest.add(createClassForTest(createEvent("type1"), createEvent("type2"), createEvent("type3")));
        classesForTest.add(createClassForTest(createEvent("type4"), createEvent("type5"), createEvent("type6")));
        classesForTest.add(createClassForTest(createEvent("type7"), createEvent("type8"), createEvent("type9")));
        // when
        Elements<Event> eventElements = elements(classesForTest).flatMap(classForTest -> classForTest.getEventsAsList().stream());
        List<Event> events = eventElements.asList();
        // then
        assertThat(events).hasSize(9);
        assertThat(events.get(0)).isEqualTo(createEvent("type1"));
        assertThat(events.get(3)).isEqualTo(createEvent("type4"));
        assertThat(events.get(8)).isEqualTo(createEvent("type9"));
        assertThat(eventElements.getFirst()).isEqualTo(createEvent("type1"));
        assertThat(eventElements.getLast()).isEqualTo(createEvent("type9"));
    }

    private ClassForTest createClassForTest(Event... events) {
        ClassForTest classForTest = new ClassForTest();
        classForTest.setEventsAsList(asList(events));
        return classForTest;
    }

    private Event createEvent(String typeName) {
        Event event = new Event();
        event.setTypeName(typeName);
        return event;
    }
}