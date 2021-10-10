package pl.jalokim.utils.reflection;

import lombok.Data;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.NORMAL_BEAN;
import static pl.jalokim.utils.reflection.TypeMetadataAssertionUtils.TypeMetadataKind.SIMPLE;

@Data
public class MetadataAssertionContext {

    private final MetadataAssertionContext childContext;
    private final TypeMetadata typeMetadata;

    public MetadataAssertionContext getGenericType(int index) {
        assertThat(typeMetadata.hasGenericTypes()).isTrue();
        return new MetadataAssertionContext(this, typeMetadata.getGenericType(index));
    }

    public MetadataAssertionContext getParent() {
        assertThat(typeMetadata.hasParent()).isTrue();
        return new MetadataAssertionContext(this, typeMetadata.getParentTypeMetadata());
    }

    public MetadataAssertionContext isEqualTo(MetadataAssertionContext anotherContext) {
        assertThat(typeMetadata).isEqualTo(anotherContext.getTypeMetadata());
        return this;
    }

    public MetadataAssertionContext getChildAssertContext() {
        assertThat(childContext).isNotNull();
        return childContext;
    }

    public MetadataAssertionContext assertTypeMetadata(Class<?> rawClass, int genericsSize, TypeMetadataAssertionUtils.TypeMetadataKind typeMetadataKind) {
        return assertTypeMetadata(rawClass, false, genericsSize, typeMetadataKind);
    }

    public MetadataAssertionContext assertTypeMetadata(Class<?> rawClass, boolean hasParent, TypeMetadataAssertionUtils.TypeMetadataKind typeMetadataKind) {
        return assertTypeMetadata(rawClass, hasParent, 0, typeMetadataKind);
    }

    public MetadataAssertionContext assertTypeMetadata(Class<?> rawClass, TypeMetadataAssertionUtils.TypeMetadataKind typeMetadataKind) {
        return assertTypeMetadata(rawClass, false, 0, typeMetadataKind);
    }

    public MetadataAssertionContext assertTypeMetadata(Class<?> rawClass, boolean hasParent,
                                               int genericsSize, TypeMetadataAssertionUtils.TypeMetadataKind typeMetadataKind) {
        return TypeMetadataAssertionUtils.assertTypeMetadata(typeMetadata, rawClass, hasParent, genericsSize, typeMetadataKind);
    }

    public MetadataAssertionContext assertGenericType(int index, Class<?> rawClass, boolean hasParent,
                                              int genericsSize, TypeMetadataAssertionUtils.TypeMetadataKind typeMetadataKind) {
        TypeMetadataAssertionUtils.assertTypeMetadata(typeMetadata.getGenericType(index), rawClass, hasParent, genericsSize, typeMetadataKind);
        return this;
    }

    public MetadataAssertionContext assertGenericType(int index, Class<?> rawClass) {
        TypeMetadataAssertionUtils.assertTypeMetadata(typeMetadata.getGenericType(index), rawClass, false, 0, SIMPLE);
        return this;
    }

    public MetadataAssertionContext assertGenericType(int index, Class<?> rawClass, TypeMetadataAssertionUtils.TypeMetadataKind typeMetadataKind) {
        TypeMetadataAssertionUtils.assertTypeMetadata(typeMetadata.getGenericType(index), rawClass, false, 0, typeMetadataKind);
        return this;
    }

    public MetadataAssertionContext assertGenericType(int index, Class<?> rawClass, int genericsSize, TypeMetadataAssertionUtils.TypeMetadataKind typeMetadataKind) {
        TypeMetadataAssertionUtils.assertTypeMetadata(typeMetadata.getGenericType(index), rawClass, false, genericsSize, typeMetadataKind);
        return this;
    }

    public MetadataAssertionContext assertGenericTypesAsRawObject() {
        assertThat(typeMetadata.hasGenericTypes()).isTrue();
        elements(typeMetadata.getGenericTypes())
                .forEachWithIndex((index, typeMetadata1) -> assertGenericType(index, Object.class, false, 0, NORMAL_BEAN));
        return this;
    }
}
