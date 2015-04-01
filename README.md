## Everlaw Utf8

Provides a `Utf8` utility class and a `Utf8Iterator`.

Include this library in your Maven project by adding the following to your
POM:
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

Provides UTF-8-related functionality, serving as a complement to
`com.google.common.base.Utf8`. One method, `Utf8.toPackedInt`, converts a
single- or multi-`char` code point into its UTF-8 byte representation, packed
into a single `int`.

### Utf8Iterator

Provides a `PrimitiveIterator.OfInt` over the UTF-8 bytes of a `CharSequence`.
This allows for simple, efficient iteration:
```java
Utf8Iterator utf8 = new Utf8Iterator(string);
while (utf8.hasNext()) {
    byte b = utf8.nextByte(); // convenience method for (byte) utf8.nextInt()
    // do something with b
}
```

### Semantic versioning

This project uses [Semantic versioning](http://semver.org/).

### Contributing

We are happy to receive Pull Requests. If you are planning a big change, it's
probably best to discuss it as an Issue first.

### Building

In the root directory, run `mvn install`. That will build everything.
