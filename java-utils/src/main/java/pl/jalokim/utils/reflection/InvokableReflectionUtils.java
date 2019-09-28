package pl.jalokim.utils.reflection;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;
import static pl.jalokim.utils.collection.CollectionUtils.filterToSet;
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
        Method method = getMethod(targetClass, methodName, argClasses);
        try {
            method.setAccessible(true);
            T result = (T) method.invoke(target, args.toArray(new Object[0]));
            method.setAccessible(false);
            return result;
        } catch(ReflectiveOperationException e) {
            throw new ReflectionOperationException(e);
        }
    }

    public static <T> Set<Class<? extends T>> getAllChildClassesForAbstractClass(Class<T> abstractType, String packageDefinition) {
        /*
         * It is not working from another module when package prefix is empty...
         * So this is reason why we have here this my package.
         */
        Reflections reflections = new Reflections(packageDefinition);

        Set<Class<? extends T>> findAllClassesInClassPath = reflections.getSubTypesOf(abstractType);

        findAllClassesInClassPath = filterToSet(findAllClassesInClassPath,
                                                classMeta -> !Modifier.isAbstract(classMeta.getModifiers()));
        return unmodifiableSet(findAllClassesInClassPath);
    }

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
