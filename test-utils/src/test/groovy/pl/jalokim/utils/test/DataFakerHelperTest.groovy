package pl.jalokim.utils.test

import com.github.javafaker.Faker
import spock.lang.Specification

class DataFakerHelperTest extends Specification {

    def "can get singleton of Faker instance"() {
        when:
        Faker result = DataFakerHelper.faker

        then:
        result != null
    }

    def "return default size 15 of random text"() {
        when:
        def randomText = DataFakerHelper.randomText()

        then:
        randomText.size() == 15
    }

    def "return size 20 of random text"() {
        when:
        def randomText = DataFakerHelper.randomText(20)

        then:
        randomText.size() == 20
    }

    def "return size 20 of random numbers as text"() {
        when:
        def randomText = DataFakerHelper.randomNumbersAsText(20)

        then:
        randomText.size() == 20
    }

    def "return random value of long"() {
        when:
        def randomLong = DataFakerHelper.randomLong(20)

        then:
        randomLong <= 20 && randomLong >= 0
    }

    def "return random value of integer"() {
        when:
        def randomInteger = DataFakerHelper.randomInteger(20)

        then:
        randomInteger <= 20 && randomInteger >= 0
    }
}
