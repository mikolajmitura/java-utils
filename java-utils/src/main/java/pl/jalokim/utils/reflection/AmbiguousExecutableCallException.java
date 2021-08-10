package pl.jalokim.utils.reflection;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;

public class AmbiguousExecutableCallException extends ReflectionOperationException {

    private static final long serialVersionUID = 1L;

    public AmbiguousExecutableCallException(List<Executable> foundExecutables) {
        super(String.format("Found more than one %s which match:%n%s", executableType(foundExecutables),
            elements(foundExecutables).concatWithNewLines()));
    }

    private static String executableType(List<Executable> foundExecutables) {
        Executable first = elements(foundExecutables).getFirst();
        if (first instanceof Method) {
            return "method";
        }
        return "constructor";
    }
}
