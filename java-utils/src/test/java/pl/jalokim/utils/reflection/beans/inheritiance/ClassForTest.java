package pl.jalokim.utils.reflection.beans.inheritiance;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class ClassForTest {
    private Event[] events;
    private List<Event> eventsAsList;
    private Map<Event, ZonedDateTime> textByEvent;
    private Map<Integer, IntegerInfo> integerInfoByNumber;
    private Map<String, IntegerInfo> integerInfoByText;
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
    private Float[] objectFloatArray;
    private String testSimpleField;
    private NextObject nextObject;
    private NextObject requester1;
    private NextObject requester2;

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
        private Map<String, IntegerInfo> integerInfoByNumber;;
    }
}
