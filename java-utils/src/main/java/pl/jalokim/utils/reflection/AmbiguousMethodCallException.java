package pl.jalokim.utils.reflection;

import static pl.jalokim.utils.collection.Elements.elements;

import java.lang.reflect.Method;
import java.util.List;

public class AmbiguousMethodCallException extends ReflectionOperationException {

    private static final long serialVersionUID = 1L;

    public AmbiguousMethodCallException(List<Method> foundMethods) {
        super(String.format("Found more than one method which match:%n%s",
            elements(foundMethods).concatWithNewLines()));
    }
}
