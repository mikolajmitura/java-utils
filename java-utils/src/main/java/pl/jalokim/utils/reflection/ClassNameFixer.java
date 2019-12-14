package pl.jalokim.utils.reflection;

final class ClassNameFixer {

    private static final char DOLLAR_CHAR = '$';
    private static final int TWO = 2;
    private static final int ONE = 1;

    private ClassNameFixer() {

    }

    static String fixClassName(String someClassName) {
        int numberOfDollars = countSearchedChar(someClassName);
        if (numberOfDollars == 0) {
            return someClassName;
        }
        int currentIndex = 0;
        for (char currentChar : someClassName.toCharArray()) {
            if (currentChar == DOLLAR_CHAR) {
                String textBeforeDollar = someClassName.substring(0, currentIndex);
                int sizeOfClassNamePart = textBeforeDollar.length();
                String secondPart = getNewClassName(someClassName, currentIndex, sizeOfClassNamePart);
                if (secondPart != null) {
                    return secondPart;
                }
            }
            currentIndex++;
        }
        return someClassName;
    }

    private static String getNewClassName(String someClassName, int currentIndex, int sizeOfClassNamePart) {
        if (Math.abs(sizeOfClassNamePart) % TWO == ONE) {
            int middleIndex = sizeOfClassNamePart / 2;
            String firstPart = someClassName.substring(0, middleIndex);
            String secondPart = someClassName.substring(middleIndex + 1, currentIndex);
            if (firstPart.equals(secondPart)) {
                return secondPart + someClassName.substring(currentIndex);
            }
        }
        return null;
    }

    private static int countSearchedChar(String text) {
        char[] chars = text.toCharArray();
        int counter = 0;
        for (char aChar : chars) {
            if (aChar == DOLLAR_CHAR) {
                counter++;
            }
        }
        return counter;
    }
}
