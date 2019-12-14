package pl.jalokim.utils.reflection;

import org.apache.commons.lang3.ClassUtils;
import org.reflections.Reflections;
import pl.jalokim.utils.string.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static pl.jalokim.utils.collection.CollectionUtils.filterToSet;
import static pl.jalokim.utils.collection.CollectionUtils.mapToList;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.reflection.ClassNameFixer.fixClassName;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildForArrayField;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromClass;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromField;
import static pl.jalokim.utils.reflection.TypeWrapperBuilder.buildFromType;

/**
 * Gather some metadata about classes.
 */
@SuppressWarnings("PMD.GodClass")
public final class MetadataReflectionUtils {

    private static final List<Class<?>> SIMPLE_CLASSES = createSimpleClassesList();
    private static final List<Class<?>> SIMPLE_NUMBERS = createPrimitiveClassesList();
    private static final String FIELD_IS_NOT_ARRAY_MSG = "field: '%s' is not array type, is type: %s";

    private MetadataReflectionUtils() {

    }

    private static List<Class<?>> createSimpleClassesList() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(String.class);
        classes.add(Character.class);
        classes.add(Number.class);
        classes.add(Boolean.class);
        classes.add(Enum.class);
        classes.add(LocalDate.class);
        classes.add(LocalDateTime.class);
        classes.add(LocalTime.class);
        classes.add(ZonedDateTime.class);
        return unmodifiableList(classes);
    }

    private static List<Class<?>> createPrimitiveClassesList() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(int.class);
        classes.add(short.class);
        classes.add(byte.class);
        classes.add(long.class);
        classes.add(float.class);
        return unmodifiableList(classes);
    }

    /**
     * Returns lang.reflect.Field for given target object and fieldName.
     * It searches for first mach field in class hierarchy.
     *
     * @param targetObject object from which we will start searching.
     * @param fieldName    name of object property.
     * @return instance of Field.
     */
    public static Field getField(Object targetObject, String fieldName) {
        return getField(targetObject.getClass(), fieldName);
    }

    /**
     * Returns lang.reflect.Field for given target class and fieldName.
     * It searches for first mach field in class hierarchy.
     *
     * @param targetClass class from which will start searching.
     * @param fieldName   name of object property.
     * @return instance of Field.
     */
    public static Field getField(Class<?> targetClass, String fieldName) {
        Field foundField = null;
        Class<?> currentClass = targetClass;
        List<Class<?>> searchedClasses = new ArrayList<>();
        while (currentClass != null) {
            try {
                foundField = currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                searchedClasses.add(currentClass);
            }
            if (foundField != null) {
                break;
            }
            currentClass = currentClass.getSuperclass();
        }

        if (foundField == null) {
            throw new ReflectionOperationException("field '" + fieldName + "' not exist in classes: " + mapToList(searchedClasses, Class::getCanonicalName));
        }
        return foundField;
    }

    /**
     * Return type of field which is expected to be array field.
     *
     * @param field instance of field.
     * @return type which stores a array field.
     */
    public static Class<?> getTypeOfArrayField(Field field) {
        if (!isArrayType(field.getType())) {
            throw new ReflectionOperationException(format(FIELD_IS_NOT_ARRAY_MSG, field, field.getType()));
        }
        return field.getType().getComponentType();
    }


    /**
     * It returns method for given target object.
     *
     * @param targetObject instance for which will start looking for method in object hierarchy.
     * @param methodName   method name.
     * @param argClasses   types of arguments.
     * @return instance of method.
     */
    public static Method getMethod(Object targetObject, String methodName,
                                   Class<?>... argClasses) {
        return getMethod(targetObject.getClass(), methodName, argClasses);
    }

    /**
     * It returns method for given target object.
     *
     * @param targetClass target class for which will start looking for method in object hierarchy.
     * @param methodName  method name.
     * @param argClasses  types of arguments.
     * @return instance of method.
     */
    public static Method getMethod(Class<?> targetClass, String methodName,
                                   Class<?>... argClasses) {
        Method method = null;
        Class<?> currentClass = targetClass;
        List<String> noSuchMethodExceptions = new ArrayList<>();
        while (currentClass != null) {
            try {
                method = currentClass.getDeclaredMethod(methodName, argClasses);
            } catch (NoSuchMethodException e) {
                noSuchMethodExceptions.add(e.getMessage());
            }
            if (method != null) {
                break;
            }
            currentClass = currentClass.getSuperclass();
        }

        if (method == null) {
            throw new ReflectionOperationException(new NoSuchMethodException(StringUtils.concatElementsAsLines(noSuchMethodExceptions)));
        }
        return method;
    }

    /**
     * It will search for all classes which inherit from provided super type.
     * It will not add to result class for superType argument. It can add abstract classes to result or not.
     *
     * @param superType           super type which will not added to result set.
     * @param packageDefinition   package from which will search for child classes.
     * @param withAbstractClasses by this flag depends that result set will have abstract classes in result set.
     * @param <T>                 generic type of super type of class.
     * @return result set with child classes for super class, without super class instance.
     */
    public static <T> Set<Class<? extends T>> getAllChildClassesForClass(Class<T> superType, String packageDefinition, boolean withAbstractClasses) {
        /*
         * It is not working from another module when package prefix is empty...
         * So this is reason why we have here this my package.
         */
        Reflections reflections = new Reflections(packageDefinition);

        Set<Class<? extends T>> findAllClassesInClassPath = reflections.getSubTypesOf(superType);
        if (!withAbstractClasses) {
            findAllClassesInClassPath = filterToSet(findAllClassesInClassPath,
                                                    classMeta -> !Modifier.isAbstract(classMeta.getModifiers()));
        }
        return unmodifiableSet(findAllClassesInClassPath);
    }

    /**
     * It checks that given field is simple type.
     * If is primitive or class of field inherits from some classes from {@link MetadataReflectionUtils#SIMPLE_CLASSES}
     *
     * @param field field which will be verified.
     * @return boolean value
     */
    public static boolean isSimpleType(Field field) {
        return isSimpleType(field.getType());
    }

    /**
     * It checks that given class is simple type.
     * If is primitive or class inherits from some classes from {@link MetadataReflectionUtils#SIMPLE_CLASSES}
     *
     * @param someClass class which will be verified.
     * @return boolean value
     */
    public static boolean isSimpleType(Class<?> someClass) {
        return someClass.isPrimitive() || SIMPLE_CLASSES.stream().anyMatch(type -> type.isAssignableFrom(someClass));
    }

    /**
     * It checks that given field is number type.
     * If is primitive or class of field inherits from some classes from {@link MetadataReflectionUtils#SIMPLE_NUMBERS}
     *
     * @param field field which will be verified.
     * @return boolean value
     */
    public static boolean isNumberType(Field field) {
        return isNumberType(field.getType());
    }

    /**
     * It checks that given class is number type.
     * If is primitive or class inherit from some class from {@link MetadataReflectionUtils#SIMPLE_NUMBERS}
     *
     * @param someClass class which will be verified.
     * @return boolean value
     */
    public static boolean isNumberType(Class<?> someClass) {
        return Number.class.isAssignableFrom(someClass) || SIMPLE_NUMBERS.contains(someClass);
    }

    /**
     * It checks that given field is simple string type.
     *
     * @param field field which will be verified.
     * @return boolean value
     */
    public static boolean isTextType(Field field) {
        return isTextType(field.getType());
    }

    /**
     * It checks that given class is simple string type.
     *
     * @param someClass class which will be verified.
     * @return boolean value
     */
    public static boolean isTextType(Class<?> someClass) {
        return String.class.isAssignableFrom(someClass);
    }

    /**
     * It checks that given field is simple enum type.
     *
     * @param field field which will be verified.
     * @return boolean value
     */
    public static boolean isEnumType(Field field) {
        return isEnumType(field.getType());
    }

    /**
     * It checks that given class is simple enum type.
     *
     * @param someClass class which will be verified.
     * @return boolean value
     */
    public static boolean isEnumType(Class<?> someClass) {
        return someClass.isEnum();
    }

    /**
     * It checks that given field is map type.
     *
     * @param field field which will be verified.
     * @return boolean value
     */
    public static boolean isMapType(Field field) {
        return isMapType(field.getType());
    }

    /**
     * It checks that given class is map type.
     *
     * @param someClass class which will be verified.
     * @return boolean value
     */
    public static boolean isMapType(Class<?> someClass) {
        return Map.class.isAssignableFrom(someClass);
    }

    /**
     * It checks that given field is collection type.
     *
     * @param field field which will be verified.
     * @return boolean value
     */
    public static boolean isCollectionType(Field field) {
        return isCollectionType(field.getType());
    }

    /**
     * It checks that given class is collection type.
     *
     * @param someClass class which will be verified.
     * @return boolean value
     */
    public static boolean isCollectionType(Class<?> someClass) {
        return Collection.class.isAssignableFrom(someClass);
    }

    /**
     * It checks that given field is array or collection type.
     *
     * @param field field which will be verified.
     * @return boolean value
     */
    public static boolean isHavingElementsType(Field field) {
        return isHavingElementsType(field.getType());
    }

    /**
     * It checks that given class is array or collection type.
     *
     * @param someClass class which will be verified.
     * @return boolean value
     */
    public static boolean isHavingElementsType(Class<?> someClass) {
        return isCollectionType(someClass) || isArrayType(someClass);
    }

    /**
     * It checks that given field is array type.
     *
     * @param field field which will be verified.
     * @return boolean value
     */
    public static boolean isArrayType(Field field) {
        return isArrayType(field.getType());
    }

    /**
     * It checks that given class is array type.
     *
     * @param someClass class which will be verified.
     * @return boolean value
     */
    public static boolean isArrayType(Class<?> someClass) {
        return someClass.isArray();
    }

    /**
     * It returns a type of generic field by given index.
     *
     * @param field field for which will be returned class for generic type.
     * @param index of wanted generic type
     * @return type of generic type from field from specified index.
     */
    public static Class<?> getParametrizedType(Field field, int index) {
        try {
            return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[index];
        } catch (Exception ex) {
            throw new ReflectionOperationException(format("Cannot find parametrized type for field with class: '%s', at: %s index", field.getType(), index), ex);
        }
    }

    /**
     * It returns a type of generic object by given index.
     *
     * @param targetObject instance object for which will be returned class for generic type.
     * @param index        of wanted generic type
     * @return type of generic type from field from specified index.
     */
    public static Class<?> getParametrizedType(Object targetObject, int index) {
        return getParametrizedType(targetObject.getClass(), index);
    }

    /**
     * It returns a type of generic class by given index.
     *
     * @param originalClass class for which will be returned class for generic type.
     * @param index         of wanted generic type
     * @return type of generic type from field from specified index.
     */
    public static Class<?> getParametrizedType(Class<?> originalClass, int index) {
        Class<?> currentClass = originalClass;
        Exception exception = null;
        while (currentClass != null) {
            try {
                return (Class<?>) ((ParameterizedType) currentClass
                        .getGenericSuperclass()).getActualTypeArguments()[index];
            } catch (Exception ex) {
                currentClass = currentClass.getSuperclass();
                exception = ex;
            }
        }
        throw new ReflectionOperationException(format("Cannot find parametrized type for class: '%s', at: %s index", originalClass, index), exception);
    }

    public static Class<?> getClassForName(String className) {
        try {
            return ClassUtils.getClass(fixClassName(className));
        } catch (ClassNotFoundException e) {
            throw new ReflectionOperationException(e);
        }
    }

    public static List<Type> getParametrizedRawTypes(Class<?> someClass) {
        TypeVariable<? extends Class<?>>[] typeParameters = someClass.getTypeParameters();
        if (!isEnumType(someClass) && typeParameters.length > 0) {
            List<Type> types = new ArrayList<>(elements(typeParameters)
                                                       .asList());
            return unmodifiableList(types);
        }
        return emptyList();
    }

    public static TypeMetadata getTypeMetadataFromClass(Class<?> someClass) {
        return buildFromClass(someClass);
    }

    public static TypeMetadata getTypeMetadataFromField(Field field) {
        try {
            return buildFromField(field);
        } catch (UnresolvedRealClassException ex) {
            throw new UnresolvedRealClassException(
                    format("Cannot resolve some type for field: %s for class: %s",
                                  field.getName(), field.getDeclaringClass()), ex);
        }
    }

    public static TypeMetadata getTypeMetadataFromField(Field field, int index) {
        TypeMetadata parametrizedField = getTypeMetadataFromField(field);
        return parametrizedField.getGenericType(index);
    }

    public static TypeMetadata getTypeMetadataFromType(Type type) {
        if (type instanceof Class) {
            return buildFromClass((Class<?>) type);
        } else {
            return buildFromType(type);
        }
    }

    public static TypeMetadata getTypeMetadataOfArray(Field field) {
        if (!isArrayType(field.getType())) {
            throw new ReflectionOperationException(format(FIELD_IS_NOT_ARRAY_MSG, field, field.getType()));
        }
        TypeMetadata typeMetadata = buildForArrayField(field);
        return typeMetadata.getGenericType(0);
    }
}
