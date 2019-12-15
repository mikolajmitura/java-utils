package pl.jalokim.utils.reflection;

import org.junit.Test;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.NORMAL_BEAN;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.assertTypeMetadata;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromClass;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

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
        TypeMetadata eType = stringTupleNexObject.getTypeMetadataForField(ExampleClass.RawTuple.class, "E");
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
        when(() -> stringTupleNexObject.getTypeMetadataForField(ExampleClass.RawTuple.class, "F"))
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
                .thenException(ReflectionOperationException.class, "field 'someField$' not exist in classes: [pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass, java.lang.Object]");
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
        assertThat("{StringTuple extends {TupleClass extends RawTuple<Map<Number,List<String>>><String,Map<Number,List<String>>>}<RawTuple<List<Map<String,RawTuple<{ConcreteClass extends {StringTuple extends {TupleClass extends RawTuple<NextObject><String,NextObject>}<String,NextObject>}}[][][]>>>[][][]>,Map<Number,List<String>>>}[][]")
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
}