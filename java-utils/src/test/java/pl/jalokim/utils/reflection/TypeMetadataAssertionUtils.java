package pl.jalokim.utils.reflection;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getParametrizedRawTypes;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.ENUM;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.HAVING_ELEMENTS;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.MAP;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.NATIVE_ARRAY;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.NORMAL_BEAN;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.SIMPLE;

public class TypeMetadataAssertionUtils {

    public static MetadataAssertionContext assertTypeMetadata(TypeMetadata typeMetadata, Class<?> rawClass, int genericsSize, TypeMetadataKind typeMetadataKind) {
        return assertTypeMetadata(typeMetadata, rawClass, false, genericsSize, typeMetadataKind);
    }

    public static MetadataAssertionContext assertTypeMetadata(TypeMetadata typeMetadata, Class<?> rawClass, boolean hasParent, TypeMetadataKind typeMetadataKind) {
        return assertTypeMetadata(typeMetadata, rawClass, hasParent, 0, typeMetadataKind);
    }

    public static MetadataAssertionContext assertTypeMetadata(TypeMetadata typeMetadata, Class<?> rawClass, boolean hasParent) {
        return assertTypeMetadata(typeMetadata, rawClass, hasParent, 0, SIMPLE);
    }

    public static MetadataAssertionContext assertTypeMetadata(TypeMetadata typeMetadata, Class<?> rawClass, int genericsSize) {
        return assertTypeMetadata(typeMetadata, rawClass, false, genericsSize, NORMAL_BEAN);
    }

    public static MetadataAssertionContext assertTypeMetadata(TypeMetadata typeMetadata, Class<?> rawClass) {
        return assertTypeMetadata(typeMetadata, rawClass, false, 0, SIMPLE);
    }

    public static MetadataAssertionContext assertTypeMetadata(TypeMetadata typeMetadata, Class<?> rawClass, TypeMetadataKind typeMetadataKind) {
        return assertTypeMetadata(typeMetadata, rawClass, false, 0, typeMetadataKind);
    }

    public static MetadataAssertionContext assertTypeMetadata(TypeMetadata typeMetadata, Class<?> rawClass, boolean hasParent,
                                                      int genericsSize, TypeMetadataKind typeMetadataKind) {
        if (MAP.equals(typeMetadataKind)) {
            assertThat(genericsSize).isEqualTo(2);
        }

        if (NATIVE_ARRAY.equals(typeMetadataKind)) {
            assertThat(genericsSize).isEqualTo(1);
        }

        if (HAVING_ELEMENTS.equals(typeMetadataKind)) {
            assertThat(genericsSize).isEqualTo(1);
        }

        if (SIMPLE.equals(typeMetadataKind)) {
            assertThat(genericsSize).isEqualTo(0);
        }

        if (ENUM.equals(typeMetadataKind)) {
            assertThat(genericsSize).isEqualTo(0);
        }

        assertThat(typeMetadata.getRawType()).isEqualTo(rawClass);
        assertThat(typeMetadata.getCanonicalName()).isEqualTo(rawClass.getCanonicalName());
        assertThat(typeMetadata.hasParent()).isEqualTo(hasParent);
        if (genericsSize == 0) {
            assertThat(typeMetadata.hasGenericTypes()).isFalse();
            assertThat(typeMetadata.getGenericTypes()).hasSize(0);
        } else {
            assertThat(typeMetadata.hasGenericTypes()).isTrue();
            assertThat(typeMetadata.getGenericTypes()).hasSize(genericsSize);
        }
        List<Type> parametrizedRawTypes = getParametrizedRawTypes(rawClass);
        if (rawClass.isArray()) {
            assertThat(1).isEqualTo(typeMetadata.getGenericTypes().size());
        } else {
            assertThat(parametrizedRawTypes.size()).isEqualTo(typeMetadata.getGenericTypes().size());
        }

        typeMetadataKind.assertType(typeMetadata);
        return new MetadataAssertionContext(null, typeMetadata);
    }

    public enum TypeMetadataKind {

        SIMPLE(typeMetadata -> {
            assertThat(typeMetadata.isSimpleType()).isTrue();
            assertThat(typeMetadata.isEnumType()).isFalse();
            assertThat(typeMetadata.isArrayType()).isFalse();
            assertThat(typeMetadata.isHavingElementsType()).isFalse();
            assertThat(typeMetadata.isMapType()).isFalse();
        }),
        ENUM(typeMetadata -> {
            assertThat(typeMetadata.isSimpleType()).isTrue();
            assertThat(typeMetadata.isEnumType()).isTrue();
            assertThat(typeMetadata.isArrayType()).isFalse();
            assertThat(typeMetadata.isHavingElementsType()).isFalse();
            assertThat(typeMetadata.isMapType()).isFalse();
        }),
        NATIVE_ARRAY(typeMetadata -> {
            assertThat(typeMetadata.isSimpleType()).isFalse();
            assertThat(typeMetadata.isEnumType()).isFalse();
            assertThat(typeMetadata.isArrayType()).isTrue();
            assertThat(typeMetadata.isHavingElementsType()).isTrue();
            assertThat(typeMetadata.isMapType()).isFalse();
        }),
        MAP(typeMetadata -> {
            assertThat(typeMetadata.isSimpleType()).isFalse();
            assertThat(typeMetadata.isEnumType()).isFalse();
            assertThat(typeMetadata.isArrayType()).isFalse();
            assertThat(typeMetadata.isHavingElementsType()).isTrue();
            assertThat(typeMetadata.isMapType()).isTrue();
        }),
        HAVING_ELEMENTS(typeMetadata -> {
            assertThat(typeMetadata.isSimpleType()).isFalse();
            assertThat(typeMetadata.isEnumType()).isFalse();
            assertThat(typeMetadata.isArrayType()).isFalse();
            assertThat(typeMetadata.isHavingElementsType()).isTrue();
            assertThat(typeMetadata.isMapType()).isFalse();
        }),
        NORMAL_BEAN(typeMetadata -> {
            assertThat(typeMetadata.isSimpleType()).isFalse();
            assertThat(typeMetadata.isEnumType()).isFalse();
            assertThat(typeMetadata.isArrayType()).isFalse();
            assertThat(typeMetadata.isHavingElementsType()).isFalse();
            assertThat(typeMetadata.isMapType()).isFalse();
        });

        Consumer<TypeMetadata> assertWay;

        TypeMetadataKind(Consumer<TypeMetadata> assertWay) {
            this.assertWay = assertWay;
        }

        void assertType(TypeMetadata typeMetadata) {
            assertWay.accept(typeMetadata);
        }
    }
}
