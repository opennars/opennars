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
    public CharArrayRope(char[] sequence) {
        this.sequence = sequence;
        len = sequence.length;
    }

    public CharArrayRope(StringBuilder sb) {
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
    public CharArrayRope(char[] sequence, int offset, int length) {
        if (length > sequence.length) {
            throw new IllegalArgumentException("Length must be less than " + sequence.length);
        }
        this.sequence = new char[length];
        System.arraycopy(sequence, offset, this.sequence, 0, length);
    }

    @Override
    public char charAt(int index) {
        return sequence[index];
    }

    @Override
    public byte depth() {
        return 0;
    }

    @Override
    public int hashCode() {
        int h = hash;
        if (h == 0) {
            int newhash = Arrays.hashCode(sequence);
            if (newhash == 0) newhash = 1;
            return hash = newhash;
        }
        return h;
    }

    
    /*
     * Implementation Note: This is a reproduction of the AbstractRope
     * indexOf implementation. Calls to charAt have been replaced
     * with direct array access to improve speed.
     */
    @Override
    public int indexOf(char ch) {
        for (int j = 0; j < sequence.length; ++j) {
            if (sequence[j] == ch) {
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
    public int indexOf(char ch, int fromIndex) {
        if (fromIndex < 0 || fromIndex >= length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + fromIndex);
        }
        for (int j = fromIndex; j < length(); ++j) {
            if (sequence[j] == ch) {
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
    public int indexOf(CharSequence sequence, int fromIndex) {
		// Implementation of Boyer-Moore-Horspool algorithm with
        // special support for unicode.

        // step 0. sanity check.
        int length = length();
        if (length == 0) {
            return -1;
        }
        if (length == 1) {
            return indexOf(sequence.charAt(0), fromIndex);
        }

        int[] bcs = new int[256]; // bad character shift
        Arrays.fill(bcs, length);

        // step 1. preprocessing.
        for (int j = 0; j < length - 1; ++j) {
            char c = sequence.charAt(j);
            int l = (c & 0xFF);
            bcs[l] = Math.min(length - j - 1, bcs[l]);
        }

        // step 2. search.
        for (int j = fromIndex + length - 1; j < length();) {
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
    public Iterator<Character> iterator(int start) {
        if (start < 0 || start > length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }

        return new Iterator<Character>() {
            int current = start;

            @Override
            public boolean hasNext() {
                return current < length();
            }

            @Override
            public Character next() {
                return sequence[current++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Rope iterator is read-only.");
            }
        };
    }

    @Override
    public boolean equals(Object other) {
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
    public Iterator<Character> reverseIterator(int start) {
        if (start < 0 || start > length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }
        return new Iterator<Character>() {
            int current = length() - start;

            @Override
            public boolean hasNext() {
                return current > 0;
            }

            @Override
            public Character next() {
                return sequence[--current];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Rope iterator is read-only.");
            }
        };
    }

    @Override
    public Rope subSequence(int start, int end) {
        if (start == 0 && end == length()) {
            return this;
        }
        return end - start < 16 ? new CharArrayRope(sequence, start, end - start) : new SubstringRope(this, start, end - start);
    }

    @Override
    public String toString() {
        return new String(sequence);
    }

    @Override
    public String toString(int offset, int length) {
        return new String(sequence, offset, length);
    }

    @Override
    public void write(Writer out) throws IOException {
        write(out, 0, length());
    }

    @Override
    public void write(Writer out, int offset, int length) throws IOException {
        out.write(sequence, offset, length);
    }
}
