package pl.jalokim.utils.reflection;

import lombok.Data;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import pl.jalokim.utils.constants.Constants;
import pl.jalokim.utils.reflection.beans.SuperObject2;
import pl.jalokim.utils.reflection.beans.inheritiance.AbstractClassExSuperObject;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass;
import pl.jalokim.utils.reflection.beans.inheritiance.Event;
import pl.jalokim.utils.reflection.beans.inheritiance.SecondLevelSomeConcreteObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SomeConcreteObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SuperObject;
import pl.jalokim.utils.reflection.beans.inheritiance.innerpack.ThirdLevelConcrClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.constants.Constants.COMMA;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getAllChildClassesForClass;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getMethod;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getParametrizedRawTypes;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getParametrizedType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataOfArray;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeOfArrayField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isArrayType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isCollectionType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isEnumType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isHavingElementsType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isMapType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isNumberType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isSimpleType;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.isTextType;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.MAP;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.assertTypeMetadata;
import static pl.jalokim.utils.string.StringUtils.concat;
import static pl.jalokim.utils.string.StringUtils.concatElements;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

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
        Field nextObjectField = getField(ExampleClass.class, "nextObject");
        // then
        assertThat(nextObjectField.getName()).isEqualTo("nextObject");
        assertThat(nextObjectField.getType()).isEqualTo(ExampleClass.NextObject.class);
    }

    @Test
    public void givenCollectionFieldIsArrayType() {
        // given
        Field eventsField = getField(ExampleClass.class, "eventsAsList");
        Field nextObjectField = getField(ExampleClass.class, "nextObject");
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
        Field eventsField = getField(ExampleClass.class, "events");
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
        Field requester1Field = getField(ExampleClass.class, "requester1");
        // when
        boolean arrayType = isArrayType(requester1Field.getType());
        // then
        assertThat(arrayType).isFalse();
        assertThat(isArrayType(requester1Field)).isFalse();
    }

    @Test
    public void returnExpectedTypeInGivenCollectionField() {
        // given
        Field eventsAsListField = getField(ExampleClass.class, "eventsAsList");
        // when
        Class<?> typeOfArrayField = getParametrizedType(eventsAsListField, 0);
        // then
        assertThat(typeOfArrayField).isEqualTo(Event.class);
    }

    @Test
    public void returnExpectedTypeInGivenArrayField() {
        // given
        Field eventsField = getField(ExampleClass.class, "events");
        // when
        Class<?> typeOfArrayField = getTypeOfArrayField(eventsField);
        // then
        assertThat(typeOfArrayField).isEqualTo(Event.class);
    }

    @Test
    public void cannotGetTypeOfArrayForNotArrayField() {
        // given
        Field eventsAsListField = getField(ExampleClass.class, "eventsAsList");
        // when
        when(() -> {
            getTypeOfArrayField(eventsAsListField);
        }).thenException(ReflectionOperationException.class,
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
            ).thenException(ReflectionOperationException.class,
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
        Field additionalIdentifiersField = getField(ExampleClass.class, "integerInfoByNumber");
        // when
        Class<?> typeOfKey = getParametrizedType(additionalIdentifiersField, 0);
        Class<?> typeOfValue = getParametrizedType(additionalIdentifiersField, 1);
        // then
        assertThat(typeOfKey).isEqualTo(Integer.class);
        assertThat(isNumberType(typeOfKey)).isTrue();
        assertThat(typeOfValue).isEqualTo(ExampleClass.IntegerInfo.class);
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
                .thenException(
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
        Field eventsField = getField(ExampleClass.class, "events");
        // when
        when(() -> getParametrizedType(eventsField, 0))
                .thenException(ReflectionOperationException.class,
                               format("Cannot find parametrized type for field with class: '%s', at: %s index", eventsField.getType(), 0));
    }

    @Test
    public void simpleIntIsNumber() {
        // given
        Field simpleIntField = getField(ExampleClass.class, "simpleInt");
        Field stringField = getField(ExampleClass.class, "string");
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
        Field enumMapField = getField(ExampleClass.class, "enumMap");
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
        Field textField = getField(ExampleClass.class, "string");
        Field simpleByteField = getField(ExampleClass.class, "simpleByte");
        // when // then
        assertThat(isTextType(textField)).isTrue();
        assertThat(isTextType(textField.getType())).isTrue();
        assertThat(isTextType(simpleByteField.getType())).isFalse();
        assertThat(isTextType(simpleByteField.getType())).isFalse();
    }

    @Test
    public void isEnumFieldTest() {
        // given
        Field someEnumField = getField(ExampleClass.class, "someEnum");
        Field simpleByteField = getField(ExampleClass.class, "simpleByte");
        // when // then
        assertThat(isEnumType(someEnumField)).isTrue();
        assertThat(isEnumType(someEnumField.getType())).isTrue();
        assertThat(isEnumType(simpleByteField.getType())).isFalse();
        assertThat(isEnumType(simpleByteField.getType())).isFalse();
    }

    @Test
    public void isMapFieldTest() {
        // given
        Field mapField = getField(ExampleClass.class, "textByEvent");
        Field simpleByteField = getField(ExampleClass.class, "simpleByte");
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
                create(ExampleClass.class, "textByEvent", false),
                create(ExampleClass.class, "events", false),
                create(ExampleClass.class, "simpleFloat", true),
                create(ExampleClass.class, "simpleInt", true),
                create(ExampleClass.class, "objectInt", true),
                create(ExampleClass.class, "simpleDouble", true),
                create(ExampleClass.class, "objectDouble", true),
                create(ExampleClass.class, "simpleChar", true),
                create(ExampleClass.class, "objectChar", true),
                create(ExampleClass.class, "string", true),
                create(ExampleClass.class, "simpleByte", true),
                create(ExampleClass.class, "objectByte", true),
                create(ExampleClass.class, "dayOfWeek", true),
                create(ExampleClass.class, "localDate", true),
                create(ExampleClass.class, "localDateTime", true),
                create(ExampleClass.class, "localTime", true),
                create(ExampleClass.class, "booleanWrapper", true)
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


    @Test
    public void getParametrizedTypeTest() {
        // given
        Field field = getField(ExampleClass.class, "textByEvent");
        // when
        TypeMetadata typeWrapper = getTypeMetadataFromField(field, 1);
        // then
        assertThat(typeWrapper.hasGenericTypes()).isFalse();
        assertThat(typeWrapper.getRawType()).isEqualTo(ZonedDateTime.class);
    }

    @Test
    public void buildTypeWrapperFromMapWithSickGenericsFieldFromExampleClass() {
        // given
        Field field = getField(ExampleClass.class, "mapWithSickGenerics");
        // when
        TypeMetadata typeWrapperField = getTypeMetadataFromField(field, 1);
        // then
        assertThat(typeWrapperField.getRawType()).isEqualTo(Map.class);
        assertThat(typeWrapperField.isMapType()).isTrue();
        assertThat(typeWrapperField.isSimpleType()).isFalse();
        assertThat(typeWrapperField.hasGenericTypes()).isTrue();
        List<TypeMetadata> genericTypes = typeWrapperField.getGenericTypes();
        assertThat(genericTypes.get(0).getRawType()).isEqualTo(String.class);
        assertThat(genericTypes.get(1).getRawType()).isEqualTo(List.class);
        assertThat(genericTypes.get(1).isHavingElementsType()).isTrue();
        assertThat(genericTypes.get(1).isSimpleType()).isFalse();

        List<TypeMetadata> genericsForList = genericTypes.get(1).getGenericTypes();
        assertThat(genericsForList.get(0).getRawType()).isEqualTo(Map.class);
        List<TypeMetadata> genericsForMap = genericsForList.get(0).getGenericTypes();
        assertThat(genericsForMap.get(0).getRawType()).isEqualTo(String.class);
        assertThat(genericsForMap.get(1).getRawType()).isEqualTo(Integer.class);
    }

    @Test
    public void buildTypeWrapperOnEmptyGenericTypes() {
        // given
        Field field = getField(ExampleClass.class, "emptyGenericsInList");
        // when
        TypeMetadata typeWrapperField = getTypeMetadataFromField(field, 0);
        // then
        assertThat(typeWrapperField.getRawType()).isEqualTo(List.class);
        assertThat(typeWrapperField.hasGenericTypes()).isTrue();
        assertThat(typeWrapperField.getGenericTypes()).hasSize(1);
        assertThat(typeWrapperField.getGenericType(0).getRawType()).isEqualTo(Object.class);
    }

    @Test
    public void buildTypeMetadataWhereSomeArrayIsAsGenericTypeWithEventAsType() {
        // given
        Field field = getField(ExampleClass.class, "mapOfTextWithListOfEventArrays");
        // when
        TypeMetadata typeWrapperField = getTypeMetadataFromField(field, 1);
        // then
        assertThat(typeWrapperField.getRawType()).isEqualTo(List.class);
        assertThat(typeWrapperField.hasGenericTypes()).isTrue();
        assertThat(typeWrapperField.isArrayType()).isFalse();

        TypeMetadata arrayOfEvents = typeWrapperField.getGenericTypes().get(0);
        assertThat(arrayOfEvents.hasGenericTypes()).isTrue();
        assertThat(arrayOfEvents.getRawType()).isEqualTo(Event[].class);
        assertThat(arrayOfEvents.isArrayType()).isTrue();

        TypeMetadata metadataOfEvent = arrayOfEvents.getGenericTypes().get(0);
        assertThat(metadataOfEvent.isArrayType()).isFalse();
        assertThat(metadataOfEvent.hasGenericTypes()).isFalse();
        assertThat(metadataOfEvent.getRawType()).isEqualTo(Event.class);
    }

    @Test
    public void buildTypeMetadataWhereSomeArrayIsAsGenericTypeWithSetOfEventsAsType() {
        // given
        Field field = getField(ExampleClass.class, "mapOfTextWithListOfSetOfEventArrays");
        // when
        TypeMetadata secondTypeAsListOfArrayOfSets = getTypeMetadataFromField(field, 1);
        // then
        assertThat(secondTypeAsListOfArrayOfSets.getRawType()).isEqualTo(List[].class);
        assertThat(secondTypeAsListOfArrayOfSets.hasGenericTypes()).isTrue();
        assertThat(secondTypeAsListOfArrayOfSets.isArrayType()).isTrue();

        TypeMetadata metaOfArrayOfList = secondTypeAsListOfArrayOfSets.getGenericTypes().get(0);

        assertThat(metaOfArrayOfList.getRawType()).isEqualTo(List.class);
        assertThat(metaOfArrayOfList.hasGenericTypes()).isTrue();
        assertThat(metaOfArrayOfList.isArrayType()).isFalse();

        TypeMetadata metaOfSet = metaOfArrayOfList.getGenericTypes().get(0);
        assertThat(metaOfSet.getRawType()).isEqualTo(Set.class);
        assertThat(metaOfSet.isArrayType()).isFalse();

        TypeMetadata metaOfEvent = metaOfSet.getGenericTypes().get(0);
        assertThat(metaOfEvent.getRawType()).isEqualTo(Event.class);
        assertThat(metaOfEvent.isArrayType()).isFalse();
        assertThat(metaOfEvent.hasGenericTypes()).isFalse();
    }

    @Test
    public void getTypeOfArrayWithObject() {
        // given
        Field field = getField(ExampleClass.class, "events");
        // when
        TypeMetadata typeOfArray = getTypeMetadataOfArray(field);
        // then
        assertThat(typeOfArray.isArrayType()).isFalse();
        assertThat(typeOfArray.hasGenericTypes()).isFalse();
        assertThat(typeOfArray.getRawType()).isEqualTo(Event.class);
    }

    @Test
    public void getTypeOfArrayWithSimpleStructure() {
        // given
        Field field = getField(ExampleClass.class, "simpleIntArray");
        // when
        TypeMetadata typeOfArray = getTypeMetadataOfArray(field);
        // then
        assertThat(typeOfArray.isArrayType()).isFalse();
        assertThat(typeOfArray.getRawType()).isEqualTo(int.class);
        assertThat(typeOfArray.hasGenericTypes()).isFalse();
    }

    @Test
    public void getTypeOfArrayWithNestedGenerics() {
        // given
        Field field = getField(ExampleClass.class, "arrayWithListsOfEvents");
        // when
        TypeMetadata typeOfArray = getTypeMetadataOfArray(field);
        // then
        assertThat(typeOfArray.isArrayType()).isFalse();
        assertThat(typeOfArray.hasGenericTypes()).isTrue();
        assertThat(typeOfArray.getRawType()).isEqualTo(List.class);

        List<TypeMetadata> genericTypes = typeOfArray.getGenericTypes();
        TypeMetadata metadataOfMap = genericTypes.get(0);
        assertThat(metadataOfMap.isArrayType()).isFalse();
        assertThat(metadataOfMap.getRawType()).isEqualTo(Map.class);

        List<TypeMetadata> genericTypesOfMap = metadataOfMap.getGenericTypes();
        assertThat(genericTypesOfMap.get(0).getRawType()).isEqualTo(String.class);
        assertThat(genericTypesOfMap.get(1).getRawType()).isEqualTo(Event.class);
    }

    @Test
    public void getTypeOf2DimPrimitiveArray() {
        // given
        Field field = getField(ExampleClass.class, "twoDimSimpleIntArray");
        // when
        TypeMetadata oneDimArray = getTypeMetadataOfArray(field);
        // then
        assertThat(oneDimArray.isArrayType()).isTrue();
        assertThat(oneDimArray.getRawType()).isEqualTo(int[].class);
        assertThat(oneDimArray.isHavingElementsType()).isTrue();
        assertThat(oneDimArray.isSimpleType()).isFalse();
        assertThat(oneDimArray.isEnumType()).isFalse();
        assertThat(oneDimArray.isMapType()).isFalse();

        TypeMetadata primitiveType = oneDimArray.getGenericType(0);
        assertThat(primitiveType.isArrayType()).isFalse();
        assertThat(primitiveType.getRawType()).isEqualTo(int.class);
        assertThat(primitiveType.isSimpleType()).isTrue();
    }

    @Test
    public void getTypeOf3DimArray() {
        // given
        Field field = getField(ExampleClass.class, "threeDimEvents");
        // when
        TypeMetadata twoDimArrayType = getTypeMetadataOfArray(field);
        // then
        assertThat(twoDimArrayType.isArrayType()).isTrue();
        assertThat(twoDimArrayType.getRawType()).isEqualTo(Event[][].class);
        assertThat(twoDimArrayType.hasGenericTypes()).isTrue();

        List<TypeMetadata> typesOfTwoDimArray = twoDimArrayType.getGenericTypes();
        assertThat(typesOfTwoDimArray).hasSize(1);
        TypeMetadata oneDimArray = typesOfTwoDimArray.get(0);
        assertThat(oneDimArray.isArrayType()).isTrue();
        assertThat(oneDimArray.getRawType()).isEqualTo(Event[].class);
        assertThat(oneDimArray.hasGenericTypes()).isTrue();

        TypeMetadata metaOfEvent = oneDimArray.getGenericTypes().get(0);
        assertThat(oneDimArray.getGenericTypes()).hasSize(1);
        assertThat(metaOfEvent.isArrayType()).isFalse();
        assertThat(metaOfEvent.hasGenericTypes()).isFalse();
        assertThat(metaOfEvent.getRawType()).isEqualTo(Event.class);
    }

    @Test
    public void getRawTypesForSecondImplSomeGenericClass() {
        // given
        // when
        List<Type> parametrizedTypesForClass = getParametrizedRawTypes(TypeWrapperBuilderTest.SecondImplSomeGenericClass.class);
        // then
        assertThat(parametrizedTypesForClass).isEmpty();
    }

    @Test
    public void getRawTypesForSomeGenericClass() {
        // given
        // when
        List<Type> parametrizedTypesForClass = getParametrizedRawTypes(TypeWrapperBuilderTest.SomeGenericClass.class);
        // then
        assertThat(parametrizedTypesForClass).hasSize(2);
        assertThat(parametrizedTypesForClass.get(0).getTypeName()).isEqualTo("R");
        assertThat(parametrizedTypesForClass.get(1).getTypeName()).isEqualTo("T");
    }

    @Test
    public void getTypeMetadataFromFieldUnresolvedFieldType() {
        // given
        Field field = getField(ExampleClass.TupleClass.class, "valueOfT");
        when(() ->
                     getTypeMetadataFromField(field))
                .thenException(UnresolvedRealClassException.class,
                               format("Cannot resolve some type for field: %s for class: %s",
                                      "valueOfT",
                                      ExampleClass.TupleClass.class.getCanonicalName()));
        // then
    }

    @Test
    public void buildMetadataFromGenericTypeOfField() {
        // given
        Field field = getField(ExampleClass.class, "integerInfoByText");
        // when
        TypeMetadata fieldMetadata = getTypeMetadataFromType(field.getGenericType());
        // then
        assertThat(fieldMetadata.getRawType()).isEqualTo(Map.class);
        assertThat(fieldMetadata.getGenericTypes()).hasSize(2);
    }

    @Test
    public void buildFromSomeOwnTypeImpl() {
        // given
        Type type = new Type() {
            @Override
            public String getTypeName() {
                return buildTypeName(ExampleClass.class, buildTypeName(List.class), buildTypeName(Map.class, buildTypeName(String.class), buildTypeName(Object.class)));
            }
        };
        // when
        when(()-> getTypeMetadataFromType(type))
                // then
                .thenException(ReflectionOperationException.class,
                               format("raw class: %s doesn't have any parametrized types, but tried put generic types:", ExampleClass.class.getCanonicalName()),
                               "0. List<Object>",
                               "1. Map<String,Object>"
                              );
    }

    @Test
    public void cannotFindSomeClassInType() {
        // given
        Type type = new Type() {
            @Override
            public String getTypeName() {
                return buildTypeName(ExampleClass.class, buildTypeName(List.class), buildTypeName(Map.class, "pl.test.test.SomeClassName", buildTypeName(Object.class)));
            }
        };
        // when
        when(()-> getTypeMetadataFromType(type))
                // then
                .thenException(UnresolvedRealClassException.class,
                               "pl.jalokim.utils.reflection.ReflectionOperationException: java.lang.ClassNotFoundException: pl.test.test.SomeClassName");
    }

    @Test
    public void invalidLabelNameInClass() {
        // given
        Type type = new Type() {
            @Override
            public String getTypeName() {
                return buildTypeName(Map.class, buildTypeName(List.class), "VALUE");
            }
        };
        // when
        when(()-> getTypeMetadataFromType(type))
                // then
                .thenException(UnresolvedRealClassException.class,
                               "pl.jalokim.utils.reflection.ReflectionOperationException: java.lang.ClassNotFoundException: VALUE");
    }

    private static String buildTypeName(Class<?> rawClass, String... genericTypes) {
        if (genericTypes.length > 0) {
            return concat(rawClass.getCanonicalName(), "<", concatElements(Constants.COMMA, genericTypes),  ">");
        }
        return rawClass.getCanonicalName();
    }

    @Test
    public void buildMetadataFromGenericTypeWhenWasRawClass() {
        // given
        Type type = Map.class;
        // when
        TypeMetadata rawMapMetadata = getTypeMetadataFromType(type);
        // then
        assertTypeMetadata(rawMapMetadata, Map.class, 2, MAP)
                .assertGenericTypesAsRawObject();
    }

    @Test
    public void buildMetadataFromGenericTypeWhenWasClass() {
        // given
        Type type = ExampleClass.ConcreteClass.class;
        // when
        TypeMetadata typeMetadataFromType = getTypeMetadataFromType(type);
        assertThat(typeMetadataFromType.hasParent()).isTrue();
        assertThat(typeMetadataFromType.getRawType()).isEqualTo(ExampleClass.ConcreteClass.class);
    }

    @Test
    public void cannotGetTypeOfArrayWhenIsNotArrayField() {
        // given
        Field field = getField(ExampleClass.class, "eventsAsList");
        when(() -> getTypeMetadataOfArray(field))
                .thenException(ReflectionOperationException.class,
                               "field: '" + field + "' is not array type, is type: " + List.class);
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