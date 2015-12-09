/*
 *  ConcatenationRope.java
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
 * A rope that represents the concatenation of two other ropes.
 *
 * @author Amin Ahmad
 */
public class ConcatenationRope extends AbstractRope {

    public final Rope left;
    public final Rope right;
    private final byte depth;
    private final int length;

    /**
     * Create a new concatenation rope from two ropes.
     *
     * @param left the first rope.
     * @param right the second rope.
     */
    public ConcatenationRope(Rope left, Rope right) {
        this.left = left;
        this.right = right;
        depth = (byte) (Math.max(Rope.depth(left), Rope.depth(right)) + 1);
        length = left.length() + right.length();
    }

    @Override
    public char charAt(int index) {
        //if (index >= this.length())
        //    throw new IndexOutOfBoundsException("Rope index out of range: " + index);

        int leftLen = left.length();
        return (index < leftLen) ? left.charAt(index) : right.charAt(index - leftLen);
    }

    @Override
    public byte depth() {
        return depth;
    }

    @Override
    public CharSequence getForSequentialAccess() {
        return getForSequentialAccess(this);
    }

    /*
     * Returns this object as a char sequence optimized for
     * regular expression searches.
     * <p>
     * <emph>This method is public only to facilitate unit
     * testing.</emph>
     */
    private CharSequence getForSequentialAccess(Rope rope) {
        return new CharSequence() {

            private final ConcatenationRopeIteratorImpl iterator = (ConcatenationRopeIteratorImpl) rope.iterator(0);

            @Override
            public char charAt(int index) {
                if (index > iterator.getPos()) {
                    iterator.skip(index - iterator.getPos() - 1);
                    try {
                        char c = iterator.next();
                        return c;
                    } catch (IllegalArgumentException e) {
                        System.out.println("Rope length is: " + rope.length() + " charAt is " + index);
                        throw e;
                    }
                } else { /* if (index <= lastIndex) */

                    int toMoveBack = iterator.getPos() - index + 1;
                    if (iterator.canMoveBackwards(toMoveBack)) {
                        iterator.moveBackwards(toMoveBack);
                        return iterator.next();
                    } else {
                        return rope.charAt(index);
                    }
                }
            }

            @Override
            public int length() {
                return rope.length();
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return rope.subSequence(start, end);
            }

        };
    }

    /**
     * Return the left-hand rope.
     *
     * @return the left-hand rope.
     */
    public Rope getLeft() {
        return left;
    }

    /**
     * Return the right-hand rope.
     *
     * @return the right-hand rope.
     */
    public Rope getRight() {
        return right;
    }

    @Override
    public Iterator<Character> iterator(int start) {
        if (start < 0 || start > length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }
        return start >= left.length() ? right.iterator(start - left.length()) : new ConcatenationRopeIteratorImpl(this, start);
    }

    @Override
    public int length() {
        return length;
    }

    @Override
    public Rope rebalance() {
        return Rope.rebalance(this);
    }

    @Override
    public Rope reverse() {
        return Rope.cat(getRight().reverse(), getLeft().reverse());
    }

    @Override
    public Iterator<Character> reverseIterator(int start) {
        if (start < 0 || start > length()) {
            throw new IndexOutOfBoundsException("Rope index out of range: " + start);
        }
        return start >= right.length() ? left.reverseIterator(start - right.length()) : new ConcatenationRopeReverseIteratorImpl(this, start);
    }

    @Override
    public Rope subSequence(int start, int end) {
        if (start < 0 || end > length()) {
            throw new IllegalArgumentException("Illegal subsequence (" + start + ',' + end + ')');
        }
        if (start == 0 && end == length()) {
            return this;
        }
        int l = left.length();
        if (end <= l) {
            return left.subSequence(start, end);
        }
        if (start >= l) {
            return right.subSequence(start - l, end - l);
        }
        return Rope.cat(
                left.subSequence(start, l),
                right.subSequence(0, end - l));
    }

    @Override
    public void write(Writer out) throws IOException {
        left.write(out);
        right.write(out);
    }

    @Override
    public void write(Writer out, int offset, int length) throws IOException {
        if (offset + length <= left.length()) {
            left.write(out, offset, length);
        } else if (offset >= left.length()) {
            right.write(out, offset - left.length(), length);
        } else {
            int writeLeft = left.length() - offset;
            left.write(out, offset, writeLeft);
            right.write(out, 0, length - writeLeft);
        }
    }

//  /**
//   * Not currently used. Can be used if rebalancing is performed
//   * during concatenation.
//   **/
//  private ConcatenationRope rotateLeft(final ConcatenationRope input) {
//      final Rope _R = input.getRight();
//      if (!(_R instanceof ConcatenationRope))
//          return input;
//      final ConcatenationRope R = (ConcatenationRope) _R;
//      final Rope L = input.getLeft();
//      final Rope A = R.getLeft();
//      final Rope B = R.getRight();
//      return new ConcatenationRope(new ConcatenationRope(L, A), B);
//  }
//
//  /**
//   * Not currently used. Can be used if rebalancing is performed
//   * during concatenation.
//   **/
//  private ConcatenationRope rotateRight(final ConcatenationRope input) {
//      final Rope _L = input.getLeft();
//      if (!(_L instanceof ConcatenationRope))
//          return input;
//      final ConcatenationRope L = (ConcatenationRope) _L;
//      final Rope R = input.getRight();
//      final Rope A = L.getLeft();
//      final Rope B = L.getRight();
//      return new ConcatenationRope(A, new ConcatenationRope(B, R));
//  }
}
