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
 * A utility class for constants and operations related to UTF-8.
 *
 * @author Brandon Mintern
 */
public class Utf8 {

    /**
     * Converts the Unicode code point beginning at {@code str[index]} to a UTF-8 representation
     * packed into an {@code int} that can be unpacked as follows:
     * <pre>{@code
     *  int utf8 = Utf8.toPackedInt(str, index);
     *  byte[] unpacked = new byte[4];
     *  int i = 0;
     *  do {
     *      unpacked[i++] = (byte) (utf8 & 0xFF);
     *      utf8 >>>= 8;
     *  } while (utf8 != 0);
     * }</pre>
     * <p>
     * Note: if this method returns successfully and
     * {@code Character.isHighSurrogate(str.charAt(index))}, then the next character (if there are
     * any) begins at {@code index + 2}.
     *
     * @throws IndexOutOfBoundsException    if {@code index >= str.length()}
     * @throws IllegalArgumentException     if {@code str[index]} (possibly combined with
     *                                      {@code str[index + 1]} for surrogate pairs) is not a
     *                                      valid Unicode code point
     */
    public static int toPackedInt(String str, int index) {
        // http://en.wikipedia.org/wiki/UTF-8#Description was helpful in implementing this method.
        char c = str.charAt(index);
        int ch;
        if (Character.isHighSurrogate(c)) {
            if (index + 1 >= str.length()) {
                throw new IllegalArgumentException("Invalid high surrogate character at " + index);
            }
            char low = str.charAt(index + 1);
            if (! Character.isLowSurrogate(low)) {
                throw new IllegalArgumentException("Invalid surrogate pair at " + index);
            }
            ch = Character.toCodePoint(c, low);
        } else if (Character.isLowSurrogate(c)) {
            throw new IllegalArgumentException("Invalid low surrogate character at " + index);
        } else if (Character.isDefined(c)) {
            ch = c;
        } else {
            throw new IllegalArgumentException("Undefined character at " + index);
        }
        if (ch < 0x80) {
            return ch;
        }
        int bytes;
        if (ch < 0x800) {
            bytes = 2;
        } else if (ch < 0x10000) {
            bytes = 3;
        } else {
            bytes = 4;
        }
        int leadingMask = -1 << (8 - bytes) & 0xFF;
        int utf8 = 0;
        for (; bytes > 1; bytes--) {
            utf8 |= (ch & 0b0011_1111) | 0b1000_0000;
            utf8 <<= 8;
            ch >>= 6;
        }
        return utf8 | ch | leadingMask;
    }
}
