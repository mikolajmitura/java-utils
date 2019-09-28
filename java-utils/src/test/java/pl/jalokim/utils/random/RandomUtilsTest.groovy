package pl.jalokim.utils.random

import spock.lang.Specification

class RandomUtilsTest extends Specification {

    def "always return in range"() {
        when:
        List<Integer> numbers = [3, 4, 5, 6, 7]

        then:
        Set<Integer> generatedNumbers = new HashSet<>()
        while (generatedNumbers.size() != numbers.size()) {
            int generated = RandomUtils.randomInRange(3, 7)
            generatedNumbers.add(generated)
            assert numbers.contains(generated)
        }
    }

    def "always return element from list"() {
        when:
        List<Integer> numbers = [100, 120, 123, 665, 6127]
        then:
        Set<Integer> generatedNumbers = new HashSet<>()
        while (generatedNumbers.size() != numbers.size()) {
            int generated = RandomUtils.randomElement(numbers)
            generatedNumbers.add(generated)
            assert numbers.contains(generated)
        }
    }

    def "generate expected Probability Array"(int percent, int expectedSize, int expectedTrue) {
        when:
        RandomUtilImpl impl = new RandomUtilImpl()
        def array = impl.generateProbabilityArray(percent)
        then:
        array.size() == expectedSize
        int trueCounter = 0
        array.forEach({
            element ->
                if (element) {
                    trueCounter++
                }
        })
        trueCounter == expectedTrue
        where:
        percent | expectedSize | expectedTrue
        10      | 10           | 1
        20      | 5            | 1
        33      | 3            | 1
        5       | 20           | 1
        80      | 5            | 4
    }


    def "thrown exception while max < min"() {
        when:
        RandomUtils.randomInRange(9, 6)
        then:
        RandomException randomException = thrown()
        randomException.message == "Max: 6 should be greater than or equals with min: 9"

    }

    def "cannot get random element in empty collection"() {
        when:
        RandomUtils.randomElement([])
        then:
        RandomException randomException = thrown()
        randomException.message == "Cannot get random element from empty list: []"
    }
}
