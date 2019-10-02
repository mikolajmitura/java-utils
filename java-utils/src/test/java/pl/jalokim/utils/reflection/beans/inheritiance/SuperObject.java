package pl.jalokim.utils.reflection.beans.inheritiance;

import lombok.Getter;

@Getter
public class SuperObject extends SuperAbstractObject<Integer, String, Short> {


    private int constructorNr;

    public SuperObject() {
        constructorNr = 0;
    }

    public SuperObject(String var1, Integer var2) {
        constructorNr = 1;
    }

    public SuperObject(String var1, Number var2) {
        constructorNr = 2;
    }

    private SuperObject(Integer var1) {
        constructorNr = 3;
    }

    public static final String PRIVATE_FIELD_INIT = "privateField_Init";

    private static final Integer STATIC_FINAL_INTEGER = 0;
    private static final Integer STATIC_FINAL_INTEGER2 = 2;
    private static final int PRIMITIVE_STATIC_FINAL_INTEGER = 0;
    private static final int PRIMITIVE_STATIC_FINAL_INTEGER2 = 0;
    private static final String CAN_BE_UPDATE_STATIC_FINAL;

    static {
        CAN_BE_UPDATE_STATIC_FINAL = "0";
    }

    private String privateField = PRIVATE_FIELD_INIT;
    private String anotherPrivateField = PRIVATE_FIELD_INIT;
    private final Integer privateFinalField = 1;
    private final int primitiveIntFinalField = 1;

    public final String getSuperValueOfAnotherPrivateField() {
        return anotherPrivateField;
    }

    public static Integer getSTATIC_FINAL_INTEGER() {
        return STATIC_FINAL_INTEGER;
    }

    public static Integer getSTATIC_FINAL_INTEGER2() {
        return STATIC_FINAL_INTEGER2;
    }

    public static String getCAN_BE_UPDATE_STATIC_FINAL() {
        return CAN_BE_UPDATE_STATIC_FINAL;
    }

    private void someMethod(Integer arg1, String arg2) {

    }

    private Integer returnIntegerVal(String var1, Number number) {
        return 10;
    }
}
