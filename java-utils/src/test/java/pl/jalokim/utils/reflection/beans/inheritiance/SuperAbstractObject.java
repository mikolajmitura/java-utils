package pl.jalokim.utils.reflection.beans.inheritiance;

public abstract class SuperAbstractObject<T1, T2, T3> {

    private static int nextId = 0;
    public static String STATIC_STRING = "";

    String returnResultOf(String var1, String var2) {
        return "SuperAbstractObject";
    }
    String returnResultOf(String var1) {
        return "SuperAbstractObject";
    }

    private String privateMethodString() {
        return "abstractClass";
    }

    private static int incrementValue(Number number) {
        nextId++;
        return nextId;
    }

    public static void reset() {
        nextId = 0;
    }

    private static void updateSTATIC_STRING(String value) {
        STATIC_STRING = value;
    }

}
