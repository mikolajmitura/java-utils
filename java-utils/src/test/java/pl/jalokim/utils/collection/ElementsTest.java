package pl.jalokim.utils.collection;


import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.collection.Elements.elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.Test;
import pl.jalokim.utils.file.FileUtils;
import pl.jalokim.utils.reflection.beans.inheritiance.Event;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass;

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
        elements(sourceList).forEachWithIndexed(element -> {
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
                .forEach((index, element) -> events[index] = element);
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

    @Test
    public void writeElementsToFile() {
        // given
        File file = new File("from_elements");
        Event event1 = createEvent("T", 1);
        Event event2 = createEvent("T", 2);
        // when
        elements(event1, event2)
            .writeToFile(file.toString());

        // then
        List<String> lines = FileUtils.readAsList(file);
        assertThat(lines).containsExactly(
            "Event(typeName=T, index=1)",
            "Event(typeName=T, index=2)"
        );
        FileUtils.deleteFileOrDirectory(file);
    }

    @Test
    public void expectedInvocationOfMapToOthersStreams() {
        // when
        IntStream intStream = createElements().mapToInt(Integer::valueOf);
        DoubleStream doubleStream = createElements().mapToDouble(Double::valueOf);
        LongStream longStream = createElements().mapToLong(Long::valueOf);

        // then
        assertThat(intStream.sum()).isEqualTo(10);
        assertThat(doubleStream.count()).isEqualTo(4);
        assertThat(longStream.count()).isEqualTo(4);
    }

    @Test
    public void flatMapToOtherStreams() {
        // when
        IntStream intStream = createSomeClassesElements().flatMapToInt(element ->
            element.getEventsAsList().stream().mapToInt(eventElement -> eventElement.getTypeName().length()));
        LongStream longStream = createSomeClassesElements().flatMapToLong(element ->
            element.getEventsAsList().stream().mapToLong(eventElement -> (long) eventElement.getTypeName().length()));
        DoubleStream doubleStream = createSomeClassesElements().flatMapToDouble(element ->
            element.getEventsAsList().stream().mapToDouble(eventElement -> (double) eventElement.getTypeName().length()));

        // then
        assertThat(intStream.boxed().collect(Collectors.toList())).isEqualTo(Arrays.asList(2, 2, 1, 1, 2, 1, 3, 1));
        assertThat(longStream.boxed().collect(Collectors.toList())).isEqualTo(Arrays.asList(2L, 2L, 1L, 1L, 2L, 1L, 3L, 1L));
        assertThat(doubleStream.boxed().collect(Collectors.toList())).isEqualTo(Arrays.asList(2.0, 2.0, 1.0, 1.0, 2.0, 1.0, 3.0, 1.0));
    }

    @Test
    public void sortedDistinctAndPeekTest() {
        // given
        List<String> eventNames = new ArrayList<>();

        // when
        List<String> collectedToList = getEventsAsElements()
            .distinct()
            .peek(element ->
                eventNames.add(element.getTypeName())
            )
            .sorted(Comparator.comparing(Event::getTypeName))
            .map(Event::getTypeName)
            .skip(1).
                collect(Collectors.toList());

        List<String> defaultSorted = elements(eventNames).sorted().asList();

        // then
        assertThat(eventNames).isEqualTo(Arrays.asList("AC", "UA", "A", "D"));
        assertThat(collectedToList).isEqualTo(Arrays.asList("AC", "D", "UA"));
        assertThat(defaultSorted).isEqualTo(Arrays.asList("A", "AC", "D", "UA"));
    }

    @Test
    public void expectedInvocationOfOtherMethods() {
        // when
        IntStream intStream = createElements().mapToInt(Integer::valueOf);
        long count = createElements().count();
        Elements<String> limited = createElements().limit(2);
        Optional<String> max = createElements().max(Comparator.comparingInt(Integer::valueOf));
        Optional<String> min = createElements().min(Comparator.comparingInt(Integer::valueOf));

        // then
        assertThat(intStream.sum()).isEqualTo(10);
        assertThat(count).isEqualTo(4);
        assertThat(limited.count()).isEqualTo(2);
        assertThat(max.get()).isEqualTo("4");
        assertThat(min.get()).isEqualTo("1");
    }

    @Test
    public void returnExpectedAsConcatText() {
        // given
        Elements<String> elements = createElements();

        // when
        String result = elements.asConcatText(", ");

        // then
        assertThat(result).isEqualTo("1, 2, 3, 4");
    }

    @Test
    public void foreachTests() {
        // given
        List<String> texts1 = new ArrayList<>();
        List<String> texts2 = new ArrayList<>();

        // when
        createElements().forEach((Consumer<String>) texts1::add);
        createElements().forEachOrdered(texts2::add);

        // then
        assertThat(texts1).isEqualTo(createElements().asList());
        assertThat(texts2).isEqualTo(createElements().asList());
    }

    private Elements<String> createElements() {
        return Elements.of("1", "2", "3", "4");
    }

    private void assertEventMap(Map<String, Event> eventByName, String mapKey) {
        assertThat(eventByName.get(mapKey).getTypeName()).isEqualTo(mapKey);
    }

    private void assertEventMap(Map<String, Integer> eventByName, String mapKey, Integer expectedIndex) {
        assertThat(eventByName.get(mapKey)).isEqualTo(expectedIndex);
    }

    private Elements<ExampleClass> createSomeClassesElements() {
        return Elements.of(
            createClassForTest(getSomeEventsArray()),
            createClassForTest(createEvent("U"),
                createEvent("AAA"),
                createEvent("U"))
        );
    }

    private Elements<Event> getEventsAsElements() {
        return Elements.elements(getSomeEventsArray());
    }

    private Event[] getSomeEventsArray() {
        return new Event[]{createEvent("AC"),
            createEvent("UA"),
            createEvent("A"),
            createEvent("D"),
            createEvent("UA")};
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
