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
        Map<Integer, List<Method>> methodsByLengthOfPath = new HashMap<>();
        while (currentClass != null) {
            List<Method> foundMethods = elements(currentClass.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> method.getParameterTypes().length == argClasses.length)
                .asList();

            matchMethodsByArgTypes(targetClass, methodsByLengthOfPath, foundMethods, argClasses);
            currentClass = currentClass.getSuperclass();
        }

        if (methodsByLengthOfPath.isEmpty()) {
            return null;
        }

        Integer shortestPath = elements(methodsByLengthOfPath.keySet())
            .min(Integer::compareTo)
            .get();

        List<Method> methods = methodsByLengthOfPath.get(shortestPath);

        if (methods.size() != 1) {
            throw new AmbiguousMethodCallException(methods);
        }
        return methods.get(0);
    }

    private static void matchMethodsByArgTypes(Class<?> targetClass, Map<Integer, List<Method>> methodsByScore,
        List<Method> foundMethods, Class<?>... argClasses) {
        TypeMetadata typeMetadata = TypeWrapperBuilder.buildFromClass(targetClass);
        methods:
        for (Method foundMethod : foundMethods) {
            int typeDifferenceLevel = 0;
            for (int argIndex = 0; argIndex < foundMethod.getParameterTypes().length; argIndex++) {
                MethodMetadata metaForMethod = typeMetadata.getMetaForMethod(foundMethod);
                Class<?> classFromMethodArg = metaForMethod.getParameters().get(argIndex)
                    .getTypeOfParameter().getRawType();
                ClassParentsInfo parentsInfo = ClassParentsParser.parentsInfoFromClass(argClasses[argIndex]);

                if (parentsInfo.canBeCastTo(classFromMethodArg)) {
                    typeDifferenceLevel = typeDifferenceLevel + parentsInfo.getHierarchyDiffLength(classFromMethodArg);
                }  else {
                    continue methods;
                }
            }
            putFoundMethodByScore(methodsByScore, foundMethod, typeDifferenceLevel);
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
