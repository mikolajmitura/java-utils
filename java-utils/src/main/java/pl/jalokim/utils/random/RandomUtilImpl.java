package pl.jalokim.utils.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static pl.jalokim.utils.random.RandomUtils.randomIndex;

public class RandomUtilImpl {

    private Random RANDOM = new Random();

    public Integer randomInRangeImpl(int min, int max) {
        if (max < min) {
            throw new RandomException("Max: " + max + " should be greater than or equals with min: " + min);
        }
        return RANDOM.nextInt((max - min) + 1) + min;
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

    protected List<Boolean> generateProbabilityArray(int probabilityOfTrueInPercent) {
        List<Boolean> probabilityOfTrueList = new ArrayList<>();
        boolean defaultValue = probabilityOfTrueInPercent <= 50;
        probabilityOfTrueInPercent = probabilityOfTrueInPercent <= 50 ? probabilityOfTrueInPercent : 100 - probabilityOfTrueInPercent;
        int size = 100 / probabilityOfTrueInPercent;
        for (int i = 0; i < size; i++) {
            probabilityOfTrueList.add(!defaultValue);
        }
        int indexToChange = randomIndex(probabilityOfTrueList);
        probabilityOfTrueList.set(indexToChange, defaultValue);
        return probabilityOfTrueList;
    }
}
