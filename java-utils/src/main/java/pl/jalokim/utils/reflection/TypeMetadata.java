package pl.jalokim.utils.reflection;

import lombok.Data;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static pl.jalokim.utils.collection.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.COMMA;
import static pl.jalokim.utils.constants.Constants.DOT;
import static pl.jalokim.utils.constants.Constants.EMPTY;
import static pl.jalokim.utils.constants.Constants.SPACE;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getParametrizedRawTypes;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromClass;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromField;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromType;
import static pl.jalokim.utils.string.StringUtils.concat;
import static pl.jalokim.utils.string.StringUtils.concatElements;
import static pl.jalokim.utils.string.StringUtils.concatElementsAsLines;
import static pl.jalokim.utils.string.StringUtils.concatObjects;

/**
 * Class which represents real types for generic types.
 * This can store information about real generic nested type in classes and in fields.
 * This needs all generic types resolved as real types.
 */
@Data
public class TypeMetadata {

    static final TypeMetadata NATIVE_OBJECT_META = buildFromClass(Object.class);
    private static final String ARRAY_MARK = "[]";
    private final List<TypeMetadata> genericTypes;
    private final Class<?> rawType;
    private final Map<String, TypeMetadata> genericTypesByRawLabel;
    private final TypeMetadata parentTypeMetadata;

    TypeMetadata(Class<?> rawType, List<TypeMetadata> genericTypes) {
        List<TypeMetadata> tempGenerics = isEmpty(genericTypes) ? new ArrayList<>() : genericTypes;
        this.rawType = rawType;
        List<Type> parametrizedTypesForClass = getParametrizedRawTypes(rawType);
        if (isEmpty(parametrizedTypesForClass) && isNotEmpty(tempGenerics) && !rawType.isArray()) {
            AtomicInteger index = new AtomicInteger();
            throw new ReflectionOperationException(format("raw class: %s doesn't have any parametrized types, but tried put generic types:%n%s",
                                                          rawType.getCanonicalName(),
                                                          concatElementsAsLines(tempGenerics, text -> concatObjects(index.getAndIncrement(), DOT, SPACE, text))
                                                         ));
        }

        Map<String, TypeMetadata> tempMap = new ConcurrentHashMap<>();
        elements(parametrizedTypesForClass)
                .forEach((index, type) -> {
                             if (tempGenerics.size() == index) {
                                 buildFromClass(Object.class);
                                 tempGenerics.add(index, NATIVE_OBJECT_META);
                                 tempMap.put(type.getTypeName(), NATIVE_OBJECT_META);
                             } else {
                                 tempMap.put(type.getTypeName(), tempGenerics.get(index));
                             }
                         }
                        );
        this.genericTypes = unmodifiableList(tempGenerics);
        genericTypesByRawLabel = unmodifiableMap(tempMap);
        parentTypeMetadata = buildParentMetadata(this, rawType, genericTypesByRawLabel);
    }

    private TypeMetadata buildParentMetadata(TypeMetadata childMetadata,
                                             Class<?> childClass,
                                             Map<String, TypeMetadata> childGenericTypesByRawLabel) {
        Class<?> parentClass = childClass.getSuperclass();
        if (parentClass == null) {
            return null;
        }

        if (parentClass == Object.class || childMetadata.getRawType().isEnum() || childMetadata.isArrayType()) {
            return null;
        }

        Type genericSuperclass = childClass.getGenericSuperclass();
        if (genericSuperclass.equals(parentClass)) {
            return new TypeMetadata(parentClass, null);
        } else {
            List<TypeMetadata> types = elements(((ParameterizedType) genericSuperclass).getActualTypeArguments())
                    .map(type -> mapFromType(type, childGenericTypesByRawLabel))
                    .asList();
            return new TypeMetadata(parentClass, types);
        }
    }

    private TypeMetadata mapFromType(Type type, Map<String, TypeMetadata> childGenericTypesByRawLabel) {
        if (type instanceof Class) {
            return buildFromClass((Class<?>) type);
        } else {
            TypeMetadata typeMetadata = childGenericTypesByRawLabel.get(type.getTypeName());
            if (typeMetadata == null) {
                typeMetadata = buildFromType(type);
            }
            return typeMetadata;
        }
    }

    /**
     * If raw class is generic types then this will return true.
     * And if raw class is array then it has generic types too.
     *
     * @return boolean
     */
    public boolean hasGenericTypes() {
        return isNotEmpty(genericTypes);
    }

    /**
     * It returns canonical class name for current raw type.
     *
     * @return canonical class name
     */
    public String getCanonicalName() {
        return rawType.getCanonicalName();
    }

    /**
     * It returns resolved metadata for some raw generic type when exists one.
     *
     * @param fieldOwner real owner of raw generic field.
     * @param typeName   real name of label from generic class.
     * @return instance of TypeMetadata
     */
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
        throw new ReflectionOperationException(
                format("Cannot find raw type: '%s' for class: '%s' in current context: %s ",
                       typeName, fieldOwner.getCanonicalName(), toString())
        );
    }

    /**
     * It returns metadata for raw generic label for current class, it not search in parents classes.
     *
     * @param genericLabel real name of generic raw type. For example raw type is List&lt;E&gt;
     *                     It Returns real metadata for E label in List.
     * @return instance of TypeMetadata
     */
    public TypeMetadata getByGenericLabel(String genericLabel) {
        return genericTypesByRawLabel.get(genericLabel);
    }

    public boolean isEnumType() {
        return getRawType().isEnum();
    }

    public boolean isArrayType() {
        return getRawType().isArray();
    }

    public boolean isMapType() {
        return MetadataReflectionUtils.isMapType(getRawType());
    }

    /**
     * When is some bag, it means when is array type or some collection.
     *
     * @return true when is array type or some collection.
     */
    public boolean isHavingElementsType() {
        return isArrayType() || MetadataReflectionUtils.isHavingElementsType(getRawType());
    }

    /**
     * When is simple primitive type.
     *
     * @return true when is simple primitive type.
     */
    public boolean isSimpleType() {
        return MetadataReflectionUtils.isSimpleType(getRawType());
    }

    /**
     * When raw class have parent class which is not raw Object.
     *
     * @return boolean value
     */
    public boolean hasParent() {
        return parentTypeMetadata != null;
    }

    /**
     * It returns metadata for field stored in current raw class
     * It searches for field in whole raw class hierarchy.
     *
     * @param fieldName real field name
     * @return metadata for field
     */
    public TypeMetadata getMetaForField(String fieldName) {
        Field field = getField(rawType, fieldName);
        if (field.getGenericType() instanceof Class) {
            return buildFromField(field);
        } else {
            return buildFromType(field.getGenericType(), field, this);
        }
    }

    /**
     * It returns metadata for generic type under provided index.
     * For arrays type, type of array it stored under first index.
     * Array is treated as generic list.
     *
     * @param index of generic type for current raw type.
     * @return metadata of generic type under provided index.
     */
    public TypeMetadata getGenericType(int index) {
        if (hasGenericTypes() && getGenericTypes().size() - 1 >= index) {
            return genericTypes.get(index);
        }
        throw new ReflectionOperationException("Cannot find generic type at index: " + index);
    }

    @Override
    public String toString() {
        if (isArrayType()) {
            return concat(concatElements(getGenericTypes()), ARRAY_MARK);
        }

        String genericsPart = EMPTY;
        if (hasGenericTypes()) {
            genericsPart = concatElements("<", getGenericTypes(), TypeMetadata::toString, COMMA, ">");
        }

        String classType = rawType.getSimpleName();
        if (hasParent()) {
            return concat("{", classType, " extends ", parentTypeMetadata.toString(), genericsPart, "}");
        }

        return concat(classType, genericsPart);
    }
}
