package pl.jalokim.utils.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getMethod;

@SuppressWarnings("unchecked")
public class InvokableReflectionUtils {

    /**
     * Setup new value for target object for given field name.
     * It will looks in whole hierarchy if find first match field name then will change value.
     * It not check that field and new value will have the same type.
     *
     * @param targetObject reference for object for which will be changed value
     * @param fieldName    field name
     * @param newValue     new value for set.
     */
    public static void setValueForField(Object targetObject, String fieldName, Object newValue) {
        setValueForField(targetObject, targetObject.getClass(), fieldName, newValue);
    }

    /**
     * Setup new value for target object for given field name.
     * It will looks in whole hierarchy but it will start from targetClass if find first match field name then will change value.
     * It not check that field and new value will have the same type.
     * It can't override primitive final fields and final String fields.
     * <p>
     * Some interesting links below:
     * @see <a href="https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection">Stackoverflow thread: Change private static final field using Java reflection</a>
     * @see <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.28">Constant Expressions</a>
     *
     * @param targetObject reference for object for which will be changed value
     * @param targetClass  target class from which will start looking for field with that name.
     * @param fieldName    field name
     * @param newValue     new value for set.
     */
    public static void setValueForField(Object targetObject, Class<?> targetClass,
                                        String fieldName, Object newValue) {
        Field foundField = getField(targetClass, fieldName);
        try {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            int oldModifiers = foundField.getModifiers();
            modifiersField.setAccessible(true);
            modifiersField.setInt(foundField, foundField.getModifiers() & ~Modifier.FINAL);
            foundField.setAccessible(true);

            foundField.set(targetObject, newValue);

            modifiersField.setInt(foundField, oldModifiers);
            modifiersField.setAccessible(false);
            foundField.setAccessible(false);
        } catch(Exception e) {
            throw new ReflectionOperationException(e);
        }
    }

    /**
     * It can't override primitive static final fields and static final String fields.
     * <p>
     * Some interesting links below:
     * @see <a href="https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection">Stackoverflow thread: Change private static final field using Java reflection</a>
     * @see <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.28">Constant Expressions</a>
     *
     * @param targetClass class for which will be changed static field
     * @param fieldName name of static field
     * @param newValue new value for static field.
     */
    public static void setValueForStaticField(Class<?> targetClass,
                                              String fieldName, Object newValue) {
        setValueForField(null, targetClass, fieldName, newValue);
    }

    public static <T> T invokeMethod(Object target, String methodName,
                                     List<Class<?>> argClasses, List<Object> args) {
        return invokeMethod(target, target.getClass(), methodName, argClasses, args);
    }

    public static <T> T invokeMethod(Object target, String methodName, List<Object> args) {
        return invokeMethod(target, methodName, args.toArray());
    }

    public static <T> T invokeMethod(Object target, String methodName, Object... args) {

        List<Class<?>> argClasses = mapToList(Object::getClass, args);
        return invokeMethod(target, target.getClass(), methodName, argClasses, Arrays.asList(args));
    }

    public static <T> T invokeMethod(Object target, Class<?> targetClass, String methodName, List<Object> args) {
        return invokeMethod(target, targetClass, methodName, args.toArray());
    }

    public static <T> T invokeMethod(Object target, Class<?> targetClass, String methodName, Object... args) {

        List<Class<?>> argClasses = mapToList(Object::getClass, args);
        return invokeMethod(target, targetClass, methodName, argClasses, Arrays.asList(args));
    }

    public static <T> T invokeMethod(Object target, Class<?> targetClass, String methodName,
                                     List<Class<?>> argClasses, List<Object> args) {
        Method method = getMethod(targetClass, methodName, argClasses.toArray(new Class[0]));
        try {
            method.setAccessible(true);
            T result = (T) method.invoke(target, args.toArray(new Object[0]));
            method.setAccessible(false);
            return result;
        } catch(ReflectiveOperationException e) {
            throw new ReflectionOperationException(e);
        }
    }

    // TODO get field  value

    // TODO with classes too like in methods
    public static <T> T newInstance(Class<T> type, List<Class<?>> argsClasses, List<Object> args) {
        return newInstance(type, argsClasses, args.toArray());
    }

    public static <T> T newInstance(Class<T> type, List<Class<?>> argsClasses, Object... args) {
        try {
            Constructor<T> constructor = type.getConstructor(argsClasses.toArray(new Class<?>[0]));
            return constructor.newInstance(args);
        } catch(Exception e) {
            throw new ReflectionOperationException(e);
        }
    }

    public static <T> T newInstance(Class<T> type, Object... args) {
        List<Class<?>> argumentClasses = mapToList(Object::getClass, args);
        return newInstance(type, argumentClasses, args);
    }

    public static <T> T newInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch(Exception e) {
            throw new ReflectionOperationException(e);
        }
    }
}
