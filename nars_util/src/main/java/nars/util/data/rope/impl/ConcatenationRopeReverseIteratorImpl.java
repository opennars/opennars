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

    public ConcatenationRopeReverseIteratorImpl(Rope rope) {
        this(rope, 0);
    }

    public ConcatenationRopeReverseIteratorImpl(Rope rope, int start) {
        this.rope = rope;
        toTraverse = new ArrayDeque<>();
        toTraverse.push(rope);
        currentRope = null;
        initialize();

        if (start < 0 || start > rope.length()) {
            throw new IllegalArgumentException("Rope index out of range: " + start);
        }
        moveForward(start);
    }

    public boolean canMoveBackwards(int amount) {
        return (currentRopePos + amount <= currentRope.length());
    }

    public int getPos() {
        return currentAbsolutePos;
    }

    @Override
    public boolean hasNext() {
        return currentRopePos > 0 || !toTraverse.isEmpty();
    }

    /**
     * Initialize the currentRope and currentRopePos fields.
     */
    private void initialize() {
        while (!toTraverse.isEmpty()) {
            currentRope = toTraverse.pop();
            if (currentRope instanceof ConcatenationRope) {
                toTraverse.push(((ConcatenationRope) currentRope).getLeft());
                toTraverse.push(((ConcatenationRope) currentRope).getRight());
            } else {
                break;
            }
        }
        if (currentRope == null) {
            throw new IllegalArgumentException("No terminal ropes present.");
        }
        currentRopePos = currentRope.length();
        currentAbsolutePos = rope.length();
    }

    public void moveBackwards(int amount) {
        if (!canMoveBackwards(amount)) {
            throw new IllegalArgumentException("Unable to move backwards " + amount + '.');
        }
        currentRopePos += amount;
        currentAbsolutePos += amount;
    }

    public void moveForward(int amount) {
        currentAbsolutePos -= amount;
        int remainingAmt = amount;
        while (remainingAmt != 0) {
            if (currentRopePos - remainingAmt > -1) {
                currentRopePos -= remainingAmt;
                return;
            }
            remainingAmt -= currentRopePos;
            if (remainingAmt > 0 && toTraverse.isEmpty()) {
                throw new IllegalArgumentException("Unable to move forward " + amount + ". Reached end of rope.");
            }

            while (!toTraverse.isEmpty()) {
                currentRope = toTraverse.pop();
                if (currentRope instanceof ConcatenationRope) {
                    toTraverse.push(((ConcatenationRope) currentRope).getLeft());
                    toTraverse.push(((ConcatenationRope) currentRope).getRight());
                } else {
                    currentRopePos = currentRope.length();
                    break;
                }
            }
        }
    }

    @Override
    public Character next() {
        moveForward(1 + skip);
        skip = 0;
        return currentRope.charAt(currentRopePos);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Rope iterator is read-only.");
    }

    /* (non-Javadoc)
     * @see org.ahmadsoft.ropes.impl.RopeIterators#skip(int)
     */
    public void skip(int skip) {
        this.skip = skip;
    }
}
