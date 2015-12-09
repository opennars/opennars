/*
 * Copyright (C) 2014 Francis Galiegue <fgaliegue@gmail.com>
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

package com.github.fge.grappa.buffers;

import com.github.fge.grappa.support.Chars;
import com.github.fge.grappa.support.IndexRange;
import com.github.fge.grappa.support.Position;
import com.google.common.base.Preconditions;
import com.google.common.collect.Range;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An {@link InputBuffer} over a {@link CharSequence}
 *
 * <p>A {@link CharSequence} is the most basic character interface in the JDK.
 * It is implemented by a lot of character related classes, including {@link
 * String}, and is also the argument type used by a {@link Pattern}'s {@link
 * Matcher}.</p>
 *
 * <p>Among other things, this means you can use this package on very large
 * files using <a href="https://github.com/fge/largetext">largetext</a>, which
 * implements {@link CharSequence} over multi-gigabyte files.</p>
 */
@Immutable
public final class CharSequenceInputBuffer
    implements InputBuffer
{
//    private static final ExecutorService EXECUTOR_SERVICE;
//
//    static {
//        final ThreadFactory factory = new ThreadFactoryBuilder()
//            .setDaemon(true).setNameFormat("linecounter-thread-%d").build();
//        EXECUTOR_SERVICE = Executors.newCachedThreadPool(factory);
//    }

    private final CharSequence charSequence;
    private final /*Future<*/LineCounter lineCounter;

    public CharSequenceInputBuffer(@Nonnull CharSequence charSequence)
    {
        this.charSequence = Objects.requireNonNull(charSequence);
        /*lineCounter = EXECUTOR_SERVICE.submit(new Callable<LineCounter>()
        {
            @Override
            public LineCounter call()
                throws Exception
            {
                return new LineCounter(charSequence);
            }
        });*/
        lineCounter = new LineCounter(charSequence);
    }

    @Override
    public char charAt(int index)
    {
        /*if (index < 0)
           throw new IllegalArgumentException("index is negative");*/

        return index < charSequence.length() ?
                charSequence.charAt(index) : Chars.EOI;
    }

    @SuppressWarnings("ImplicitNumericConversion")
    @Override
    public int codePointAt(int index)
    {
        int length = charSequence.length();
        if (index >= length)
            return -1;
        if (index < 0)
            throw new IllegalArgumentException("index is negative");

        char c = charSequence.charAt(index);
        if (!Character.isHighSurrogate(c))
            return c;
        if (index == length - 1)
            return c;
        char c2 = charSequence.charAt(index + 1);
        return Character.isLowSurrogate(c2) ? Character.toCodePoint(c, c2) : c;
    }

    @Override
    public char[] extractChars(int start, int end)
    {
        int realStart = Math.max(start, 0);
        int realEnd = Math.min(end, charSequence.length());
        char[] x = new char[realEnd - realStart];
        int j = 0;
        for (int i = realStart; i < realEnd; i++)
            x[j++] = charSequence.charAt(i);
        return x;
    }

    @Override
    public String extract(int start, int end)
    {
        int realStart = Math.max(start, 0);
        int realEnd = Math.min(end, charSequence.length());
        return charSequence.subSequence(realStart, realEnd).toString();
    }

    @Override
    public String extract(IndexRange range)
    {
        return extract(range.start, range.end);
    }

    @Override
    public Position getPosition(int index)
    {
        return lineCounter
        //return Futures.getUnchecked(lineCounter)
            .toPosition(index);

    }

    @Override
    public String extractLine(int lineNumber)
    {
        Preconditions.checkArgument(lineNumber > 0, "line number is negative");
        LineCounter counter = lineCounter; //Futures.getUnchecked(lineCounter);
        Range<Integer> range = counter.getLineRange(lineNumber);
        int start = range.lowerEndpoint();
        int end = range.upperEndpoint();
        if (charAt(end - 1) == '\n')
            end--;
        if (charAt(end - 1) == '\r')
            end--;
        return extract(start, end);
    }

    @SuppressWarnings("AutoUnboxing")
    @Override
    public IndexRange getLineRange(int lineNumber)
    {
        Range<Integer> range =
            //= Futures.getUnchecked(lineCounter)
            lineCounter
                .getLineRange(lineNumber);
        return new IndexRange(range.lowerEndpoint(), range.upperEndpoint());
    }

    @Override
    public int getLineCount()
    {
        //return Futures.getUnchecked(lineCounter)
                return lineCounter.getNrLines();
    }

    @Override
    public int length()
    {
        return charSequence.length();
    }
}
