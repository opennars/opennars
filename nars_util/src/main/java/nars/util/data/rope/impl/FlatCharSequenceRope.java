/*
 *  FlatCharSequenceRope.java
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A rope constructed from an underlying character sequence.
 *
 * @author Amin Ahmad
 */
public class FlatCharSequenceRope extends AbstractRope implements FlatRope {

    public final CharSequence sequence;

    /**
     * Constructs a new rope from an underlying character sequence.
     *
     * @param sequence
     */
    public FlatCharSequenceRope(final CharSequence sequence) {
        this.sequence = sequence;
    }

    @Override
    public char charAt(final int index) {
        return sequence.charAt(index);
    }

    @Override
    public byte depth() {
        return 0;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof FlatCharSequenceRope) {
            CharSequence otherSeq = ((FlatCharSequenceRope)other).sequence;            
            if ((sequence.getClass() == String.class) && (otherSeq.getClass() == String.class)) {
                return sequence.equals(otherSeq);
            }
            return super.equals(other);
        }
        return false;
    }
    
    

    @Override
    public Iterator<Character> iterator(final int start) {
        if (start < 0 || start > this.length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }

        return new Iterator<Character>() {
            int current = start;
            final int len = length();

            @Override
            public boolean hasNext() {
                return current < len;
            }

            @Override
            public Character next() {
                return sequence.charAt(current++);
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Rope iterator is read-only.");
            }
        };
    }

    @Override
    public int length() {
        return sequence.length();
    }

    @Override
    public Matcher matcher(final Pattern pattern) {
        // optimized to return a matcher directly on the underlying sequence.
        return pattern.matcher(this.sequence);
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
            int current = FlatCharSequenceRope.this.length() - start;

            @Override
            public boolean hasNext() {
                return this.current > 0;
            }

            @Override
            public Character next() {
                return FlatCharSequenceRope.this.sequence.charAt(--this.current);
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
        if (end - start < 8 || this.sequence instanceof String /* special optimization for String */) {
            return new FlatCharSequenceRope(this.sequence.subSequence(start, end));
        } else {
            return new SubstringRope(this, start, end - start);
        }
    }

    @Override
    public String toString() {
        return this.sequence.toString();
    }

    @Override
    public String toString(final int offset, final int length) {
        return this.sequence.subSequence(offset, offset + length).toString();
    }

    @Override
    public void write(final Writer out) throws IOException {
        this.write(out, 0, this.length());
    }

    @Override
    public void write(final Writer out, final int offset, final int length) throws IOException {
        if (offset < 0 || offset + length > this.length()) {
            throw new IndexOutOfBoundsException("Rope index out of bounds:" + (offset < 0 ? offset : offset + length));
        }

        if (this.sequence instanceof String) {	// optimization for String
            out.write(((String) this.sequence).substring(offset, offset + length));
            return;
        }
        for (int j = offset; j < offset + length; ++j) {
            out.write(this.sequence.charAt(j));
        }
    }
}
