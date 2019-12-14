package pl.jalokim.utils.reflection;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.COMMA;
import static pl.jalokim.utils.constants.Constants.EMPTY;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getParametrizedRawTypes;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromClass;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromField;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromType;
import static pl.jalokim.utils.string.StringUtils.concat;
import static pl.jalokim.utils.string.StringUtils.concatElements;

/**
 * Class which represents real types for generic types.
 * This can store information about real generic nested type in classes and in fields.
 */
@Data
public class TypeMetadata {

    private final List<TypeMetadata> genericTypes;
    private final Class<?> rawType;
    private final Map<String, TypeMetadata> genericTypesByRawLabel = new ConcurrentHashMap<>();
    private final TypeMetadata parentTypeMetadata;
    private final TypeMetadata childMetadata;

    TypeMetadata(Class<?> rawType, List<TypeMetadata> genericTypes) {
        this(rawType, genericTypes, null);
    }

    private TypeMetadata(Class<?> rawType, List<TypeMetadata> genericTypes, TypeMetadata childTypeMetadata) {
        this.genericTypes = genericTypes;
        this.rawType = rawType;
        this.childMetadata = childTypeMetadata;
        List<Type> parametrizedTypesForClass = getParametrizedRawTypes(rawType);

        elements(parametrizedTypesForClass)
                .forEach((index, type) ->
                                 genericTypesByRawLabel.put(type.getTypeName(), genericTypes.get(index))

                        );
        parentTypeMetadata = buildParentMetadata(this, rawType, genericTypesByRawLabel);
    }

    private TypeMetadata buildParentMetadata(TypeMetadata childMetadata,
                                             Class<?> childClass,
                                             Map<String, TypeMetadata> childGenericTypesByRawLabel) {
        Class<?> parentClass = childClass.getSuperclass();
        Type genericSuperclass = childClass.getGenericSuperclass();
        if (parentClass == null || genericSuperclass.equals(parentClass) || childMetadata.getRawType().isEnum() || childMetadata.isArrayType()) {
            return null;
        }

        List<TypeMetadata> types = elements(((ParameterizedType) genericSuperclass).getActualTypeArguments())
                .map(type -> {
                    if (type instanceof Class) {
                        return buildFromClass((Class<?>) type);
                    } else {
                        TypeMetadata typeMetadata = childGenericTypesByRawLabel.get(type.getTypeName());
                        if (typeMetadata == null) {
                            typeMetadata = buildFromType(type);
                        }
                        return typeMetadata;
                    }
                })
                .asList();
        return new TypeMetadata(parentClass, types, childMetadata);
    }

    public boolean hasGenericTypes() {
        return isNotEmpty(genericTypes);
    }

    public String getCanonicalName() {
        return rawType.getCanonicalName();
    }

    public TypeMetadata getTypeMetadataForField(Class<?> fieldOwner, String typeName) {
        TypeMetadata currentMeta = this;
        while (currentMeta != null) {
            if (currentMeta.getRawType().equals(fieldOwner)) {
                TypeMetadata typeMetadataForGenericLabel = currentMeta.getByGenericLabel(typeName);
                if (typeMetadataForGenericLabel != null) {
                    return typeMetadataForGenericLabel;
                }
            }
            currentMeta = currentMeta.getParentTypeMetadata();
        }
        return null;
    }

    public TypeMetadata getByGenericLabel(String genericLabel) {
        return genericTypesByRawLabel.get(genericLabel);
    }

    public boolean isEnumType() {
        return !isArrayType() && getRawType().isEnum();
    }

    public boolean isArrayType() {
        return getRawType().isArray();
    }

    public boolean isMapType() {
        return !isArrayType() && MetadataReflectionUtils.isMapType(getRawType());
    }

    public boolean isHavingElementsType() {
        return isArrayType() || MetadataReflectionUtils.isHavingElementsType(getRawType());
    }

    public boolean isSimpleType() {
        return !isArrayType() && MetadataReflectionUtils.isSimpleType(getRawType());
    }

    public boolean hasParent() {
        return parentTypeMetadata != null;
    }

    public boolean hasChild() {
        return childMetadata != null;
    }

    public TypeMetadata getMetaForField(String fieldName) {
        Field field = getField(rawType, fieldName);
        if (field.getGenericType() instanceof Class) {
            return buildFromField(field);
        } else {
            return buildFromType(field.getGenericType(), field, this);
        }
    }

    public TypeMetadata getGenericType(int index) {
        if (hasGenericTypes() && getGenericTypes().size() - 1 >= index) {
            return genericTypes.get(index);
        }
        throw new ReflectionOperationException("Cannot find generic type at index: " + index);
    }

    @Override
    public String toString() {
        String classType = rawType.getSimpleName();

        String genericsPart = EMPTY;
        if (hasGenericTypes() && !isArrayType()) {
            genericsPart = concatElements("<", getGenericTypes(), TypeMetadata::toString, COMMA, ">");
        }

        String parentPart = EMPTY;
        if (hasParent()) {
            parentPart = " extends " + parentTypeMetadata.toString();
        }
        return concat(classType, genericsPart, parentPart);
    }
}
