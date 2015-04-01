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
     * Returns true iff the given {@code codepoint} is a Unicode character that is also valid as
     * UTF-8. A surrogate codepoint is only valid UTF-16, not UTF-8.
     *
     * @param codepoint the value to check
     * @return true iff {@code codepoint} can be serialized as UTF-8
     * @see Character#isValidCodePoint(int)
     * @see Character#isSurrogate(char)
     */
    public static boolean isValid(int codepoint) {
        return Character.isValidCodePoint(codepoint)
                && (codepoint < Character.MIN_SURROGATE || codepoint > Character.MAX_SURROGATE);
    }

    /**
     * Converts the Unicode code point beginning at {@code cseq[index]} to a UTF-8 representation
     * packed into an {@code int}. The UTF-8 bytes can be unpacked as follows:
     * <pre>{@code
     *  int utf8 = Utf8.toPackedInt(cseq, index);
     *  byte[] unpacked = new byte[4];
     *  int i = 0;
     *  do {
     *      unpacked[i++] = (byte) (utf8 & 0xFF);
     *      utf8 >>>= 8;
     *  } while (utf8 != 0);
     * }</pre>
     * Note: if this method returns successfully and
     * {@code Character.isHighSurrogate(cseq.charAt(index))}, then the next character (if there is
     * one) begins at {@code index + 2}.
     *
     * @param cseq the sequence containing the codepoint
     * @param index the starting index of the codepoint
     * @return a packed {@code int} as described above
     * @throws IndexOutOfBoundsException if {@code index >= cseq.length()}
     * @throws IllegalArgumentException if {@code cseq[index]} (possibly combined with
     *         {@code cseq[index + 1]} for surrogate pairs) is not a valid UTF-8 code point
     */
    public static int toPackedInt(CharSequence cseq, int index) {
        char c = cseq.charAt(index);
        int codepoint; // the Unicode codepoint beginning at cseq[index]
        if (Character.isHighSurrogate(c)) {
            if (index + 1 >= cseq.length()) {
                throw new IllegalArgumentException("Unpaired high surrogate character at " + index);
            }
            char low = cseq.charAt(index + 1);
            if (! Character.isLowSurrogate(low)) {
                throw new IllegalArgumentException("Invalid surrogate pair at " + index);
            }
            codepoint = Character.toCodePoint(c, low);
        } else if (Character.isLowSurrogate(c)) {
            throw new IllegalArgumentException("Unpaired low surrogate character at " + index);
        } else {
            // validity will be checked by toPackedInt(codepoint)
            codepoint = c;
        }
        return toPackedInt(codepoint);
    }

    /**
     * Converts a Unicode {@code codepoint} to a UTF-8 representation packed into an {@code int}, as
     * described by {@link #toPackedInt(CharSequence, int)}.
     *
     * @param codepoint a Unicode codepoint
     * @return the packed {@code int}
     * @throws IllegalArgumentException if {@code ! Utf8.isValid(codepoint)}
     * @see #isValid(int)
     */
    public static int toPackedInt(int codepoint) {
        if (! isValid(codepoint)) {
            throw new IllegalArgumentException("Invalid UTF-8 codepoint " + codepoint);
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
