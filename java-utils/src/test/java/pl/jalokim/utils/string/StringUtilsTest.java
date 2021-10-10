package pl.jalokim.utils.string;

import org.junit.Assert;
import org.junit.Test;
import pl.jalokim.utils.collection.Elements;
import pl.jalokim.utils.constants.Constants;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilsTest {

    @Test
    public void testIsEmpty() {
        boolean result = StringUtils.isEmpty("text");
        Assert.assertFalse(result);
        result = StringUtils.isEmpty("");
        Assert.assertTrue(result);
        result = StringUtils.isEmpty(null);
        Assert.assertTrue(result);
    }

    @Test
    public void testIsNotEmpty() throws Exception {
        boolean result = StringUtils.isNotEmpty("text");
        Assert.assertTrue(result);
        result = StringUtils.isNotEmpty("");
        Assert.assertFalse(result);
        result = StringUtils.isNotEmpty(null);
        Assert.assertFalse(result);
    }

    @Test
    public void testIsBlank() {
        boolean result = StringUtils.isBlank("text");
        Assert.assertFalse(result);
        result = StringUtils.isBlank("  text");
        Assert.assertFalse(result);
        result = StringUtils.isBlank("              ");
        Assert.assertTrue(result);
        result = StringUtils.isBlank("");
        Assert.assertTrue(result);
        result = StringUtils.isBlank(" ");
        Assert.assertTrue(result);
        result = StringUtils.isBlank(null);
        Assert.assertTrue(result);
    }

    @Test
    public void testIsNotBlank() {
        boolean result = StringUtils.isNotBlank("text");
        Assert.assertTrue(result);
        result = StringUtils.isNotBlank("              ");
        Assert.assertFalse(result);
        result = StringUtils.isNotBlank("");
        Assert.assertFalse(result);
        result = StringUtils.isNotBlank(" ");
        Assert.assertFalse(result);
    }

    @Test
    public void testConcatElementsAsLines() {
        // when
        String result = StringUtils.concatElementsAsLines(Arrays.asList("10", "11", "12"));
        // then
        Assert.assertEquals(String.format("10%n11%n12"), result);
    }

    @Test
    public void testConcatElementsAsLinesFromArray() {
        // when
        String result = StringUtils.concatElementsAsLines("10", "11", "12", 13);
        // then
        Assert.assertEquals(String.format("10%n11%n12%n13"), result);
    }

    @Test
    public void testConcatElementsAsLinesFromArrayWithMapper() {
        // when
        String result = StringUtils.concatElementsAsLines(number-> Integer.toString(number),10, 11, 12, 13);
        // then
        Assert.assertEquals(String.format("10%n11%n12%n13"), result);
    }

    @Test
    public void testConcatElementsAsLines2() {
        // given
        List<Integer> numbers = Arrays.asList(10, 12, 11, 6542, 234);
        // when
        String result = StringUtils.concatElementsAsLines(numbers, number -> number.toString().substring(0, 1));
        // then
        Assert.assertEquals(String.format("1%n1%n1%n6%n2"), result);
    }

    @Test
    public void testTabsNTimes() {
        // when
        String result = StringUtils.tabsNTimes(3);
        // then
        Assert.assertEquals("\t\t\t", result);
    }

    @Test
    public void testRepeatTextNTimes() {
        // when
        String result = StringUtils.repeatTextNTimes(3, "text");
        // then
        Assert.assertEquals("texttexttext", result);
    }

    @Test
    public void testConcatElements() {
        // given
        List<Integer> numbers = Arrays.asList(10, 12, 11, 6542, 234);
        // when
        String result = StringUtils.concatElements(numbers);
        // then
        Assert.assertEquals("1012116542234", result);
    }

    @Test
    public void testConcatElementsWithMapper() {
        // given
        List<Integer> numbers = Arrays.asList(10, 12, 11, 6542, 234);
        // when
        String result = StringUtils.concatElements(numbers, number -> number.toString().substring(0, 1));
        // then
        Assert.assertEquals("11162", result);
    }

    @Test
    public void testConcatElementsWithJoinText() {
        // given
        String result = StringUtils.concatElements(Arrays.asList(10, 12, 11, 6542, 234), Constants.COMMA);
        // then
        Assert.assertEquals("10,12,11,6542,234", result);
    }

    @Test
    public void testConcatElementsWithMapperAndJoinText() {
        // given
        List<Integer> numbers = Arrays.asList(10, 12, 11, 6542, 234);
        // when
        String result = StringUtils.concatElements(numbers, number -> number.toString().substring(0, 1), Constants.COMMA);
        // then
        Assert.assertEquals("1,1,1,6,2", result);
    }

    @Test
    public void testConcatElementsWithTextPrefixAndSuffix() {
        // given
        List<Integer> numbers = Arrays.asList(10, 12, 11, 6542, 234);
        // when
        String result = StringUtils.concatElements("START_", numbers, number -> number.toString().substring(0, 1), Constants.COMMA, "_END");
        // then
        Assert.assertEquals("START_1,1,1,6,2_END", result);
    }

    @Test
    public void testConcatElements6() {
        // when
        String result = StringUtils.concatElements(Constants.COMMA, "1", "1", "12341234");
        // then
        Assert.assertEquals("1,1,12341234", result);
    }

    @Test
    public void testConcat() {
        // when
        String result = StringUtils.concat("text1", "text2", "_2");
        // then
        Assert.assertEquals("text1text2_2", result);
    }

    @Test
    public void concatObjects() {
        // when
        String result = StringUtils
                .concatObjects(1, 2, 3, 1.1, " test");
        // then
        assertThat(result).isEqualTo("1231.1 test");
    }

    @Test
    public void concatElementsByElementsSkipNullsAsExpected() {
        // given
        Elements<String> elements = Elements.elements("1", "2", null, "3");
        // when
        String s = StringUtils.concatElementsSkipNulls(elements);
        // then
        assertThat(s).isEqualTo("123");
    }

    @Test
    public void concatElementsByCollectionSkipNullsAsExpected() {
        // given
        List<String> elements = Arrays.asList("1", "2", null, "3");
        // when
        String s = StringUtils.concatElementsSkipNulls(elements);
        // then
        assertThat(s).isEqualTo("123");
    }

    @Test
    public void concatElementsWithJoinByElementsSkipNullsAsExpected() {
        // given
        Elements<String> elements = Elements.elements("1", null, "2", null, "3");
        // when
        String s = StringUtils.concatElementsSkipNulls(elements, ", ");
        // then
        assertThat(s).isEqualTo("1, 2, 3");
    }

    @Test
    public void concatElementsWithJoinByCollectionSkipNullsAsExpected() {
        // given
        List<String> elements = Arrays.asList("1", "2", null, "3");
        // when
        String s = StringUtils.concatElementsSkipNulls(elements, ", ");
        // then
        assertThat(s).isEqualTo("1, 2, 3");
    }

    @Test
    public void shouldClearAllGivenTexts() {
        // given
        String textToClear = "//this is some\\$ /text{}/";
        // when
        String result = StringUtils.replaceAllWithEmpty(textToClear, "/", "\\$", "{", "}");
        // then
        assertThat(result).isEqualTo("this is some text");
    }
}
