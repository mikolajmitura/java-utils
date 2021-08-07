package pl.jalokim.utils.reflection;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
class MethodByArgsMatcher {

    public static Method findMethod(Class<?> targetClass, String methodName, Class<?>... argClasses) {
        Class<?> currentClass = targetClass;
        Map<Integer, List<Method>> methodsByScore = new HashMap<>();
        while (currentClass != null) {
            List<Method> foundMethods = elements(currentClass.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> method.getParameterTypes().length == argClasses.length)
                .asList();

            matchMethodsByArgTypes(methodsByScore, foundMethods, argClasses);
            currentClass = currentClass.getSuperclass();
        }

        if (methodsByScore.isEmpty()) {
            return null;
        }

        Integer maxScore = elements(methodsByScore.keySet())
            .max(Integer::compareTo)
            .get();

        List<Method> methods = methodsByScore.get(maxScore);

        if (methods.size() != 1) {
            throw new AmbiguousMethodCallException(methods);
        }
        return methods.get(0);
    }

    private static void matchMethodsByArgTypes(Map<Integer, List<Method>> methodsByScore,
        List<Method> foundMethods, Class<?>... argClasses) {
        methods:
        for (Method foundMethod : foundMethods) {
            int matchScore = 0;
            for (int argIndex = 0; argIndex < foundMethod.getParameterTypes().length; argIndex++) {
                Class<?> classFromMethodArg = foundMethod.getParameterTypes()[argIndex];
                Class<?> classFromToInvoke = argClasses[argIndex];
                if (classFromMethodArg.equals(classFromToInvoke)) {
                    matchScore = matchScore + 2;
                } else if (classFromMethodArg.isAssignableFrom(classFromToInvoke)) {
                    matchScore = matchScore + 1;
                } else {
                    continue methods;
                }
            }
            putFoundMethodByScore(methodsByScore, foundMethod, matchScore);
        }
    }

    private static void putFoundMethodByScore(Map<Integer, List<Method>> methodsByScore,
        Method foundMethod, int matchScore) {
        List<Method> methods = methodsByScore.get(matchScore);
        if (methods == null) {
            List<Method> newMethods = new ArrayList<>();
            newMethods.add(foundMethod);
            methodsByScore.put(matchScore, newMethods);
        } else {
            methods.add(foundMethod);
        }
    }
}
