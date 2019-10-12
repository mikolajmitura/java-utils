package pl.jalokim.utils.reflection;

import junit.framework.TestCase;
import org.junit.Test;
import pl.jalokim.utils.reflection.beans.inheritiance.ClassWithoutDefConstr;
import pl.jalokim.utils.reflection.beans.inheritiance.SecondLevelSomeConcreteObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SomeConcreteObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SuperAbstractObject;
import pl.jalokim.utils.reflection.beans.inheritiance.SuperObject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.getValueForStaticField;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.getValueOfField;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.invokeMethod;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.invokeStaticMethod;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForField;
import static pl.jalokim.utils.reflection.InvokableReflectionUtils.setValueForStaticField;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getField;
import static pl.jalokim.utils.reflection.beans.inheritiance.SecondLevelSomeConcreteObject.CONCRETE_ANOTHER_PRIVATE_FIELD;
import static pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.PRIVATE_FIELD_INIT;
import static pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.getCAN_BE_UPDATE_STATIC_FINAL;
import static pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.getSTATIC_FINAL_INTEGER;
import static pl.jalokim.utils.reflection.beans.inheritiance.SuperObject.getSTATIC_FINAL_INTEGER2;
import static pl.jalokim.utils.test.ExpectedErrorUtilBuilder.when;

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
        Integer newValue = getValueForStaticField(SuperObject.class, "PRIMITIVE_STATIC_FINAL_INTEGER");
        assertThat(newValue).isEqualTo(NEW_NUMBER_VALUE);
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
        }).thenException(IllegalAccessException.class, "Class pl.jalokim.utils.reflection.InvokableReflectionUtilsTest can not access a member of class pl.jalokim.utils.reflection.beans.inheritiance.SuperObject with modifiers \"private static final\"");
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

    @Test
    public void invokeMethodFromTheSameClassWithVarargs() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = invokeMethod(instance, "returnResultOf", "_1", "_2");
        // then
        assertThat(result).isEqualTo("SecondLevelSomeConcreteObject" + "_1" + "_2");
    }

    @Test
    public void invokeMethodFromTheSameClassWithListArgs() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = invokeMethod(instance, "returnResultOf", Arrays.asList("_1", "_2"));
        // then
        assertThat(result).isEqualTo("SecondLevelSomeConcreteObject" + "_1" + "_2");
    }

    @Test
    public void invokeMethodFromTheSameClassWithListTypesAndArgList() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = invokeMethod(instance,
                                     "returnResultOf",
                                     Arrays.asList(String.class, Object.class),
                                     Arrays.asList("_1", 2));
        // then
        assertThat(result).isEqualTo("SecondLevelSomeConcreteObject" + "_1" + "2");
    }

    @Test
    public void invokeMethodFromSuperClassWithVarargs() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = invokeMethod(instance, SuperObject.class, "returnResultOf", "_1");
        // then
        assertThat(result).isEqualTo("SuperAbstractObject");
    }

    @Test
    public void invokeMethodFromSuperClassWithListArgs() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = invokeMethod(instance, SuperObject.class, "returnResultOf", singletonList("_1"));
        // then
        assertThat(result).isEqualTo("SuperAbstractObject");
    }

    @Test
    public void invokePrivateMethodFromSuperClassWithVarargs() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = invokeMethod(instance, SuperObject.class, "privateMethodString");
        // then
        assertThat(result).isEqualTo("abstractClass");
    }

    @Test
    public void invokePrivateMethodFromTheSameClassWithVarargs() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = invokeMethod(instance, "privateMethodString");
        // then
        assertThat(result).isEqualTo("concreteClass2ndLevel");
    }

    @Test
    public void invokePrivateMethodFromSuperClassWithExplicitArgTypes() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        Integer result = invokeMethod(instance, SuperObject.class,
                                      "returnIntegerVal",
                                      asList(String.class, Number.class),
                                      asList("text", 1L));
        // then
        assertThat(result).isEqualTo(10);
    }

    @Test
    public void cannotInvokeNonStaticMethodWithoutTargetInstance() {
        when(() -> invokeMethod(null, SuperObject.class,
                                "returnIntegerVal",
                                asList(String.class, Number.class),
                                asList("text", 1L)))
                .thenException(
                        new ReflectionOperationException("Cannot invoke non static method on null target object"));
        SuperAbstractObject.reset();
    }

    @Test
    public void invokePrivateStaticMethodFromSuperClassWithExplicitArgTypes() {
        // given
        Integer result = invokeStaticMethod(SuperObject.class,
                                            "incrementValue",
                                            singletonList(Number.class),
                                            singletonList(12L));
        // then
        assertThat(result).isEqualTo(1);
    }

    @Test
    public void invokePrivateStaticMethodFromSuperClassWithVarargs() {
        // given
        String new_value = "new_value";
        invokeStaticMethod(SuperObject.class,
                           "updateSTATIC_STRING",
                           singletonList(new_value));
        // then
        assertThat(SuperAbstractObject.STATIC_STRING).isEqualTo(new_value);
    }

    @Test
    public void invokePrivateStaticMethodFromSuperClassWithArgumentList() {
        // given
        String new_value = "new_value";
        invokeStaticMethod(SuperObject.class,
                           "updateSTATIC_STRING",
                           new_value);
        // then
        assertThat(SuperAbstractObject.STATIC_STRING).isEqualTo(new_value);
    }

    @Test
    public void getValueFromSuperClassForGivenTargetObjectForPrivateField() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = getValueOfField(instance, SomeConcreteObject.class, "privateString");
        // then
        assertThat(result).isEqualTo("private_String_SA");
    }

    @Test
    public void getValueFromInstanceForGivenTargetObjectForPrivateField() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = getValueOfField(instance, "privateString");
        // then
        assertThat(result).isEqualTo("private_String_2LEVEL");
    }

    @Test
    public void getValueFromSuperClassForGivenTargetObjectForPrivateFinalField() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = getValueOfField(instance, SomeConcreteObject.class, "finalString");
        // then
        assertThat(result).isEqualTo("FINAL_STRING_SA");
    }

    @Test
    public void getValueFromInstanceForGivenTargetObjectForPrivateFinalField() {
        // given
        SecondLevelSomeConcreteObject instance = new SecondLevelSomeConcreteObject();
        // when
        String result = getValueOfField(instance, "finalString");
        // then
        assertThat(result).isEqualTo("FINAL_STRING_2LEVEL");
    }

    @Test
    public void getStaticFinalValueFromFieldFromSuperClass() {
        // when
        Integer result = getValueForStaticField(SecondLevelSomeConcreteObject.class, "PRIMITIVE_STATIC_FINAL_INTEGER2");
        // then
        assertThat(result).isEqualTo(0);
    }

    @Test
    public void cannotFindNonStaticFieldWithoutTargetInstance() {
        when(() -> getValueForStaticField(SuperObject.class,
                                          "anotherPrivateField"))
                .thenException(
                        new ReflectionOperationException("Cannot find non static field on null target object"));
        SuperAbstractObject.reset();
    }

    @Test
    public void createInstanceWithoutArgs() {
        // when
        SuperObject superObject = InvokableReflectionUtils.newInstance(SuperObject.class);
        // then
        assertThat(superObject.getConstructorNr()).isEqualTo(0);
    }

    @Test
    public void createInstanceOnlyWithArrayArgs() {
        // given
        String var1 = "";
        Integer var2 = 1;
        // when
        SuperObject superObject = InvokableReflectionUtils.newInstance(SuperObject.class, var1, var2);
        // then
        assertThat(superObject.getConstructorNr()).isEqualTo(1);
    }

    @Test
    public void createInstanceOnlyWithListArgs() {
        // given
        String var1 = "";
        Integer var2 = 1;
        // when
        SuperObject superObject = InvokableReflectionUtils.newInstance(SuperObject.class, Arrays.asList(var1, var2));
        // then
        assertThat(superObject.getConstructorNr()).isEqualTo(1);
    }

    @Test
    public void createInstanceWithTypesListAndArrayArgs() {
        // given
        String var1 = "";
        Integer var2 = 1;
        // when
        SuperObject superObject = InvokableReflectionUtils.newInstance(SuperObject.class, Arrays.asList(String.class, Number.class), var1, var2);
        // then
        assertThat(superObject.getConstructorNr()).isEqualTo(2);
    }

    @Test
    public void createInstanceWithTypesListAndListArgs() {
        // given
        String var1 = "";
        Integer var2 = 1;
        // when
        SuperObject superObject = InvokableReflectionUtils.newInstance(SuperObject.class, Arrays.asList(String.class, Number.class), Arrays.asList(var1, var2));
        // then
        assertThat(superObject.getConstructorNr()).isEqualTo(2);
    }

    @Test
    public void cannotCreateObjectWithEmptyArguments() {
        when(() ->
                     InvokableReflectionUtils.newInstance(ClassWithoutDefConstr.class)
            ).thenException(ReflectionOperationException.class);
    }

    @Test
    public void invokePrivateConstructor() {
        // given
        Integer var1 = 1;
        // when
        SuperObject superObject = InvokableReflectionUtils.newInstance(SuperObject.class, var1);
        // then
        assertThat(superObject.getConstructorNr()).isEqualTo(3);
    }
}