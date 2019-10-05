package pl.jalokim.utils.random

import spock.lang.Specification

import static pl.jalokim.utils.collection.CollectionUtils.hasTheSameElements

class RandomUtilImplTest extends Specification {

    private RandomUtilImpl tested = new RandomUtilImpl()

    void cleanup() {
        RandomUtils.ownRandomImpl(new RandomUtilImpl())
    }

    def "randomTrueWithProbability return true when probability 20%"() {
        RandomUtilImpl mockImpl = Mock(RandomUtilImpl)
        RandomUtils.ownRandomImpl(mockImpl)

        given:
        mockImpl.randomInRangeImpl(_, _) >> {
            args ->
                if (args[0] == 0 && args[1] == 4) {
                    return 3
                }
                throw new UnsupportedOperationException("not mock for args: $args")
        }

        mockImpl.randomElementImpl(_) >> {
            args->
                if (args[0] == [false, false, false, true, false]) {
                    return true
                }
                throw new UnsupportedOperationException("not mock for args: $args")
        }

        when:
        def result = tested.randomTrueWithProbability(20)
        then:
        result
    }

    def "randomTrueWithProbability return false when probability 30%"() {
        RandomUtilImpl mockImpl = Mock(RandomUtilImpl)
        RandomUtils.impl = mockImpl

        given:
        mockImpl.randomInRangeImpl(_, _) >> {
            args ->
                if (args[0] == 0 && args[1] == 2) {
                    return 2
                }
                throw new UnsupportedOperationException("not mock for args: $args")
        }

        mockImpl.randomElementImpl(_) >> {
            args->
                if (args[0] == [false, false, true]) {
                    return false
                }
                throw new UnsupportedOperationException("not mock for args: $args")
        }

        when:
        def result = tested.randomTrueWithProbability(30)
        then:
        !result
    }

    def "randomTrueWithProbability return true when probability 70%"() {
        RandomUtilImpl mockImpl = Mock(RandomUtilImpl)
        RandomUtils.impl = mockImpl

        given:
        mockImpl.randomInRangeImpl(_, _) >> {
            args ->
                if (args[0] == 0 && args[1] == 2) {
                    return 2
                }
                throw new UnsupportedOperationException("not mock for args: $args")
        }

        mockImpl.randomElementImpl(_) >> {
            args->
                if (args[0] == [true, true, false]) {
                    return true
                }
                throw new UnsupportedOperationException("not mock for args: $args")
        }

        when:
        def result = tested.randomTrueWithProbability(70)
        then:
        result
    }

    def "randomTrueWithProbability return false when probability 80%"() {
        RandomUtilImpl mockImpl = Mock(RandomUtilImpl)
        RandomUtils.impl = mockImpl

        given:
        mockImpl.randomInRangeImpl(_, _) >> {
            args ->
                if (args[0] == 0 && args[1] == 4) {
                    return 0
                }
                throw new UnsupportedOperationException("not mock for args: $args")
        }

        mockImpl.randomElementImpl(_) >> {
            args->
                if (args[0] == [false, true, true, true, true]) {
                    return false
                }
                throw new UnsupportedOperationException("not mock for args: $args")
        }

        when:
        def result = tested.randomTrueWithProbability(80)
        then:
        !result
    }

    def "return true when 1"() {
        given:
        Random randomMock = Mock(Random)
        tested = new RandomUtilImpl(randomMock)

        randomMock.nextInt(_) >> {
            args->
            if (args[0] == 2) {
                return 1
            }
            throw new UnsupportedOperationException("not mock for args: $args")
        }

        when:
        boolean result = tested.randomTrue()
        then:
        result
    }

    def "return true when not 1"() {
        given:
        Random randomMock = Mock(Random)
        tested = new RandomUtilImpl(randomMock)

        randomMock.nextInt(_) >> {
            args->
                if (args[0] == 2) {
                    return 0
                }
                throw new UnsupportedOperationException("not mock for args: $args")
        }

        when:
        boolean result = tested.randomTrue()
        then:
        !result
    }

    def "random negative numbers"() {
        Set<Integer> randomNumbers = new HashSet<>()
        when:
        while (true) {
            int randomNumber = tested.randomInRangeImpl(-10, 0)
            randomNumbers.add(randomNumber)
            if (randomNumbers.size() == 11) {
                break
            }
        }
        then:
        hasTheSameElements(randomNumbers, [-10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0])
    }
}