# JsonSingle

JsonSingle is a minimalistic JSON library containing only **ONE** Java class.

## Features

- Tiny. The compressed jar is about 3.3 kB (without the manifest file).

- Minimalistic. The library is just a single class without any dependencies. Object allocation is minimized and no regular expression is used.

- Reliable. JsonSingle passed all the tests in the comprehensive [JSON Parsing Test Suite](https://github.com/nst/JSONTestSuite).

## Usage

JsonSingle uses only a single JsonValue class to complete all the operations.

The JsonValue class represents an immutable JSON value and provides methods to serialize or deserialize JSON values. It also provides a method to create a JsonValue object from ordinary Java objects. Here are some examples:

```java
/* creates a JsonValue object from Java objects */
Map map = new HashMap();
map.put("a", 1.0);
JsonValue v1 = JsonValue.valueOf(map);

/* or creates a JsonValue object by deserializing a string */
String s = "{\"a\":1.0}";
JsonValue v2 = JsonValue.parse(s);

/* serializes a JsonValue object */
System.out.println(s.equals(v1.toString())); // true
System.out.println(s.equals(v2.toString())); // true
```

The JsonValue class provides a method to select a value, using a [JSON Pointer](https://tools.ietf.org/html/rfc6901), at the referenced location of the JSON value. The selected value is also a JsonValue object and can be retrieved as a corresponding Java object, such as String or Number.

```java
Map map = new HashMap();
map.put("a", "b");
List list = new ArrayList();
list.add(map);
list.add(1.0);

JsonValue v = JsonValue.valueOf(list);
String s = v.get("/0/a").asString(); // using a JSON Pointer
System.out.println(s.equals("b")); // true
Number n = v.get("/1").asNumber();
System.out.println(n.doubleValue() == 1.0); // true
```

The mapping of JSON values to corresponding Java objects or interfaces is: JSON object to Map, JSON array to List, JSON string to String, and JSON number to Number. JSON null and boolean values are defined as constants: JsonValue.NULL, JsonValue.TRUE, and JsonValue.FALSE.

The JsonValue class is written in a way that requires parameter correctness. If a correct Java object type or the well-formedness of a JSON text or a JSON Pointer is not guaranteed, it is necessary to catch a possible IllegalArgumentException.

```java
try {
	JsonValue.valueOf(new Object()); // cannot be a JSON value
	JsonValue.parse("[1 2]"); // a malformed JSON text
	JsonValue.parse("[1]").get("0"); // a malformed JSON Pointer
} catch (IllegalArgumentException e) {
	// some error handling codes
}
```

The arbitrary-precision numbers are supported by corresponding Java classes (such as BigInteger or BigDecimal objects) or deserializing their string representations.

```java
/* creates a large number from a BigDecimal object */
BigDecimal bd = BigDecimal.valueOf(Long.MAX_VALUE);
JsonValue v1 = JsonValue.valueOf(bd.add(bd));
String s = v1.asNumber().toString();
System.out.println(s.equals("18446744073709551614")); // true

/* or creates a large number by deserializing a string */
JsonValue v2 = JsonValue.parse("18446744073709551614");
System.out.println(v2.asNumber().toString().equals(s)); // true
```

For each JsonValue object, the value type of the object is queryable. The Java object type can be queried before retrieving the value to avoid a possible ClassCastException. Note that a JSON number is always mapped to the Java Number class, regardless of its actual type (which is always a subclass of Number).

```java
JsonValue v1 = JsonValue.parse("\"a\"");
if (v1.getType() == String.class) {
	System.out.println(v1.asString().equals("a")); // true
}
JsonValue v2 = JsonValue.parse("18446744073709551614"); // a BigDecimal
System.out.println(v2.getType() == Number.class); // true
```

## API Documentation

### *Class io.github.jtchen.jsonsingle.JsonValue*

- *static NULL*
  - JSON null value.
- *static TRUE*
  - JSON true value.
- *static FALSE*
  - JSON false value.
- *Class getType()*
  - Returns this JSON value type as a Class object. For a JSON null value, it returns null. For other JSON values, it returns one of the following Class objects: Boolean, Map, List, String, or Number. Note that the internal representation of a JSON number object is actually an instance of a subclass of Number.
- *static JsonValue valueOf(Object)*
  - Returns a JSON value which contains the value of the specified object. The specified object can be null, or be an instance of the following objects or interfaces: Boolean, Map, List, String, or Number, or a combination of them. An IllegalArgumentException is thrown if the value of the specified object cannot be contained in a JSON value.
- *JsonValue get(String)*
  - Get the value referenced by the provided JSON Pointer in this JSON value. It returns null if a non-existing value is referenced. An IllegalArgumentException is thrown if the JSON Pointer is malformed.
- *java.util.Map asMap()*
  - Returns this JSON value as a Map object.
- *java.util.List asList()*
  - Returns this JSON value as a List object.
- *String asString()*
  - Returns this JSON value as a String object.
- *Number asNumber()*
  - Returns this JSON value as a Number object. For arbitrary-precision large numbers, use asNumber().toString() to obtain their full representations.
- *int intValue()*
  - Returns this JSON value as an int. This is a convenience method for asNumber().intValue().
- *double doubleValue()*
  - Returns this JSON value as a double. This is a convenience method for asNumber().doubleValue().
- *static JsonValue parse(String)*
  - Parses the JSON text and constructs a JSON value represented by the JSON text. This method always creates BigDecimal objects because large numbers may exceed the limits of Java primitive types. An IllegalArgumentException is thrown if the JSON text is malformed.
- *String toString()*
  - Returns this JSON value as a JSON text.

## License

JsonSingle is released as CAREWARE. You can use and copy it at will, but you are encouraged to make a donation for needy children in your country or any part of the world.

Disclaimer: JsonSingle is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
