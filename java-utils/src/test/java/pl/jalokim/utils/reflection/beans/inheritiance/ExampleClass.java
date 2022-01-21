package pl.jalokim.utils.reflection.beans.inheritiance;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import pl.jalokim.utils.collection.Elements;

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
    private short simpleShort;
    private Short shortObject;
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
    private ConcreteClass[][][] threeDimConcreteArray;
    private StringTuple<RawTuple<List<Map<String, RawTuple<ConcreteClass[][][]>>>[][][]>, Map<Number, List<String>>>[][] superMixedArray;
    private Stream<String> someStream;
    private Elements<String> someElements;
    private OffsetDateTime offsetDateTimeField;
    private Duration durationField;
    private Period periodField;
    private Instant instantField;

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
    }

    public static class ClassForTest2 {

        private Map<String, IntegerInfo> integerInfoByNumber;
    }

    public static class NestedWith$InName {

    }

    public interface GenericInterface<D, U> {

        D convertFromU(U value);
    }

    public interface OtherInterface<D, U> {

        D convertFromU(U value, D value2);
    }

    @Getter
    @Setter
    public static class TupleClass<T, F> extends RawTuple<F> implements GenericInterface<T, F>, OtherInterface<List<T>, F> {

        private T valueOfT;
        private F valueOfF;
        private List<F> listOfF;

        @SomeAnnotation
        public TupleClass(T valueOfT, @OtherAnnotation F valueOfF) {
            this.valueOfT = valueOfT;
            this.valueOfF = valueOfF;
        }

        public TupleClass() {
        }

        public F returnF(@SomeAnnotation @OtherAnnotation List<Number> arArg0,
            HashMap<F, T> mapOfFAndT, CharSequence string) {
            return listOfF.get(0);
        }

        @SomeAnnotation
        public F returnF(@SomeAnnotation @OtherAnnotation F arArg,
            Map<F, T> mapOfFAndT, String string) {
            return valueOfF;
        }

        @Override
        public T convertFromU(F value) {
            return null;
        }

        @Override
        public List<T> convertFromU(F value, List<T> value2) {
            return null;
        }
    }

    @Getter
    public static class RawTuple<E> {

        private E rawValueE;
        private TupleClass tupleClassRaw;
    }

    public static class TupleClassImpl extends TupleClass<String, List<Number>> {

    }

    public static class TupleClassImpl2 extends TupleClass<String, ArrayList<Number>> {

        @SomeAnnotation
        public TupleClassImpl2(String valueOfT, @OtherAnnotation @SomeAnnotation ArrayList<Number> valueOfF) {
            super(valueOfT, valueOfF);
        }

        public TupleClassImpl2() {
            super();
        }
    }

    public static class InvalidTupleExtension extends TupleClass {

    }

    public static class InvalidRawTupleExtension extends RawTuple<TupleClass> {

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

    @Retention(RetentionPolicy.RUNTIME)
    public @interface SomeAnnotation {

    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface OtherAnnotation {

    }

    public static class RecursionGenericClass
        extends RawTuple<RecursionGenericClass>
        implements Comparable<RecursionGenericClass> {

        private RecursionGenericClass someObject;

        @Override
        public int compareTo(RecursionGenericClass o) {
            return 0;
        }
    }
}
