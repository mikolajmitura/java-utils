package pl.jalokim.utils.reflection;

import lombok.Data;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pl.jalokim.utils.reflection.beans.SuperObject2;
import pl.jalokim.utils.reflection.beans.inheritiance.AbstractClassExSuperObject;
import pl.jalokim.utils.reflection.beans.inheritiance.ClassForTest;
import pl.jalokim.utils.reflection.beans.inheritiance.Event;
import pl.jalokim.utils.reflection.beans.inheritiance.SecondLevelSomeConcreteObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SomeConcreteObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SuperObject;
import pl.jalokim.utils.reflection.beans.inheritiance.innerpack.ThirdLevelConcrClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getAllChildClassesForClass;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getMethod;
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
import static pl.jalokim.utils.test.ErrorProneTestUtil.ErrorProneTestUtilBuilder.when;

public class MetadataReflectionUtilsTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void getFieldForClassWillReturnFieldFromSuperClass() {
        // when
        Field privateField = getField(ThirdLevelConcrClass.class, "privateField");
        // then
        assertThat(privateField.getType()).isEqualTo(String.class);
    }

    @Test
    public void getFieldForInstanceWillReturnFieldFromSuperClass() {
        ThirdLevelConcrClass instance = new ThirdLevelConcrClass();
        // when
        Field privateField = getField(instance, "privateField");
        // then
        assertThat(privateField.getType()).isEqualTo(String.class);
    }

    @Test
    public void getFieldFromClassReturnExpectedField() {
        // when
        Field nextObjectField = getField(ClassForTest.class, "nextObject");
        // then
        assertThat(nextObjectField.getName()).isEqualTo("nextObject");
        assertThat(nextObjectField.getType()).isEqualTo(ClassForTest.NextObject.class);
    }

    @Test
    public void givenCollectionFieldIsArrayType() {
        // given
        Field eventsField = getField(ClassForTest.class, "eventsAsList");
        Field nextObjectField = getField(ClassForTest.class, "nextObject");
        // when
        boolean arrayType = isCollectionType(eventsField.getType());

        // then
        assertThat(arrayType).isTrue();
        assertThat(isCollectionType(eventsField)).isTrue();
        assertThat(isCollectionType(nextObjectField.getType())).isFalse();
        assertThat(isCollectionType(nextObjectField)).isFalse();
        assertThat(isHavingElementsType(eventsField.getType())).isTrue();
        assertThat(isHavingElementsType(nextObjectField.getType())).isFalse();
        assertThat(isHavingElementsType(nextObjectField)).isFalse();
    }

    @Test
    public void givenArrayFieldIsArrayType() {
        // given
        Field eventsField = getField(ClassForTest.class, "events");
        // when
        boolean arrayType = isArrayType(eventsField.getType());
        // then
        assertThat(arrayType).isTrue();
        assertThat(isArrayType(eventsField)).isTrue();
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
        assertThat(isArrayType(requester1Field)).isFalse();
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
    public void cannotGetTypeOfArrayForNotArrayField() {
        // given
        Field eventsAsListField = getField(ClassForTest.class, "eventsAsList");
        // when
        when(() -> {
            getTypeOfArrayField(eventsAsListField);
        }).thenExpectedException(ReflectionOperationException.class,
                                 String.format("field: '%s' is not array type, is type: %s", eventsAsListField, eventsAsListField.getType()));
    }

    @Test
    public void getMethodForSuperClassFromConcreteClass() {
        // when
        Method someMethod = getMethod(ThirdLevelConcrClass.class, "someMethod", Integer.class, String.class);
        // then
        assertThat(someMethod.getParameterCount()).isEqualTo(2);
    }

    @Test
    public void getMethodForSuperClassFromConcreteInstance() {
        // given
        ThirdLevelConcrClass thirdLevelConcrClass = new ThirdLevelConcrClass();
        // when
        Method someMethod = getMethod(thirdLevelConcrClass, "someMethod", Integer.class, String.class);
        // then
        assertThat(someMethod.getParameterCount()).isEqualTo(2);
    }

    @Test
    public void cannotGetMethodForSuperClassFromConcreteInstance() {
        // given
        ThirdLevelConcrClass thirdLevelConcrClass = new ThirdLevelConcrClass();
        // when
        when(() ->
                     getMethod(thirdLevelConcrClass, "someMethod", Integer.class, Integer.class)
            ).thenExpectedException(ReflectionOperationException.class,
                                    "java.lang.NoSuchMethodException: pl.jalokim.utils.reflection.beans.inheritiance.innerpack.ThirdLevelConcrClass.someMethod(java.lang.Integer, java.lang.Integer)",
                                    "pl.jalokim.utils.reflection.beans.inheritiance.SecondLevelSomeConcreteObject.someMethod(java.lang.Integer, java.lang.Integer)",
                                    "pl.jalokim.utils.reflection.beans.inheritiance.SomeConcreteObject.someMethod(java.lang.Integer, java.lang.Integer)",
                                    "pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.someMethod(java.lang.Integer, java.lang.Integer)",
                                    "pl.jalokim.utils.reflection.beans.inheritiance.SuperAbstractObject.someMethod(java.lang.Integer, java.lang.Integer)",
                                    "java.lang.Object.someMethod(java.lang.Integer, java.lang.Integer)");
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
    public void returnsExpectedTypesForSomeConcreteInstance() {
        // given
        ThirdLevelConcrClass instance = new ThirdLevelConcrClass();
        // when
        Class intClass = getParametrizedType(instance, 0);
        Class stringClass = getParametrizedType(instance, 1);
        Class shortClass = getParametrizedType(instance, 2);
        // then
        assertThat(intClass).isEqualTo(Integer.class);
        assertThat(stringClass).isEqualTo(String.class);
        assertThat(shortClass).isEqualTo(Short.class);
    }

    @Test
    public void cannotGetParametrizedTypeForRawObjectInstance() {
        // given
        Object instance = new Object();
        // when
        when(() -> getParametrizedType(instance, 0))
                .thenExpectedException(
                        ReflectionOperationException.class,
                        format("Cannot find parametrized type for class: '%s', at: %s index", Object.class, 0))
                .then(ex -> {
                    Throwable cause = ex.getCause();
                    assertThat(cause).isNotNull();
                });
    }

    @Test
    public void returnsExpectedTypesForSomeConcreteClass() {
        // given
        Class concreteClass = ThirdLevelConcrClass.class;
        // when
        Class intClass = getParametrizedType(concreteClass, 0);
        Class stringClass = getParametrizedType(concreteClass, 1);
        Class shortClass = getParametrizedType(concreteClass, 2);
        // then
        assertThat(intClass).isEqualTo(Integer.class);
        assertThat(stringClass).isEqualTo(String.class);
        assertThat(shortClass).isEqualTo(Short.class);
    }

    @Test
    public void cannotGetParametrizedTypeForGivenField() {
        // given
        Field eventsField = getField(ClassForTest.class, "events");
        // when
        when(() -> getParametrizedType(eventsField, 0))
                .thenExpectedException(ReflectionOperationException.class,
                                       format("Cannot find parametrized type for field with class: '%s', at: %s index", eventsField.getType(), 0));
    }

    @Test
    public void simpleIntIsNumber() {
        // given
        Field simpleIntField = getField(ClassForTest.class, "simpleInt");
        Field stringField = getField(ClassForTest.class, "string");
        // when
        // then
        assertThat(isNumberType(simpleIntField.getType())).isTrue();
        assertThat(isNumberType(stringField.getType())).isFalse();
        assertThat(isNumberType(simpleIntField)).isTrue();
        assertThat(isNumberType(stringField)).isFalse();
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
    public void isTextFieldTest() {
        // given
        Field textField = getField(ClassForTest.class, "string");
        Field simpleByteField = getField(ClassForTest.class, "simpleByte");
        // when // then
        assertThat(isTextType(textField)).isTrue();
        assertThat(isTextType(textField.getType())).isTrue();
        assertThat(isTextType(simpleByteField.getType())).isFalse();
        assertThat(isTextType(simpleByteField.getType())).isFalse();
    }

    @Test
    public void isEnumFieldTest() {
        // given
        Field someEnumField = getField(ClassForTest.class, "someEnum");
        Field simpleByteField = getField(ClassForTest.class, "simpleByte");
        // when // then
        assertThat(isEnumType(someEnumField)).isTrue();
        assertThat(isEnumType(someEnumField.getType())).isTrue();
        assertThat(isEnumType(simpleByteField.getType())).isFalse();
        assertThat(isEnumType(simpleByteField.getType())).isFalse();
    }

    @Test
    public void isMapFieldTest() {
        // given
        Field mapField = getField(ClassForTest.class, "textByEvent");
        Field simpleByteField = getField(ClassForTest.class, "simpleByte");
        // when // then
        assertThat(isMapType(mapField)).isTrue();
        assertThat(isMapType(mapField.getType())).isTrue();
        assertThat(isMapType(simpleByteField.getType())).isFalse();
        assertThat(isMapType(simpleByteField.getType())).isFalse();
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
            assertThat(simpleFieldResult).isEqualTo(isSimpleType(fieldExpectation.getField()));
            String msgPart = fieldExpectation.isExpectedResult() ? "" : "not ";
            Assert.assertEquals("field " + fieldExpectation + " expected to be " + msgPart + "simple field",
                                simpleFieldResult, fieldExpectation.isExpectedResult());
        });
    }


    @Test
    public void getAllChildClassesForSuperObjectClassFromConcretePackageWithAbstract() {
        // when
        Set<Class<? extends SuperObject>> allChildClassesForAbstractClass =
                getAllChildClassesForClass(SuperObject.class, "pl.jalokim.utils.reflection.beans.inheritiance", true);
        // then
        assertThat(allChildClassesForAbstractClass).containsExactlyInAnyOrder(SomeConcreteObject.class,
                                                                              SecondLevelSomeConcreteObject.class,
                                                                              ThirdLevelConcrClass.class,
                                                                              AbstractClassExSuperObject.class);
    }

    @Test
    public void getAllChildClassesForSuperObjectClassFromWiderConcretePackageWithAbstract() {
        // when
        Set<Class<? extends SuperObject>> allChildClassesForAbstractClass =
                getAllChildClassesForClass(SuperObject.class, "pl.jalokim.utils.reflection.beans", true);
        // then
        assertThat(allChildClassesForAbstractClass).containsExactlyInAnyOrder(SomeConcreteObject.class,
                                                                              SecondLevelSomeConcreteObject.class,
                                                                              ThirdLevelConcrClass.class,
                                                                              AbstractClassExSuperObject.class,
                                                                              SuperObject2.class);
    }

    @Test
    public void getAllChildClassesForSuperObjectClassFromConcretePackageWithoutAbstract() {
        // when
        Set<Class<? extends SuperObject>> allChildClassesForAbstractClass =
                getAllChildClassesForClass(SuperObject.class, "pl.jalokim.utils.reflection.beans.inheritiance", false);
        // then
        assertThat(allChildClassesForAbstractClass).containsExactlyInAnyOrder(SomeConcreteObject.class,
                                                                              SecondLevelSomeConcreteObject.class,
                                                                              ThirdLevelConcrClass.class);
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