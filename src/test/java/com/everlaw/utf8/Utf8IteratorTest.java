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
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * {@link Utf8Iterator} sanity checks.
 *
 * @author Brandon Mintern
 */
public class Utf8IteratorTest {

    @Test
    public void testEmpty() {
        test("");
    }

    @Test
    public void testNull() {
        test("\0");
        test("\0a");
    }

    @Test
    public void testLongStrings() {
        String s = "a";
        while (true) {
            test(s);
            if (s.length() > 4 * 1024 * 1024) {
                break;
            }
            s += s;
        }
    }

    @Test
    public void testMultibyte() {
        test("\u0080", 2);
        test("\u07FF", 2);
        test("\u0800", 3);
        test("\uFFFF", 3);
        test("\uD800\uDC00", 4);
        test("\uDBFF\uDDFF", 4);
    }

    private void test(String s) {
        test(s, s.length());
    }

    private void test(String s, int expectedLen) {
        byte[] expectedUtf8 = toUtf8(s);
        Utf8Iterator utf8 = new Utf8Iterator(s);
        assertEquals(expectedLen, utf8.remainingLength());
        int i = 0;
        for (; utf8.hasNext(); i++) {
            assertTrue(i < expectedUtf8.length);
            assertEquals(expectedUtf8[i], utf8.nextByte());
        }
        assertEquals(expectedUtf8.length, i);
        assertEquals(0, utf8.remainingLength());
        utf8 = new Utf8Iterator(s);
        for (i = 0; utf8.hasNext(); i++) {
            assertTrue(i < expectedUtf8.length);
            assertEquals(expectedUtf8[i], (byte) utf8.nextInt());
        }
        assertEquals(expectedUtf8.length, i);
        assertEquals(0, utf8.remainingLength());
    }

    private byte[] toUtf8(String s) {
        ByteBuffer bb = StandardCharsets.UTF_8.encode(s);
        byte[] utf8 = new byte[bb.remaining()];
        bb.get(utf8);
        return utf8;
    }
}
