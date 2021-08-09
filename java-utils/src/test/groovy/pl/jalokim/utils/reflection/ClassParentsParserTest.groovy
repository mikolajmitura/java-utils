package pl.jalokim.utils.reflection

import pl.jalokim.utils.reflection.beans.inheritiance.ExampleClass
import spock.lang.Specification

class ClassParentsParserTest extends Specification {

    def "return expected levels of class hierarchy difference"() {
        when:
        def parentsInfo = ClassParentsParser.parentsInfoFromClass(ExampleClass.TupleClassImpl)
        then:
        parentsInfo.canBeCastTo(ExampleClass.TupleClassImpl)
        parentsInfo.canBeCastTo(ExampleClass.TupleClass)
        parentsInfo.canBeCastTo(ExampleClass.RawTuple)
        parentsInfo.canBeCastTo(ExampleClass.GenericInterface)
        parentsInfo.canBeCastTo(ExampleClass.OtherInterface)
        parentsInfo.canBeCastTo(Object)
        !parentsInfo.canBeCastTo(String)

        parentsInfo.getHierarchyDiffLength(ExampleClass.TupleClassImpl) == 0
        parentsInfo.getHierarchyDiffLength(ExampleClass.TupleClass) == 1
        parentsInfo.getHierarchyDiffLength(ExampleClass.RawTuple) == 2
        parentsInfo.getHierarchyDiffLength(ExampleClass.GenericInterface) == 2
        parentsInfo.getHierarchyDiffLength(ExampleClass.GenericInterface) == 2
        parentsInfo.getHierarchyDiffLength(Object) == 3
    }
}
