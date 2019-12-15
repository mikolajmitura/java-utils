package pl.jalokim.utils.reflection;

import lombok.Data;
import org.junit.Test;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass;
import pl.jalokim.utils.reflection.beans.inheritiance.TransactionType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromClass;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.ENUM;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.HAVING_ELEMENTS;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.MAP;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.NATIVE_ARRAY;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.NORMAL_BEAN;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.assertTypeMetadata;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromClass;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromField;

public class TypeWrapperBuilderTest {

    @Test
    public void gatherTypeMetadataForImplSomeGenericClassClass() {
        // given
        // when
        TypeMetadata implSomeGenericClass = buildFromClass(ImplSomeGenericClass.class);
        TypeMetadata typeMetadataFromMetaUtils = getTypeMetadataFromClass(ImplSomeGenericClass.class);
        // then
        assertThat(implSomeGenericClass).isEqualTo(typeMetadataFromMetaUtils);

        assertTypeMetadata(implSomeGenericClass,
                           ImplSomeGenericClass.class,
                           true,
                           NORMAL_BEAN)
                .getParent()
                .assertGenericType(0, Number.class)
                .getGenericType(1)
                .assertTypeMetadata(List[].class, 1, NATIVE_ARRAY)
                .getGenericType(0)
                .assertTypeMetadata(List.class, 1, HAVING_ELEMENTS)
                .getGenericType(0)
                .assertTypeMetadata(Set.class, 1, HAVING_ELEMENTS)
                .getGenericType(0)
                .assertTypeMetadata(ExampleClass[].class, 1, NATIVE_ARRAY)
                .getGenericType(0)
                .assertTypeMetadata(ExampleClass.class, NORMAL_BEAN);
    }

    @Test
    public void gatherTypeMetadataForSomeGenericClass() {
        // given
        // when
        TypeMetadata someGenericClass = buildFromClass(SomeGenericClass.class);
        // then
        assertTypeMetadata(someGenericClass, SomeGenericClass.class,
                           2,
                           NORMAL_BEAN)
                .assertGenericType(0, Object.class, false, 0, NORMAL_BEAN)
                .assertGenericType(1, Object.class, false, 0, NORMAL_BEAN);
    }

    @Test
    public void gatherTypeMetadataForTransactionType() {
        // given
        // when
        TypeMetadata typeMetadata = buildFromClass(TransactionType.class);
        // then
        assertTypeMetadata(typeMetadata, TransactionType.class, ENUM);
    }

    @Test
    public void gatherTypeMetadataForSecondImplSomeGenericClass() {
        // given
        // when
        TypeMetadata rootMetadataSecondImpl = buildFromClass(SecondImplSomeGenericClass.class);
        // then
        assertThat(rootMetadataSecondImpl.isArrayType()).isFalse();
        assertThat(rootMetadataSecondImpl.getRawType()).isEqualTo(SecondImplSomeGenericClass.class);
        assertThat(rootMetadataSecondImpl.hasGenericTypes()).isFalse();

        TypeMetadata someGenericClassMeta = rootMetadataSecondImpl.getParentTypeMetadata();

        List<TypeMetadata> genericTypes = someGenericClassMeta.getGenericTypes();
        assertThat(genericTypes.get(0).getRawType()).isEqualTo(String.class);
        assertThat(genericTypes.get(0).hasGenericTypes()).isFalse();

        TypeMetadata metaDataForList = genericTypes.get(1);
        assertThat(metaDataForList.getRawType()).isEqualTo(List.class);
        assertThat(metaDataForList.hasGenericTypes()).isTrue();
        assertThat(metaDataForList.getGenericType(0).getRawType()).isEqualTo(Object.class);
    }

    @Test
    public void gatherTypeMetadataForSomeGenericClassWithExtends() {
        // given
        // when
        TypeMetadata someExtendsGenericMeta = buildFromClass(SomeGenericClassWithExtends.class);
        // then
        assertThat(someExtendsGenericMeta.isArrayType()).isFalse();
        assertThat(someExtendsGenericMeta.getRawType()).isEqualTo(SomeGenericClassWithExtends.class);
        assertThat(someExtendsGenericMeta.hasGenericTypes()).isFalse();

        TypeMetadata someGenericClassMeta = someExtendsGenericMeta.getParentTypeMetadata();

        TypeMetadata firstList = someGenericClassMeta.getGenericType(0);
        assertThat(firstList.getRawType()).isEqualTo(List.class);
        assertThat(firstList.hasGenericTypes()).isTrue();
        assertThat(firstList.getGenericType(0).getRawType()).isEqualTo(Cat.class);

        TypeMetadata secondList = someGenericClassMeta.getGenericType(1);
        assertThat(secondList.getRawType()).isEqualTo(List.class);
        assertThat(secondList.hasGenericTypes()).isTrue();
        assertThat(secondList.getGenericType(0).getRawType()).isEqualTo(Object.class);
    }

    @Test
    public void buildTypeMetadataFromTupleIntegerInfoField() {
        // given
        Field tupleIntegerInfo = getField(ExampleClass.class, "tupleIntegerInfo");
        // when
        TypeMetadata typeMetadata = buildFromField(tupleIntegerInfo);
        // then
        assertThat(typeMetadata.isArrayType()).isFalse();
        assertThat(typeMetadata.hasGenericTypes()).isTrue();
        assertThat(typeMetadata.getRawType()).isEqualTo(ExampleClass.TupleClass.class);
        TypeMetadata metadataOfIntegerInfo = typeMetadata.getGenericType(0);

        assertThat(metadataOfIntegerInfo.getRawType()).isEqualTo(ExampleClass.IntegerInfo.class);
        assertThat(metadataOfIntegerInfo.isArrayType()).isFalse();
        assertThat(metadataOfIntegerInfo.hasGenericTypes()).isFalse();
    }

    @Test
    public void buildTypeMetadataFromRequester2Field() {
        // given
        Field requester2Metadata = getField(ExampleClass.class, "requester2");
        // when
        TypeMetadata typeMetadata = buildFromField(requester2Metadata);
        // then
        assertThat(typeMetadata.isArrayType()).isFalse();
        assertThat(typeMetadata.hasGenericTypes()).isFalse();
        assertThat(typeMetadata.getRawType()).isEqualTo(ExampleClass.NextObject.class);
    }

    @Test
    public void buildTypeMetadataFromTupleNextObjectArrayField() {
        // given
        Field tupleIntegerInfo = getField(ExampleClass.class, "tupleNextObjectArray");
        // when
        TypeMetadata typeMetadata = buildFromField(tupleIntegerInfo);
        // then
        assertThat(typeMetadata.isArrayType()).isFalse();
        assertThat(typeMetadata.hasGenericTypes()).isTrue();
        assertThat(typeMetadata.getRawType()).isEqualTo(ExampleClass.TupleClass.class);
        TypeMetadata metaOfNextObjectArray = typeMetadata.getGenericType(0);

        assertThat(metaOfNextObjectArray.getRawType()).isEqualTo(ExampleClass.NextObject[].class);
        assertThat(metaOfNextObjectArray.isArrayType()).isTrue();

        TypeMetadata metaOfNextObject = metaOfNextObjectArray.getGenericType(0);
        assertThat(metaOfNextObject.getRawType()).isEqualTo(ExampleClass.NextObject.class);
        assertThat(metaOfNextObject.isArrayType()).isFalse();
    }

    @Test
    public void buildTypeMetadataFromPrimitiveField() {
        // given
        Field tupleIntegerInfo = getField(ExampleClass.class, "simpleChar");
        // when
        TypeMetadata typeMetadata = buildFromField(tupleIntegerInfo);
        // then
        assertThat(typeMetadata.getRawType()).isEqualTo(char.class);
        assertThat(typeMetadata.hasGenericTypes()).isFalse();
        assertThat(typeMetadata.isArrayType()).isFalse();
        assertThat(typeMetadata.getParentTypeMetadata()).isNull();
    }

    @Test
    public void buildTypeMetadataFromStringTupleOfIntegerAndNumber() {
        // given
        Field tupleIntegerInfo = getField(ExampleClass.class, "stringTupleIntegerNumber");
        // when
        TypeMetadata metadataOfField = buildFromField(tupleIntegerInfo);
        // then
        assertThat(metadataOfField.getRawType()).isEqualTo(ExampleClass.StringTuple.class);

        assertThat(metadataOfField.getGenericType(0).getRawType()).isEqualTo(Integer.class);

        TypeMetadata mapMetadata = metadataOfField.getGenericType(1);
        assertThat(mapMetadata.getRawType()).isEqualTo(Map.class);
        assertThat(mapMetadata.getGenericType(0).getRawType()).isEqualTo(Number.class);
        assertThat(mapMetadata.getGenericType(1).getRawType()).isEqualTo(String.class);


        TypeMetadata metadataOfTupleClass = metadataOfField.getParentTypeMetadata();
        assertThat(metadataOfTupleClass.getRawType()).isEqualTo(ExampleClass.TupleClass.class);
        assertThat(metadataOfTupleClass.getGenericType(0).getRawType()).isEqualTo(String.class);
        assertThat(metadataOfTupleClass.getGenericType(1).getRawType()).isEqualTo(Map.class);
        assertThat(mapMetadata == metadataOfTupleClass.getGenericType(1)).isTrue();

        TypeMetadata metaOfRawTuple = metadataOfTupleClass.getParentTypeMetadata();
        assertThat(metaOfRawTuple.getRawType()).isEqualTo(ExampleClass.RawTuple.class);

        assertThat(metaOfRawTuple.getParentTypeMetadata()).isNull();
        assertThat(metaOfRawTuple.getGenericType(0).getRawType()).isEqualTo(Map.class);
        assertThat(metaOfRawTuple.getGenericType(0) == mapMetadata).isTrue();
    }

    @Test
    public void testOfBuildArrayMetaDataFromOneDimPrimitiveArrayField() {
        // given
        Field simpleIntArray = getField(ExampleClass.class, "simpleIntArray");
        // when
        TypeMetadata typeMetadataOfSimpleField = buildFromField(simpleIntArray);
        // then
        assertThat(typeMetadataOfSimpleField.getRawType()).isEqualTo(int[].class);
        assertThat(typeMetadataOfSimpleField.isArrayType()).isTrue();
        assertThat(typeMetadataOfSimpleField.getGenericTypes()).hasSize(1);

        TypeMetadata typeOfArray = typeMetadataOfSimpleField.getGenericType(0);
        assertThat(typeOfArray.getRawType()).isEqualTo(int.class);
        assertThat(typeOfArray.hasGenericTypes()).isFalse();
    }

    @Test
    public void testOfBuildArrayMetaDataFromTwoDimPrimitiveArrayField() {
        // given
        Field twoDimSimpleIntArray = getField(ExampleClass.class, "twoDimSimpleIntArray");
        // when
        TypeMetadata twoDimArray = buildFromField(twoDimSimpleIntArray);
        // then
        // then
        assertThat(twoDimArray.isArrayType()).isTrue();
        assertThat(twoDimArray.getRawType()).isEqualTo(int[][].class);

        TypeMetadata oneDimArray = twoDimArray.getGenericType(0);

        assertThat(oneDimArray.isArrayType()).isTrue();
        assertThat(oneDimArray.getRawType()).isEqualTo(int[].class);

        TypeMetadata primitiveType = oneDimArray.getGenericType(0);
        assertThat(primitiveType.isArrayType()).isFalse();
        assertThat(primitiveType.getRawType()).isEqualTo(int.class);
        assertThat(primitiveType.isSimpleType()).isTrue();
    }

    @Test
    public void metadataForTransactionTypeEnumArray() {
        // given
        Field transactionTypesArray = getField(ExampleClass.class, "transactionTypes");
        // when
        TypeMetadata oneDimArray = buildFromField(transactionTypesArray);
        // then
        assertThat(oneDimArray.isArrayType()).isTrue();
        assertThat(oneDimArray.getRawType()).isEqualTo(TransactionType[].class);

        TypeMetadata primitiveType = oneDimArray.getGenericType(0);
        assertThat(primitiveType.isArrayType()).isFalse();
        assertThat(primitiveType.isEnumType()).isTrue();
        assertThat(primitiveType.isSimpleType()).isTrue();
        assertThat(primitiveType.getRawType()).isEqualTo(TransactionType.class);
    }

    @Test
    public void testOfBuildArrayMetaDataFromThreeDimConcreteArrayField() {
        // given
        Field threeDimConcreteArray = getField(ExampleClass.class, "threeDimConcreteArray");
        // when
        TypeMetadata metaOd3DimArray = buildFromField(threeDimConcreteArray);
        // then
        assertThat(metaOd3DimArray.getRawType()).isEqualTo(ExampleClass.ConcreteClass[][][].class);
        assertThat(metaOd3DimArray.isArrayType()).isTrue();
        assertThat(metaOd3DimArray.hasParent()).isFalse();

        TypeMetadata metaOd2DimArray = metaOd3DimArray.getGenericType(0);
        assertThat(metaOd2DimArray.isArrayType()).isTrue();
        assertThat(metaOd2DimArray.getRawType()).isEqualTo(ExampleClass.ConcreteClass[][].class);

        TypeMetadata metaOd1DimArray = metaOd2DimArray.getGenericType(0);
        assertThat(metaOd1DimArray.isArrayType()).isTrue();
        assertThat(metaOd1DimArray.getRawType()).isEqualTo(ExampleClass.ConcreteClass[].class);

        TypeMetadata rawTypeOfArray = metaOd1DimArray.getGenericType(0);
        assertThat(rawTypeOfArray.isArrayType()).isFalse();
        assertThat(rawTypeOfArray.getRawType()).isEqualTo(ExampleClass.ConcreteClass.class);

        TypeMetadata parentTypeMetadata = rawTypeOfArray.getParentTypeMetadata();
        assertThat(parentTypeMetadata.getRawType()).isEqualTo(ExampleClass.StringTuple.class);
        assertThat(parentTypeMetadata.getGenericType(0).getRawType()).isEqualTo(String.class);
        assertThat(parentTypeMetadata.getGenericType(1).getRawType()).isEqualTo(ExampleClass.NextObject.class);
    }

    @Test
    public void buildMetadataForStringTupleNexObject() {
        // given
        Field stringTupleNexObjectField = getField(ExampleClass.class, "stringTupleNexObject");
        // when
        TypeMetadata stringTupleNexObjectMeta = buildFromField(stringTupleNexObjectField);
        // then
        MetadataAssertionContext rootContextAssert = assertTypeMetadata(stringTupleNexObjectMeta, ExampleClass.StringTuple.class, true, 2, NORMAL_BEAN)
                .assertGenericType(0, ExampleClass.NextObject.class, NORMAL_BEAN);

        rootContextAssert.getGenericType(1)
                         .assertTypeMetadata(Map.class, 2, MAP)
                         .assertGenericType(0, Number.class)
                         .assertGenericType(1, List.class, 1, HAVING_ELEMENTS)
                         .getGenericType(1)
                         .assertGenericType(0, String.class);

        rootContextAssert.getParent()
                         .assertTypeMetadata(ExampleClass.TupleClass.class, true, 2, NORMAL_BEAN)
                         .assertGenericType(0, String.class)
                         .getGenericType(1).isEqualTo(rootContextAssert.getGenericType(1))
                         .getChildContext()
                         .getParent()
                         .assertTypeMetadata(ExampleClass.RawTuple.class, 1, NORMAL_BEAN)
        .getGenericType(0).isEqualTo(rootContextAssert.getGenericType(1));
    }

    @Test
    public void testToStringOnInnerTypeMetaData() {
        // given
        TypeWrapperBuilder.InnerTypeMetaData innerTypeMetaData = new TypeWrapperBuilder.InnerTypeMetaData(null, null);
        String simpleClassName = "pl.jalokim.test.ClassName";
        populateInnerMetadata(innerTypeMetaData, simpleClassName);
        // when
        String text = innerTypeMetaData.toString();
        // then
        assertThat(text).isEqualTo("InnerTypeMetaData{parent=null, className=" + simpleClassName + ", genericTypes=[]}");
    }

    @Test
    public void testToStringOnInnerTypeMetaDataWithParentData() {
        // given
        TypeWrapperBuilder.InnerTypeMetaData innerTypeMetaData = new TypeWrapperBuilder.InnerTypeMetaData(null, null);
        String simpleClassName = "pl.jalokim.test.ClassName";
        populateInnerMetadata(innerTypeMetaData, simpleClassName);

        TypeWrapperBuilder.InnerTypeMetaData parentMetadata = new TypeWrapperBuilder.InnerTypeMetaData(null, null);
        String parentClassName = "pl.jalokim.ParentMeta";
        populateInnerMetadata(parentMetadata, parentClassName);
        innerTypeMetaData.setParent(parentMetadata);
        // when
        String text = innerTypeMetaData.toString();
        // then
        assertThat(text).isEqualTo("InnerTypeMetaData{parent=" + parentClassName + ", className=" + simpleClassName + ", genericTypes=[]}");
    }

    private void populateInnerMetadata(TypeWrapperBuilder.InnerTypeMetaData innerTypeMetaData, String text) {
        for (char currentChar : text.toCharArray()) {
            innerTypeMetaData.appendToClassName(currentChar);
        }
    }

    @Data
    public static class SomeGenericClass<R, T> {
        R objectR;
        T objectT;
    }

    public static class ImplSomeGenericClass extends SomeGenericClass<Number, List<Set<ExampleClass[]>>[]> {

    }

    public static class SecondImplSomeGenericClass extends SomeGenericClass<String, List<?>> {

    }

    public static class SomeGenericClassWithExtends extends SomeGenericClass<List<? extends Cat>, List<? super Cat>> {
        public void testGenericTypes() {
            List<? extends Cat> objectR = getObjectR();
//             objectR.add(new Cat());  // not compile
//             objectR.add(new Tiger()); // not compile
            Cat cat = objectR.get(0);
            objectR = new ArrayList<Cat>();
            objectR = new ArrayList<Tiger>();
//            objectR = new ArrayList<AbstractAnimal>();

            List<? super Cat> objectTs = getObjectT();
            objectT.add(new Tiger());
            objectT.add(new Cat());
            Object objectT = objectTs.get(0);
            //objectTs = new ArrayList<Tiger>();
            objectTs = new ArrayList<Cat>();
            objectTs = new ArrayList<AbstractAnimal>();
            objectTs = new ArrayList<Object>();
        }
    }

    public static class AbstractAnimal {

    }

    public static class Cat extends AbstractAnimal {

    }

    public static class Tiger extends Cat {

    }

}