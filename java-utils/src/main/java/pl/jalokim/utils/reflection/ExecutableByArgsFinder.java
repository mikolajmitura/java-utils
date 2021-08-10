package pl.jalokim.utils.reflection;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExecutableByArgsFinder {

    public static Method findMethod(Class<?> targetClass, String methodName, Class<?>... argClasses) {
        Class<?> currentClass = targetClass;
        List<Method> foundMethods = new ArrayList<>();
        while (currentClass != null) {
            foundMethods.addAll(elements(currentClass.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> method.getParameterTypes().length == argClasses.length)
                .asList());
            currentClass = currentClass.getSuperclass();
        }
        return findExecutable(targetClass, foundMethods, argClasses);
    }

    public static Constructor<?> findConstructor(Class<?> targetClass, Class<?>... argClasses) {
        List<Constructor<?>> foundConstructors = elements(targetClass.getDeclaredConstructors())
            .filter(constructor -> constructor.getParameterTypes().length == argClasses.length)
            .asList();
        return findExecutable(targetClass, foundConstructors, argClasses);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Executable> T findExecutable(Class<?> targetClass, List<? extends Executable> foundExecutables, Class<?>... argClasses) {

        Map<Integer, List<Executable>> executablesByLengthOfPath = matchExecutableByArgTypes(targetClass, foundExecutables, argClasses);

        if (executablesByLengthOfPath.isEmpty()) {
            return null;
        }

        Integer shortestPath = elements(executablesByLengthOfPath.keySet())
            .min(Integer::compareTo)
            .get();

        List<Executable> executables = executablesByLengthOfPath.get(shortestPath);

        if (executables.size() != 1) {
            throw new AmbiguousExecutableCallException(executables);
        }
        return (T) executables.get(0);
    }
    
    private static Map<Integer, List<Executable>> matchExecutableByArgTypes(Class<?> targetClass,
        List<? extends Executable> foundExecutables, Class<?>... argClasses) {

        Map<Integer, List<Executable>> executablesByLengthOfPath = new HashMap<>();
        TypeMetadata typeMetadata = TypeWrapperBuilder.buildFromClass(targetClass);
        executables:
        for (Executable foundExecutable : foundExecutables) {
            int typeDifferenceLevel = 0;
            for (int argIndex = 0; argIndex < foundExecutable.getParameterTypes().length; argIndex++) {
                ExecutableMetadata<?> metaForExecutable = getExecutableMetadataFor(typeMetadata, foundExecutable);
                Class<?> classFromExecutableArg = metaForExecutable.getParameters().get(argIndex)
                    .getTypeOfParameter().getRawType();
                ClassParentsInfo parentsInfo = ClassParentsParser.parentsInfoFromClass(argClasses[argIndex]);

                if (parentsInfo.canBeCastTo(classFromExecutableArg)) {
                    typeDifferenceLevel = typeDifferenceLevel + parentsInfo.getHierarchyDiffLength(classFromExecutableArg);
                }  else {
                    continue executables;
                }
            }
            putFoundExecutableByScore(executablesByLengthOfPath, foundExecutable, typeDifferenceLevel);
        }
        return executablesByLengthOfPath;
    }

    private ExecutableMetadata<?> getExecutableMetadataFor(TypeMetadata typeMetadata, Executable executable) {
        if (executable instanceof Method) {
            return typeMetadata.getMetaForMethod((Method) executable);
        }
        if (executable instanceof Constructor) {
            return typeMetadata.getMetaForConstructor((Constructor<?>) executable);
        }
        throw new IllegalArgumentException("Executable instance not Method or not Constructor type, was given type: " + executable.getDeclaringClass());
    }

    private static void putFoundExecutableByScore(Map<Integer, List<Executable>> executableByScore,
        Executable foundExecutable, int matchScore) {
        List<Executable> executables = executableByScore.get(matchScore);
        if (executables == null) {
            List<Executable> newExecutables = new ArrayList<>();
            newExecutables.add(foundExecutable);
            executableByScore.put(matchScore, newExecutables);
        } else {
            executables.add(foundExecutable);
        }
    }
}
