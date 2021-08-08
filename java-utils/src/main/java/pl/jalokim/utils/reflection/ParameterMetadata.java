package pl.jalokim.utils.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ParameterMetadata {

    List<Annotation> annotations;
    String name;
    Parameter parameter;
    TypeMetadata typeOfParameter;
}
