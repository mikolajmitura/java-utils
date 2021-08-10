package pl.jalokim.utils.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.List;

public interface ExecutableMetadata<T extends Executable> {

    List<Annotation> getAnnotations();

    List<ParameterMetadata> getParameters();
}
