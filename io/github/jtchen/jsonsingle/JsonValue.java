/*
 *	This file is part of JsonSingle. It is distributed WITHOUT ANY WARRANTY.
 *	Details can be found on <https://github.com/jtchen/jsonsingle>.
 */
package io.github.jtchen.jsonsingle;

import java.util.*;
import java.text.*;
import java.math.*;

/**
 *	<p>
 *	The {@code JsonValue} class represents an immutable JSON value and provides
 *	methods to serialize or deserialize JSON values. It also provides a method
 *	to create a {@code JsonValue} object from ordinary Java objects. Here are
 *	some examples:
 *
 *	<pre>
 *	/&#42; creates a JsonValue object from Java objects &#42;/
 *	Map map = new HashMap();
 *	map.put("a", 1.0);
 *	JsonValue v1 = JsonValue.valueOf(map);
 *
 *	/&#42; or creates a JsonValue object by deserializing a string &#42;/
 *	String s = "{\"a\":1.0}";
 *	JsonValue v2 = JsonValue.parse(s);
 *
 *	/&#42; serializes a JsonValue object &#42;/
 *	System.out.println(s.equals(v1.toString())); // true
 *	System.out.println(s.equals(v2.toString())); // true</pre>
 *
 *	<p>
 *	The {@code JsonValue} class provides a method to select a value, using a
 *	JSON Pointer, at the referenced location of the JSON value. The selected
 *	value is also a {@code JsonValue} object and can be retrieved as a
 *	corresponding Java object, such as {@code String} or {@code Number}.
 *
 *	<pre>
 *	Map map = new HashMap();
 *	map.put("a", "b");
 *	List list = new ArrayList();
 *	list.add(map);
 *	list.add(1.0);
 *
 *	JsonValue v = JsonValue.valueOf(list);
 *	String s = v.get("/0/a").asString(); // using a JSON Pointer
 *	System.out.println(s.equals("b")); // true
 *	Number n = v.get("/1").asNumber();
 *	System.out.println(n.doubleValue() == 1.0); // true</pre>
 *
 *	<p>
 *	The mapping of JSON values to corresponding Java objects or interfaces is:
 *	JSON object to {@code Map}, JSON array to {@code List}, JSON string to
 *	{@code String}, and JSON number to {@code Number}. JSON null and boolean
 *	values are defined as constants: {@code JsonValue.NULL}, {@code
 *	JsonValue.TRUE}, and {@code JsonValue.FALSE}.
 *
 *	<p>
 *	The {@code JsonValue} class is written in a way that requires parameter
 *	correctness. If a correct Java object type or the well-formedness of a JSON
 *	text or a JSON Pointer is not guaranteed, it is necessary to catch a
 *	possible {@code IllegalArgumentException}.
 *
 *	<pre>
 *	try {
 *		JsonValue.valueOf(new Object()); // cannot be a JSON value
 *		JsonValue.parse("[1 2]"); // a malformed JSON text
 *		JsonValue.parse("[1]").get("0"); // a malformed JSON Pointer
 *	} catch (IllegalArgumentException e) {
 *		// some error handling codes
 *	}</pre>
 *
 *	<p>
 *	The arbitrary-precision numbers are supported by corresponding Java classes
 *	(such as {@code BigInteger} or {@code BigDecimal} objects) or deserializing
 *	their string representations.
 *
 *	<pre>
 *	/&#42; creates a large number from a BigDecimal object &#42;/
 *	BigDecimal bd = BigDecimal.valueOf(Long.MAX_VALUE);
 *	JsonValue v1 = JsonValue.valueOf(bd.add(bd));
 *	String s = v1.asNumber().toString();
 *	System.out.println(s.equals("18446744073709551614")); // true
 *
 *	/&#42; or creates a large number by deserializing a string &#42;/
 *	JsonValue v2 = JsonValue.parse("18446744073709551614");
 *	System.out.println(v2.asNumber().toString().equals(s)); // true</pre>
 *
 *	<p>
 *	For each {@code JsonValue} object, the value type of the object is
 *	queryable. The Java object type can be queried before retrieving the value
 *	to avoid a possible {@code ClassCastException}. Note that a JSON number is
 *	always mapped to the Java {@code Number} class, regardless of its actual
 *	type (which is always a subclass of {@code Number}).
 *
 *	<pre>
 *	JsonValue v1 = JsonValue.parse("\"a\"");
 *	if (v1.getType() == String.class) {
 *		System.out.println(v1.asString().equals("a")); // true
 *	}
 *	JsonValue v2 = JsonValue.parse("18446744073709551614"); // a BigDecimal
 *	System.out.println(v2.getType() == Number.class); // true</pre>
 *
 *	@author Jian-Ting Chen
 *	@version 1.0b1 (2020-07-11)
 */
public class JsonValue {

	/** JSON null value. */
	public static final JsonValue NULL = new JsonValue(null);

	/** JSON true value. */
	public static final JsonValue TRUE = new JsonValue(Boolean.TRUE);

	/** JSON false value. */
	public static final JsonValue FALSE = new JsonValue(Boolean.FALSE);

	private Object value = null;

	private JsonValue(Object obj) {
		value = obj;
	}

	private static JsonValue create(Object obj) {
		if (obj == null) {
			return NULL;
		}
		if (obj instanceof Boolean) {
			if (obj.equals(Boolean.TRUE)) {
				return TRUE;
			}
			return FALSE;
		}
		if ((obj instanceof Map) || (obj instanceof List)
				|| (obj instanceof String) || (obj instanceof Number)) {
			return new JsonValue(obj);
		}
		throw new IllegalArgumentException();
	}

	/**
	 *	Returns this JSON value type as a {@code Class} object. For a JSON null
	 *	value, it returns {@code null}. For other JSON values, it returns one
	 *	of the following {@code Class} objects: {@code Boolean}, {@code Map},
	 *	{@code List}, {@code String}, or {@code Number}. Note that the internal
	 *	representation of a JSON number object is actually an instance of a
	 *	subclass of {@code Number}.
	 *
	 *	@return a {@code Class} representation of the JSON value type, or
	 *	{@code null} for JSON null value
	 */
	public Class getType() {
		if (value == null) {
			return null;
		}
		if (value instanceof Map) {
			return Map.class;
		}
		if (value instanceof List) {
			return List.class;
		}
		if (value instanceof Number) {
			return Number.class;
		}
		return value.getClass();
	}

	/**
	 *	Returns a JSON value which contains the value of the specified object.
	 *	The specified object can be {@code null}, or be an instance of the
	 *	following objects or interfaces: {@code Boolean}, {@code Map}, {@code
	 *	List}, {@code String}, or {@code Number}, or a combination of them. An
	 *	{@code IllegalArgumentException} is thrown if the value of the
	 *	specified object cannot be contained in a JSON value.
	 *
	 *	@param obj the object whose value is to be contained in a JSON value
	 *	@return a JSON value contains the value of the specified object
	 *	@throws IllegalArgumentException if the value of the specified object
	 *	cannot be contained in a JSON value
	 */
	public static JsonValue valueOf(Object obj) {
		if (obj instanceof Map) {
			Map<String, JsonValue> map = new HashMap<String, JsonValue>();
			for (Object o : ((Map) obj).entrySet()) {
				Map.Entry<?, ?> e = (Map.Entry) o;
				map.put((String) e.getKey(), JsonValue.valueOf(e.getValue()));
			}
			return JsonValue.create(Collections.unmodifiableMap(map));
		}
		if (obj instanceof List) {
			List<JsonValue> list = new ArrayList<JsonValue>();
			for (Object e : (List) obj) {
				list.add(JsonValue.valueOf(e));
			}
			return JsonValue.create(Collections.unmodifiableList(list));
		}
		return JsonValue.create(obj);
	}

	/**
	 *	Get the value referenced by the provided JSON Pointer in this JSON
	 *	value. It returns {@code null} if a non-existing value is referenced.
	 *	An {@code IllegalArgumentException} is thrown if the JSON Pointer is
	 *	malformed.
	 *
	 *	@param pointer the string containing the JSON Pointer
	 *	@return the {@code JsonValue} object at the referenced location, or
	 *	{@code null} if a non-existing value is referenced
	 *	@throws IllegalArgumentException if the JSON Pointer is malformed
	 */
	public JsonValue get(String pointer) {
		JsonValue v = this;
		if (pointer.length() > 0) {
			if (pointer.charAt(0) != '/') {
				throw new IllegalArgumentException();
			}
			int begin = 0;
			for (int i = 1; i < pointer.length(); i += 1) {
				if ((pointer.charAt(i) == '/') && (i > begin)) {
					v = get(v, pointer.substring(begin + 1, i));
					begin = i; // pointer.charAt(begin) always equals '/'
				}
			}
			v = get(v, pointer.substring(begin + 1));
		}
		return v;
	}

	private JsonValue get(JsonValue v, String s) {
		if (v != null) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < s.length(); i += 1) {
				char c = s.charAt(i);
				if (c == '~') {
					i += 1;
					if (i < s.length()) {
						c = s.charAt(i);
						if (c == '0') {
							sb.append('~');
							continue;
						} else if (c == '1') {
							sb.append('/');
							continue;
						}
					}
					throw new IllegalArgumentException();
				}
				sb.append(c);
			}

			if (v.value instanceof Map) {
				return (JsonValue) ((Map) v.value).get(sb.toString());
			}
			if ((v.value instanceof List) && (sb.length() > 0)) {
				List list = (List) v.value;
				int i = Integer.parseInt(sb.toString());
				if ((i >= 0) && (i < list.size())) {
					return (JsonValue) list.get(i);
				}
			}
		}
		return null;
	}

	/**
	 *	Returns this JSON value as a {@code Map} object.
	 *
	 *	@return a {@code Map} representation of the JSON value
	 */
	public Map asMap() {
		return (Map) value;
	}

	/**
	 *	Returns this JSON value as a {@code List} object.
	 *
	 *	@return a {@code List} representation of the JSON value
	 */
	public List asList() {
		return (List) value;
	}

	/**
	 *	Returns this JSON value as a {@code String} object.
	 *
	 *	@return a {@code String} representation of the JSON value
	 */
	public String asString() {
		return (String) value;
	}

	/**
	 *	Returns this JSON value as a {@code Number} object. For
	 *	arbitrary-precision large numbers, use {@code asNumber().toString()} to
	 *	obtain their full representations.
	 *
	 *	@return a {@code Number} representation of the JSON value
	 */
	public Number asNumber() {
		return (Number) value;
	}

	/**
	 *	Returns this JSON value as an {@code int}. This is a convenience method
	 *	for {@code asNumber().intValue()}.
	 *
	 *	@return an {@code int} representation of the JSON value
	 */
	public int intValue() {
		return ((Number) value).intValue();
	}

	/**
	 *	Returns this JSON value as a {@code double}. This is a convenience
	 *	method for {@code asNumber().doubleValue()}.
	 *
	 *	@return a {@code double} representation of the JSON value
	 */
	public double doubleValue() {
		return ((Number) value).doubleValue();
	}

	/**
	 *	Parses the JSON text and constructs a JSON value represented by the
	 *	JSON text. This method always creates {@code BigDecimal} objects
	 *	because large numbers may exceed the limits of Java primitive types. An
	 *	{@code IllegalArgumentException} is thrown if the JSON text is
	 *	malformed.
	 *
	 *	@param s the string containing the JSON text
	 *	@return a JSON value represented by the JSON text
	 *	@throws IllegalArgumentException if the JSON text is malformed
	 */
	public static JsonValue parse(String s) {
		StringCharacterIterator it = new StringCharacterIterator(s);
		Object o = JsonValue.next(it, 0);
		for (char c = it.current();
				it.getIndex() < it.getEndIndex(); c = it.next()) {
			if (! isSpace(c)) {
				throw new IllegalArgumentException();
			}
		}
		return JsonValue.create(o);
	}

	private static boolean isSpace(char c) {
		return (c == ' ') || (c == '\t') || (c == '\n') || (c == '\r');
	}

	private static Object next(StringCharacterIterator it, int depth) {
		char c = it.current();
		while (isSpace(c)) {
			c = it.next();
		}

		if ((c == '{') && (depth <= 64)) {
			it.next();
			Map<String, JsonValue> map = new HashMap<String, JsonValue>();
			boolean hasValue = false;
			depth += 1;
			while (true) {
				Object k = JsonValue.next(it, depth);
				if ((k != null) && k.equals('}')
						&& ((map.size() == 0) || hasValue)) {
					return Collections.unmodifiableMap(map);
				}
				if ((k != null) && k.equals(',') && hasValue) {
					hasValue = false;
				} else if (! hasValue) {
					if (! JsonValue.next(it, depth).equals(':')) {
						break;
					}
					Object v = JsonValue.next(it, depth);
					map.put((String) k, JsonValue.create(v));
					hasValue = true;
				} else {
					break;
				}
			}
		} else if ((c == '[') && (depth <= 64)) {
			it.next();
			List<JsonValue> list = new ArrayList<JsonValue>();
			boolean hasValue = false;
			depth += 1;
			while (true) {
				Object e = JsonValue.next(it, depth);
				if ((e != null) && e.equals(']')
						&& ((list.size() == 0) || hasValue)) {
					return Collections.unmodifiableList(list);
				}
				if ((e != null) && e.equals(',') && hasValue) {
					hasValue = false;
				} else if (! hasValue) {
					list.add(JsonValue.create(e));
					hasValue = true;
				} else {
					break;
				}
			}
		} else if (c == '"') {
			it.next();
			StringBuilder sb = new StringBuilder();
			for (c = it.current();
					it.getIndex() < it.getEndIndex(); c = it.next()) {
				if (c == '\"') {
					it.next(); // skips the ending '\"'
					return sb.toString();
				}
				if (c == '\\') {
					c = it.next();
					if (c == 'b') {
						c = '\b';
					} else if (c == 'f') {
						c = '\f';
					} else if (c == 'n') {
						c = '\n';
					} else if (c == 'r') {
						c = '\r';
					} else if (c == 't') {
						c = '\t';
					} else if (c == 'u') {
						int u = 0;
						for (int shift = 12; shift >= 0; shift -= 4) {
							int i = Character.digit(it.next(), 16);
							if ((i < 0) || (i >= 16)) {
								throw new IllegalArgumentException();
							}
							u += (i << shift);
						}
						c = (char) u;
					} else if (! ((c == '\"') || (c == '\\') || (c == '/'))) {
						break;
					}
				} else if (c < '\u0020') {
					break;
				}
				sb.append(c);
			}
		} else if ((c == '}') || (c == ']') || (c == ',') || (c == ':')) {
			it.next();
			return c; // uses autoboxing
		} else {
			StringBuilder sb = new StringBuilder();
			for (c = it.current();
					it.getIndex() < it.getEndIndex(); c = it.next()) {
				if (isSpace(c) || (c == ',') || (c == '}') || (c == ']')) {
					break;
				}
				sb.append(c);
			}
			String s = sb.toString();
			if (s.equals("null")) {
				return null;
			}
			if (s.equals("true")) {
				return Boolean.TRUE;
			}
			if (s.equals("false")) {
				return Boolean.FALSE;
			}
			if (isNumber(s)) {
				return new BigDecimal(s);
			}
		}
		throw new IllegalArgumentException();
	}

	private static boolean isNumber(String s) {
		char[] chars = s.toCharArray();
		if (chars.length == 0) {
			return false;
		}

		int begin = 0;
		if (chars[0] == '-') {
			begin = 1;
		}
		boolean isValid = false;
		boolean hasLeadingZero = false;
		boolean hasFraction = false;
		boolean hasExponent = false;
		for (int i = begin; i < chars.length; i += 1) {
			char c = chars[i];
			if ((c >= '0') && (c <= '9')) {
				if ((! hasFraction) && (! hasExponent)) {
					if (i == begin) {
						if (c == '0') {
							hasLeadingZero = true;
						} else {
							hasLeadingZero = false;
						}
					} else if (hasLeadingZero) {
						return false;
					}
				}
				isValid = true;
			} else if (c == '.') {
				if (hasFraction || hasExponent || (! isValid)) {
					return false;
				}
				hasFraction = true;
				isValid = false;
			} else if ((c == 'e') || (c == 'E')) {
				if (hasExponent || (! isValid) || ((i + 1) == chars.length)) {
					return false;
				}
				c = chars[i + 1];
				if ((c == '+') || (c == '-')) {
					i += 1;
				} else if ((c < '0') || (c > '9')) {
					return false;
				}
				hasExponent = true;
				isValid = false;
			} else {
				return false;
			}
		}
		return isValid;
	}

	private static String serialize(String s) {
		StringBuilder sb = new StringBuilder();
		sb.append('\"');
		for (int i = 0; i < s.length(); i += 1) {
			char c = s.charAt(i);
			if (c == '\b') {
				sb.append("\\b");
			} else if (c == '\f') {
				sb.append("\\f");
			} else if (c == '\n') {
				sb.append("\\n");
			} else if (c == '\r') {
				sb.append("\\r");
			} else if (c == '\t') {
				sb.append("\\t");
			} else if (c == '\"') {
				sb.append("\\\"");
			} else if (c == '\\') {
				sb.append("\\\\");
			} else {
				if (c < '\u0020') {
					String hex = Integer.toHexString((int) c);
					sb.append("\\u00");
					if (hex.length() == 1) {
						sb.append('0');
					}
					sb.append(hex);
				} else {
					sb.append(c);
				}
			}
		}
		sb.append('\"');
		return sb.toString();
	}

	/**
	 *	Returns this JSON value as a JSON text.
	 *
	 *	@return a JSON text representation of the JSON value
	 */
	@Override public String toString() {
		if (value == null) {
			return "null";
		}
		if (value instanceof Map) {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			for (Object o : ((Map) value).entrySet()) {
				Map.Entry<?, ?> e = (Map.Entry) o;
				sb.append(JsonValue.serialize((String) e.getKey()));
				sb.append(':');
				sb.append(e.getValue().toString());
				sb.append(',');
			}
			if (sb.charAt(sb.length() - 1) == ',') {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append('}');
			return sb.toString();
		}
		if (value instanceof List) {
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			for (Object e : ((List) value)) {
				sb.append(e.toString());
				sb.append(',');
			}
			if (sb.charAt(sb.length() - 1) == ',') {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append(']');
			return sb.toString();
		}
		if (value instanceof String) {
			return JsonValue.serialize((String) value);
		}
		return value.toString();
	}

}
