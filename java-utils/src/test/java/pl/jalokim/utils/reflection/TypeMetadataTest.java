package pl.jalokim.utils.reflection;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getConstructor;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.NORMAL_BEAN;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.assertTypeMetadata;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromClass;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

import com.google.common.collect.Sets;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.GenericInterface;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.OtherAnnotation;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.OtherInterface;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.SomeAnnotation;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.TupleClass;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.TupleClassImpl;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.TupleClassImpl2;
import pl.jalokim.utils.test.DataFakerHelper;

public class TypeMetadataTest {

    private static final String EXPECTED_META_FOR_ARRAY_LIST = "ArrayList<Number> extends AbstractList<Number> extends AbstractCollection<Number>";
    private static final String METHOD_NAME_RETURN_F = "returnF";

    @Test
    public void getCanonicalNameOfRawClass() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        // when
        // then
        assertThat(exampleClassMeta.getCanonicalName()).isEqualTo(ExampleClass.class.getCanonicalName());
    }

    @Test
    public void getFieldFromTypeMetadata() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        // when
        TypeMetadata stringTupleNexObject = exampleClassMeta.getMetaForField("stringTupleNexObject");
        // then
        assertThat(stringTupleNexObject.getRawType()).isEqualTo(ExampleClass.StringTuple.class);
        assertThat(stringTupleNexObject.hasParent()).isTrue();
        assertThat(stringTupleNexObject.getGenericTypes()).hasSize(2);
    }

    @Test
    public void getMetadataByLabelAndIsAvailable() {
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        TypeMetadata stringTupleNexObject = exampleClassMeta.getMetaForField("stringTupleNexObject");
        // when
        TypeMetadata eType = stringTupleNexObject.getTypeMetadataByGenericLabel(ExampleClass.RawTuple.class, "E");
        // then
        assertThat(eType.getRawType()).isEqualTo(Map.class);
        assertThat(eType.getGenericType(0).getRawType()).isEqualTo(Number.class);
        assertThat(eType.getGenericType(1).getRawType()).isEqualTo(List.class);
        assertThat(eType.getByGenericLabel("K").getRawType()).isEqualTo(Number.class);
    }

    @Test
    public void getMetadataByLabelAndIsNotAvailable() {
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        TypeMetadata stringTupleNexObject = exampleClassMeta.getMetaForField("stringTupleNexObject");
        // when
        when(() -> stringTupleNexObject.getTypeMetadataByGenericLabel(ExampleClass.RawTuple.class, "F"))
            .thenException(ReflectionOperationException.class,
                format("Cannot find raw type: '%s' for class: '%s' in current context: %s ",
                    "F", ExampleClass.RawTuple.class.getCanonicalName(), stringTupleNexObject));
    }

    @Test
    public void generateInfoForExtendedClass() {
        // when
        TypeMetadata tupleImplMeta = buildFromClass(ExampleClass.TupleClassImpl.class);
        // then
        assertThat(tupleImplMeta.toString())
            .isEqualTo("TupleClassImpl extends TupleClass<String,List<Number>> extends RawTuple<List<Number>>");
    }

    @Test
    public void cannotGetGenericTypeByIndexWhenNotExist() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        TypeMetadata stringTupleNexObject = exampleClassMeta.getMetaForField("stringTupleNexObject");

        when(() -> {
                assertThat(stringTupleNexObject.getGenericTypes()).hasSize(2);
                stringTupleNexObject.getGenericType(2);
            }
            // then
        ).thenException(new ReflectionOperationException("Cannot find generic type at index: 2"));

        when(() -> {
                assertThat(exampleClassMeta.hasGenericTypes()).isFalse();
                exampleClassMeta.getGenericType(0);
            }
            // then
        ).thenException(new ReflectionOperationException("Cannot find generic type at index: 0"));
    }

    @Test
    public void toStringOnMetaWhenArrayType() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        TypeMetadata arrayWithListsOfEvents = exampleClassMeta.getMetaForField("arrayWithListsOfEvents");
        // when
        String arrayMetaToString = arrayWithListsOfEvents.toString();
        // then
        assertThat(arrayMetaToString).isEqualTo("List<Map<String,Event>>[]");
    }

    @Test
    public void toStringOnMapOfTextWithListOfSetOfEventArrays() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        TypeMetadata mapOfTextWithListOfSetOfEventArrays = exampleClassMeta.getMetaForField("mapOfTextWithListOfSetOfEventArrays");
        // when
        String arrayMetaToString = mapOfTextWithListOfSetOfEventArrays.toString();
        // then
        assertThat(arrayMetaToString).isEqualTo("Map<String,List<Set<Event>>[]>");
    }

    @Test
    public void getFieldWhenInNotGenericClass() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        // when
        TypeMetadata nextObject = exampleClassMeta.getMetaForField("nextObject");
        // then
        assertThat(nextObject.hasGenericTypes()).isFalse();
        assertThat(nextObject.getRawType()).isEqualTo(ExampleClass.NextObject.class);
    }

    @Test
    public void getFieldWhichComesFromAnotherField() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        TypeMetadata stringTupleNexObjectMeta = exampleClassMeta.getMetaForField("stringTupleNexObject");
        // when
        TypeMetadata fromStringATypeNextObject = stringTupleNexObjectMeta.getMetaForField("fromStringA");
        // then
        assertThat(fromStringATypeNextObject.getRawType()).isEqualTo(ExampleClass.NextObject.class);
    }

    @Test
    public void cannotGetFieldFromExampleClass() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        when(() -> exampleClassMeta.getMetaForField("someField$"))
            .thenException(ReflectionOperationException.class,
                "field 'someField$' not exist in classes: [pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass, java.lang.Object]");
    }

    @Test
    public void cannotGetGenericTypesForTupleClassRaw() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        // when
        TypeMetadata tupleNextObjectMeta = exampleClassMeta.getMetaForField("tupleNextObject");
        TypeMetadata tupleClassRaw = tupleNextObjectMeta.getMetaForField("tupleClassRaw");
        // then
        assertThat(tupleClassRaw.getGenericType(0).getRawType()).isEqualTo(Object.class);
        assertThat(tupleClassRaw.getGenericType(1).getRawType()).isEqualTo(Object.class);
    }

    @Test
    public void buildFromClassInvalidTupleExtension() {
        // when
        TypeMetadata invalidTupleExtends = buildFromClass(ExampleClass.InvalidTupleExtension.class);
        // then
        assertThat(invalidTupleExtends.getRawType()).isEqualTo(ExampleClass.InvalidTupleExtension.class);

        assertThat(invalidTupleExtends.hasGenericTypes()).isFalse();
        assertThat(invalidTupleExtends.hasParent()).isTrue();

        TypeMetadata tupleMetadata = invalidTupleExtends.getParentTypeMetadata();
        assertThat(tupleMetadata.getRawType()).isEqualTo(ExampleClass.TupleClass.class);
        assertThat(tupleMetadata.getGenericType(0).getRawType()).isEqualTo(Object.class);
        assertThat(tupleMetadata.getGenericType(1).getRawType()).isEqualTo(Object.class);

        TypeMetadata rawTupleMetadata = tupleMetadata.getParentTypeMetadata();
        assertThat(rawTupleMetadata.getRawType()).isEqualTo(ExampleClass.RawTuple.class);
        assertThat(rawTupleMetadata.getGenericType(0).getRawType()).isEqualTo(Object.class);
    }

    @Test
    public void getTypeOfFieldFromInvalidTupleExtension() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.InvalidTupleExtension.class);
        assertThat(exampleClassMeta.getRawType()).isEqualTo(ExampleClass.InvalidTupleExtension.class);
        // when
        TypeMetadata rawValueE = exampleClassMeta.getMetaForField("rawValueE");
        // then
        assertThat(rawValueE.getRawType()).isEqualTo(Object.class);
    }

    @Test
    public void testToStringOnSuperMixedArrayField() {
        // given
        TypeMetadata exampleClassMeta = buildFromClass(ExampleClass.class);
        // when
        TypeMetadata superMixedArray = exampleClassMeta.getMetaForField("superMixedArray");
        // then
        assertThat(
            "StringTuple<RawTuple<List<Map<String,RawTuple<ConcreteClass extends StringTuple<String,NextObject> "
                + "extends TupleClass<String,NextObject> extends RawTuple<NextObject>[][][]>>>[][][]>,Map<Number,List<String>>> "
                + "extends TupleClass<String,Map<Number,List<String>>> extends RawTuple<Map<Number,List<String>>>[][]")
            .isEqualTo(superMixedArray.toString());
    }

    @Test
    public void buildFromClassInvalidRawTupleExtension() {
        // when
        TypeMetadata invalidRawTupleExt = buildFromClass(ExampleClass.InvalidRawTupleExtension.class);
        // then
        assertTypeMetadata(invalidRawTupleExt,
            ExampleClass.InvalidRawTupleExtension.class,
            true,
            NORMAL_BEAN)
            .getParent()
            .assertTypeMetadata(ExampleClass.RawTuple.class,
                1,
                NORMAL_BEAN)
            .getGenericType(0)
            .assertTypeMetadata(ExampleClass.TupleClass.class,
                true,
                2,
                NORMAL_BEAN)
            .assertGenericType(0, Object.class, false, 0, NORMAL_BEAN)
            .assertGenericType(1, Object.class, false, 0, NORMAL_BEAN);
    }

    @Test
    public void returnExpectedMethodMetadataAndRawField() throws NoSuchMethodException {
        // given
        TypeMetadata typeMetadata = buildFromClass(TupleClassImpl.class);

        // when
        TypeMetadata rawValueE1 = typeMetadata.getMetaForField("rawValueE");

        // then
        assertThat(rawValueE1.toString()).isEqualTo("List<Number>");

        // and
        // given
        Method returnEMethod = elements(Arrays.stream(TupleClass.class.getDeclaredMethods())
            .filter(method -> method.getName().equals("returnF")))
            .filter(method -> method.getParameterTypes()[2].equals(String.class))
            .getFirst();

        // when
        MethodMetadata returnEMethodMeta = typeMetadata.getMetaForMethod(returnEMethod);

        // then
        assertThat(returnEMethodMeta.getMethod()).isEqualTo(returnEMethod);
        assertThat(returnEMethodMeta.getName()).isEqualTo("returnF");
        assertAnnotations(returnEMethodMeta.getAnnotations(), SomeAnnotation.class);

        assertThat(returnEMethodMeta.getReturnType().toString()).isEqualTo("List<Number>");

        assertParameterMetadata(returnEMethodMeta.getParameters(), 0, "arArg", "List<Number>",
            SomeAnnotation.class, OtherAnnotation.class);

        assertParameterMetadata(returnEMethodMeta.getParameters(), 1, "mapOfFAndT", "Map<List<Number>,String>");

        assertParameterMetadata(returnEMethodMeta.getParameters(), 2, "string", "String");
    }

    @Test
    public void returnExpectedMethodMetadataByNameAndArgs() {
        // given
        TypeMetadata typeMetadata = buildFromClass(TupleClassImpl2.class);
        List<Number> numbers = new ArrayList<>();
        String justString = DataFakerHelper.randomText();
        Map<List<Number>, String> map = new HashMap<>();
        Method expectedReturnEMethod = elements(Arrays.stream(TupleClass.class.getDeclaredMethods())
            .filter(method -> method.getName().equals(METHOD_NAME_RETURN_F)))
            .filter(method -> method.getParameterTypes()[2].equals(String.class))
            .getFirst();

        // when
        MethodMetadata returnEMethodMeta = typeMetadata.getMetaForMethodByArgsToInvoke(METHOD_NAME_RETURN_F, numbers, map, justString);
        // then
        assertGenericMethodReturnF(expectedReturnEMethod, EXPECTED_META_FOR_ARRAY_LIST, returnEMethodMeta);

        // when
        MethodMetadata returnEMethodMetaByRealArgToInvoke = typeMetadata.getMetaForMethodByArgsToInvoke(METHOD_NAME_RETURN_F,
            Arrays.asList(numbers, map, justString));
        // then
        assertGenericMethodReturnF(expectedReturnEMethod, EXPECTED_META_FOR_ARRAY_LIST, returnEMethodMetaByRealArgToInvoke);

        // and
        // when
        MethodMetadata returnEMethodMetaByNameAndClasses = typeMetadata.getMetaForMethod(METHOD_NAME_RETURN_F, Object.class, Map.class, String.class);
        // when
        assertGenericMethodReturnF(expectedReturnEMethod, EXPECTED_META_FOR_ARRAY_LIST, returnEMethodMetaByNameAndClasses);

        // and
        // given
        List<Class<?>> argTypes = Arrays.asList(Object.class, Map.class, String.class);
        // when
        MethodMetadata metaForMethodByArgClasses = typeMetadata.getMetaForMethod(METHOD_NAME_RETURN_F, argTypes);
        // then
        assertGenericMethodReturnF(expectedReturnEMethod, EXPECTED_META_FOR_ARRAY_LIST, metaForMethodByArgClasses);
    }

    private void assertGenericMethodReturnF(Method expectedReturnEMethod, String metaForArrayList, MethodMetadata returnEMethodMeta) {
        assertThat(returnEMethodMeta.getReturnType().toString()).isEqualTo(metaForArrayList);

        assertThat(returnEMethodMeta.getMethod()).isEqualTo(expectedReturnEMethod);
        assertThat(returnEMethodMeta.getName()).isEqualTo(METHOD_NAME_RETURN_F);
        assertAnnotations(returnEMethodMeta.getAnnotations(), SomeAnnotation.class);

        assertParameterMetadata(returnEMethodMeta.getParameters(), 0, "arArg", metaForArrayList,
            SomeAnnotation.class, OtherAnnotation.class);

        assertParameterMetadata(returnEMethodMeta.getParameters(), 1, "mapOfFAndT", "Map<" + metaForArrayList + ",String>");

        assertParameterMetadata(returnEMethodMeta.getParameters(), 2, "string", "String");
    }

    @Test
    public void listMetaAboutInterfacesMetadata() {
        // when
        TypeMetadata typeMetadata = buildFromClass(TupleClassImpl2.class);
        // then
        assertThat(typeMetadata.getRawType()).isEqualTo(TupleClassImpl2.class);
        assertThat(typeMetadata.getParentInterfaces()).isEmpty();

        TypeMetadata tupleClassMetadata = typeMetadata.getParentTypeMetadata();
        List<TypeMetadata> parentInterfaces = tupleClassMetadata.getParentInterfaces();

        assertThat(tupleClassMetadata.getRawType()).isEqualTo(TupleClass.class);
        assertThat(parentInterfaces).hasSize(2);

        TypeMetadata genericInterfaceMetadata = parentInterfaces.get(0);
        assertThat(genericInterfaceMetadata.getRawType()).isEqualTo(GenericInterface.class);
        TypeMetadata typeMetadataOfGeneric1 = genericInterfaceMetadata.getGenericTypes().get(0);
        assertThat(typeMetadataOfGeneric1.getRawType()).isEqualTo(String.class);
        assertThat(typeMetadataOfGeneric1.getParentInterfaces()).hasSize(3);
        assertThat(elements(typeMetadataOfGeneric1.getParentInterfaces())
            .map(TypeMetadata::getRawType)
            .asSet()).isEqualTo(Sets.newHashSet(Serializable.class, CharSequence.class, Comparable.class));

        assertThat(genericInterfaceMetadata.getGenericTypes().get(1).getRawType()).isEqualTo(ArrayList.class);

        TypeMetadata otherInterfaceMetadata = parentInterfaces.get(1);
        assertThat(otherInterfaceMetadata.getRawType()).isEqualTo(OtherInterface.class);
        assertThat(otherInterfaceMetadata.getGenericTypes().get(0).getRawType()).isEqualTo(List.class);
        assertThat(otherInterfaceMetadata.getGenericTypes().get(1).getRawType()).isEqualTo(ArrayList.class);
    }

    @Test
    public void getMetaForConstructor() {
        // given
        TypeMetadata typeMetadata = buildFromClass(TupleClassImpl2.class);
        Constructor<?> constructor = getConstructor(TupleClassImpl2.class, String.class, ArrayList.class);

        // when
        ConstructorMetadata metaForConstructorByInstance = typeMetadata.getMetaForConstructor(constructor);

        // then
        assertConstructorMetadata(constructor, metaForConstructorByInstance);

        // and
        // when
        ConstructorMetadata metaForConstructorByArgTypes = typeMetadata.getMetaForConstructor(String.class, ArrayList.class);

        // then
        assertConstructorMetadata(constructor, metaForConstructorByArgTypes);

        // and
        // when
        metaForConstructorByArgTypes = typeMetadata.getMetaForConstructor(Arrays.asList(String.class, ArrayList.class));

        // then
        assertConstructorMetadata(constructor, metaForConstructorByArgTypes);

        // and
        // given
        List<String> numbers = new ArrayList<>();
        String justString = DataFakerHelper.randomText();

        // when
        metaForConstructorByArgTypes = typeMetadata.getMetaForConstructorByArgsToInvoke(justString, numbers);

        // then
        assertConstructorMetadata(constructor, metaForConstructorByArgTypes);
    }

    private void assertConstructorMetadata(Constructor<?> constructor, ConstructorMetadata metaForConstructorByInstance) {
        assertThat(metaForConstructorByInstance.getConstructor()).isEqualTo(constructor);
        assertAnnotations(metaForConstructorByInstance.getAnnotations(), SomeAnnotation.class);
        List<ParameterMetadata> constructorParameters = metaForConstructorByInstance.getParameters();
        assertParameterMetadata(constructorParameters, 0, "valueOfT", "String");
        assertParameterMetadata(constructorParameters, 1, "valueOfF", EXPECTED_META_FOR_ARRAY_LIST,
            OtherAnnotation.class, SomeAnnotation.class);
    }

    private void assertParameterMetadata(List<ParameterMetadata> parameters, int index, String name, String typeOfParameter, Class<?>... annotations) {
        ParameterMetadata parameterMetadata = parameters.get(index);
        assertThat(parameterMetadata.getName()).isEqualTo(name);
        assertThat(parameterMetadata.getParameter().getName()).isEqualTo(name);
        assertThat(parameterMetadata.getTypeOfParameter().toString()).isEqualTo(typeOfParameter);
        assertAnnotations(parameterMetadata.getAnnotations(), annotations);
    }

    private void assertAnnotations(List<Annotation> toCheck, Class<?>... expectedTypes) {
        assertThat(elements(toCheck)
            .map(Annotation::annotationType)
            .asList())
            .isEqualTo(elements(expectedTypes).asList());
    }
}
