package pl.jalokim.utils.reflection;


import org.junit.Test;
import pl.jalokim.utils.reflection.beans.inheritiance.ClassNameWith$;
import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.reflection.ClassNameFixer.fixClassName;

public class ClassNameFixerTest {

    @Test
    public void returnRealNestedClassName() {
        // given
        String brokenClassName = "pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass$TupleClass";
        // when
        String fixedClassName = fixClassName(brokenClassName);
        // then
        assertThat(fixedClassName).isEqualTo("pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass$TupleClass");
        Class<?> classForName = MetadataReflectionUtils.getClassForName(fixedClassName);
        assertThat(classForName).isEqualTo(ExampleClass.TupleClass.class);
    }

    @Test
    public void returnUnchangedClassNameWith$ButNotNestedClass() {
        // given
        String brokenClassName = "pl.jalokim.utils.reflection.beans.inheritiance.ClassNameWith$";
        // when
        String fixedClassName = fixClassName(brokenClassName);
        // then
        assertThat(fixedClassName).isEqualTo("pl.jalokim.utils.reflection.beans.inheritiance.ClassNameWith$");
        Class<?> classForName = MetadataReflectionUtils.getClassForName(fixedClassName);
        assertThat(classForName).isEqualTo(ClassNameWith$.class);
    }

    @Test
    public void returnFixedClassNameWith$ButNestedClass() {
        // given
        String brokenClassName = "pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass.pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass$NestedWith$InName";
        // when
        String fixedClassName = fixClassName(brokenClassName);
        // then
        assertThat(fixedClassName).isEqualTo("pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass$NestedWith$InName");
        Class<?> classForName = MetadataReflectionUtils.getClassForName(fixedClassName);
        assertThat(classForName).isEqualTo(ExampleClass.NestedWith$InName.class);
    }

    @Test
    public void returnUnchangedClassNameForValidClassNameWithout$() {
        // given
        String brokenClassName = "pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass";
        // when
        String fixedClassName = fixClassName(brokenClassName);
        // then
        assertThat(fixedClassName).isEqualTo("pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass");
        Class<?> classForName = MetadataReflectionUtils.getClassForName(fixedClassName);
        assertThat(classForName).isEqualTo(ExampleClass.class);
    }

}