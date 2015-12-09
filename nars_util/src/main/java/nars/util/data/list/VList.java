package nars.util.data.list;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An implementation of the List abstraction backed by a VList. The VList is
 * similar to a standard dynamic array implementation, except that the old
 * arrays are chained together to form the implementation, rather than
 * discarded. For example, here is one possible VList holding six elements:
 * <p>
 * <pre>
 *
 *        +------+     +------+     +------+
 *        | next | --} | next | --} | next | -| nil
 *        +------+     +------+     +------+
 * nil |- | prev | {-- | prev | {-- | prev |
 *        +------+     +------+     +------+
 *        |      |     |   2  |     |   0  |
 *        +------+     +------+     +------+
 *        |   5  |     |   1  |
 *        +------+     +------+
 *        |   4  |
 *        +------+
 *        |   3  |
 *        +------+
 *
 * </pre>
 * <p>
 * Each column represents a linked list cell containing a fixed-size array. The
 * size of this array depends on which cell is being referenced. In particular,
 * the nth cell from the end has 2^n elements in it. In this manner, for a
 * uniformly-randomly chosen position, the expected time to look up that element
 * is O(1), though in the worst case it is O(log n). This random access is
 * significantly better than a linked list, though in the worst case is not as
 * good as a dynamic array.
 * <p>
 * Notice that in each array, elements grow up, with the lower-numbered indices
 * of the array holding elements in the array whose positions are higher. This
 * makes it easier to look up individual elements.
 * <p>
 *
 * @author Keith Schwarz (htiek@cs.stanford.edu)
 *
 * TODO implement custom forEach visitor
 * TODO missing insert
 */
public final class VList<T> extends AbstractList<T> {

    final VisitVList<T, T> getValueFunction = (VListCell<T> cell, int offset) -> {
        if (cell == null) return null;
            /* Return the element in the current position of this array. */
        return cell.mElems[offset];
    };

    /**
     * A single cell in the VList implementation.
     */
    private static final class VListCell<T> {
        public final T[] mElems;
        public final VListCell<T> mNext;

        /* This field is not mutable because when new elements are added/deleted
         * from the main list, the previous pointer needs to be updated.
         * However, next links never change because the list only grows in one
         * direction.
         */
        public VListCell<T> mPrev;

        /* The number of unused elements in this cell. Alternatively, you can
         * think of this as the index in the array in which the first used
         * element appears. Both interpretations are used in this
         * implementation.
         */
        public int mFreeSpace;

        /**
         * Constructs a new VListCell with the specified number of elements and
         * specified next element.
         *
         * @param numElems The number of elements this cell should have space for.
         * @param next     The cell in the list of cells that follows this one.
         */
        public VListCell(int numElems, VListCell<T> next) {
            mElems = (T[]) new Object[numElems];
            mNext = next;
            mPrev = null;

      /* Update the next cell to point back to us. */
            if (next != null)
                next.mPrev = this;

      /* We have free space equal to the number of elements. */
            mFreeSpace = numElems;
        }
    }

    /**
     * A utility struct containing information about where an element is in the
     * VList. Methods that need to manipulate individual elements of the list
     * use this struct to communicate where in the list to look for that
     * element.
     */
    @Deprecated private static final class VListLocation<T> {
        public final VListCell<T> mCell;
        public final int mOffset;

        public VListLocation(VListCell<T> cell, int offset) {
            mCell = cell;
            mOffset = offset;
        }
    }

    @FunctionalInterface
    interface VisitVList<T, Y> {
        Y get(VListCell<T> cell, int offset);
    }

    /* Pointer to the head of the VList, which contains the final elements of
     * the list.
     */
    private VListCell<T> mHead;

    /* Cached total number of elements in the array. */
    private int mSize;

    /**
     * Adds a new element to the end of the array.
     *
     * @param elem The element to add.
     * @return true
     */
    @Override
    public boolean add(T elem) {
    /* If no free space exists, add a new element to the list. */
        if (mHead == null || mHead.mFreeSpace == 0)
            mHead = new VListCell<>(mHead == null ? 1 : mHead.mElems.length * 2, mHead);

    /* Prepend this element to the current cell. */
        mHead.mElems[(mHead.mFreeSpace--) - 1] = elem;
        ++mSize;

    /* Success! */
        return true;
    }

    /**
     * Given an absolute offset into the VList, returns an object describing
     * where that object is in the VList.
     *
     * @param index The index into the VList.
     * @return A VListLocation object holding information about where that
     * element can be found.
     */
    @Deprecated
    private VListLocation<T> locateElement(int index) {
    /* Bounds-check. */
        if (index >= size() || index < 0)
            throw new IndexOutOfBoundsException("Position " + index + "; size "
                    + size());

    /* Because the list is stored with new elements in front and old
     * elements in back, we'll invert the index so that 0 refers to the
     * final element of the array and size() - 1 refers to the first
     * element.
     */
        index = size() - 1 - index;

    /* Scan across the cells, looking for the first one that can hold our
     * entry. We do this by continuously skipping cells until we find one
     * that can be sure to hold this element.
     * 
     * Note that each cell has mElems.length elements, of which mFreeSpace
     * is used. This means that the total number of used elements in each
     * cell is mElems.length - mFreeSpace.
     */
        VListCell<T> curr = mHead;
        int delta;
        while (index >= (delta = (curr.mElems.length - curr.mFreeSpace))) {
      /* Skip past all these elements. */
            index -= delta;
            curr = curr.mNext;
        }

    /* We're now in the correct location for what we need to do. The element
     * we want can be found by indexing the proper amount beyond the free
     * space.
     */
        return new VListLocation<>(curr, index + curr.mFreeSpace);
    }


    /**
     * Given an absolute offset into the VList, returns an object describing
     * where that object is in the VList.
     *
     * @param index The index into the VList.
     * @return A VListLocation object holding information about where that
     * element can be found.
     */
    private <X> X indexFunction(int index, VisitVList<T, X> where) {
    /* Bounds-check. */
        if (index >= size() || index < 0)
            throw new IndexOutOfBoundsException("Position " + index + "; size "
                    + size());

    /* Because the list is stored with new elements in front and old
     * elements in back, we'll invert the index so that 0 refers to the
     * final element of the array and size() - 1 refers to the first
     * element.
     */
        index = size() - 1 - index;

    /* Scan across the cells, looking for the first one that can hold our
     * entry. We do this by continuously skipping cells until we find one
     * that can be sure to hold this element.
     *
     * Note that each cell has mElems.length elements, of which mFreeSpace
     * is used. This means that the total number of used elements in each
     * cell is mElems.length - mFreeSpace.
     */
        VListCell<T> curr = mHead;
        int delta;
        while (index >= (delta = (curr.mElems.length - curr.mFreeSpace))) {
      /* Skip past all these elements. */
            index -= delta;
            curr = curr.mNext;
        }

    /* We're now in the correct location for what we need to do. The element
     * we want can be found by indexing the proper amount beyond the free
     * space.
     */
        return where.get(curr, index + curr.mFreeSpace);
    }


    /**
     * Scans for the proper location in the cell list for the element, then
     * returns the element at that position.
     *
     * @param index The index at which to look up the element.
     * @return The element at that position.
     */
    @Override
    public T get(int index) {
        return indexFunction(index, getValueFunction);
    }

    /**
     * Returns the cached size.
     *
     * @return The size of the VList.
     */
    @Override
    public int size() {
        return mSize;
    }

    /**
     * Sets an element at a particular position to have a particular value.
     *
     * @param index The index at which to write a new value.
     * @param value The value to write at that position.
     * @return The value originally held at that position.
     */
    @Override
    public T set(int index, T value) {
        //TODO use lambda w/ indexFunction here

        VListLocation<T> where = locateElement(index);

    /* Cache the element in the current position of this array. */
        T result = where.mCell.mElems[where.mOffset];
        where.mCell.mElems[where.mOffset] = value;
        return result;
    }

    /**
     * Removes the element at the specified position from the VList, returning
     * its value.
     *
     * @param index The index at which the element should be removed.
     * @return The value held at that position.
     */
    @Override
    public T remove(int index) {
        VListLocation<T> where = locateElement(index);

    /* Cache the value that will be removed. */
        T result = where.mCell.mElems[where.mOffset];

    /* Invoke the helper to do most of the work. */
        removeAtPosition(where);

        return result;
    }

    /**
     * Removes the element at the indicated VListLocation.
     *
     * @param where The location at which the element should be removed.
     */
    private void removeAtPosition(VListLocation<T> where) {
    /* Scan backward across the blocks after this element, shuffling array
     * elements down a position and copying the last element of the next
     * block over to fill in the top.
     * 
     * The variable shuffleTargetPosition indicates the first element of the
     * block that should be overwritten during the shuffle-down. In the
     * first block, this is the position of the element that was
     * overwritten. In all other blocks, it's the last element.
     */
        VListCell<T> curr = where.mCell;
        for (int shuffleTargetPosition = where.mOffset; curr != null;
             curr = curr.mPrev, shuffleTargetPosition = (curr == null ? 0 : curr.mElems.length - 1)) {
      /* Shuffle down each element in the current array on top of the
       * target position. Note that in the final block, this may end up
       * copying a whole bunch of null values down. This is more work than
       * necessary, but is harmless and doesn't change the asymptotic
       * runtime (since the last block has size O(n)).
       */
            System.arraycopy(curr.mElems, 0, curr.mElems, 1, shuffleTargetPosition - 1 + 1);

      /* Copy the last element of the next array to the top of this array,
       * unless this is the first block (in which case there is no next
       * array).
       */
            if (curr.mPrev != null)
                curr.mElems[0] = curr.mPrev.mElems[curr.mPrev.mElems.length - 1];
        }

    /* The head just lost an element, so it has some more free space. Null
     * out the lost element and increase the free space.
     */
        ++mHead.mFreeSpace;
        mHead.mElems[mHead.mFreeSpace - 1] = null;

    /* The whole list just lost an element. */
        --mSize;

    /* If the head is entirely free, remove it from the list. */
        if (mHead.mFreeSpace == mHead.mElems.length) {
            mHead = mHead.mNext;

      /* If there is at least one block left, remove the previous block
       * from the linked list.
       */
            if (mHead != null)
                mHead.mPrev = null;
        }
    }

    /**
     * A custom iterator class that traverses the elements of this container in
     * an intelligent way. The normal iterator will call get repeatedly, which
     * is slow because it has to continuously scan for the proper location of
     * the next element. This iterator works by traversing the cells as a proper
     * linked list.
     */
    private final class VListIterator implements Iterator<T> {
        /* The cell and position in that cell that we are about to visit. We
         * maintain the invariant that if there is a next element, mCurrCell is
         * non-null and conversely that if mCurrCell is null, there is no next
         * element.
         */
        private VListCell<T> mCurrCell;
        private int mCurrIndex;

        /* Stores whether we have something to remove (i.e. whether we've called
         * next() without an invervening remove()).
         */
        private boolean mCanRemove;

        /**
         * Constructs a new VListIterator that will traverse the elements of the
         * containing VList.
         */
        public VListIterator() {
      /* Scan to the tail using the "pointer chase" algorithm. When this
       * terminates, prev will hold a pointer to the last element of the
       * list.
       */
            VListCell<T> curr, prev;
            for (curr = mHead, prev = null; curr != null; prev = curr, curr = curr.mNext)
                ;

      /* Set the current cell to the tail. */
            mCurrCell = prev;

      /* If the tail isn't null, it must be a full list of size 1. Set the
       * current index appropriately.
       */
            if (mCurrCell != null)
                mCurrIndex = 0;
        }

        /**
         * As per our invariant, returns whether mCurrCell is non-null.
         */
        @Override
        public boolean hasNext() {
            return mCurrCell != null;
        }

        /**
         * Advances the iterator and returns the element it used to be over.
         */
        @Override
        public T next() {
      /* Bounds-check. */
            if (!hasNext())
                throw new NoSuchElementException();

      /* Cache the return value; we'll be moving off of it soon. */
            T result = mCurrCell.mElems[mCurrIndex];

      /* Back up one step. */
            --mCurrIndex;

      /* If we walked off the end of the buffer, advance to the next
       * element of the list.
       */
            if (mCurrIndex < mCurrCell.mFreeSpace) {
                mCurrCell = mCurrCell.mPrev;

        /* Update the next get location, provided of course that we
         * didn't just walk off the end of the list.
         */
                if (mCurrCell != null)
                    mCurrIndex = mCurrCell.mElems.length - 1;
            }

      /* Since there was indeed an element, we can remove it. */
            mCanRemove = true;

            return result;
        }

        /**
         * Removes the last element we visited.
         */
        @Override
        public void remove() {
      /* Check whether there's something to remove. */
            if (!mCanRemove)
                throw new IllegalStateException(
                        "remove() without next(), or double remove().");

      /* Clear the flag saying we can do this. */
            mCanRemove = false;

      /* There are several cases to consider. If the current cell is null,
       * we've walked off the end of the array, so we want to remove the
       * very last element. If the current cell isn't null and the cursor
       * is in the middle, remove the previous element and back up a step.
       * If the current cell isn't null and the cursor is at the front,
       * remove the element one step before us and back up a step.
       */

      /* Case 1. */
            if (mCurrCell == null)
                VList.this.remove(size() - 1);
      /* Case 2. */
            else if (mCurrIndex != mCurrCell.mElems.length - 1) {
        /* Back up a step, and remove the element at this position.
         * After the remove completes, the element here should be the
         * next element to visit.
         */
                ++mCurrIndex;
                removeAtPosition(new VListLocation<>(mCurrCell, mCurrIndex));
            }
      /* Case 3. */
            else {
        /* Back up a step to the top of the previous list. We know that
         * the top will be at position 0, since all internal blocks are
         * completely full. We also know that we aren't at the very
         * front of the list, since if we were, then the call to next()
         * that enabled this call would have pushed us to the next
         * location.
         */
                mCurrCell = mCurrCell.mNext;
                mCurrIndex = 0;
                removeAtPosition(new VListLocation<>(mCurrCell, mCurrIndex));
            }
        }
    }

    /**
     * Returns a custom iterator rather than the default.
     */
    @Override
    public Iterator<T> iterator() {
        return new VListIterator();
    }
}