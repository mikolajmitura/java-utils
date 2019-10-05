package pl.jalokim.utils.random;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Util class for some random things.
 */
public final class RandomUtils {

    private static RandomUtilImpl impl = new RandomUtilImpl();

    private RandomUtils() {

    }

    /**
     * Return random integer number.
     *
     * @param min returned value
     * @param max returned value
     * @return random value between min and max arguments.
     */
    public static int randomInRange(int min, int max) {
        return impl.randomInRangeImpl(min, max);
    }

    /**
     * It returns random index for element from collection.
     * @param elements for random index.
     * @param <T> generic type for collection argument.
     * @return random index.
     */
    public static <T> int randomIndex(List<T> elements) {
        return randomInRange(0, elements.size() - 1);
    }

    /**
     * It returns random index for element from array.
     * @param elements for random index.
     * @param <T> generic type for array argument.
     * @return random index.
     */
    public static <T> int randomIndex(T[] elements) {
        return randomInRange(0, elements.length - 1);
    }

    /**
     * It returns random element from collection
     * @param elements from which will be found random element.
     * @param <T> generic type for collection argument.
     * @return random element.
     */
    public static <T> T randomElement(Collection<T> elements) {
        Objects.requireNonNull(elements);
        return impl.randomElementImpl(elements);
    }

    /**
     * It returns random element from array
     * @param elements from which will be found random element.
     * @param <T> generic type for array.
     * @return random element.
     */
    public static <T> T randomElement(T[] elements) {
        return impl.randomElementImpl(Arrays.asList(elements));
    }

    /**
     * It returns true or false randomly.
     * @return random boolean value.
     */
    public static boolean randomTrue() {
        return impl.randomTrue();
    }

    /**
     * It returns true or false randomly based on probability of true value of boolean.
     * @return random boolean value.
     */
    public static boolean randomTrue(int probabilityOfTrueInPercent) {
        return impl.randomTrueWithProbability(probabilityOfTrueInPercent);
    }
}
