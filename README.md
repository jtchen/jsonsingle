# JsonSingle

JsonSingle is a minimalistic JSON library containing only __ONE__ Java class.

## Features

- __Tiny__. The compressed jar is about 3.6 kB.

- __Minimalistic__. The library is just a single class without any dependencies. Object allocation is minimized and no regular expression is used.

- __Reliable__. JsonSingle passed all the tests in the comprehensive [JSON Parsing Test Suite](https://github.com/nst/JSONTestSuite).

## Usage

JsonSingle uses only a single JsonValue class to complete all the operations.

The `JsonValue` class represents an immutable JSON value, and provides methods to serialize or deserialize JSON values:

```java
String s = "[1,2,3]";
JsonValue v = JsonValue.parse(s);

System.out.println(v.toString()); // [1,2,3]
```

It is also simple to create a `JsonValue` object from ordinary Java objects:

```java
Map map = new HashMap();
map.put("a", 1.0);
JsonValue v = JsonValue.valueOf(map); // {"a":1.0}
```

The `JsonValue` class provides a method to select a value, using a [JSON Pointer](https://tools.ietf.org/html/rfc6901), at the referenced location of the JSON value. The selected value is also a `JsonValue` object and can be retrieved as a corresponding Java object, such as `String` or `Number`.

```java
JsonValue v = JsonValue.parse("[1.0,{\"a\":\"b\"}]");

String s = v.get("/1/a").asString(); // s.equals("b")
Number n = v.get("/0").asNumber(); // n.doubleValue() == 1.0
```

The mapping of JSON values to corresponding Java objects or interfaces is: JSON object to `Map`, JSON array to `List`, JSON string to `String`, and JSON number to `Number`.

JSON null and boolean values are defined as constants: `JsonValue.NULL`, `JsonValue.TRUE`, and `JsonValue.FALSE`.

The `JsonValue` class is written in a way that requires parameter correctness. If a correct Java object type or the well-formedness of a JSON text or a JSON Pointer is not guaranteed, it is necessary to catch a possible `IllegalArgumentException`.

```java
try {
	JsonValue.valueOf(new Object()); // cannot be a JSON value
	JsonValue.parse("[1 2]"); // a malformed JSON text
	JsonValue.parse("[1]").get("0"); // a malformed JSON Pointer
} catch (IllegalArgumentException e) {
	// some error handling codes
}
```

The arbitrary-precision numbers are supported by corresponding Java classes (such as `BigInteger` or `BigDecimal` objects) or deserializing their string representations.

```java
BigDecimal bd = BigDecimal.valueOf(Long.MAX_VALUE);
JsonValue v1 = JsonValue.valueOf(bd);
JsonValue v2 = JsonValue.parse("12345678901234567890");
```

For each `JsonValue` object, the value type of the object is queryable. The Java object type can be queried before retrieving the value to avoid a possible `ClassCastException`. Note that a JSON number is always mapped to the Java `Number` class, regardless of its actual type.

```java
JsonValue v1 = JsonValue.parse("\"a\""); // v1.getType() == String.class
JsonValue v2 = JsonValue.parse("0"); // v2.getType() == Number.class
```

## API Documentation

### *Class io.github.jtchen.jsonsingle.JsonValue*

- *static __NULL__*
  - JSON null value.
- *static __TRUE__*
  - JSON true value.
- *static __FALSE__*
  - JSON false value.
- *Class __getType__()*
  - Returns this JSON value type as a Class object. For a JSON null value, it returns null. For other JSON values, it returns one of the following Class objects: Boolean, Map, List, String, or Number. Note that the internal representation of a JSON number object is actually an instance of a subclass of Number.
- *static JsonValue __valueOf__(Object)*
  - Returns a JSON value which contains the value of the specified object. The specified object can be null, or be an instance of the following objects or interfaces: Boolean, Map, List, String, or Number, or a combination of them. An IllegalArgumentException is thrown if the value of the specified object cannot be contained in a JSON value.
- *JsonValue __get__(String)*
  - Get the value referenced by the provided JSON Pointer in this JSON value. It returns null if a non-existing value is referenced. An IllegalArgumentException is thrown if the JSON Pointer is malformed.
- *java.util.Map __asMap__()*
  - Returns this JSON value as a Map object.
- *java.util.List __asList__()*
  - Returns this JSON value as a List object.
- *String __asString__()*
  - Returns this JSON value as a String object.
- *Number __asNumber__()*
  - Returns this JSON value as a Number object. For arbitrary-precision large numbers, use asNumber().toString() to obtain their full representations.
- *int __intValue__()*
  - Returns this JSON value as an int. This is a convenience method for asNumber().intValue().
- *double __doubleValue__()*
  - Returns this JSON value as a double. This is a convenience method for asNumber().doubleValue().
- *static JsonValue __parse__(String)*
  - Parses the JSON text and constructs a JSON value represented by the JSON text. This method always creates BigDecimal objects because large numbers may exceed the limits of Java primitive types. An IllegalArgumentException is thrown if the JSON text is malformed.
- *String __toString__()*
  - Returns this JSON value as a JSON text.

## License

JsonSingle is released as __CAREWARE__. You can use and copy it at will, but you are encouraged to make a donation for needy children in your country or any part of the world.

Disclaimer: JsonSingle is distributed in the hope that it will be useful, but __WITHOUT ANY WARRANTY__.
