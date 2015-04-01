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

/**
 * A utility class for UTF-8 operations.
 *
 * @author Brandon Mintern
 */
public final class Utf8 {

    /**
     * Converts the Unicode code point beginning at {@code str[index]} to a UTF-8 representation
     * packed into an {@code int}. The UTF-8 bytes can be unpacked as follows:
     * <pre>{@code
     *  int utf8 = Utf8.toPackedInt(str, index);
     *  byte[] unpacked = new byte[4];
     *  int i = 0;
     *  do {
     *      unpacked[i++] = (byte) (utf8 & 0xFF);
     *      utf8 >>>= 8;
     *  } while (utf8 != 0);
     * }</pre>
     * Note: if this method returns successfully and
     * {@code Character.isHighSurrogate(str.charAt(index))}, then the next character (if there is
     * one) begins at {@code index + 2}.
     *
     * @param str the sequence containing the codepoint
     * @param index the starting index of the codepoint
     * @return a packed {@code int} as described above
     * @throws IndexOutOfBoundsException if {@code index >= str.length()}
     * @throws IllegalArgumentException if {@code str[index]} (possibly combined with
     *         {@code str[index + 1]} for surrogate pairs) is not a valid UTF-8 code point
     */
    public static int toPackedInt(String str, int index) {
        char c = str.charAt(index);
        int codepoint; // the Unicode codepoint beginning at str[index]
        if (Character.isHighSurrogate(c)) {
            if (index + 1 >= str.length()) {
                throw new IllegalArgumentException("Unpaired high surrogate character at " + index);
            }
            char low = str.charAt(index + 1);
            if (! Character.isLowSurrogate(low)) {
                throw new IllegalArgumentException("Invalid surrogate pair at " + index);
            }
            codepoint = Character.toCodePoint(c, low);
        } else if (Character.isLowSurrogate(c)) {
            throw new IllegalArgumentException("Unpaired low surrogate character at " + index);
        } else {
            codepoint = c;
        }
        if (! Character.isValidCodePoint(codepoint)) {
            throw new IllegalArgumentException("Invalid codepoint " + codepoint);
        }
        if (codepoint < 0x80) {
            return codepoint;
        }
        int bytes;
        if (codepoint < 0x800) {
            bytes = 2;
        } else if (codepoint < 0x10000) {
            bytes = 3;
        } else {
            bytes = 4;
        }
        int leadingMask = -1 << (8 - bytes) & 0xFF;
        int utf8 = 0;
        for (; bytes > 1; bytes--) {
            utf8 |= (codepoint & 0b0011_1111) | 0b1000_0000;
            utf8 <<= 8;
            codepoint >>= 6;
        }
        return utf8 | codepoint | leadingMask;
    }

    private Utf8(){}
}
