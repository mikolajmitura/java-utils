package pl.jalokim.utils.random;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RandomUtils {

    private static RandomUtilImpl impl = new RandomUtilImpl();

    public static int randomInRange(int min, int max) {
        return impl.randomInRangeImpl(min, max);
    }

    public static <T> int randomIndex(List<T> elements) {
        return randomInRange(0, elements.size() - 1);
    }

    public static <T> int randomIndex(T[] elements) {
        return randomInRange(0, elements.length - 1);
    }

    public static <T> T randomElement(Collection<T> elements) {
        assert elements != null;
        return impl.randomElementImpl(elements);
    }

    public static <T> T randomElement(T[] elements) {
        return impl.randomElementImpl(Arrays.asList(elements));
    }

    public static boolean randomTrue() {
        return impl.randomTrue();
    }

    public static boolean randomTrue(int probabilityOfTrueInPercent) {
        return impl.randomTrueWithProbability(probabilityOfTrueInPercent);
    }
}
