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
        "some0987ext(12xf(12x" | "some(12xText(12xf(12x" | "(12xT"      | "0987"
        "someText"               | "someText"              | "("        | ")"
    }
}
