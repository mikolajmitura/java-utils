package pl.jalokim.utils.string

import spock.lang.Specification
import spock.lang.Unroll

class SpockStringUtilsTest extends Specification {

    @Unroll
    def "return expected changed text"() {
        when:
        def result = StringUtils.replaceLast(originText, changeFrom, changeTo)

        then:
        result == expected

        where:
        expected                 | originText              | changeFrom | changeTo
        "some(12xText(12xf0987x" | "some(12xText(12xf(12x" | "(12"      | "0987"
        "some0987ext(12xf(12x"   | "some(12xText(12xf(12x" | "(12xT"    | "0987"
        "someText"               | "someText"              | "("        | ")"
    }

    @Unroll
    def "should split text by line ending mark as expected"() {
        when:
        def result = StringUtils.splitByLineEnding(text)

        then:
        result == expectedResult

        where:
        expectedResult              | text
        ["text1", "text2", "text3"] | "text1\ntext2\ntext3"
        ["text1", "text2", "text3"] | "text1\r\ntext2\r\ntext3"
        ["text1", "text2\ntext3"]   | "text1\r\ntext2\ntext3"
    }

    @Unroll
    def "should return expected result for equalsIgnoreLineEndings"() {
        when:
        def result = StringUtils.equalsIgnoreLineEndings(text1, text2)

        then:
        result == expectedResult

        where:
        expectedResult | text1                 | text2
        true           | "text1\ntext2\ntext3" | "text1\r\ntext2\r\ntext3"
        false          | "text1\ntext2\ntext3" | "text1\r\ntext2\ntext3"
        false          | null                  | "text1\r\ntext2\ntext3"
        false          | "text1\ntext2\ntext3" | null
        true           | null                  | null
    }
}
