package pl.jalokim.utils.reflection.beans;

import lombok.Getter;

@Getter
public class SuperObject {

    public static final String STATIC_FINAL_STRING_VALUE = "some_value";
    private static final String STATIC_FINAL_STRING = STATIC_FINAL_STRING_VALUE;
    public static final String PRIVATE_FIELD_INIT = "privateField_Init";
    public static final String PRIVATE_FINAL_FIELD_INIT = "privateFinalField_init";

    private String privateField = PRIVATE_FIELD_INIT;
    private String anotherPrivateField = PRIVATE_FIELD_INIT;
    private final String privateFinalField = PRIVATE_FINAL_FIELD_INIT;

    public final String getSuperValueOfAnotherPrivateField() {
        return anotherPrivateField;
    }

    public static String getValueOfStaticFinalField() {
        return STATIC_FINAL_STRING;
    }
}
