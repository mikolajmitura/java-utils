package pl.jalokim.utils.reflection.beans.inheritiance;

import lombok.Getter;

@Getter
public class SecondLevelSomeConcreteObject extends SomeConcreteObject {

    public static final String CONCRETE_ANOTHER_PRIVATE_FIELD = "concrete-anotherPrivateField";
    private String anotherPrivateField = CONCRETE_ANOTHER_PRIVATE_FIELD;

    private final String finalString = "FINAL_STRING_2LEVEL";
    private  String privateString = "private_String_2LEVEL";

    String returnResultOf(String var1, String var2) {
        return "SecondLevelSomeConcreteObject" + var1 + var2;
    }

    String returnResultOf(String var1, Object var2) {
        return "SecondLevelSomeConcreteObject" + var1 + var2;
    }

    private String privateMethodString() {
        return "concreteClass2ndLevel";
    }
}
