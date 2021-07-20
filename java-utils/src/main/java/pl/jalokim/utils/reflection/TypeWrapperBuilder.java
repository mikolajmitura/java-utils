package pl.jalokim.utils.reflection;

import static java.util.Collections.singletonList;
import static pl.jalokim.utils.collection.Elements.elements;
import static pl.jalokim.utils.constants.Constants.EMPTY;
import static pl.jalokim.utils.constants.Constants.QUESTION_SIGN;
import static pl.jalokim.utils.reflection.ClassNameFixer.fixClassName;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getClassForName;
import static pl.jalokim.utils.reflection.MetadataReflectionUtils.getParametrizedRawTypes;
import static pl.jalokim.utils.reflection.TypeMetadata.NATIVE_OBJECT_META;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/**
 * Class for build TypeMetadata from class, type, field.
 */
final class TypeWrapperBuilder {

    private static final CharsParser CHARS_PARSER = new CharsParser();
    private static final String NATIVE_ARRAY_PATTERN = "(\\[)+L(.)+;";
    private static final String TYPE_NAME_OF_ARRAY_CLASS = ".*\\[]";

    private TypeWrapperBuilder() {

    }

    static TypeMetadata buildFromClass(Class<?> someClass) {
        if (someClass.isArray()) {
            return buildFromType(someClass);
        }

        List<Type> genericsTypes = getParametrizedRawTypes(someClass);

        if (genericsTypes.isEmpty()) {
            return new TypeMetadata(someClass, null);
        }

        return new TypeMetadata(someClass, elements(genericsTypes)
                .map(type -> NATIVE_OBJECT_META)
                .asList());
    }

    static TypeMetadata buildFromField(Field field) {
        if (field.getType().isPrimitive()) {
            return buildFromClass(field.getType());
        } else if (field.getType().isArray()) {
            return buildForArrayField(field);
        }
        return buildFromType(field.getGenericType());
    }

    static TypeMetadata buildForArrayField(Field arrayField) {
        return buildFromType(arrayField.getGenericType());
    }

    static TypeMetadata buildFromType(Type type) {
        return buildFromType(type, null, null);
    }

    static TypeMetadata buildFromType(Type type,
                                      Field originalField,
                                      TypeMetadata currentContext) {
        String fullName = type.getTypeName();
        char[] arrayOfChars = fullName.toCharArray();
        InnerTypeMetaData current = new InnerTypeMetaData(originalField, currentContext);
        for (char nextChar : arrayOfChars) {
            current = CHARS_PARSER.parse(nextChar, current);
        }
        return buildFromInnerTypeMetaData(current);
    }

    static TypeMetadata buildFromInnerTypeMetaData(InnerTypeMetaData typeWrapper) {
        String typeName = typeWrapper.getClassName();
        if (QUESTION_SIGN.equals(typeName)) {
            return new TypeMetadata(Object.class, null);
        } else if (typeName.matches("^\\?extends(.)+")) {
            typeName = typeName.replace("?extends", EMPTY);
        } else if (typeName.matches("^\\?super(.)+")) {
            return new TypeMetadata(Object.class, null);
        }
        if (hasArraySignature(typeName)) {
            return buildFromArrayClass(typeWrapper, typeName);
        }

        Class<?> realClass;
        try {
            realClass = getFixedClassName(typeName);
        } catch (ReflectionOperationException exception) {
            if (typeWrapper.getAvailableContext() != null) {
                Field originalField = typeWrapper.getOriginalField();
                Class<?> fieldOwner = originalField.getDeclaringClass();
                TypeMetadata availableContext = typeWrapper.getAvailableContext();
                return availableContext.getTypeMetadataForField(fieldOwner, typeName);
            }
            throw new UnresolvedRealClassException(exception);
        }
        return new TypeMetadata(realClass,
                                buildGenericsList(typeWrapper.getGenericTypes()));
    }

    private static TypeMetadata buildFromArrayClass(InnerTypeMetaData typeWrapper,
                                                    String rawCurrentClassName) {
        Class<?> rawClassForArray = getFixedClassName(rawCurrentClassName);
        String currentClassName = rawClassForArray.getTypeName();
        String typeOfStoredInArray = currentClassName.replaceAll("(\\[])$", EMPTY);

        TypeMetadata genericDataOfArray;
        if (hasArraySignature(typeOfStoredInArray)) {
            genericDataOfArray = buildFromArrayClass(typeWrapper, typeOfStoredInArray);
        } else {
            genericDataOfArray = new TypeMetadata(getFixedClassName(typeOfStoredInArray),
                                                  buildGenericsList(typeWrapper.getGenericTypes()));
        }
        return new TypeMetadata(rawClassForArray, singletonList(genericDataOfArray));
    }

    private static boolean hasArraySignature(String className) {
        return className.matches(TYPE_NAME_OF_ARRAY_CLASS) || className.matches(NATIVE_ARRAY_PATTERN);
    }

    static List<TypeMetadata> buildGenericsList(List<InnerTypeMetaData> generics) {
        return elements(generics)
                .map(TypeWrapperBuilder::buildFromInnerTypeMetaData)
                .filter(Objects::nonNull)
                .asList();
    }

    private static Class<?> getFixedClassName(String className) {
        return getClassForName(fixClassName(className));
    }

    @Data
    static class InnerTypeMetaData {
        private InnerTypeMetaData parent;
        @SuppressWarnings("PMD.AvoidStringBufferField")
        private final StringBuilder classNameBuilder = new StringBuilder();
        private final List<InnerTypeMetaData> genericTypes = new ArrayList<>();
        private final Field originalField;
        private final TypeMetadata availableContext;

        void addChild(InnerTypeMetaData innerTypeMetaData) {
            genericTypes.add(innerTypeMetaData);
        }

        void appendToClassName(char nextChar) {
            classNameBuilder.append(nextChar);
        }

        String getClassName() {
            return classNameBuilder.toString();
        }

        @Override
        public String toString() {
            String parentText = parent == null ? "null" : parent.getClassName();
            return "InnerTypeMetaData{"
                   + "parent=" + parentText
                   + ", className=" + getClassName()
                   + ", genericTypes=" + genericTypes
                   + '}';
        }
    }

    private static class CharsParser {

        private static final char START_GENERIC_CHAR = '<';
        private static final char COMMA_CHAR = ',';
        private static final char END_GENERIC_CHAR = '>';
        private static final char SPACE_CHAR = ' ';

        InnerTypeMetaData parse(char nextChar, InnerTypeMetaData currentMetadata) {
            if (nextChar == START_GENERIC_CHAR) {
                InnerTypeMetaData child = new InnerTypeMetaData(currentMetadata.getOriginalField(),
                                                                currentMetadata.getAvailableContext());
                currentMetadata.addChild(child);
                child.setParent(currentMetadata);
                return child;
            } else if (nextChar == COMMA_CHAR) {
                InnerTypeMetaData parent = currentMetadata.getParent();
                InnerTypeMetaData child = new InnerTypeMetaData(currentMetadata.getOriginalField(),
                                                                currentMetadata.getAvailableContext());
                parent.addChild(child);
                child.setParent(parent);
                return child;
            } else if (nextChar == END_GENERIC_CHAR) {
                return currentMetadata.getParent();
            } else if (nextChar == SPACE_CHAR) {
                return currentMetadata;
            } else {
                currentMetadata.appendToClassName(nextChar);
                return currentMetadata;
            }
        }
    }
}
