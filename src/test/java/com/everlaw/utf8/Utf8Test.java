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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests Everlaw's {@link Utf8} utility methods. Where reasonable, we exhaustively test all possible
 * input values.
 *
 * @author Brandon Mintern
 */
public class Utf8Test {

    @Test
    public void testIsValid() {
        for (int c = Character.MAX_CODE_POINT + 1; c >= -1; c--) {
            boolean valid = Character.isValidCodePoint(c);
            if (valid && c <= Character.MAX_VALUE && Character.isSurrogate((char) c)) {
                valid = false;
            }
            assertEquals(valid, Utf8.isValid(c));
        }
    }

    @Test
    public void testOutOfBounds() {
        testOob("empty/0", "", 0);
        testOob("length-1/1", "a", 1);
    }

    @Test
    public void testAllChars() {
        char[] c = new char[1];
        CharBuffer cbuf = CharBuffer.wrap(c);
        char ch = 0;
        do {
            c[0] = ch;
            if (Utf8.isValid(ch)) {
                checkPacked(Utf8.toPackedInt(cbuf, 0), ch);
            } else {
                testIllegal(cbuf, "0x%04X", ch);
            }
        } while (++ch != 0);
    }

    @Test
    public void testAllSurrogates() {
        char[] pair = new char[2];
        CharBuffer cbuf = CharBuffer.wrap(pair);
        // An exhaustive test throws a lot of exceptions, and Throwable.fillInStackTrace is *way*
        // too slow to make that practical. So we only test the full range of low surrogate chars at
        // the high surrogate boundaries.
        testSurrogates(Character.MIN_HIGH_SURROGATE, 0, Character.MAX_VALUE, pair, cbuf);
        for (char high = Character.MIN_HIGH_SURROGATE + 1; high < Character.MAX_HIGH_SURROGATE; high++) {
            testSurrogates(high, Character.MIN_LOW_SURROGATE - 1, Character.MIN_LOW_SURROGATE + 1, pair, cbuf);
        }
        testSurrogates(Character.MAX_HIGH_SURROGATE, 0, Character.MAX_VALUE, pair, cbuf);
    }

    private void testSurrogates(char high, int minLow, int maxLow, char[] pair, CharBuffer cbuf) {
        pair[0] = high;
        char low = (char) minLow;
        while (true) {
            pair[1] = low;
            if (Character.isSurrogatePair(high, low)) {
                checkPacked(Utf8.toPackedInt(cbuf, 0), Character.toCodePoint(high, low));
            } else {
                testIllegal(cbuf, "0x%08X", high << 16 | low);
            }
            // Be careful of overflow!
            if (low == maxLow) {
                break;
            }
            low++;
        }
    }

    @Test
    public void testAllCodepoints() {
        for (int i = Character.MAX_CODE_POINT + 1; i >= 0; i--) {
            if (Utf8.isValid(i)) {
                checkPacked(Utf8.toPackedInt(i), i);
            } else {
                try {
                    Utf8.toPackedInt(i);
                    illegalFail("0x%08X", i);
                } catch (IllegalArgumentException e) {
                    // expected; other exceptions propagate
                }
            }
        }
    }

    @Test
    public void testIndexing() {
        for (String s: Arrays.asList("hello world", "\uFFFC\uFFFD\uFFFE\uFFFF")) {
            for (int i = 0; i < s.length(); i++) {
                checkPacked(Utf8.toPackedInt(s, i), s.charAt(i));
            }
        }
    }

    @Test
    public void testIsContinuationByteForBytes() {
        for (byte b = (byte) 0b1000_0000; b != (byte) 0b1100_0000; b++) {
            assertTrue(Utf8.isContinuationByte(b));
        }
        for (byte b = (byte) 0b1100_0000; b != (byte) 0b1000_0000; b++){
            assertFalse(Utf8.isContinuationByte(b));
        }
    }

    @Test
    public void testIsContinuationByteForInts() {
        for (int b = 0; b <= 0xFF; b++) {
            assertEquals(b >= 0b10000000 && b <= 0b10111111, Utf8.isContinuationByte(b));
        }
    }

    @Test
    public void testNumContinuationBytesForBytes() {
        for (byte b = 0; b != (byte) 0b1000_0000; b++) {
            assertEquals(0, Utf8.numContinuationBytes(b));
        }
        for (byte b = (byte) 0b1000_0000; b != (byte) 0b1100_0000; b++) {
            assertTrue(Utf8.numContinuationBytes(b) < 0);
        }
        for (byte b = (byte) 0b1100_0000; b != (byte) 0b1110_0000; b++) {
            assertEquals(1, Utf8.numContinuationBytes(b));
        }
        for (byte b = (byte) 0b1110_0000; b != (byte) 0b1111_0000; b++) {
            assertEquals(2, Utf8.numContinuationBytes(b));
        }
        for (byte b = (byte) 0b1111_0000; b != (byte) 0b1111_1000; b++) {
            assertEquals(3, Utf8.numContinuationBytes(b));
        }
        for (byte b = (byte) 0b1111_1000; b != 0; b++) {
            assertTrue(Utf8.numContinuationBytes(b) < 0);
        }
    }

    @Test
    public void testNumContinuationBytesForInts() {
        for (int b = 0; b <= 0xFF; b++) {
            if (b <= 0b0111_1111) {
                assertEquals(0, Utf8.numContinuationBytes(b));
            } else if (b <= 0b1011_1111) {
                assertTrue(Utf8.numContinuationBytes(b) < 0);
            } else if (b <= 0b1101_1111) {
                assertEquals(1, Utf8.numContinuationBytes(b));
            } else if (b <= 0b1110_1111) {
                assertEquals(2, Utf8.numContinuationBytes(b));
            } else if (b <= 0b1111_0111) {
                assertEquals(3, Utf8.numContinuationBytes(b));
            } else {
                assertTrue(Utf8.numContinuationBytes(b) < 0);
            }
        }
    }

    private void checkPacked(int packed, int codepoint) {
        ByteBuffer b = ByteBuffer.allocate(4);
        do {
            b.put((byte) (packed & 0xFF));
            packed >>>= 8;
        } while (packed != 0);
        b.flip();
        CharBuffer c = CharBuffer.allocate(2);
        assertEquals(CoderResult.UNDERFLOW, StandardCharsets.UTF_8.newDecoder().decode(b, c, true));
        c.flip();
        char c1 = c.get();
        assertEquals(codepoint, c.hasRemaining() ? Character.toCodePoint(c1, c.get()) : c1);
    }

    private void testOob(String msg, CharSequence cseq, int i) {
        try {
            Utf8.toPackedInt(cseq, i);
            throwFail(msg, IndexOutOfBoundsException.class);
        } catch (IndexOutOfBoundsException e) {
            // expected; propagate other exceptions
        }
    }

    private void testIllegal(CharSequence cseq, String fmt, int val) {
        try {
            Utf8.toPackedInt(cseq, 0);
            illegalFail(fmt, val);
        } catch (IllegalArgumentException e) {
            // expected; other exceptions propagate
        }
    }

    private void illegalFail(String fmt, int val) {
        throwFail(String.format(fmt, val), IllegalArgumentException.class);
    }

    private void throwFail(String val, Class<? extends Exception> err) {
        fail(val + " failed to throw " + err);
    }
}
