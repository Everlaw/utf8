/*
 * Copyright (C) 2015 Everlaw
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.everlaw.utf8;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/**
 * Iterates over the UTF-8 bytes of a given String. Each call to {@link #nextInt} returns an int
 * between 0 and 255, inclusive. Each call to {@link #nextByte} returns {@code (byte) nextInt()}.
 *
 * @author Brandon Mintern
 */
public class Utf8Iterator implements PrimitiveIterator.OfInt {

    private final String str;
    /**
     * When nonzero, holds additional UTF-8 bytes representing the Unicode code point that
     * corresponds to 1-2 chars from {@link #str}.
     *
     * @see Utf8#toPackedInt(String, int)
     */
    private int utf8 = 0;
    /**
     * The current index into the next character in {@link #str}.
     */
    private int pos = 0;

    public Utf8Iterator(String s) {
        str = s;
    }

    @Override
    public boolean hasNext() {
        return utf8 != 0 || pos < str.length();
    }

    public byte nextByte() {
        return (byte) nextInt();
    }

    @Override
    public int nextInt() {
        if (utf8 != 0) {
            int b = utf8 & 0xFF;
            utf8 >>>= 8;
            return b;
        }
        if (pos < str.length()) {
            utf8 = Utf8.toPackedInt(str, pos);
            pos += Character.isHighSurrogate(str.charAt(pos)) ? 2 : 1;
            if (utf8 == 0) {
                // Must handle NUL explicitly.
                return 0;
            }
            return nextInt();
        }
        throw new NoSuchElementException();
    }

    /**
     * Returns the number of bytes necessary to encode the remainder of the string. This method
     * should not be called repeatedly, as its runtime increases linearly with the length of the
     * remaining string.
     */
    public int remainingLength() {
        int length = com.google.common.base.Utf8.encodedLength(str.substring(pos));
        for (int u = utf8; u != 0; u >>>= 8) {
            length++;
        }
        return length;
    }
}
