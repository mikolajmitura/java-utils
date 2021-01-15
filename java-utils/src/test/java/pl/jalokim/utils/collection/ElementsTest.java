package pl.jalokim.utils.collection;


import org.junit.Test;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass;
import pl.jalokim.utils.reflection.beans.inheritiance.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;
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
        List<ExampleClass> classesForTest = buildClassesForTest();
        // when
        Elements<Event> eventElements = elements(classesForTest).flatMap(classForTest -> classForTest.getEventsAsList().stream());
        List<Event> events = eventElements.asList();
        // then
        assertDataInList(events);
    }

    @Test
    public void flatMapByElementsTest() {
        // given
        List<ExampleClass> classesForTest = buildClassesForTest();
        // when
        Elements<Event> eventElements = elements(classesForTest).flatMapByElements(classForTest -> elements(classForTest.getEventsAsList()));
        List<Event> events = eventElements.asList();
        // then
        assertDataInList(events);
    }

    @Test
    public void flatMapByIterablesTest() {
        // given
        List<ExampleClass> classesForTest = buildClassesForTest();
        // when
        Elements<Event> eventElements = elements(classesForTest).flatMapByIterables(ExampleClass::getEventsAsList);
        List<Event> events = eventElements.asList();
        // then
        assertDataInList(events);
    }

    @Test
    public void flatMapByArrayTest() {
        // given
        List<ExampleClass> classesForTest = buildClassesForTest();
        classesForTest.forEach(exampleClass -> {
            List<Event> eventsAsList = exampleClass.getEventsAsList();
            Event[] events = new Event[eventsAsList.size()];
            elements(eventsAsList)
                    .forEach((index, element)-> events[index] = element);
            exampleClass.setEvents(events);
        });
        // when
        Elements<Event> eventElements = elements(classesForTest).flatMapByArray(ExampleClass::getEvents);
        List<Event> events = eventElements.asList();
        // then
        assertDataInList(events);
    }

    private void assertDataInList(List<Event> events) {
        assertThat(events).hasSize(9);
        assertThat(events.get(0)).isEqualTo(createEvent("type1"));
        assertThat(events.get(3)).isEqualTo(createEvent("type4"));
        assertThat(events.get(8)).isEqualTo(createEvent("type9"));
        assertThat(elements(events).getFirst()).isEqualTo(createEvent("type1"));
        assertThat(elements(events).getLast()).isEqualTo(createEvent("type9"));
    }

    private List<ExampleClass> buildClassesForTest() {
        List<ExampleClass> classesForTest = new ArrayList<>();
        classesForTest.add(createClassForTest(createEvent("type1"), createEvent("type2"), createEvent("type3")));
        classesForTest.add(createClassForTest(createEvent("type4"), createEvent("type5"), createEvent("type6")));
        classesForTest.add(createClassForTest(createEvent("type7"), createEvent("type8"), createEvent("type9")));
        return classesForTest;
    }

    @Test
    public void simpleToStringOnElements() {
        Object[] objects = {1, 2, 3, 4, "test"};
        // when
        String elementsAsText = elements(objects).toString();
        // then
        assertThat(elementsAsText).isEqualTo("[1, 2, 3, 4, test]");
        assertThat(elementsAsText).isEqualTo(elements(objects).asText());
        assertThat(elementsAsText).isEqualTo(elements(objects).asText(", "));
    }

    @Test
    public void asTextWithJoinTextOnElements() {
        Object[] objects = {1, 2, 3, 4, "test"};
        // when
        String elementsAsText = elements(objects).asText("#");
        // then
        assertThat(elementsAsText).isEqualTo("[1#2#3#4#test]");
    }

    @Test
    public void simpleAsMap() {
        Event event1 = createEvent("event1");
        Event event2 = createEvent("event2");
        Event event3 = createEvent("event3");

        // when
        Map<String, Event> eventByName = elements(event1, event2, event3)
                .asMap(Event::getTypeName);
        // then
        assertEventMap(eventByName, "event1");
        assertEventMap(eventByName, "event2");
        assertEventMap(eventByName, "event3");
    }

    @Test
    public void extendedAsMap() {
        Event event1 = createEvent("event1", 1);
        Event event2 = createEvent("event2", 2);
        Event event3 = createEvent("event3", 3);

        // when
        Map<String, Integer> eventByName = elements(event1, event2, event3)
                .asMap(Event::getTypeName, Event::getIndex);
        // then
        assertEventMap(eventByName, "event1", 1);
        assertEventMap(eventByName, "event2", 2);
        assertEventMap(eventByName, "event3", 3);
    }

    @Test
    public void returnsExpectedForAsMapGroupedBy() {
        // given
        Event event1 = createEvent("T", 1);
        Event event2 = createEvent("T", 2);
        Event event3 = createEvent("N", 3);
        // when
        Map<String, List<Event>> eventsByType = elements(event1, event2, event3)
                .asMapGroupedBy(Event::getTypeName);
        // then
        assertThat(eventsByType.get("T")).hasSize(2);
        assertThat(eventsByType.get("N")).hasSize(1);
    }

    private void assertEventMap(Map<String, Event> eventByName, String mapKey) {
        assertThat(eventByName.get(mapKey).getTypeName()).isEqualTo(mapKey);
    }

    private void assertEventMap(Map<String, Integer> eventByName, String mapKey, Integer expectedIndex) {
        assertThat(eventByName.get(mapKey)).isEqualTo(expectedIndex);
    }

    private ExampleClass createClassForTest(Event... events) {
        ExampleClass classForTest = new ExampleClass();
        classForTest.setEventsAsList(asList(events));
        return classForTest;
    }

    private Event createEvent(String typeName) {
        return createEvent(typeName, null);
    }

    private Event createEvent(String typeName, Integer index) {
        Event event = new Event();
        event.setTypeName(typeName);
        event.setIndex(index);
        return event;
    }
}