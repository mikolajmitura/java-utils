package pl.jalokim.utils.reflection;

import junit.framework.TestCase;
import org.junit.Ignore;
import org.junit.Test;
import pl.jalokim.utils.reflection.beans.SecondLevelSomeConcreteObject;
import pl.jalokim.utils.reflection.beans.SomeConcreteObject;
import pl.jalokim.utils.reflection.beans.SuperObject;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.reflection.beans.SecondLevelSomeConcreteObject.CONCRETE_ANOTHER_PRIVATE_FIELD;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForField;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForStaticField;
import static pl.jalokim.utils.reflection.beans.SuperObject.PRIVATE_FIELD_INIT;
import static pl.jalokim.utils.reflection.beans.SuperObject.PRIVATE_FINAL_FIELD_INIT;
import static pl.jalokim.utils.reflection.beans.SuperObject.STATIC_FINAL_STRING_VALUE;
import static pl.jalokim.utils.reflection.beans.SuperObject.getValueOfStaticFinalField;

public class InvokableReflectionUtilsTest {

    private static final String NEW_VALUE = "new_VALUE";

    @Test
    public void setPrivateFieldValueForTheSameClassLikeTargetObject() {
        // given
        SuperObject superObject = new SuperObject();
        assertThat(superObject.getPrivateField()).isEqualTo(PRIVATE_FIELD_INIT);
        // when
        setValueForField(superObject, "privateField", NEW_VALUE);
        // then
        assertThat(superObject.getPrivateField()).isEqualTo(NEW_VALUE);
    }

    @Test
    public void setPrivateFieldValueForSuperClassForTargetObject() {
        SuperObject superObject = new SecondLevelSomeConcreteObject();
        assertThat(superObject.getPrivateField()).isEqualTo(PRIVATE_FIELD_INIT);
        // when
        setValueForField(superObject, "privateField", NEW_VALUE);
        // then
        assertThat(superObject.getPrivateField()).isEqualTo(NEW_VALUE);
    }

    @Test
    public void setPrivateFieldForSuperClassWhichExistInConcreteClassToo() {
        // given
        SecondLevelSomeConcreteObject superObject = new SecondLevelSomeConcreteObject();
        assertThat(superObject.getSuperValueOfAnotherPrivateField()).isEqualTo(PRIVATE_FIELD_INIT);
        assertThat(superObject.getAnotherPrivateField()).isEqualTo(CONCRETE_ANOTHER_PRIVATE_FIELD);
        // when
        setValueForField(superObject, SomeConcreteObject.class, "anotherPrivateField", NEW_VALUE);
        // then
        assertThat(superObject.getSuperValueOfAnotherPrivateField()).isEqualTo(NEW_VALUE);
        assertThat(superObject.getAnotherPrivateField()).isEqualTo(CONCRETE_ANOTHER_PRIVATE_FIELD);
    }

    @Test
    public void setPrivateFieldForConcreteClassWhichExistInSuperClassToo() {
        SecondLevelSomeConcreteObject superObject = new SecondLevelSomeConcreteObject();
        assertThat(superObject.getSuperValueOfAnotherPrivateField()).isEqualTo(PRIVATE_FIELD_INIT);
        assertThat(superObject.getAnotherPrivateField()).isEqualTo(CONCRETE_ANOTHER_PRIVATE_FIELD);
        // when
        setValueForField(superObject, "anotherPrivateField", NEW_VALUE);
        // then
        assertThat(superObject.getSuperValueOfAnotherPrivateField()).isEqualTo(PRIVATE_FIELD_INIT);
        assertThat(superObject.getAnotherPrivateField()).isEqualTo(NEW_VALUE);
    }

    @Test(expected = ReflectionOperationException.class)
    public void cannotFindFieldInWholeHierarchy() {
        // given
        List<String> expectedClasses = asList(SecondLevelSomeConcreteObject.class.getCanonicalName(),
                                                     SomeConcreteObject.class.getCanonicalName(),
                                                     SuperObject.class.getCanonicalName(),
                                                     Object.class.getCanonicalName());
        try {
            SecondLevelSomeConcreteObject superObject = new SecondLevelSomeConcreteObject();
            // when
            setValueForField(superObject, "_some_field", NEW_VALUE);
            TestCase.fail();
            // then
        } catch(Exception ex) {
            assertThat(ex.getMessage()).isEqualTo("field '_some_field' not exist in classes: " + expectedClasses.toString());
            throw ex;
        }
    }

    @Test(expected = ReflectionOperationException.class)
    public void cannotSetFieldWithAnotherType() {
        // given
        try {
            SecondLevelSomeConcreteObject superObject = new SecondLevelSomeConcreteObject();
            // when
            setValueForField(superObject, "privateField", new Object());
            TestCase.fail();
            // then
        } catch(Exception ex) {
            assertThat(ex.getCause().getMessage()).isEqualTo("Can not set java.lang.String field pl.jalokim.utils.reflection.beans.SuperObject.privateField to java.lang.Object");
            throw ex;
        }
    }

    @Ignore
    @Test
    public void setPrivateFinalFieldValueForTheSameClassLikeTargetObject() {
        // given
        SuperObject superObject = new SuperObject();
        assertThat(superObject.getPrivateFinalField()).isEqualTo(PRIVATE_FINAL_FIELD_INIT);
        // when
        setValueForField(superObject, "privateFinalField", NEW_VALUE);
        // then
        assertThat(superObject.getPrivateFinalField()).isEqualTo(NEW_VALUE);
    }

    @Ignore
    @Test
    public void setPrivateStaticFinalFieldValueForTheSameClassLikeTargetObject() {
        // given
        assertThat(getValueOfStaticFinalField()).isEqualTo(STATIC_FINAL_STRING_VALUE);
        // when
        setValueForStaticField(SuperObject.class, "STATIC_FINAL_STRING", NEW_VALUE);
        // then
        assertThat(getValueOfStaticFinalField()).isEqualTo(NEW_VALUE);
    }

    @Test
    public void setPrivateFinalFieldValueForSuperClassForTargetObject() {
        // given

        // when
        // then
    }
}