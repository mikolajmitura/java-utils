package pl.jalokim.utils.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConstructorMetadata implements ExecutableMetadata<Constructor> {

    Constructor<?> constructor;
    List<Annotation> annotations;
    List<ParameterMetadata> parameters;
}
