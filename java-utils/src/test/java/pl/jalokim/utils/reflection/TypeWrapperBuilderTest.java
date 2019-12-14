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
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromClass;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromField;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

public class TypeWrapperBuilderTest {

    @Test
    public void gatherTypeMetadataForImplSomeGenericClassClass() {
        // given
        // when
        TypeMetadata typeMetadata = buildFromClass(ImplSomeGenericClass.class);
        // then
        assertThat(typeMetadata.isArrayType()).isFalse();
        assertThat(typeMetadata.getRawType()).isEqualTo(ImplSomeGenericClass.class);
        assertThat(typeMetadata.hasGenericTypes()).isFalse();

        List<TypeMetadata> genericTypesForParent = typeMetadata.getParentTypeMetadata().getGenericTypes();

        assertThat(genericTypesForParent.get(0).getRawType()).isEqualTo(Number.class);
        assertThat(genericTypesForParent.get(0).hasGenericTypes()).isFalse();

        TypeMetadata metadataForArrayOfList = genericTypesForParent.get(1);
        assertThat(metadataForArrayOfList.getRawType()).isEqualTo(List[].class);
        assertThat(metadataForArrayOfList.hasGenericTypes()).isTrue();
        assertThat(metadataForArrayOfList.isArrayType()).isTrue();
        assertThat(metadataForArrayOfList.hasParent()).isFalse();
        assertThat(metadataForArrayOfList.hasChild()).isFalse();

        TypeMetadata metadataForList = metadataForArrayOfList.getGenericTypes().get(0);

        assertThat(metadataForList.isArrayType()).isFalse();
        assertThat(metadataForList.getRawType()).isEqualTo(List.class);
        assertThat(metadataForList.hasGenericTypes()).isTrue();

        TypeMetadata metadataForSet = metadataForList.getGenericTypes().get(0);

        assertThat(metadataForSet.isArrayType()).isFalse();
        assertThat(metadataForSet.hasGenericTypes()).isTrue();

        TypeMetadata metadataArrayOfSomeBean = metadataForSet.getGenericTypes().get(0);

        assertThat(metadataArrayOfSomeBean.isArrayType()).isTrue();
        assertThat(metadataArrayOfSomeBean.isHavingElementsType()).isTrue();
        assertThat(metadataArrayOfSomeBean.getRawType()).isEqualTo(ExampleClass[].class);
        assertThat(metadataArrayOfSomeBean.hasGenericTypes()).isTrue();

        TypeMetadata metadataOfSomeBean = metadataArrayOfSomeBean.getGenericTypes().get(0);
        assertThat(metadataOfSomeBean.getRawType()).isEqualTo(ExampleClass.class);
        assertThat(metadataOfSomeBean.hasGenericTypes()).isFalse();
        assertThat(metadataOfSomeBean.isArrayType()).isFalse();
    }

    @Test
    public void gatherTypeMetadataForSomeGenericClass() {
        // given
        // when
        when(() -> buildFromClass(SomeGenericClass.class)
            ).thenException(UnresolvedRealClassException.class,
                            "Cannot find class for 'R' for class: "
                            + TypeWrapperBuilderTest.SomeGenericClass.class.getCanonicalName());
    }

    @Test
    public void gatherTypeMetadataForTransactionType() {
        // given
        // when
        TypeMetadata typeMetadata = buildFromClass(TransactionType.class);
        // then
        assertThat(typeMetadata.isArrayType()).isFalse();
        assertThat(typeMetadata.getRawType()).isEqualTo(TransactionType.class);
        assertThat(typeMetadata.hasGenericTypes()).isFalse();
        assertThat(typeMetadata.isEnumType()).isTrue();
        assertThat(typeMetadata.isSimpleType()).isTrue();
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

        assertThat(rootMetadataSecondImpl.getChildMetadata()).isNull();
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
        assertThat(typeMetadata.getChildMetadata()).isNull();
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

        assertThat(metadataOfField.getChildMetadata()).isNull();

        TypeMetadata metadataOfTupleClass = metadataOfField.getParentTypeMetadata();
        assertThat(metadataOfTupleClass.getRawType()).isEqualTo(ExampleClass.TupleClass.class);
        assertThat(metadataOfTupleClass.getGenericType(0).getRawType()).isEqualTo(String.class);
        assertThat(metadataOfTupleClass.getGenericType(1).getRawType()).isEqualTo(Map.class);
        assertThat(mapMetadata == metadataOfTupleClass.getGenericType(1)).isTrue();

        assertThat(metadataOfTupleClass.getChildMetadata() == metadataOfField).isTrue();

        TypeMetadata metaOfRawTuple = metadataOfTupleClass.getParentTypeMetadata();
        assertThat(metaOfRawTuple.getRawType()).isEqualTo(ExampleClass.RawTuple.class);

        assertThat(metaOfRawTuple.getParentTypeMetadata()).isNull();
        assertThat(metaOfRawTuple.getGenericType(0).getRawType()).isEqualTo(Map.class);
        assertThat(metaOfRawTuple.getGenericType(0) == mapMetadata).isTrue();
        assertThat(metaOfRawTuple.getChildMetadata() == metadataOfTupleClass).isTrue();
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
        assertThat(metaOd3DimArray.hasChild()).isFalse();

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
        Field threeDimConcreteArray = getField(ExampleClass.class, "stringTupleNexObject");
        // when
        TypeMetadata rootFieldMeta = buildFromField(threeDimConcreteArray);
        // then
        assertThat(rootFieldMeta.getRawType()).isEqualTo(ExampleClass.StringTuple.class);

        TypeMetadata nextObjectMeta = rootFieldMeta.getGenericType(0);
        assertThat(nextObjectMeta.getRawType()).isEqualTo(ExampleClass.NextObject.class);
        assertThat(nextObjectMeta.isArrayType()).isFalse();
        assertThat(nextObjectMeta.hasGenericTypes()).isFalse();

        TypeMetadata mapMetadata = rootFieldMeta.getGenericType(1);
        assertThat(mapMetadata.getRawType()).isEqualTo(Map.class);
        assertThat(mapMetadata.isArrayType()).isFalse();
        assertThat(mapMetadata.isMapType()).isTrue();
        assertThat(mapMetadata.hasGenericTypes()).isTrue();

        TypeMetadata numberMetadata = mapMetadata.getGenericType(0);
        assertThat(numberMetadata.getRawType()).isEqualTo(Number.class);
        assertThat(numberMetadata.isSimpleType()).isTrue();
        assertThat(numberMetadata.isHavingElementsType()).isFalse();
        assertThat(numberMetadata.hasGenericTypes()).isFalse();

        TypeMetadata listMetadata = mapMetadata.getGenericType(1);
        assertThat(listMetadata.getRawType()).isEqualTo(List.class);
        assertThat(listMetadata.isSimpleType()).isFalse();
        assertThat(listMetadata.isHavingElementsType()).isTrue();
        assertThat(listMetadata.hasGenericTypes()).isTrue();

        TypeMetadata stringMetadata = listMetadata.getGenericType(0);
        assertThat(stringMetadata.getRawType()).isEqualTo(String.class);
        assertThat(stringMetadata.hasGenericTypes()).isFalse();
        assertThat(stringMetadata.isSimpleType()).isTrue();

        TypeMetadata parentTypeMetadata = rootFieldMeta.getParentTypeMetadata();
        assertThat(parentTypeMetadata.getRawType()).isEqualTo(ExampleClass.TupleClass.class);
        assertThat(parentTypeMetadata.getGenericType(0).getRawType()).isEqualTo(String.class);
        assertThat(parentTypeMetadata.getGenericType(1)).isEqualTo(mapMetadata);
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