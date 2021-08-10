package pl.jalokim.utils.reflection;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
class ClassParentsParser {

    public static ClassParentsInfo parentsInfoFromClass(Class<?> type) {
        Map<Class<?>, Integer> levelByType = new HashMap<>();
        addToHierarchy(levelByType, type, 0);

        return new ClassParentsInfo(type, levelByType);
    }

    public static void addToHierarchy(Map<Class<?>, Integer> levelByType, Class<?> currentClass, Integer currentLevel) {
        if (currentClass == null) {
            return;
        }
        if (levelByType.containsKey(currentClass)) {
            return;
        } else {
            levelByType.put(currentClass, currentLevel);
        }

        int nextLevel = currentLevel + 1;
        addToHierarchy(levelByType, currentClass.getSuperclass(), nextLevel);
        for (Class<?> anInterface : currentClass.getInterfaces()) {
            addToHierarchy(levelByType, anInterface, nextLevel);
        }
    }
}
