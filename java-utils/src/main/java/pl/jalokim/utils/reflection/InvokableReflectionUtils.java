package pl.jalokim.utils.reflection;

import static pl.jalokim.utils.collection.CollectionUtils.mapToList;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getMethod;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Set of utils methods for set value, get value for field, invoke methods etc by reflection.
 */
@SuppressWarnings({"SuppressWarnings", "unchecked"})
public final class InvokableReflectionUtils {

    private InvokableReflectionUtils() {

    }

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
     * * @see <a href="https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection">Stackoverflow thread: Change private static final field using Java reflection</a>
     * * @see <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.28">Constant Expressions</a>
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
            Field modifiersField = null;
            int oldModifiers = -1;
            if (Modifier.isFinal(foundField.getModifiers())) {
                modifiersField = Field.class.getDeclaredField("modifiers");
                oldModifiers = foundField.getModifiers();
                modifiersField.setAccessible(true);
                modifiersField.setInt(foundField, foundField.getModifiers() & ~Modifier.FINAL);
            }
            foundField.setAccessible(true);
            foundField.set(targetObject, newValue);
            if (modifiersField != null) {
                modifiersField.setInt(foundField, oldModifiers);
                modifiersField.setAccessible(false);
            }
            foundField.setAccessible(false);
        } catch (Exception e) {
            throw new ReflectionOperationException(e);
        }
    }

    /**
     * It can't override primitive static final fields and static final String fields.
     * <p>
     * Some interesting links below:
     * * @see <a href="https://stackoverflow.com/questions/3301635/change-private-static-final-field-using-java-reflection">Stackoverflow thread: Change private static final field using Java reflection</a>
     * * @see <a href="https://docs.oracle.com/javase/specs/jls/se7/html/jls-15.html#jls-15.28">Constant Expressions</a>
     *
     * @param targetClass class for which will be changed static field
     * @param fieldName   name of static field
     * @param newValue    new value for static field.
     */
    public static void setValueForStaticField(Class<?> targetClass,
                                              String fieldName, Object newValue) {
        setValueForField(null, targetClass, fieldName, newValue);
    }

    /**
     * It gets value for given field name from target object.
     * It looks in whole class hierarchy for target object.
     *
     * @param targetObject target object
     * @param fieldName    field name
     * @param <T>          expected return type
     * @return value from field.
     */
    public static <T> T getValueOfField(Object targetObject, String fieldName) {
        return getValueOfField(targetObject, targetObject.getClass(), fieldName);
    }

    /**
     * It gets value for given field name from target object.
     * It looks in whole class hierarchy but starts from target class.
     *
     * @param targetObject target object
     * @param targetClass  target class
     * @param fieldName    field name
     * @param <T>          expected return type
     * @return value from field.
     */
    public static <T> T getValueOfField(Object targetObject, Class targetClass, String fieldName) {
        Field field = getField(targetClass, fieldName);
        if (!Modifier.isStatic(field.getModifiers()) && targetObject == null) {
            throw new ReflectionOperationException("Cannot find non static field on null target object");
        }

        try {
            field.setAccessible(true);
            Object result = field.get(targetObject);
            field.setAccessible(false);
            return (T) result;
        } catch (IllegalAccessException e) {
            throw new ReflectionOperationException(e);
        }
    }

    /**
     * It gets value for static field by name from target class.
     * It starts looking for field from target class up in hierarchy.
     *
     * @param targetClass target class
     * @param fieldName   field name
     * @param <T>         expected return type
     * @return value from field.
     */
    public static <T> T getValueForStaticField(Class targetClass, String fieldName) {
        return getValueOfField(null, targetClass, fieldName);
    }

    /**
     * Invokes first match method in hierarchy of target object which has arguments with explicitly provided
     * types, and invoke method with list of arguments.
     * If target is null, then it will invoke it as static method.
     *
     * @param target     on that instance will be invoked method
     * @param methodName name of method.
     * @param argClasses explicit list of classes of arguments.
     * @param args       list of arguments for method.
     * @param <T>        type of returned object.
     * @return return result of invoked method.
     */
    public static <T> T invokeMethod(Object target, String methodName,
                                     List<Class<?>> argClasses, List<Object> args) {
        return invokeMethod(target, target.getClass(), methodName, argClasses, args);
    }

    /**
     * Invokes first match method in hierarchy of target object which has arguments with expected
     * types, and invokes method with list of arguments. It looks for method which has the same types like
     * argument list. It will not invoke method when as argument type will have some super class of argument instance.
     * If you want call someMethod(String text, Number number) then if you pass someMethod("text", new Long(1))
     * it will not invoke that method, because Number class is super class for Long...
     * If target is null, then it will invoke it as static method.
     *
     * @param target     on that instance will be invoked method
     * @param methodName name of method.
     * @param args       list of arguments for method.
     * @param <T>        type of returned object.
     * @return return result of invoked method.
     */
    public static <T> T invokeMethod(Object target, String methodName, List<Object> args) {
        return invokeMethod(target, methodName, args.toArray());
    }

    /**
     * Invoke first match method in hierarchy of target object which has arguments with expected
     * types, and invokes method with array of arguments. It looks for method which has the same types like
     * argument array. It will not invoke method when as argument type will have some super class of argument instance.
     * If you want call someMethod(String text, Number number) then if you pass someMethod("text", new Long(1))
     * it will not invoke that method, because Number class is super class for Long...
     * If target is null, then it will invoke it as static method.
     *
     * @param target     on that instance will be invoked method
     * @param methodName name of method.
     * @param args       list of arguments for method.
     * @param <T>        type of returned object.
     * @return return result of invoked method.
     */
    public static <T> T invokeMethod(Object target, String methodName, Object... args) {
        List<Class<?>> argClasses = mapToList(Object::getClass, args);
        return invokeMethod(target, target.getClass(), methodName, argClasses, Arrays.asList(args));
    }

    /**
     * Invoke first match method in hierarchy starting from target class, it invoke on target object which has arguments with expected
     * types, and invokes method with list of arguments. It looks for method which has the same types like
     * argument list. It will not invoke method when as argument type will have some super class of argument instance.
     * It is useful for private method which is hidden in super class. But our concrete class has the same signature and the same name
     * like private method in super class, so we can put target class here.
     * If you want call someMethod(String text, Number number) then if you pass someMethod("text", new Long(1))
     * it will not invoke that method, because Number class is super class for Long...
     * If target is null, then it will invoke it as static method.
     *
     * @param target      on that instance will be invoked method
     * @param targetClass from this class will start searching for match method.
     * @param methodName  name of method.
     * @param args        list of arguments for method.
     * @param <T>         type of returned object.
     * @return return result of invoked method.
     */
    public static <T> T invokeMethod(Object target, Class<?> targetClass, String methodName, List<Object> args) {
        return invokeMethod(target, targetClass, methodName, args.toArray());
    }

    /**
     * Invoke first match method in hierarchy starting from target class, it invoke on target object which has arguments with expected
     * types, and invokes method with list of arguments. It looks for method which has the same types like
     * argument array. It will not invoke method when as argument type will have some super class of argument instance.
     * It is useful for private method which is hidden in super class. But our concrete class has the same signature and the same name
     * like private method in super class, so we can put target class here.
     * If you want call someMethod(String text, Number number) then if you pass someMethod("text", new Long(1))
     * it will not invoke that method, because Number class is super class for Long...
     * If target is null, then it will invoke it as static method.
     *
     * @param target      on that instance will be invoked method
     * @param targetClass from this class will start searching for match method.
     * @param methodName  name of method.
     * @param args        array of arguments for method.
     * @param <T>         type of returned object.
     * @return return result of invoked method.
     */
    public static <T> T invokeMethod(Object target, Class<?> targetClass, String methodName, Object... args) {

        List<Class<?>> argClasses = mapToList(Object::getClass, args);
        return invokeMethod(target, targetClass, methodName, argClasses, Arrays.asList(args));
    }

    /**
     * Invokes first match method in hierarchy starting from target class, it invoke on target object which has arguments with explicitly provided
     * types, and invoke method with list of arguments.
     * It is useful for private method which is hidden in super class. But our concrete class has the same signature and the same name
     * like private method in super class, so we can put target class here.
     * If target is null, then it will invoke it as static method.
     *
     * @param target      on that instance will be invoked method
     * @param targetClass from this class will start searching for match method.
     * @param methodName  name of method.
     * @param argClasses  explicit list of classes of arguments.
     * @param args        list of arguments for method.
     * @param <T>         type of returned object.
     * @return return result of invoked method.
     */
    public static <T> T invokeMethod(Object target, Class<?> targetClass, String methodName,
                                     List<Class<?>> argClasses, List<Object> args) {
        Method method = getMethod(targetClass, methodName, argClasses.toArray(new Class[0]));
        if (!Modifier.isStatic(method.getModifiers()) && target == null) {
            throw new ReflectionOperationException("Cannot invoke non static method on null target object");
        }
        try {
            method.setAccessible(true);
            T result = (T) method.invoke(target, args.toArray(new Object[0]));
            method.setAccessible(false);
            return result;
        } catch (ReflectiveOperationException e) {
            throw new ReflectionOperationException(e);
        }
    }

    /**
     * It will search for first match static method, it will start searching from target class.
     * It will search for method with explicitly provided list of argument types.
     *
     * @param targetClass from this class will start searching for match method.
     * @param methodName  name of method.
     * @param argClasses  explicit list of classes of arguments.
     * @param args        list of arguments for method.
     * @param <T>         type of returned object.
     * @return return result of invoked method.
     */
    public static <T> T invokeStaticMethod(Class<?> targetClass, String methodName,
                                           List<Class<?>> argClasses, List<Object> args) {
        return invokeMethod(null, targetClass, methodName, argClasses, args);
    }

    /**
     * It will search for first match static method, it will start searching from target class.
     * It looks for method which has the same types like arguments list.
     * It will not invoke method when as argument type will have some super class of argument instance.
     * If you want call someMethod(String text, Number number) then if you pass someMethod("text", new Long(1))
     * it will not invoke that method, because Number class is super class for Long...
     *
     * @param targetClass from this class will start searching for match method.
     * @param methodName  name of method.
     * @param args        list of arguments for method.
     * @param <T>         type of returned object.
     * @return return result of invoked method.
     */
    public static <T> T invokeStaticMethod(Class<?> targetClass, String methodName, List<Object> args) {
        return invokeMethod(null, targetClass, methodName, args);
    }

    /**
     * It will search for first match static method, it will start searching from target class.
     * It looks for method which has the same types like arguments array.
     * It will not invoke method when as argument type will have some super class of argument instance.
     * If you want call someMethod(String text, Number number) then if you pass someMethod("text", new Long(1))
     * it will not invoke that method, because Number class is super class for Long...
     *
     * @param targetClass from this class will start searching for match method.
     * @param methodName  name of method.
     * @param args        array of arguments for method.
     * @param <T>         type of returned object.
     * @return return result of invoked method.
     */
    public static <T> T invokeStaticMethod(Class<?> targetClass, String methodName, Object... args) {
        return invokeMethod(null, targetClass, methodName, args);
    }

    /**
     * It create instance of object based on below arguments.
     *
     * @param type        expected type
     * @param argsClasses list with every type for every constructor argument.
     * @param args        list with every constructor argument.
     * @param <T>         expected type of create object
     * @return instance of new object
     */
    public static <T> T newInstance(Class<T> type, List<Class<?>> argsClasses, List<Object> args) {
        return newInstance(type, argsClasses, args.toArray());
    }

    /**
     * It create instance of object based on below arguments.
     *
     * @param type        expected type
     * @param argsClasses list with every type for every constructor argument.
     * @param args        array with every constructor argument.
     * @param <T>         expected type of create object
     * @return instance of new object
     */
    public static <T> T newInstance(Class<T> type, List<Class<?>> argsClasses, Object... args) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor(argsClasses.toArray(new Class<?>[0]));
            constructor.setAccessible(true);
            T instance = constructor.newInstance(args);
            constructor.setAccessible(false);
            return instance;
        } catch (Exception e) {
            throw new ReflectionOperationException(e);
        }
    }

    /**
     * It create instance of object based on below arguments.
     *
     * @param type expected type
     * @param args list with every constructor argument.
     * @param <T>  expected type of create object
     * @return instance of new object
     */
    public static <T> T newInstance(Class<T> type, List<Object> args) {
        List<Class<?>> argumentClasses = mapToList(args, Object::getClass);
        return newInstance(type, argumentClasses, args);
    }

    /**
     * It create instance of object based on below arguments.
     *
     * @param type expected type
     * @param args array with every constructor argument.
     * @param <T>  expected type of create object
     * @return instance of new object
     */
    public static <T> T newInstance(Class<T> type, Object... args) {
        return newInstance(type, Arrays.asList(args));
    }

    /**
     * It create instance of object without constructor arguments.
     *
     * @param type expected type
     * @param <T>  expected type of create object
     * @return instance of new object
     */

    public static <T> T newInstance(Class<T> type) {
        return newInstance(type, new ArrayList[0]);
    }
}
