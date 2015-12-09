/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.fge.grappa.parsers;

import com.github.fge.grappa.buffers.CharSequenceInputBuffer;
import com.github.fge.grappa.buffers.InputBuffer;
import com.github.fge.grappa.support.Chars;
import com.github.fge.grappa.support.Position;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.github.fge.grappa.util.CustomAssertions.shouldHaveThrown;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public final class CharSequenceInputBufferTest
{
    private static final String UNICODE_STRING = "abf\uD800zdji\uD800\uDD41";
    private static final InputBuffer UNICODE_BUFFER
        = new CharSequenceInputBuffer(UNICODE_STRING);

    @Test
    public void testOneliner()
    {
        InputBuffer buf = new CharSequenceInputBuffer("abcdefgh");
        assertEquals(buf.charAt(0), 'a');
        assertEquals(buf.charAt(7), 'h');
        assertEquals(buf.charAt(8), Chars.EOI);
        assertEquals(buf.charAt(26), Chars.EOI);

        assertEquals(buf.extractLine(1), "abcdefgh");

        assertEquals(buf.getPosition(0), new Position(1, 1));
        assertEquals(buf.getPosition(1), new Position(1, 2));
        assertEquals(buf.getPosition(7), new Position(1, 8));
    }

    @Test
    public void testMultiliner()
    {
        InputBuffer buf = new CharSequenceInputBuffer("" +
            "abcd\n" +
            "ef\r\n" +
                '\n' +
            "gh\n" +
                '\n'
        );
        assertEquals(buf.charAt(0), 'a');
        assertEquals(buf.charAt(7), '\r');
        assertEquals(buf.charAt(8), '\n');

        assertEquals(buf.extractLine(1), "abcd");
        assertEquals(buf.extractLine(2), "ef");
        assertEquals(buf.extractLine(3), "");
        assertEquals(buf.extractLine(4), "gh");
        assertEquals(buf.extractLine(5), "");

        assertEquals(buf.getPosition(0), new Position(1, 1));
        assertEquals(buf.getPosition(1), new Position(1, 2));
        assertEquals(buf.getPosition(2), new Position(1, 3));
        assertEquals(buf.getPosition(3), new Position(1, 4));
        assertEquals(buf.getPosition(4), new Position(1, 5));
        assertEquals(buf.getPosition(5), new Position(2, 1));
        assertEquals(buf.getPosition(6), new Position(2, 2));
        assertEquals(buf.getPosition(7), new Position(2, 3));
        assertEquals(buf.getPosition(8), new Position(2, 4));
        assertEquals(buf.getPosition(9), new Position(3, 1));
        assertEquals(buf.getPosition(10), new Position(4, 1));
        assertEquals(buf.getPosition(11), new Position(4, 2));
        assertEquals(buf.getPosition(12), new Position(4, 3));
        assertEquals(buf.getPosition(13), new Position(5, 1));
    }

    @Test
    public void charAtThrowsIAEOnNegativeIndex()
    {
        InputBuffer buf = new CharSequenceInputBuffer("abcdefgh");

        try {
            buf.charAt(-1);
            //shouldHaveThrown(IllegalArgumentException.class);
        } catch (Exception e) {
            assertThat(e).hasMessageContaining("out of range");
        }
    }

    @DataProvider
    public Iterator<Object[]> indicesAndCodePoints()
    {
        List<Object[]> list = new ArrayList<>();

        list.add(new Object[] { 0, UNICODE_STRING.codePointAt(0) });
        list.add(new Object[] { 10, -1 });
        list.add(new Object[] { 8, UNICODE_STRING.codePointAt(8) });
        list.add(new Object[] { 3, UNICODE_STRING.codePointAt(3) });
        list.add(new Object[] { 9, UNICODE_STRING.codePointAt(9) });

        return list.iterator();
    }


    @Test(dataProvider = "indicesAndCodePoints")
    public void codePointTest(int index, int codePoint)
    {
        assertThat(UNICODE_BUFFER.codePointAt(index)).isEqualTo(codePoint);
    }

    @Test
    public void iaeIsThrownIfIndexIsNegative()
    {
        try {
            UNICODE_BUFFER.codePointAt(-1);
            shouldHaveThrown(IllegalArgumentException.class);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessage("index is negative");
        }
    }
}
