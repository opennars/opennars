/*
 *  SubstringRope.java
 *  Copyright (C) 2007 Amin Ahmad.
 *
 *  This file is part of Java Ropes.
 *
 *  Java Ropes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Java Ropes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Java Ropes.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Amin Ahmad can be contacted at amin.ahmad@gmail.com or on the web at
 *  www.ahmadsoft.org.
 */
package nars.util.data.rope.impl;

import nars.util.data.rope.Rope;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * Represents a lazily-evaluated substring of another rope. For performance
 * reasons, the target rope must be a <code>FlatRope</code>.
 *
 * @author aahmad
 */
public class SubstringRope extends AbstractRope {

    private final FlatRope rope;
    private final int offset;
    private final int length;

    public SubstringRope(FlatRope rope, int offset, int length) {
        if (length < 0 || offset < 0 || offset + length > rope.length()) {
            throw new IndexOutOfBoundsException("Invalid substring offset (" + offset + ") and length (" + length + ") for underlying rope with length " + rope.length());
        }

        this.rope = rope;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public char charAt(int index) {
        if (index >= length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + index);
        }

        return rope.charAt(offset + index);
    }

    @Override
    public byte depth() {
        return Rope.depth(getRope());
    }

    int getOffset() {
        return offset;
    }

    /**
     * Returns the rope underlying this one.
     *
     * @return the rope underlying this one.
     */
    public Rope getRope() {
        return rope;
    }

    @Override
    public Iterator<Character> iterator(int start) {
        if (start < 0 || start > length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }
        return new Iterator<Character>() {

            final Iterator<Character> u = getRope().iterator(getOffset() + start);
            int position = start;

            @Override
            public boolean hasNext() {
                return position < length();
            }

            @Override
            public Character next() {
                ++position;
                return u.next();
            }

            @Override
            public void remove() {
                u.remove();
            }

        };
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public Rope reverse() {
        return new ReverseRope(this);
    }

    @Override
    public Iterator<Character> reverseIterator(int start) {
        if (start < 0 || start > length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }
        return new Iterator<Character>() {
            final Iterator<Character> u = getRope().reverseIterator(getRope().length() - getOffset() - length() + start);
            int position = length() - start;

            @Override
            public boolean hasNext() {
                return position > 0;
            }

            @Override
            public Character next() {
                --position;
                return u.next();
            }

            @Override
            public void remove() {
                u.remove();
            }
        };
    }

    @Override
    public Rope subSequence(int start, int end) {
        if (start == 0 && end == length()) {
            return this;
        }
        return new SubstringRope(rope, offset + start, end - start);
    }

    @Override
    public String toString() {
        return rope.toString(offset, length);
    }

    @Override
    public void write(Writer out) throws IOException {
        rope.write(out, offset, length);
    }

    @Override
    public void write(Writer out, int offset, int length) throws IOException {
        rope.write(out, this.offset + offset, length);
    }
}
