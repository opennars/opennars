/*
 *  AbstractRope.java
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
import java.io.ObjectStreamException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract base class for ropes that implements many of the common operations.
 *
 * @author Amin Ahmad
 */
public abstract class AbstractRope implements Rope {

    protected int hashCode = 0;

    @Override
    public Rope append(final char c) {
        return Rope.cat(this, Rope.build(String.valueOf(c)));
    }

    @Override
    public Rope append(final CharSequence suffix) {
        return Rope.cat(this, Rope.build(suffix));
    }

    @Override
    public Rope append(final CharSequence csq, final int start, final int end) {
        return Rope.cat(this, Rope.build(csq).subSequence(start, end));
    }

    @Override
    public int compareTo(final CharSequence sequence) {

        final int minCommonLength = Math.min(sequence.length(), this.length());
        //final Iterator<Character> i = this.iterator();

        for (int j = 0; j < minCommonLength; j++) {
            final char x = charAt(j);
            final char y = sequence.charAt(j);
            if (x != y) {
                return x - y;
            }
        }
        return this.length() - sequence.length();
    }

    @Override
    public Rope delete(final int start, final int end) {
        if (start == end) {
            return this;
        }
        return this.subSequence(0, start).append(this.subSequence(end, this.length()));
    }

    /*
     * The depth of the current rope, as defined in "Ropes: an Alternative
     * to Strings".
     */
    public abstract byte depth();

    @Override
    public boolean equals(final Object other) {
        System.out.println(this.getClass() + " " + Arrays.toString(new Exception().getStackTrace()));
        
        if (this == other) return true;

        if (other instanceof Rope) {
            final Rope rope = (Rope) other;
                        
            if (rope.length() != this.length()) {
                return false;
            }

            if (rope.hashCode() != this.hashCode()) {
                return false;
            }

            //non-iterator version
            for (int j = 0; j < length(); j++) {
                final char x = charAt(j);
                final char y = rope.charAt(j);
                if (x != y) {
                    return false;
                }
            }

            /*final Iterator<Character> i1 = this.iterator();
             final Iterator<Character> i2 = rope.iterator();

             while (i1.hasNext()) {
             final char a = i1.next();
             final char b = i2.next();
             if (a != b) {
             return false;
             }
             }*/
            return true;
        }
        return false;
    }

    /**
     * A utility method that returns an instance of this rope optimized for
     * sequential access.
     *
     * @return
     */
    protected CharSequence getForSequentialAccess() {
        return this;
    }

    @Override
    public int hashCode() {

        if (this.hashCode == 0 && this.length() > 0) {
            if (this.length() < 6) {
                for (final char c : this) {
                    this.hashCode = 31 * this.hashCode + c;
                }
            } else {
                final Iterator<Character> i = this.iterator();
                for (int j = 0; j < 5; ++j) {
                    this.hashCode = 31 * this.hashCode + i.next();
                }
                this.hashCode = 31 * this.hashCode + this.charAt(this.length() - 1);
            }
        }
        return this.hashCode;
    }

    @Override
    public int indexOf(final char ch) {
        int index = -1;
        for (final char c : this) {
            ++index;
            if (c == ch) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public boolean startsWith(CharSequence prefix) {
        return startsWith(prefix, 0);
    }

    @Override
    public boolean startsWith(CharSequence prefix, int offset) {
        if (offset < 0 || offset > this.length()) {
            throw new IndexOutOfBoundsException("Rope offset out of range: " + offset);
        }
        if (offset + prefix.length() > this.length()) {
            return false;
        }

        int x = 0;
        for (Iterator<Character> i = this.iterator(offset); i.hasNext() && x < prefix.length();) {
            if (i.next() != prefix.charAt(x++)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean endsWith(CharSequence suffix) {
        return endsWith(suffix, 0);
    }

    @Override
    public boolean endsWith(CharSequence suffix, int offset) {
        return startsWith(suffix, length() - suffix.length() - offset);
    }

    @Override
    public int indexOf(final char ch, final int fromIndex) {
        if (fromIndex < 0 || fromIndex >= this.length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + fromIndex);
        }
        int index = fromIndex - 1;
        for (final Iterator<Character> i = this.iterator(fromIndex); i.hasNext();) {
            ++index;
            if (i.next() == ch) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public int indexOf(final CharSequence sequence) {
        return this.indexOf(sequence, 0);
    }

    @Override
    public int indexOf(final CharSequence sequence, final int fromIndex) {
        final CharSequence me = this.getForSequentialAccess();

        // Implementation of Boyer-Moore-Horspool algorithm with
        // special support for unicode.
        // step 0. sanity check.
        final int length = sequence.length();
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
                final char c = me.charAt(x);
                if (sequence.charAt(y) != c) {
                    j += bcs[(me.charAt(j) & 0xFF)];
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
    public Rope insert(final int dstOffset, final CharSequence s) {
        final Rope r = (s == null) ? Rope.build("null") : Rope.build(s);
        if (dstOffset == 0) {
            return r.append(this);
        } else if (dstOffset == this.length()) {
            return this.append(r);
        } else if (dstOffset < 0 || dstOffset > this.length()) {
            throw new IndexOutOfBoundsException(dstOffset + " is out of insert range [" + 0 + ':' + this.length() + ']');
        }
        return this.subSequence(0, dstOffset).append(r).append(this.subSequence(dstOffset, this.length()));
    }

    @Override
    public Iterator<Character> iterator() {
        return this.iterator(0);
    }

    @Override
    public Rope trimStart() {
        int index = -1;
        for (final char c : this) {
            ++index;
            if (c > 0x20 && !Character.isWhitespace(c)) {
                break;
            }
        }
        if (index <= 0) {
            return this;
        } else {
            return this.subSequence(index, this.length());
        }
    }

    @Override
    public Matcher matcher(final Pattern pattern) {
        return pattern.matcher(this.getForSequentialAccess());
    }

    @Override
    public boolean matches(final Pattern regex) {
        return regex.matcher(this.getForSequentialAccess()).matches();
    }

    @Override
    public boolean matches(final String regex) {
        return Pattern.matches(regex, this.getForSequentialAccess());
    }

    @Override
    public Rope rebalance() {
        return this;
    }

    @Override
    public Iterator<Character> reverseIterator() {
        return this.reverseIterator(0);
    }

    @Override
    public Rope trimEnd() {
        int index = this.length() + 1;
        for (final Iterator<Character> i = this.reverseIterator(); i.hasNext();) {
            final char c = i.next();
            --index;
            if (c > 0x20 && !Character.isWhitespace(c)) {
                break;
            }
        }
        if (index >= this.length()) {
            return this;
        } else {
            return this.subSequence(0, index);
        }
    }

    @Override
    public String toString() {

        final StringWriter out = new StringWriter(this.length());
        try {
            this.write(out);
            out.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        return out.toString();
    }

    @Override
    public Rope trim() {
        return this.trimStart().trimEnd();
    }

    public Object writeReplace() throws ObjectStreamException {
        return new SerializedRope(this);
    }

    @Override
    public Rope padStart(final int toWidth) {
        return padStart(toWidth, ' ');
    }

    @Override
    public Rope padStart(final int toWidth, final char padChar) {
        final int toPad = toWidth - this.length();
        if (toPad < 1) {
            return this;
        }
        return Rope.cat(
                Rope.build(new RepeatedCharacterSequence(padChar, toPad)),
                this);
    }

    @Override
    public Rope padEnd(final int toWidth) {
        return padEnd(toWidth, ' ');
    }

    @Override
    public Rope padEnd(final int toWidth, final char padChar) {
        final int toPad = toWidth - this.length();
        if (toPad < 1) {
            return this;
        }
        return Rope.cat(
                this,
                Rope.build(new RepeatedCharacterSequence(padChar, toPad)));
    }

    @Override
    public boolean isEmpty() {
        return length() == 0;
    }
}
