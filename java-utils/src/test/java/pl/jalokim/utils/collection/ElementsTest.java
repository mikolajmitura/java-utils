package pl.jalokim.utils.collection;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.collection.Elements.elements;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import lombok.Value;
import org.junit.Test;
import pl.jalokim.utils.file.FileUtils;
import pl.jalokim.utils.reflection.beans.inheritiance.Event;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass;
import pl.jalokim.utils.test.TemporaryTestResources;

public class ElementsTest extends TemporaryTestResources {

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
        elements(sourceList).forEachWithIndex((index, element) -> {
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
                .forEachWithIndex((index, element) -> events[index] = element);
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
    public void expectedInvocationOfMapToOthersStreams() {
        // when
        IntStream intStream = getTextElements().mapToInt(Integer::valueOf);
        DoubleStream doubleStream = getTextElements().mapToDouble(Double::valueOf);
        LongStream longStream = getTextElements().mapToLong(Long::valueOf);

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
        IntStream intStream = getTextElements().mapToInt(Integer::valueOf);
        long count = getTextElements().count();
        Elements<String> limited = getTextElements().limit(2);
        Optional<String> max = getTextElements().max(Comparator.comparingInt(Integer::valueOf));
        Optional<String> min = getTextElements().min(Comparator.comparingInt(Integer::valueOf));

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
        Elements<String> elements = getTextElements();

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
        getTextElements().forEach((Consumer<String>) texts1::add);
        getTextElements().forEachOrdered(texts2::add);

        // then
        assertThat(texts1).isEqualTo(getTextElements().asList());
        assertThat(texts2).isEqualTo(getTextElements().asList());
    }

    @Test
    public void toArrayTest() {
        // when
        Object[] toArray = getTextElements().toArray();
        String[] toStringArray = getTextElements().toArray(number -> new String[]{"1", "2", "3", "44"});
        // then
        toArray[0] = "1";
        toStringArray[3] = "44";
    }

    @Test
    public void reducesAndCollectTest() {
        // when
        String reduceWithIdentity = getTextElements().reduce("0", (val1, val2) -> val1 + val2);
        Optional<String> reduce = getTextElements().reduce((val1, val2) -> val1 + val2);
        Integer reduceWithCombiner = getTextElements().reduce(1, (val1, val2) -> val1 + val2.length(), Integer::sum);
        ArrayList<String> collect = getTextElements().collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        // then
        assertThat(reduceWithIdentity).isEqualTo("01234");
        assertThat(reduce.get()).isEqualTo("1234");
        assertThat(reduceWithCombiner).isEqualTo(5);
        assertThat(collect).isEqualTo(getTextElements().asList());
    }

    @Test
    public void testMatches() {
        // when
        boolean anyMatch = getTextElements().anyMatch(it -> it.equals("1"));
        boolean allMatch = getTextElements().allMatch(it -> it.equals("1"));
        boolean allMatchLength = getTextElements().allMatch(it -> it.length() == 1);
        boolean noneMatch = getTextElements().noneMatch(it -> it.length() > 1);

        // then
        assertThat(anyMatch).isTrue();
        assertThat(allMatch).isFalse();
        assertThat(allMatchLength).isTrue();
        assertThat(noneMatch).isTrue();
    }

    @Test
    public void testFinds() {
        // when
        Optional<String> first = getTextElements().findFirst();
        Optional<String> any = getTextElements().findAny();
        Optional<Object> anyEmpty = Elements.empty().findAny();
        Optional<String> any1Found = Elements.of("1").findAny();

        // then
        assertThat(first.isPresent()).isTrue();
        assertThat(any.isPresent()).isTrue();
        assertThat(anyEmpty.isPresent()).isFalse();
        assertThat(anyEmpty).isEmpty();
        assertThat(any1Found).isPresent();
    }

    @Test
    public void iteratesAndGenarationTest() {
        // when
        List<Integer> iterated = Elements.iterate(0, value -> value + 1)
            .limit(3).asList();
        List<Integer> generated = Elements.generate(() -> 1)
            .limit(3).asList();
        Iterator<String> iterator = getTextElements().iterator();
        List<String> list = new ArrayList<>();

        while (iterator.hasNext()) {
            list.add(iterator.next());
        }

        // then
        assertThat(iterated).isEqualTo(Arrays.asList(0, 1, 2));
        assertThat(generated).isEqualTo(Arrays.asList(1, 1, 1));
        assertThat(list).isEqualTo(getTextElements().asList());
    }

    @Test
    public void concatTest() {
        // when
        List<String> concatChainAsList = getTextElements()
            .concat(getTextElements())
            .concat(Stream.of("11"))
            .asList();

        List<String> staticConcatAsList = Elements.concat(elements(concatChainAsList), getTextElements()).asList();

        // then
        assertThat(concatChainAsList).hasSize(9);
        assertThat(staticConcatAsList).hasSize(13);
    }

    @Test
    public void testCloseElements() {
        // when
        AtomicBoolean closed = new AtomicBoolean();
        Runnable runnable = () -> closed.set(true);

        getTextElements()
            .onClose(runnable)
            .close();

        // then
        assertThat(closed).isTrue();
    }

    @Test
    public void parallelActionTests() {
        // when
        Elements<String> parallel = getTextElements().parallel();
        // then
        assertThat(parallel.isParallel()).isTrue();
        assertThat(parallel.sequential().isParallel()).isFalse();
        assertThat(parallel.unordered().isParallel()).isFalse();
        assertThat(getTextElements().isParallel()).isFalse();
    }

    @Test
    public void elementsHandleWithNullableArguments() {
        // when
        String[] array = null;
        Elements<String> elementsFromArray = elements(array);
        Elements<String> elementsFromArray2 = Elements.of(array);
        Stream<Integer> stream = null;
        Elements<Integer> elementsFromStream = elements(stream);
        Iterator<String> iterator = null;
        Elements<String> elementsFromIterator = elements(iterator);
        List<String> list = null;
        Elements<String> elementsFromIterable = elements(list);

        // then
        elementsIsEmptyNotNull(elementsFromArray);
        elementsIsEmptyNotNull(elementsFromArray2);
        elementsIsEmptyNotNull(elementsFromStream);
        elementsIsEmptyNotNull(elementsFromIterator);
        elementsIsEmptyNotNull(elementsFromIterable);
    }

    @Test
    public void elementsFromIterator() {
        // given
        Iterator<String> iterator = Arrays.asList("1", "2", "3").iterator();
        // when
        Elements<String> elementsFromIterator = elements(iterator);
        // then
        List<String> resultList = elementsFromIterator.asList();
        assertThat(resultList).containsExactly("1", "2", "3");
    }

    @Test
    public void mapWithIndexAsExpected() {
        // given
        List<Integer> sourceList = Arrays.asList(12, 15, 3, 44);

        // when
        List<Pair> result = elements(sourceList)
            .mapWithIndex(Pair::new)
            .asList();

        // then
        assertThat(elements(result)
            .map(Pair::getIndex)
            .asList()).containsExactly(0, 1, 2, 3);

        assertThat(elements(result)
            .map(Pair::getValue)
            .asList()).containsExactly(12, 15, 3, 44);
    }

    @Test
    public void elementsToGenericArray() {
        // when
        String[] array = elements("1", "2", "3").toArray(new String[0]);
        // then
        assertThat(array).containsExactly("1", "2", "3");
    }


    @Test
    public void writeElementsToFileByString() {
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
    public void writeElementsToFileByFile() {
        // given
        File file = new File("from_elements");
        Elements<String> textElements = getTextElements();
        // when
        textElements.writeToFile(file);
        // then
        List<String> lines = FileUtils.readAsList(file);
        assertThat(lines).containsExactly("1", "2", "3", "4");
        FileUtils.deleteFileOrDirectory(file);
    }

    @Test
    public void writeElementsToFileByPath() {
        // given
        Path path = Paths.get("from_elements");
        Elements<String> textElements = getTextElements();
        // when
        textElements.writeToFile(path);
        // then
        List<String> lines = FileUtils.readAsList(path);
        assertThat(lines).containsExactly("1", "2", "3", "4");
        FileUtils.deleteFileOrDirectory(path);
    }

    @Test
    public void concatElements() {
        // given
        List<String> list1 = Arrays.asList("1", "2");
        List<String> list2 = null;

        // when
        List<String> concatElementsResult1 = getTextElements().concat(list1).asList();
        List<String> concatElementsResult2 = getTextElements().concat(list2).asList();

        // then
        assertThat(concatElementsResult1).containsExactly("1", "2", "3", "4", "1", "2");
        assertThat(concatElementsResult2).containsExactly("1", "2", "3", "4");
    }

    @Test
    public void readElementsFromFile() {
        // given
        File filePath = newFile("tmp");
        FileUtils.writeToFile(filePath.toString(), getTextElements().asList());
        // when
        Elements<String> elementsFromFile = Elements.fromFile(filePath);
        // then
        assertThat(elementsFromFile.asList()).isEqualTo(getTextElements().asList());
    }

    @Test
    public void readElementsFromPath() {
        // given
        File filePath = newFile("tmp");
        FileUtils.writeToFile(filePath.toString(), getTextElements().asList());
        // when
        Elements<String> elementsFromFile = Elements.fromFile(filePath.toPath());
        // then
        assertThat(elementsFromFile.asList()).isEqualTo(getTextElements().asList());
    }

    @Test
    public void readElementsFromFilePath() {
        // given
        File filePath = newFile("tmp");
        FileUtils.writeToFile(filePath.toString(), getTextElements().asList());
        // when
        Elements<String> elementsFromFile = Elements.fromFile(filePath.toString());
        // then
        assertThat(elementsFromFile.asList()).isEqualTo(getTextElements().asList());
    }

    @Test
    public void splitTextAsElements() {
        // given
        String textToSplit = "part1,part2, part3";
        // when
        Elements<String> stringElements = Elements.bySplitText(textToSplit, ",");
        // then
        assertThat(stringElements).containsExactly("part1", "part2", " part3");
    }

    @Test
    public void mapWithIndexed() {
        // when
        List<String> texts = getTextElements()
            .mapWithIndexed(indexed -> {
                if (!indexed.isFirst()) {
                    return indexed.getIndex() + indexed.getValue();
                }
                return indexed.getValue();
            })
            .asList();
        // then
        assertThat(texts).containsExactly("1", "12", "23", "34");
    }

    void elementsIsEmptyNotNull(Elements<?> elements) {
        assertThat(elements).isNotNull();
        assertThat(elements.asList().isEmpty()).isTrue();
    }

    private Elements<String> getTextElements() {
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
        return elements(getSomeEventsArray());
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

    @Value
    private static class Pair {

        Integer index;
        Integer value;
    }
}
