Java Utils
---------------------------------------------
This project contains some utils code for java for: 
- collection 
- file
- random
- reflection
- string
- override placeholders in file with template (override some variables)  

<h3>Maven dependency</h3>

```xml
    <dependency>
        <groupId>pl.jalokim.utils</groupId>
        <artifactId>java-utils</artifactId>
        <version>1.0.0</version>
    </dependency>
```

<h3>Util methods</h3>

 1) <h5>collection package</h5>
 
    - <h6>CollectionUtils class</h6>
         
        Contains methods for get last index from list or array or checks that is last index...
         
        ```java
           public static boolean isLastIndex(List<?> list, int index);
           
           public static boolean isLastIndex(Object[] array, int index);
           
           public static int getLastIndex(Collection<?> list);
           
           public static int getLastIndex(Object[] array);   
                                                     
        ```
    
        It contains methods for example like below (for Set, List, Array):
        
        ```java
            public static <T, R> Set<R> filterToSet(Collection<T> collection, Predicate<T> filter, Function<T, R> mapFunc)       
        ```
        
        For example you have some List<String> and want filter that list and map to Integer type and as result you want have Set.  
        ```java
            List<String> texts = Arrays.asList("134", "21", "3");
            
            // old way
            Set<Integer> numbers0 = texts.stream()
                                                .filter(text -> text.length() <= 2)
                                                .map(Integer::valueOf)
                                                .collect(Collectors.toSet());
                    
            // with java utils
            Set<Integer> numbers1 = CollectionUtils.filterToSet(texts, text -> text.length() <= 2, Integer::valueOf);
        ```
        
        Or when you want only filter list and return new Set...
        ```java
        List<String> texts = Arrays.asList("134", "21", "3");
            
        // old way
        Set<String> numbers0 = texts.stream()
                                    .filter(text -> text.length() <= 2)
                                    .collect(Collectors.toSet());
                    
        // with java utils
        Set<String> numbers1 = CollectionUtils.filterToSet(texts, text -> text.length() <= 2);
        ```
        
        Or when you want only map list and return new Set... then method name start with map
        ```java
            List<String> texts = Arrays.asList("134", "21", "3");
            
            // old way
            Set<Integer> numbers0 = texts.stream()
                                                .map(Integer::valueOf)
                                                .collect(Collectors.toSet());
                    
            // with java utils
            Set<Integer> numbers1 = CollectionUtils.mapToSet(texts, Integer::valueOf);
        ```
        
        When method starts with filter then as next argument can have map function. 
        And can start with map and can have as next argument have filter predicate...
        
        More for map, filter in class pl.jalokim.utils.collection.CollectionUtils
        
        more methods:
        ```java
            // it returns new source collection but sorted
            public static <T extends Comparable<? super T>> List<T> sortAsNewList(Collection<T> list) 
            
            // it returns new list but with swapped given element at given index. 
            public static <T> List<T> swapElementsAsNewList(List<T> sourceList, int indexToSwap, T newValue)
            
            public static <T> T getFirst(List<T> list)
            
            public static <T> T getLast(List<T> list)
            
            public static <T> boolean addWhenNotExist(List<T> list, T element)
            
            // it verifies that two collections have the same elements
            // order is not important
            public static <T extends Comparable<? super T>> boolean hasTheSameElements(Collection<T> first, Collection<T> second)
            
            public static <T> boolean isEmpty(List<T> list)
            
            public static <T> boolean isNotEmpty(List<T> list)
            
            // it returns common part of two source collections. 
            public static <E> List<E> intersection(Collection<E> first, Collection<E> second)
        ```
    
    - <h6>Elements class (this is simple wrapper for java stream api)</h6>
    
        ```java
            List<Integer> numbers = Arrays.asList(1, 2, 4, 5, 10, 20);
                    
            Set<String> numberAsText = Elements.elements(numbers) // can crete elements from Iterable<T>, Array and Stream<T>
                            .filter(number -> number > 9)
                            .map(Object::toString)
                            .asSet(); // it can return List, or Array or Stream<T>                     
        ```
 2) <h5>file package</h5>
 
    <h6>FileCursor class implements Iterator<String>, you can read line by line</h6>
    
    ```java
        FileCursor fileCursor = new FileCursor("/path/to/file");
        Long lineNumber = fileCursor.getLineNumber();
        String currentLine1 = fileCursor.getCurrentLine();// it can be invoke many times and will always returns the same value
        String next = fileCursor.next(); // it get next line from file. 
        boolean hasNext = fileCursor.hasNext(); // verify that have next line
    ```
        
    <h6>FileUtils class</h6>
      - Simply you can read from file to String, to List or to Consumer<String> without problematic checked exception.
      below example for read from file to String
      
      ```java
        String fileContent = FileUtils.loadFileFromPathAsText("path/to/file");
      ```
      
      - simple you can write String or List<String> to file or append to existed file.
      
      ```java
      public static void writeToFile(String filePath, String fileContent);

      public static void appendToFile(String filePath, String fileContent)
      ```
      
3) <h5>random package</h5> 
    You can get random number, random element from list, random index for array or list. Random true. 
    code examples:
    
    ```java
    String[] array = {"1", "2", "123", "34"};
    List<String> list = Arrays.asList("1", "2", "123", "34");
    
    int randomNumber = RandomUtils.randomInRange(0, 10);
    int randomIndexFromArray = RandomUtils.randomIndex(array);
    int randomIndexFromList = RandomUtils.randomIndex(list);
    
    String randomElemFromArrary = RandomUtils.randomElement(array);
    String randomElemFromList = RandomUtils.randomElement(list);
    
    boolean randomTrue = RandomUtils.randomTrue();
    
    boolean randomTrueWithPercengate = RandomUtils.randomTrue(30);
    ```
    
    Under the hood random is provided by java.security.SecureRandom class
    
4) <h5>reflection package</h5>

   - InvokableReflectionUtils class
     - You can set new value for field by, it will change field for super class when necessary
     
       ```java
       InvokableReflectionUtils.setValueForField(targetObject, "fieldName", newValue);
       // for static fields
       InvokableReflectionUtils.setValueForStaticField(targetClass, "fieldName", newValue);
       ```
     
     - you can invoke Method and get results, it will change field for super class when necessary
     
       ```java
       T result = InvokableReflectionUtils.invokeMethod(targetObject, "methodName", args)
       // for static method
       T result = InvokableReflectionUtils.invokeStaticMethod(targetClass, "methodName", args)
       ```
        
     - you can create new instance
     
        ```java
            T result = InvokableReflectionUtils.newInstance(targetClass, args)
        ```     
         
   - MetadataReflectionUtils class
     - can get java.lang.reflect.Field (you can provide some concrete class, it can gets Field from super class)
     - can get java.lang.reflect.Method (you can provide some concrete class, it can gets Method from super class)
     - can get Parametrized Type from some object by index
     - other utils methods for verify type of field or class:    
        - public static boolean isSimpleType(Field field)
        - public static boolean isSimpleType(Class<?> someClass)
        - public static boolean isNumberType(Field field)
        - public static boolean isNumberType(Class<?> someClass)
        - public static boolean isTextType(Field field)
        - public static boolean isTextType(Class<?> someClass)
        - public static boolean isEnumType(Field field)
        - public static boolean isEnumType(Class<?> someClass)
        - public static boolean isMapType(Field field)
        - public static boolean isMapType(Class<?> someClass)
        - public static boolean isCollectionType(Field field)
        - public static boolean isCollectionType(Class<?> someClass)
        - public static boolean isHavingElementsType(Field field) // is array or collection
        - public static boolean isHavingElementsType(Class<?> someClass) // is array or collection
        - public static boolean isArrayType(Field field)
        - public static boolean isArrayType(Class<?> someClass)
     - can get all child classes from provided package. (It uses super https://github.com/ronmamo/reflections project)
     
        ```java
           getAllChildClassesForClass(yourSuperClass, "some.package", withAbstractClasses)
           // withAbstractClasses pass true when you want get as result some abstract classes
           
           // for example
           Set<Class<? extends Number>> allChildClassesForClass = getAllChildClassesForClass(Number.class, "java.lang", true);
           // returns [class java.lang.Byte, class java.lang.Double, class java.lang.Long, class java.lang.Float, class java.lang.Short, class java.lang.Integer]
        ```
        
5) <h5>string package (StringUtils class)</h5>
        - it can verify that text is empty or blank (Yes, the same like apache commons etc)        
        - can repeat some text N times by repeatTextNTimes(int nTimes, String text) method        
        - concatenate text with joiner with function, mapper etc

5) <h5>template package</h5>

    when file contains below text:
            
    ```bash
    some text ${var1} 
    next line: ${var1}     
    ${var2}_text
    ```        
    then you can override those placeholders from file by TemplateAsText class
            
    ```java            
    TemplateAsText templateAsText = TemplateAsText.fromFile("path/to/file/with/placeholders");
    templateAsText.overrideVariable("var1", "someText");
    templateAsText.overrideVariable("var2", "someText_2");
    String currentTemplateText = templateAsText.getCurrentTemplateText(); // will return text from file but with resolved placeholders
    ```        
        