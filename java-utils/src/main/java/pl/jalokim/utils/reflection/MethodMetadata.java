package pl.jalokim.utils.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MethodMetadata {

    Method method;
    String name;
    List<Annotation> annotations;
    TypeMetadata returnType;
    List<ParameterMetadata> parameters;
}
