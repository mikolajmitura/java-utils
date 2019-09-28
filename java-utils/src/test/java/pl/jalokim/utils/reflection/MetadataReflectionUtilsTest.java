package pl.jalokim.utils.reflection;

import lombok.Data;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pl.jalokim.utils.reflection.beans.ClassForTest;
import pl.jalokim.utils.reflection.beans.Event;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getParametrizedType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeOfArrayField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isArrayType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isCollectionType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isEnumType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isHavingElementsType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isMapType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isNumberType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isSimpleType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTextType;

public class MetadataReflectionUtilsTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void getFieldFromClassReturnExpectedField() {
        // when
        Field requesterField = getField(ClassForTest.class, "nextObject");
        // then
        assertThat(requesterField.getName()).isEqualTo("nextObject");
        assertThat(requesterField.getType()).isEqualTo(ClassForTest.NextObject.class);
    }

    @Test
    public void givenCollectionFieldIsArrayType() {
        // given
        Field eventsField = getField(ClassForTest.class, "eventsAsList");
        // when
        boolean arrayType = isCollectionType(eventsField.getType());
        // then
        assertThat(arrayType).isTrue();
        assertThat(isHavingElementsType(eventsField.getType())).isTrue();
    }

    @Test
    public void givenArrayFieldIsArrayType() {
        // given
        Field eventsField = getField(ClassForTest.class, "events");
        // when
        boolean arrayType = isArrayType(eventsField.getType());
        // then
        assertThat(arrayType).isTrue();
        assertThat(isHavingElementsType(eventsField.getType())).isTrue();
    }

    @Test
    public void givenFieldIsNotArrayType() {
        // given
        Field requester1Field = getField(ClassForTest.class, "requester1");
        // when
        boolean arrayType = isArrayType(requester1Field.getType());
        // then
        assertThat(arrayType).isFalse();
    }


    @Test
    public void returnExpectedTypeInGivenCollectionField() {
        // given
        Field eventsAsListField = getField(ClassForTest.class, "eventsAsList");
        // when
        Class<?> typeOfArrayField = getParametrizedType(eventsAsListField, 0);
        // then
        assertThat(typeOfArrayField).isEqualTo(Event.class);
    }

    @Test
    public void returnExpectedTypeInGivenArrayField() {
        // given
        Field eventsField = getField(ClassForTest.class, "events");
        // when
        Class<?> typeOfArrayField = getTypeOfArrayField(eventsField);
        // then
        assertThat(typeOfArrayField).isEqualTo(Event.class);
    }

    @Test
    public void readKeyAndValueTypeFromMap() {
        // given
        Field additionalIdentifiersField = getField(ClassForTest.class, "integerInfoByNumber");
        // when
        Class<?> typeOfKey = getParametrizedType(additionalIdentifiersField, 0);
        Class<?> typeOfValue = getParametrizedType(additionalIdentifiersField, 1);
        // then
        assertThat(typeOfKey).isEqualTo(Integer.class);
        assertThat(isNumberType(typeOfKey)).isTrue();
        assertThat(typeOfValue).isEqualTo(ClassForTest.IntegerInfo.class);
    }

    @Test
    public void simpleIntIsNumber() {
        // given
        Field simpleIntField = getField(ClassForTest.class, "simpleInt");
        // when
        // then
        assertThat(isNumberType(simpleIntField.getType())).isTrue();
    }

    @Test
    public void enumAsKeyInMap() {
        // given
        Field enumMapField = getField(ClassForTest.class, "enumMap");
        // when
        Class<?> typeOfKey = getParametrizedType(enumMapField, 0);
        Class<?> typeOfValue = getParametrizedType(enumMapField, 1);
        // then
        assertThat(isEnumType(typeOfKey)).isTrue();
        assertThat(isTextType(typeOfValue)).isTrue();
        assertThat(isMapType(enumMapField.getType())).isTrue();
    }

    @Test
    public void givenFieldIsSimpleOrNotAsExpected() {
        // given
        List<FieldExpectation> fieldExpectations = Arrays.asList(
                create(ClassForTest.class, "textByEvent", false),
                create(ClassForTest.class, "events", false),
                create(ClassForTest.class, "simpleFloat", true),
                create(ClassForTest.class, "simpleInt", true),
                create(ClassForTest.class, "objectInt", true),
                create(ClassForTest.class, "simpleDouble", true),
                create(ClassForTest.class, "objectDouble", true),
                create(ClassForTest.class, "simpleChar", true),
                create(ClassForTest.class, "objectChar", true),
                create(ClassForTest.class, "string", true),
                create(ClassForTest.class, "simpleByte", true),
                create(ClassForTest.class, "objectByte", true),
                create(ClassForTest.class, "dayOfWeek", true),
                create(ClassForTest.class, "localDate", true),
                create(ClassForTest.class, "localDateTime", true),
                create(ClassForTest.class, "localTime", true),
                create(ClassForTest.class, "booleanWrapper", true)
                                                                );
        fieldExpectations.forEach(fieldExpectation -> {
            // when
            boolean simpleFieldResult = isSimpleType(fieldExpectation.getField().getType());
            // then

            String msgPart = fieldExpectation.isExpectedResult()? "" : "not ";
            Assert.assertEquals("field " + fieldExpectation + " expected to be " + msgPart + "simple field",
                                simpleFieldResult, fieldExpectation.isExpectedResult());
        });
    }

    private static FieldExpectation create(Class<?> type, String fieldName, boolean expectedResult) {
        return new FieldExpectation(getField(type, fieldName), expectedResult);
    }

    @Data
    private static class FieldExpectation {
        private final Field field;
        private final boolean expectedResult;
    }
}