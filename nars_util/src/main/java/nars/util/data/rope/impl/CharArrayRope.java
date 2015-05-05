/*
 *  FlatCharArrayRope.java
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
import java.util.Arrays;
import java.util.Iterator;

/**
 * A rope constructed from a character array. This rope is even flatter than a
 * regular flat rope.
 *
 * @author Amin Ahmad
 */
public final class CharArrayRope extends AbstractRope implements FlatRope {

    public final char[] sequence;
    int len;
    int hash = 0;

    /**
     * Constructs a new rope from a character array. Does not make a copy but
     * uses it directly. To create a copy, use: new FlatCharArrayRope(sequence,
     * 0, sequence.length)
     *
     * @param sequence the character array.
     */
    public CharArrayRope(final char[] sequence) {
        super();
        this.sequence = sequence;
        len = sequence.length;
    }

    public CharArrayRope(final StringBuilder sb) {
        this(Rope.getCharArray(sb));
        len = sb.length();
    }

    /**
     * Constructs a new rope from a character array range.
     *
     * @param sequence the character array.
     * @param offset the offset in the array.
     * @param length the length of the array.
     */
    public CharArrayRope(final char[] sequence, final int offset, final int length) {
        if (length > sequence.length) {
            throw new IllegalArgumentException("Length must be less than " + sequence.length);
        }
        this.sequence = new char[length];
        System.arraycopy(sequence, offset, this.sequence, 0, length);
    }

    @Override
    public char charAt(final int index) {
        return sequence[index];
    }

    @Override
    public byte depth() {
        return 0;
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Arrays.hashCode(sequence);
        }
        return hash;
    }

    
    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    @Override
    public int indexOf(final char ch) {
        for (int j = 0; j < this.sequence.length; ++j) {
            if (this.sequence[j] == ch) {
                return j;
            }
        }
        return -1;
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    @Override
    public int indexOf(final char ch, final int fromIndex) {
        if (fromIndex < 0 || fromIndex >= this.length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + fromIndex);
        }
        for (int j = fromIndex; j < this.length(); ++j) {
            if (this.sequence[j] == ch) {
                return j;
            }
        }
        return -1;
    }

    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    @Override
    public int indexOf(final CharSequence sequence, final int fromIndex) {
		// Implementation of Boyer-Moore-Horspool algorithm with
        // special support for unicode.

        // step 0. sanity check.
        final int length = length();
        if (length == 0) {
            return -1;
        }
        if (length == 1) {
            return this.indexOf(sequence.charAt(0), fromIndex);
        }

        final int[] bcs = new int[256]; // bad character shift
        Arrays.fill(bcs, length);

        // step 1. preprocessing.
        for (int j = 0; j < length - 1; ++j) {
            final char c = sequence.charAt(j);
            final int l = (c & 0xFF);
            bcs[l] = Math.min(length - j - 1, bcs[l]);
        }

        // step 2. search.
        for (int j = fromIndex + length - 1; j < this.length();) {
            int x = j, y = length - 1;
            while (true) {
                if (sequence.charAt(y) != this.sequence[x]) {
                    j += bcs[(this.sequence[x] & 0xFF)];
                    break;
                }
                if (y == 0) {
                    return x;
                }
                --x;
                --y;
            }

        }

        return -1;
    }

    @Override
    public Iterator<Character> iterator(final int start) {
        if (start < 0 || start > this.length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }

        return new Iterator<Character>() {
            int current = start;

            @Override
            public boolean hasNext() {
                return this.current < length();
            }

            @Override
            public Character next() {
                return sequence[this.current++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Rope iterator is read-only.");
            }
        };
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) return true;

        if (other instanceof CharArrayRope) {
            CharArrayRope cother = (CharArrayRope)other;                        
            
            if (cother.length()!=len)
                return false;
            return Arrays.equals(sequence, cother.sequence);
        }
        return false;
    }

    
    @Override
    public int length() {
        return len;
    }

    @Override
    public Rope reverse() {
        return new ReverseRope(this);
    }

    @Override
    public Iterator<Character> reverseIterator(final int start) {
        if (start < 0 || start > this.length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }
        return new Iterator<Character>() {
            int current = CharArrayRope.this.length() - start;

            @Override
            public boolean hasNext() {
                return this.current > 0;
            }

            @Override
            public Character next() {
                return CharArrayRope.this.sequence[--this.current];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Rope iterator is read-only.");
            }
        };
    }

    @Override
    public Rope subSequence(final int start, final int end) {
        if (start == 0 && end == this.length()) {
            return this;
        }
        if (end - start < 16) {
            return new CharArrayRope(this.sequence, start, end - start);
        } else {
            return new SubstringRope(this, start, end - start);
        }
    }

    @Override
    public String toString() {
        return new String(this.sequence);
    }

    @Override
    public String toString(final int offset, final int length) {
        return new String(this.sequence, offset, length);
    }

    @Override
    public void write(final Writer out) throws IOException {
        this.write(out, 0, this.length());
    }

    @Override
    public void write(final Writer out, final int offset, final int length) throws IOException {
        out.write(this.sequence, offset, length);
    }
}
