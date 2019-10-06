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
import static pl.jalokim.utils.collection.CollectionUtils.collectToNewList;
import static pl.jalokim.utils.collection.CollectionUtils.collectToNewSet;

@SuppressWarnings("unchecked")
public class ReflectionUtils {

    public static void setValue(Object target, String fieldName, Object value) {
        Field field = null;
        Class<?> currentClass = target.getClass();
        List<String> noSuchFieldExceptions = new ArrayList<>();
        while(currentClass != null) {
            try {
                field = currentClass.getDeclaredField(fieldName);
            } catch(NoSuchFieldException e) {
                noSuchFieldExceptions.add(e.getMessage());
            }
            if(field != null) {
                break;
            }
            currentClass = currentClass.getSuperclass();
        }

        if(field == null) {
            throw new ReflectionException(new NoSuchFieldException(noSuchFieldExceptions.toString()));
        }

        try {
            field.setAccessible(true);
            field.set(target, value);
            field.setAccessible(false);
        } catch(ReflectiveOperationException e) {
            throw new ReflectionException(e);
        }
    }

    public static <T> T invokeMethod(Object target, String methodName,
                                     List<Class<?>> argClasses, List<Object> args) {
        return invokeMethod(target, target.getClass(), methodName, argClasses, args);
    }

    public static <T> T invokeMethod(Object target, String methodName, List<Object> args) {
        return invokeMethod(target, methodName, args.toArray());
    }

    public static <T> T invokeMethod(Object target, String methodName, Object... args) {

        List<Class<?>> argClasses = collectToNewList(Object::getClass, args);
        return invokeMethod(target, target.getClass(), methodName, argClasses, Arrays.asList(args));
    }

    public static <T> T invokeMethod(Object target, Class<?> targetClass, String methodName, List<Object> args) {
        return invokeMethod(target, targetClass, methodName, args.toArray());
    }

    public static <T> T invokeMethod(Object target, Class<?> targetClass, String methodName, Object... args) {

        List<Class<?>> argClasses = collectToNewList(Object::getClass, args);
        return invokeMethod(target, targetClass, methodName, argClasses, Arrays.asList(args));
    }

    public static <T> T invokeMethod(Object target, Class<?> targetClass, String methodName,
                                     List<Class<?>> argClasses, List<Object> args) {
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
            throw new ReflectionException(new NoSuchMethodException(noSuchMethodExceptions.toString()));
        }

        try {
            method.setAccessible(true);
            T result = (T) method.invoke(target, args.toArray(new Object[0]));
            method.setAccessible(false);
            return result;
        } catch(ReflectiveOperationException e) {
            throw new ReflectionException(e);
        }
    }

    public static <T> Set<Class<? extends T>> getAllChildClassesForAbstractClass(Class<T> abstractType, String packageDefinition) {
        /*
         * It is not working from another module when package prefix is empty...
         * So this is reason why we have here this my package.
         */
        Reflections reflections = new Reflections(packageDefinition);

        Set<Class<? extends T>> findAllClassesInClassPath = reflections.getSubTypesOf(abstractType);

        findAllClassesInClassPath = collectToNewSet(findAllClassesInClassPath,
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
            throw new ReflectionException(e);
        }
    }

    public static <T> T newInstance(Class<T> type, Object... args) {
        List<Class<?>> argumentClasses = collectToNewList(Object::getClass, args);
        return newInstance(type, argumentClasses, args);
    }

    public static <T> T newInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch(Exception e) {
            throw new ReflectionException(e);
        }
    }
}
