## Everlaw Utf8

Provides both low-level and high-level interfaces for handling UTF-8, serving
as a complement to the functionality provided by `com.google.common.base.Utf8`
(Guava) and Java's built-in `StandardCharsets.UTF_8`.

Maven projects can use this library with a simple POM dependency:
```xml
<project>
...
    <dependencies>
        ...
        <dependency>
            <groupId>com.everlaw</groupId>
            <artifactId>utf8</artifactId>
            <version>1.0</version>
        </dependency>
        ...
    </dependencies>
...
</project>
```

### Utf8

A low-level utility class that provides `static` methods for testing,
encoding, and decoding UTF-8. The principal methods are:
- `isValid(codepoint)`: Returns `true` iff the given codepoint is valid UTF-8.
- `toPackedInt(cseq, i)`: Encodes the 1- or 2-char Unicode codepoint starting
  at `cseq[i]` to 1-4 bytes of UTF-8, packed into a single `int`. This enables
  incremental encoding of any `CharSequence` without heap allocations.
- `toPackedInt(codepoint)`: Encodes the given codepoint as UTF-8 packed into
  an `int` as described above.
- `isContinuationByte(byte)`: Returns `true` iff the given byte is a UTF-8
  continuation byte.
- `numContinuationBytes(byte)`: Returns the number of continuation bytes that
  follow the given first byte of a possibly-multibyte UTF-8-encoded codepoint.

### Utf8Iterator

A high-level class for iterating over the UTF-8 bytes of a `CharSequence`,
implementing Java 8's `PrimitiveIterator.OfInt`. It allows for simple,
space-efficient iteration:
```java
Utf8Iterator utf8 = new Utf8Iterator(string);
while (utf8.hasNext()) {
    byte b = utf8.nextByte(); // convenience method for (byte) utf8.nextInt()
    // do something with b
}
```

This is functionally equivalent to:
```java
ByteBuffer utf8 = StandardCharsets.UTF_8.encode(string);
while (utf8.hasRemaining()) {
    byte b = utf8.get();
    // do something with b
}
```

The main benefits of using `Utf8Iterator` are:
- It operates on `CharSequence`s of all types, not just `String`.
- It uses constant space, even for large strings, whereas the buffer returned
  from `UTF_8.encode` is proportional to the size of the string.
- It encodes incrementally, so no work is wasted if the loop is exited early.

### Semantic versioning

This project uses [Semantic versioning](http://semver.org/).

### Contributing

We are happy to receive Pull Requests. If you are planning a big change, it's
probably best to discuss it as an
[Issue](https://github.com/Everlaw/utf8/issues) first.  

### Building

In the root directory, run `mvn install`. That will build everything.
