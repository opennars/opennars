/*
 *  ConcatenationRopeReverseIteratorImpl.java
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

import java.util.ArrayDeque;
import java.util.Iterator;

/**
 * A fast reverse iterator for concatenated ropes. Iterating over a complex rope
 * structure is guaranteed to be O(n) so long as it is reasonably well-balanced.
 * Compare this to O(n log n) for iteration using <code>charAt</code>.
 *
 * @author aahmad
 */
public class ConcatenationRopeReverseIteratorImpl implements Iterator<Character> {

    private final ArrayDeque<Rope> toTraverse;
    private final Rope rope;
    private Rope currentRope;
    private int currentRopePos;
    private int skip;
    private int currentAbsolutePos;

    public ConcatenationRopeReverseIteratorImpl(final Rope rope) {
        this(rope, 0);
    }

    public ConcatenationRopeReverseIteratorImpl(final Rope rope, final int start) {
        this.rope = rope;
        this.toTraverse = new ArrayDeque<>();
        this.toTraverse.push(rope);
        this.currentRope = null;
        this.initialize();

        if (start < 0 || start > rope.length()) {
            throw new IllegalArgumentException("Rope index out of range: " + start);
        }
        this.moveForward(start);
    }

    public boolean canMoveBackwards(final int amount) {
        return (this.currentRopePos + amount <= this.currentRope.length());
    }

    public int getPos() {
        return this.currentAbsolutePos;
    }

    @Override
    public boolean hasNext() {
        return this.currentRopePos > 0 || !this.toTraverse.isEmpty();
    }

    /**
     * Initialize the currentRope and currentRopePos fields.
     */
    private void initialize() {
        while (!this.toTraverse.isEmpty()) {
            this.currentRope = this.toTraverse.pop();
            if (this.currentRope instanceof ConcatenationRope) {
                this.toTraverse.push(((ConcatenationRope) this.currentRope).getLeft());
                this.toTraverse.push(((ConcatenationRope) this.currentRope).getRight());
            } else {
                break;
            }
        }
        if (this.currentRope == null) {
            throw new IllegalArgumentException("No terminal ropes present.");
        }
        this.currentRopePos = this.currentRope.length();
        this.currentAbsolutePos = this.rope.length();
    }

    public void moveBackwards(final int amount) {
        if (!this.canMoveBackwards(amount)) {
            throw new IllegalArgumentException("Unable to move backwards " + amount + '.');
        }
        this.currentRopePos += amount;
        this.currentAbsolutePos += amount;
    }

    public void moveForward(final int amount) {
        this.currentAbsolutePos -= amount;
        int remainingAmt = amount;
        while (remainingAmt != 0) {
            if (this.currentRopePos - remainingAmt > -1) {
                this.currentRopePos -= remainingAmt;
                return;
            }
            remainingAmt -= this.currentRopePos;
            if (remainingAmt > 0 && this.toTraverse.isEmpty()) {
                throw new IllegalArgumentException("Unable to move forward " + amount + ". Reached end of rope.");
            }

            while (!this.toTraverse.isEmpty()) {
                this.currentRope = this.toTraverse.pop();
                if (this.currentRope instanceof ConcatenationRope) {
                    this.toTraverse.push(((ConcatenationRope) this.currentRope).getLeft());
                    this.toTraverse.push(((ConcatenationRope) this.currentRope).getRight());
                } else {
                    this.currentRopePos = this.currentRope.length();
                    break;
                }
            }
        }
    }

    @Override
    public Character next() {
        this.moveForward(1 + this.skip);
        this.skip = 0;
        return this.currentRope.charAt(this.currentRopePos);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Rope iterator is read-only.");
    }

    /* (non-Javadoc)
     * @see org.ahmadsoft.ropes.impl.RopeIterators#skip(int)
     */
    public void skip(final int skip) {
        this.skip = skip;
    }
}
