package pl.jalokim.utils.random;

import static pl.jalokim.utils.random.RandomUtils.randomIndex;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Implementation of random API.
 */
public class RandomUtilImpl {

    private static final int FIFTY_PERCENT = 50;
    private static final int ONE_HUNDRED_PERCENT = 100;
    private final Random random;

    public RandomUtilImpl() {
        this.random = new SecureRandom();
    }

    public RandomUtilImpl(Random random) {
        this.random = random;
    }

    public Integer randomInRangeImpl(int min, int max) {
        if (max < min) {
            throw new RandomException("Max: " + max + " should be greater than or equals with min: " + min);
        }
        return random.nextInt((max - min) + 1) + min;
    }

    public <T> T randomElementImpl(Collection<T> elements) {
        if (elements.isEmpty()) {
            throw new RandomException("Cannot get random element from empty list: " + elements);
        }
        List<T> asList = new ArrayList<>(elements);
        return asList.get(randomInRangeImpl(0, elements.size() - 1));
    }

    public boolean randomTrue() {
        int number = randomInRangeImpl(0, 1);
        return number == 1;
    }

    public boolean randomTrueWithProbability(int probabilityOfTrueInPercent) {
        return RandomUtils.randomElement(generateProbabilityArray(probabilityOfTrueInPercent));
    }

    protected List<Boolean> generateProbabilityArray(int probabilityOfTrueInPercentOriginal) {
        int probabilityOfTrueInPercent = probabilityOfTrueInPercentOriginal;
        List<Boolean> probabilityOfTrueList = new ArrayList<>();
        boolean defaultValue = probabilityOfTrueInPercent <= FIFTY_PERCENT;
        probabilityOfTrueInPercent = probabilityOfTrueInPercent <= FIFTY_PERCENT
            ? probabilityOfTrueInPercent : ONE_HUNDRED_PERCENT - probabilityOfTrueInPercent;
        int size = ONE_HUNDRED_PERCENT / probabilityOfTrueInPercent;
        for (int i = 0; i < size; i++) {
            probabilityOfTrueList.add(!defaultValue);
        }
        int indexToChange = randomIndex(probabilityOfTrueList);
        probabilityOfTrueList.set(indexToChange, defaultValue);
        return probabilityOfTrueList;
    }
}
