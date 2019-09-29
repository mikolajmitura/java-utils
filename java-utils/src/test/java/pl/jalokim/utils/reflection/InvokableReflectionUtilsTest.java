package pl.jalokim.utils.reflection;

import junit.framework.TestCase;
import org.junit.Test;
import pl.jalokim.utils.reflection.beans.inheritiance.SecondLevelSomeConcreteObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SomeConcreteObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SuperAbstractObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SuperObject;

import java.lang.reflect.Field;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForField;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForStaticField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.beans.inheritiance.SecondLevelSomeConcreteObject.CONCRETE_ANOTHER_PRIVATE_FIELD;
import static pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.PRIVATE_FIELD_INIT;
import static pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.getCAN_BE_UPDATE_STATIC_FINAL;
import static pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.getSTATIC_FINAL_INTEGER;
import static pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.getSTATIC_FINAL_INTEGER2;
import static pl.jalokim.utils.test.ErrorProneTestUtil.ErrorProneTestUtilBuilder.when;

public class InvokableReflectionUtilsTest {

    private static final String NEW_VALUE = "new_VALUE";
    private static final int NEW_NUMBER_VALUE = 12;

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
                                              SuperAbstractObject.class.getCanonicalName(),
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
            assertThat(ex.getCause().getMessage()).isEqualTo("Can not set java.lang.String field pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.privateField to java.lang.Object");
            throw ex;
        }
    }

    @Test
    public void setPrivateFinalFieldValueForTheSameClassLikeTargetObject() {
        // given
        SuperObject superObject = new SuperObject();
        assertThat(superObject.getPrivateFinalField()).isEqualTo(1);
        // when
        setValueForField(superObject, "privateFinalField", NEW_NUMBER_VALUE);
        // then
        assertThat(superObject.getPrivateFinalField()).isEqualTo(NEW_NUMBER_VALUE);
    }

    @Test
    public void setPrivateStaticFinalFieldValueForTheSameClassLikeTargetObject() {
        // given
        assertThat(getSTATIC_FINAL_INTEGER()).isEqualTo(0);
        // when
        setValueForStaticField(SuperObject.class, "STATIC_FINAL_INTEGER", NEW_NUMBER_VALUE);
        // then
        assertThat(getSTATIC_FINAL_INTEGER()).isEqualTo(NEW_NUMBER_VALUE);
    }

    @Test
    public void setPrivateStaticFinalFieldValueForTheSameClassLikeTargetObject2() {
        // given
        assertThat(getCAN_BE_UPDATE_STATIC_FINAL()).isEqualTo("0");
        // when
        String newValue = "test";
        setValueForStaticField(SuperObject.class, "CAN_BE_UPDATE_STATIC_FINAL", newValue);
        // then
        assertThat(getCAN_BE_UPDATE_STATIC_FINAL()).isEqualTo(newValue);
        setValueForStaticField(SuperObject.class, "CAN_BE_UPDATE_STATIC_FINAL", "0");
    }

    @Test
    public void cannotUpdateValueForFinalPrimitiveField() {
        // given
        SuperObject superObject = new SuperObject();
        assertThat(superObject.getPrivateFinalField()).isEqualTo(1);
        // when
        setValueForField(superObject, "primitiveIntFinalField", NEW_NUMBER_VALUE);
        // then
        assertThat(superObject.getPrivateFinalField()).isEqualTo(1);
    }

    @Test
    public void cannotUpdateValueForFinalStaticPrimitiveField() {
        // given
        assertThat(getSTATIC_FINAL_INTEGER()).isEqualTo(0);
        // when
        setValueForStaticField(SuperObject.class, "PRIMITIVE_STATIC_FINAL_INTEGER", NEW_NUMBER_VALUE);
        // then
        assertThat(getSTATIC_FINAL_INTEGER()).isEqualTo(0);
    }

    @Test
    public void cantChangeValueAfterUpdateValueSetAccessibleIsNecessaryOnceAgain() throws Exception {
        // given
        assertThat(getSTATIC_FINAL_INTEGER2()).isEqualTo(2);
        setValueForStaticField(SuperObject.class, "STATIC_FINAL_INTEGER2", NEW_NUMBER_VALUE);
        assertThat(getSTATIC_FINAL_INTEGER2()).isEqualTo(NEW_NUMBER_VALUE);
        // when
        when(() -> {
            Field foundField = getField(SuperObject.class, "STATIC_FINAL_INTEGER2");
            foundField.set(null, 123);
        }).thenExpectedException(IllegalAccessException.class, "Class pl.jalokim.utils.reflection.InvokableReflectionUtilsTest can not access a member of class pl.jalokim.utils.reflection.beans.inheritiance.SuperObject with modifiers \"private static final\"");
    }

    @Test
    public void setPrivateFinalFieldValueForSuperClassForTargetObject() {
        // given
        assertThat(getCAN_BE_UPDATE_STATIC_FINAL()).isEqualTo("0");
        // when
        String newValue = "test";
        setValueForStaticField(SecondLevelSomeConcreteObject.class, "CAN_BE_UPDATE_STATIC_FINAL", newValue);
        // then
        assertThat(getCAN_BE_UPDATE_STATIC_FINAL()).isEqualTo(newValue);
        setValueForStaticField(SuperObject.class, "CAN_BE_UPDATE_STATIC_FINAL", "0");
    }
}