package pl.jalokim.utils.reflection.beans.inheritiance;

import lombok.Getter;

@Getter
public class SecondLevelSomeConcreteObject extends SomeConcreteObject {
    public static final String CONCRETE_ANOTHER_PRIVATE_FIELD = "concrete-anotherPrivateField";
    private String anotherPrivateField = CONCRETE_ANOTHER_PRIVATE_FIELD;

}
