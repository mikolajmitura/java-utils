package pl.jalokim.utils.reflection

import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getTypeMetadataFromClass

import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings("EmptyClass")
class TypeMetadataSpockTest extends Specification {

    @Unroll
    def "returns expected generic types for certain parent class"() {
        given:
        def typeMetadata = getTypeMetadataFromClass(currentType)

        when:
        def foundParentMeta = typeMetadata.getTypeMetaDataForParentClass(parentClass)

        then:
        foundParentMeta.genericTypes[0].rawType == expectedRawClasses
        foundParentMeta.genericTypes[0].genericTypes*.rawType == expectedRawGenericTypes

        where:
        expectedRawClasses | expectedRawGenericTypes | currentType              | parentClass
        List               | [String]                | ConcreteClassSecondLevel | SuperParent
        List               | [String]                | ConcreteClassSecondLevel | SuperInterface
        List               | [String]                | ConcreteClassThirdLevel  | SuperInterface
        List               | [String]                | ConcreteClassThirdLevel  | SuperParent
        List               | [String]                | MixedParents             | SuperParent
        Map                | [String, Long]          | MixedParents             | SuperInterface
        Long               | []                      | SuperInterfaceImpl2      | SuperInterface
        Long               | []                      | SuperInterfaceImpl       | SuperInterface
    }

    def "cannot find parent class"() {
        given:
        def typeMetadata = getTypeMetadataFromClass(ConcreteClassSecondLevel)

        when:
        typeMetadata.getTypeMetaDataForParentClass(ConcreteClassThirdLevel)

        then:
        NoSuchElementException ex = thrown()
        ex.message ==
            "Cannot find metamodel for class: $ConcreteClassThirdLevel.canonicalName as parent in context of class: $ConcreteClassSecondLevel.canonicalName"
    }

    static class SuperParent<T> {

    }

    static interface SuperInterface<T> {

    }

    static class ConcreteClassFirstLevel<D> extends SuperParent<D> implements SuperInterface<D> {

    }

    static class ConcreteClassSecondLevel extends ConcreteClassFirstLevel<List<String>> {

    }

    static class ConcreteClassThirdLevel extends ConcreteClassSecondLevel {

    }

    static class MixedParents extends ConcreteClassThirdLevel implements SuperInterface<Map<String, Long>> {

    }

    static class SuperInterfaceImpl implements SuperInterface<Long> {

    }

    static class SuperInterfaceImpl2 extends SuperInterfaceImpl {

    }
}
