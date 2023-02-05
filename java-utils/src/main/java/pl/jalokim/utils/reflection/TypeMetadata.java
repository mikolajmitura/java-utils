package pl.jalokim.utils.reflection;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static pl.jalokim.utils.collection.CollectionUtils.isEmpty;
import static pl.jalokim.utils.collection.CollectionUtils.isNotEmpty;
import static pl.jalokim.utils.collection.CollectionUtils.mapToList;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.COMMA;
import static pl.jalokim.utils.constants.Constants.DOT;
import static pl.jalokim.utils.constants.Constants.EMPTY;
import static pl.jalokim.utils.constants.Constants.SPACE;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getConstructor;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getMethod;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getParametrizedRawTypes;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromClass;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromField;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromType;
import static pl.jalokim.utils.string.StringUtils.concat;
import static pl.jalokim.utils.string.StringUtils.concatElements;
import static pl.jalokim.utils.string.StringUtils.concatElementsAsLines;
import static pl.jalokim.utils.string.StringUtils.concatObjects;
import static pl.jalokim.utils.string.StringUtils.isNotBlank;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import pl.jalokim.utils.string.StringUtils;

/**
 * Class which represents real types for generic types. This can store information about real generic nested type in classes and in fields. This needs all
 * generic types resolved as real types.
 */
@Data
@SuppressWarnings("PMD.GodClass")
public final class TypeMetadata {

    private static final ThreadLocal<Map<Class<?>, String>> CURRENT_TO_STRING_HELPER = new ThreadLocal<>();
    private static final Map<String, TypeMetadata> RESOLVED_GENERIC_TYPES = new HashMap<>();

    static final TypeMetadata NATIVE_OBJECT_META = buildFromClass(Object.class);
    private static final String ARRAY_MARK = "[]";
    private final List<TypeMetadata> genericTypes;
    private final Class<?> rawType;
    private final Map<String, TypeMetadata> genericTypesByRawLabel;
    private final TypeMetadata parentTypeMetadata;
    private final List<TypeMetadata> parentInterfaces;

    public static TypeMetadata newTypeMetadata(Class<?> rawType, List<TypeMetadata> genericTypes) {
        String rawTypeAndMetadataToText = rawTypeAndMetadataToText(rawType, genericTypes);
        if (RESOLVED_GENERIC_TYPES.get(rawTypeAndMetadataToText) != null) {
            return RESOLVED_GENERIC_TYPES.get(rawTypeAndMetadataToText);
        }
        return new TypeMetadata(rawType, genericTypes);
    }

    private TypeMetadata(Class<?> rawType, List<TypeMetadata> genericTypes) {
        List<TypeMetadata> tempGenerics = isEmpty(genericTypes) ? new ArrayList<>() : genericTypes;
        this.rawType = rawType;
        RESOLVED_GENERIC_TYPES.put(rawTypeAndMetadataToText(rawType, genericTypes), this);

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
            .forEachWithIndex((index, type) -> {
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
        parentTypeMetadata = buildParentMetadata(this, genericTypesByRawLabel, rawType.getSuperclass(), rawType.getGenericSuperclass());
        parentInterfaces = buildParentInterfacesMetadata(this, genericTypesByRawLabel);
    }

    private List<TypeMetadata> buildParentInterfacesMetadata(TypeMetadata childMetadata, Map<String, TypeMetadata> childGenericTypesByRawLabel) {
        return elements(rawType.getInterfaces())
            .mapWithIndex((index, interfaceClass) -> buildParentMetadata(childMetadata, childGenericTypesByRawLabel,
                interfaceClass, rawType.getGenericInterfaces()[index]))
            .asList();
    }

    private TypeMetadata buildParentMetadata(TypeMetadata childMetadata, Map<String, TypeMetadata> childGenericTypesByRawLabel,
        Class<?> parentClass, Type genericSuperclass) {

        if (parentClass == null) {
            return null;
        }

        if (parentClass == Object.class || childMetadata.getRawType().isEnum() || childMetadata.isArrayType()) {
            return null;
        }

        if (genericSuperclass.equals(parentClass)) {
            return newTypeMetadata(parentClass, null);
        } else {
            List<TypeMetadata> types = elements(((ParameterizedType) genericSuperclass).getActualTypeArguments())
                .map(type -> mapFromType(type, childGenericTypesByRawLabel))
                .asList();
            return newTypeMetadata(parentClass, types);
        }
    }

    private TypeMetadata mapFromType(Type type, Map<String, TypeMetadata> childGenericTypesByRawLabel) {
        if (type instanceof Class) {
            return buildFromClass((Class<?>) type);
        } else {
            TypeMetadata typeMetadata = childGenericTypesByRawLabel.get(type.getTypeName());
            if (typeMetadata == null) {
                typeMetadata = buildFromType(type, rawType, this);
            }
            return typeMetadata;
        }
    }

    /**
     * If raw class is generic types then this will return true. And if raw class is array then it has generic types too.
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
     * @param typeName real name of label from generic class.
     * @return instance of TypeMetadata
     */
    public TypeMetadata getTypeMetadataByGenericLabel(Class<?> fieldOwner, String typeName) {
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
     * @param genericLabel real name of generic raw type. For example raw type is List&lt;E&gt; It Returns real metadata for E label in List.
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
     * When raw class have parent class which is not raw Object.
     *
     * @return boolean value
     */
    public boolean hasParentInterfaces() {
        return isNotEmpty(parentInterfaces);
    }

    /**
     * It returns metadata for field stored in current raw class It searches for field in whole raw class hierarchy.
     *
     * @param fieldName real field name
     * @return metadata for field
     */
    public TypeMetadata getMetaForField(String fieldName) {
        Field field = getField(rawType, fieldName);
        return getMetaForField(field);
    }

    /**
     * It returns metadata for field stored in current raw class
     *
     * @param field instance of java reflection field
     * @return metadata for field
     */
    public TypeMetadata getMetaForField(Field field) {
        if (field.getGenericType() instanceof Class) {
            return buildFromField(field);
        } else {
            return buildFromType(field.getGenericType(), field.getDeclaringClass(), this);
        }
    }

    /**
     * It returns metadata for method given as argument.
     *
     * @param method real method
     * @return metadata for method
     */
    public MethodMetadata getMetaForMethod(Method method) {
        return MethodMetadata.builder()
            .annotations(elements(method.getDeclaredAnnotations()).asList())
            .method(method)
            .name(method.getName())
            .returnType(getMetaForType(method.getGenericReturnType(), method.getDeclaringClass()))
            .parameters(createParametersMetadata(method))
            .build();
    }

    /**
     * It returns metadata for given method by and real arguments for invoke.
     *
     * @param methodName method name
     * @param argumentTypes arguments types which are specified in method
     * @return metadata for method
     */
    public MethodMetadata getMetaForMethod(String methodName, Class<?>... argumentTypes) {
        Method method = getMethod(getRawType(), methodName, argumentTypes);
        return getMetaForMethod(method);
    }

    /**
     * It returns metadata for given method by and real arguments for invoke.
     *
     * @param methodName method name
     * @param argumentTypes arguments types which are specified in method
     * @return metadata for method
     */
    public MethodMetadata getMetaForMethod(String methodName, List<Class<?>> argumentTypes) {
        Method method = getMethod(getRawType(), methodName, argumentTypes);
        return getMetaForMethod(method);
    }

    /**
     * It returns metadata for given method by and real arguments for invoke. Will try find the best Method by the best match with arguments type.
     *
     * @param methodName method name
     * @param args arguments which can by pass to invoke method
     * @return metadata for method
     */
    public MethodMetadata getMetaForMethodByArgsToInvoke(String methodName, List<Object> args) {
        List<Class<?>> argClasses = mapToList(args, Object::getClass);
        Method method = getMethod(getRawType(), methodName, argClasses);
        return getMetaForMethod(method);
    }

    /**
     * It returns metadata for given method by and real arguments for invoke. Will try find the best Method by the best match with arguments type.
     *
     * @param methodName method name
     * @param args arguments which can by pass to invoke method
     * @return metadata for method
     */
    public MethodMetadata getMetaForMethodByArgsToInvoke(String methodName, Object... args) {
        List<Class<?>> argClasses = mapToList(Object::getClass, args);
        Method method = getMethod(getRawType(), methodName, argClasses.toArray(new Class[0]));
        return getMetaForMethod(method);
    }

    /**
     * It returns metadata for given Constructor instance.
     *
     * @param constructor java reflection Constructor argument
     * @return instance of ConstructorMetadata
     */
    public ConstructorMetadata getMetaForConstructor(Constructor<?> constructor) {
        return ConstructorMetadata.builder()
            .annotations(elements(constructor.getDeclaredAnnotations()).asList())
            .constructor(constructor)
            .parameters(createParametersMetadata(constructor))
            .build();
    }

    /**
     * Get constructor metadata instance of target class with real class types.
     *
     * @param argumentTypes types of expected constructor
     * @return instance of ConstructorMetadata
     */
    public ConstructorMetadata getMetaForConstructor(Class<?>... argumentTypes) {
        Constructor<?> constructor = getConstructor(getRawType(), argumentTypes);
        return getMetaForConstructor(constructor);
    }

    /**
     * Get constructor metadata instance of target class with real class types.
     *
     * @param argumentTypes types of expected constructor
     * @return instance of ConstructorMetadata
     */
    public ConstructorMetadata getMetaForConstructor(List<? extends Class<?>> argumentTypes) {
        return getMetaForConstructor(argumentTypes.toArray(new Class<?>[0]));
    }

    /**
     * Get constructor metadata instance of target class with real arguments to invoke. Will try find the bet Constructor by the best match with arguments
     * type.
     *
     * @param args arguments which can by pass to invoke constructor
     * @return instance of ConstructorMetadata
     */
    public ConstructorMetadata getMetaForConstructorByArgsToInvoke(Object... args) {
        return getMetaForConstructorByArgsToInvoke(elements(args).asList());
    }

    /**
     * Get constructor metadata instance of target class with real arguments to invoke. Will try find the bet Constructor by the best match with arguments
     * type.
     *
     * @param args arguments which can by pass to invoke constructor
     * @return instance of ConstructorMetadata
     */
    public ConstructorMetadata getMetaForConstructorByArgsToInvoke(List<Object> args) {
        List<? extends Class<?>> argumentTypes = elements(args).map(Object::getClass).asList();
        return getMetaForConstructor(argumentTypes);
    }

    private List<ParameterMetadata> createParametersMetadata(Executable executable) {
        List<ParameterMetadata> parameterMetadata = new ArrayList<>();
        for (int parameterIndex = 0; parameterIndex < executable.getGenericParameterTypes().length; parameterIndex++) {
            Annotation[] parameterAnnotation = executable.getParameterAnnotations()[parameterIndex];
            Parameter parameter = executable.getParameters()[parameterIndex];
            parameterMetadata.add(ParameterMetadata.builder()
                .annotations(elements(parameterAnnotation).asList())
                .name(parameter.getName())
                .parameter(parameter)
                .typeOfParameter(getMetaForType(executable.getGenericParameterTypes()[parameterIndex], executable.getDeclaringClass()))
                .build());
        }
        return parameterMetadata;
    }

    /**
     * It returns metadata for generic type under provided index. For arrays type, type of array it stored under first index. Array is treated as generic list.
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

    /**
     * It search for type metadata for certain parent class. It can be all parent classes as well interfaces.
     *
     * @param type - parent class, or parent interface for which will be returned TypeMetadata
     * @return instance of TypeMetadata for provided parent/interface class.
     */
    public TypeMetadata getTypeMetaDataForParentClass(Class<?> type) {
        TypeMetadata typeMetaDataForParentClass = findTypeMetaDataForParentClass(type);
        return Optional.ofNullable(typeMetaDataForParentClass)
            .orElseThrow(() -> new NoSuchElementException("Cannot find metamodel for class: " + type.getCanonicalName() +
                " as parent in context of class: " + rawType.getCanonicalName()));
    }

    private TypeMetadata findTypeMetaDataForParentClass(Class<?> type) {
        if (getRawType().equals(type)) {
            return this;
        }

        for (TypeMetadata parentInterface : getParentInterfaces()) {
            TypeMetadata typeMetadataFromSuperInterface = parentInterface.findTypeMetaDataForParentClass(type);
            if (typeMetadataFromSuperInterface != null) {
                return typeMetadataFromSuperInterface;
            }
        }

        TypeMetadata parentTypeMetadata = getParentTypeMetadata();
        if (parentTypeMetadata != null) {
            TypeMetadata typeMetadataForClass = parentTypeMetadata.findTypeMetaDataForParentClass(type);
            if (typeMetadataForClass != null) {
                return typeMetadataForClass;
            }
        }

        return null;
    }

    @SuppressWarnings({
        "PMD.CognitiveComplexity",
        "PMD.NPathComplexity",
        "PMD.UseStringBufferForStringAppends"
    })
    public String getMetaInfo() {
        boolean firstInvokeOfGetMetaInfo = isFirstInvokeOfGetMetaInfo();
        if (firstInvokeOfGetMetaInfo) {
            CURRENT_TO_STRING_HELPER.set(new HashMap<>());
        }

        if (!hasGenericTypes()) {
            Map<Class<?>, String> classStringMap = CURRENT_TO_STRING_HELPER.get();
            if (classStringMap.containsKey(getRawType())) {
                return classStringMap.get(getRawType());
            } else {
                classStringMap.put(getRawType(), getRawType().getSimpleName());
            }
        }

        String returnText = "";
        String additionalTypeMetadata = EMPTY;

        if (isArrayType()) {
            returnText = concat(concatElements(getGenericTypes()), ARRAY_MARK);
        } else if (isSimpleType()) {
            returnText = rawType.getSimpleName();
        } else {
            if (hasGenericTypes()) {
                additionalTypeMetadata = concatElements("<", getGenericTypes(), TypeMetadata::getMetaInfo, COMMA, ">");
            }

            if (!rawClassIsComingFromJavaApi()) {
                if (hasParent()) {
                    additionalTypeMetadata = additionalTypeMetadata + " extends " + parentTypeMetadata.getMetaInfo();
                }

                if (hasParentInterfaces()) {
                    String implementedInterfaces = elements(parentInterfaces)
                        .map(TypeMetadata::getMetaInfo)
                        .asConcatText(COMMA);

                    additionalTypeMetadata = additionalTypeMetadata + " implements " + implementedInterfaces;
                }
            }
        }

        if (StringUtils.isEmpty(returnText)) {
            String classType = rawType.getSimpleName();
            returnText = isNotBlank(additionalTypeMetadata) ? format("%s%s", classType, additionalTypeMetadata) : classType;

        }
        if (firstInvokeOfGetMetaInfo) {
            CURRENT_TO_STRING_HELPER.set(new HashMap<>());
        }
        return returnText;
    }

    @Override
    public String toString() {
        return getMetaInfo();
    }

    public boolean rawClassIsComingFromJavaApi() {
        return getRawClass().getCanonicalName().startsWith("java.");
    }

    public Class<?> getRawClass() {
        return getRawType();
    }

    private static String rawTypeAndMetadataToText(Class<?> rawType, List<TypeMetadata> genericTypes) {
        String genericsInfo = elements(genericTypes)
            .mapWithIndex((index, genericType) -> format("%s=%s", index, genericType.toString()))
            .concatWithNewLines();
        return elements(rawType.getCanonicalName(), genericsInfo)
            .concatWithNewLines();
    }

    private TypeMetadata getMetaForType(Type type, Class<?> typeIsFromClass) {
        if (type instanceof Class) {
            return buildFromClass((Class<?>) type);
        } else {
            return buildFromType(type, typeIsFromClass, this);
        }
    }

    private boolean isFirstInvokeOfGetMetaInfo() {
        return elements(Thread.currentThread().getStackTrace())
            .filter(element -> element.getClassName().equals(TypeMetadata.class.getCanonicalName()) &&
                "getMetaInfo".equals(element.getMethodName()))
            .asList().size() == 1;
    }

}
