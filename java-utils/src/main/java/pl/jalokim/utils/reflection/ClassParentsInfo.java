package pl.jalokim.utils.reflection;

import java.util.Map;
import lombok.Value;

@Value
class ClassParentsInfo {

    Class<?> currentClass;
    Map<Class<?>, Integer> levelByType;

    public boolean canBeCastTo(Class<?> typeToCast) {
        return typeToCast.isAssignableFrom(currentClass);
    }

    public Integer getHierarchyDiffLength(Class<?> type) {
        Integer level = levelByType.get(type);
        if (level == null) {
            throw new IllegalArgumentException("Cannot find class " + type.getCanonicalName()
                + " as super type for: " + currentClass.getCanonicalName());
        }
        return level;
    }
}
