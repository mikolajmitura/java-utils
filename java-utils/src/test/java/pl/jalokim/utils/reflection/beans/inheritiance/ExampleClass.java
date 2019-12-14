package pl.jalokim.utils.reflection.beans.inheritiance;


import lombok.Data;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ExampleClass {
    private Event[] events;
    private List<Event> eventsAsList;
    private TransactionType[] transactionTypes;
    private Event[][][] threeDimEvents;
    private Map<String, List<Event[]>> mapOfTextWithListOfEventArrays;
    private Map<String, List<Set<Event>>[]> mapOfTextWithListOfSetOfEventArrays;
    private List<Map<String, Event>>[] arrayWithListsOfEvents;
    private Map<Event, ZonedDateTime> textByEvent;
    private Map<Integer, IntegerInfo> integerInfoByNumber;
    private Map<String, IntegerInfo> integerInfoByText;
    private Map<String, Map<String, List<Map<String, Integer>>>> mapWithSickGenerics;
    private Map<String, Map<String, Map<String, TupleClass<NextObject, Float[]>>>> mapWithSickGenerics2;
    private List<List<?>> emptyGenericsInList;
    private Map<SomeEnum, String> enumMap;
    private SomeEnum someEnum;
    private float simpleFloat;
    private int simpleInt;
    private Integer objectInt;
    private double simpleDouble;
    private Double objectDouble;
    private char simpleChar;
    private Character objectChar;
    private String string;
    private Boolean booleanWrapper;
    private byte simpleByte;
    private Byte objectByte;
    private DayOfWeek dayOfWeek;
    private LocalDate localDate;
    private LocalDateTime localDateTime;
    private LocalTime localTime;
    private List<Integer> integerList;
    private int[] simpleIntArray;
    private int[][] twoDimSimpleIntArray;
    private Float[] objectFloatArray;
    private String testSimpleField;
    private NextObject nextObject;
    private NextObject requester1;
    private NextObject requester2;
    private TupleClass<IntegerInfo, String> tupleIntegerInfo;
    private TupleClass<NextObject, String> tupleNextObject;
    private TupleClass<NextObject[], String> tupleNextObjectArray;
    private StringTuple<Integer, Map<Number, String>> stringTupleIntegerNumber;
    private StringTuple<NextObject, Map<Number, List<String>>> stringTupleNexObject;
    private ConcreteClass concreteClass;
    private ConcreteClass[][][] threeDimConcreteArray;

    public void checkTypesOfStringTupleNexObject() {
        Map<Number, List<String>> rawValueE = stringTupleNexObject.getRawValueE();
        String valueOfT = stringTupleNexObject.getValueOfT();
        Map<Number, List<String>> valueOfF = stringTupleNexObject.getValueOfF();
        NextObject fromStringA = stringTupleNexObject.getFromStringA();
        List<Map<Number, List<String>>> listOfF = stringTupleNexObject.getListOfF();
    }

    public void testTupleNextObject() {
        NextObject valueOfT = tupleNextObject.getValueOfT();
        List<String> listOfF = tupleNextObject.getListOfF();
    }

    public static class IntegerInfo {
        private String string;
        private byte simpleByte;
        private NextObject nextObject;
    }

    public static class NextObject {
        private String value;
        private Integer intValue;
        private Integer channel;
        private int[] integers;
        private Event[] pbjectArray;
        private Map<Integer, IntegerInfo> integerInfoByNumber;
        private Map<String, IntegerInfo> integerInfoByText;
        private NextObject someObject;
        private FinalClass finalClass;
    }

    public static final class FinalClass {
        private String text;
        private NextObject _$nextObject;
    }

    public static class ClassForTest2 {
        private Map<String, IntegerInfo> integerInfoByNumber;
    }

    public static class NestedWith$InName {

    }

    @Getter
    public static class TupleClass<T, F> extends RawTuple<F> {
        private T valueOfT;
        private F valueOfF;
        private List<F> listOfF;
    }

    @Getter
    public static class RawTuple<E> {
        private E rawValueE;
    }

    @Getter
    public static class StringTuple<A, G> extends TupleClass<String, G> {
        private A fromStringA;
    }

    @Getter
    public static class ConcreteClass extends StringTuple<String, NextObject> {
        @Override
        public NextObject getRawValueE() {
            return super.getRawValueE();
        }

        @Override
        public List<NextObject> getListOfF() {
            return super.getListOfF();
        }
    }
}
