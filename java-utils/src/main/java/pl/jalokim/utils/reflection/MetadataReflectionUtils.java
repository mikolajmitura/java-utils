package pl.jalokim.utils.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static pl.jalokim.utils.collection.CollectionUtils.mapToList;

public class MetadataReflectionUtils {

    private static final List<Class<?>> SIMPLE_CLASSES = createSimpleClassesList();
    private static final List<Class<?>> SIMPLE_NUMBERS = createPrimitiveClassesList();

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
        return Collections.unmodifiableList(classes);
    }

    private static List<Class<?>> createPrimitiveClassesList() {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(int.class);
        classes.add(short.class);
        classes.add(byte.class);
        classes.add(long.class);
        classes.add(float.class);
        return Collections.unmodifiableList(classes);
    }

    public static Field getField(Object targetObject, String fieldName) {
        return getField(targetObject.getClass(), fieldName);
    }

    public static Field getField(Class<?> targetClass, String fieldName) {
        Field foundField = null;
        Class<?> currentClass = targetClass;
        List<Class<?>> searchedClasses = new ArrayList<>();
        while(currentClass != null) {
            try {
                foundField = currentClass.getDeclaredField(fieldName);
            } catch(NoSuchFieldException e) {
                searchedClasses.add(currentClass);
            }
            if(foundField != null) {
                break;
            }
            currentClass = currentClass.getSuperclass();
        }

        if(foundField == null) {
            throw new ReflectionOperationException("field '" + fieldName + "' not exist in classes: " + mapToList(searchedClasses, Class::getCanonicalName));
        }
        return foundField;
    }

    public static Class<?> getTypeOfArrayField(Field field) {
        if (!isArrayType(field.getType())) {
            throw new ReflectionOperationException(format("field: '%s' is not array type, is type: %s", field, field.getType()));
        }
        return field.getType().getComponentType();
    }

    public static Method getMethod(Object targetObject, String methodName,
                                   List<Class<?>> argClasses) {
        return getMethod(targetObject.getClass(), methodName, argClasses);
    }

    public static Method getMethod(Class<?> targetClass, String methodName,
                                   List<Class<?>> argClasses) {
        Method method = null;
        Class<?> currentClass = targetClass;
        List<String> noSuchMethodExceptions = new ArrayList<>();
        while(currentClass != null) {
            try {
                method = currentClass.getDeclaredMethod(methodName, argClasses.toArray(new Class[0]));
            } catch(NoSuchMethodException e) {
                noSuchMethodExceptions.add(e.getMessage());
            }
            if(method != null) {
                break;
            }
            currentClass = currentClass.getSuperclass();
        }

        if(method == null) {
            throw new ReflectionOperationException(new NoSuchMethodException(noSuchMethodExceptions.toString()));
        }
        return method;
    }

    public static boolean isSimpleType(Class<?> fieldType) {
        return SIMPLE_CLASSES.stream().anyMatch(type -> fieldType.isPrimitive() || type.isAssignableFrom(fieldType));
    }

    public static boolean isNumberType(Class<?> keyType) {
        return Number.class.isAssignableFrom(keyType) || SIMPLE_NUMBERS.contains(keyType);
    }

    public static boolean isTextType(Class<?> keyType) {
        return String.class.isAssignableFrom(keyType);
    }

    public static boolean isEnumType(Class<?> keyType) {
        return keyType.isEnum();
    }

    public static boolean isMapType(Class<?> type) {
        return Map.class.isAssignableFrom(type);
    }

    public static boolean isCollectionType(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    public static boolean isHavingElementsType(Class<?> type) {
        return isCollectionType(type) || isArrayType(type);
    }

    public static boolean isArrayType(Class<?> type) {
        return type.isArray();
    }

    public static Class<?> getParametrizedType(Field field, int index) {
        return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[index];
    }

    public static Class<?> getParametrizedType(Object targetObject, int index) {
        return getParametrizedType(targetObject.getClass(), index);
    }

    public static Class<?> getParametrizedType(Class<?> originalClass, int index) {
        Class<?> currentClass = originalClass;
        Exception lastException = null;
        while(currentClass != null) {
            try {
                return (Class<?>) ((ParameterizedType) currentClass
                        .getGenericSuperclass()).getActualTypeArguments()[0];
            } catch(Exception ex) {
                lastException = ex;
                currentClass = currentClass.getSuperclass();
            }
        }
        throw new ReflectionOperationException("Cannot find parametrized type for class: " + originalClass, lastException);
    }

}
