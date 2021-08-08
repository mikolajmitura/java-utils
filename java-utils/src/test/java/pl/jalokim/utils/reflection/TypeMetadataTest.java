package pl.jalokim.utils.reflection;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.NORMAL_BEAN;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.assertTypeMetadata;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromClass;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.OtherAnnotation;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.SomeAnnotation;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.TupleClass;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.TupleClassImpl;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.TupleClassImpl2;
import pl.jalokim.utils.test.DataFakerHelper;

public class TypeMetadataTest {

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
            .isEqualTo("{TupleClassImpl extends {TupleClass extends RawTuple<List<Number>><String,List<Number>>}}");
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
            "{StringTuple extends {TupleClass extends RawTuple<Map<Number,List<String>>><String,Map<Number,List<String>>>}<RawTuple<List<Map<String,RawTuple<{ConcreteClass extends {StringTuple extends {TupleClass extends RawTuple<NextObject><String,NextObject>}<String,NextObject>}}[][][]>>>[][][]>,Map<Number,List<String>>>}[][]")
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
        assertThat(elements(returnEMethodMeta.getAnnotations())
            .map(Annotation::annotationType)
            .asList())
            .isEqualTo(Collections.singletonList(SomeAnnotation.class));
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
        Method returnEMethod = elements(Arrays.stream(TupleClass.class.getDeclaredMethods())
            .filter(method -> method.getName().equals("returnF")))
            .filter(method -> method.getParameterTypes()[2].equals(String.class))
            .getFirst();

        // when
        MethodMetadata returnEMethodMeta = typeMetadata.getMetaForMethod("returnF", numbers, map, justString);

        // then
        assertThat(returnEMethodMeta.getMethod()).isEqualTo(returnEMethod);
        assertThat(returnEMethodMeta.getName()).isEqualTo("returnF");
        assertThat(elements(returnEMethodMeta.getAnnotations())
            .map(Annotation::annotationType)
            .asList())
            .isEqualTo(Collections.singletonList(SomeAnnotation.class));

        String metaForArrayList = "{ArrayList extends {AbstractList extends AbstractCollection<Number><Number>}<Number>}";

        assertThat(returnEMethodMeta.getReturnType().toString()).isEqualTo(metaForArrayList);

        assertParameterMetadata(returnEMethodMeta.getParameters(), 0, "arArg", metaForArrayList,
            SomeAnnotation.class, OtherAnnotation.class);

        assertParameterMetadata(returnEMethodMeta.getParameters(), 1, "mapOfFAndT", "Map<" + metaForArrayList + ",String>");

        assertParameterMetadata(returnEMethodMeta.getParameters(), 2, "string", "String");
    }

    private void assertParameterMetadata(List<ParameterMetadata> parameters, int index, String name, String typeOfParameter, Class<?>... annotations) {
        ParameterMetadata parameterMetadata = parameters.get(index);
        assertThat(parameterMetadata.getName()).isEqualTo(name);
        assertThat(parameterMetadata.getParameter().getName()).isEqualTo(name);
        assertThat(parameterMetadata.getTypeOfParameter().toString()).isEqualTo(typeOfParameter);
        assertThat(elements(parameterMetadata.getAnnotations())
            .map(Annotation::annotationType)
            .asList()).isEqualTo(Arrays.asList(annotations));
    }
}
