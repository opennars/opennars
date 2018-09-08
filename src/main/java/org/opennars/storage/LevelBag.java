/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.storage;

import org.opennars.entity.Item;
import org.opennars.main.MiscFlags;

import java.io.Serializable;
import java.util.*;
import org.opennars.main.Parameters;

/**
 * Original Bag implementation which distributes items into
 * discrete levels (queues) according to priority
 */
public class LevelBag<E extends Item<K>,K> extends Bag<E,K> implements Serializable {

    /**
     * priority levels
     */
    public final int levels;
    /**
     * firing threshold
     */
    public final int fireCompleteLevelThreshold;

    /**
     * shared DISTRIBUTOR that produce the probability distribution
     */
    final short[] DISTRIBUTOR;

    /**
     * mapping from key to item
     */
    //public final Set<E> nameTable;
    public final Map<K, E> nameTable;

    /**
     * array of lists of items, for items on different level
     */
    public final Level<E>[] level;

    /**
     * defined in different bags
     */
    final int capacity;
    /**
     * current sum of occupied level
     */
    private float mass;
    /**
     * index to get next level, kept in individual objects
     */
    int levelIndex;
    /**
     * current take out level
     */
    int currentLevel;
    /**
     * maximum number of items to be taken out at current level
     */
    int currentCounter;
    
    
    public LevelBag(final int levels, final int capacity, Parameters narParameters) {
        this(levels, capacity, (int) (narParameters.BAG_THRESHOLD * levels));
    }

    /** thresholdLevel = 0 disables "fire level completely" threshold effect */
    public LevelBag(final int levels, final int capacity, final int thresholdLevel) {
        this.levels = levels;
        this.fireCompleteLevelThreshold = thresholdLevel;
        //THRESHOLD = levels + 1; //fair/flat takeOut policy
        this.capacity = capacity;
        nameTable = new HashMap<>(capacity);
        level = new Level[this.levels];
        DISTRIBUTOR = Distributor.get(this.levels).order;
        distributorLength = DISTRIBUTOR.length;        
        clear();
    }

    public class Level<E> extends LinkedHashSet<E> implements Serializable {
       @Override
       public boolean add(final E e) {
            if (e == null) {
               throw new IllegalStateException("Bag requires non-null items");
            }
            return super.add(e);
        }
        public E removeFirst() {
            final E e = this.iterator().next();
            this.remove(e);
            return e;
        }
    }
    
    public boolean levelEmpty(int i) {
        return level[i]==null || level[i].isEmpty();
    }
    
    @Override
    public final void clear() {
        for (int i = 0; i < levels; i++) {
            if (level[i] != null) {
                level[i].clear();
            }
        }
        nameTable.clear();
        currentLevel = levels - 1;
        levelIndex = capacity % levels; // so that different bags start at different point
        mass = 0;
        currentCounter = 0;
    }

    /**
     * The number of items in the bag
     *
     * @return The number of items
     */
    @Override
    public int size() {
        final int in = nameTable.size();
        if (MiscFlags.DEBUG_BAG && (MiscFlags.DEBUG)) {
            final int is = sizeItems();
            if (Math.abs(is-in) > 1 ) {                
                throw new IllegalStateException(this.getClass() + " inconsistent index: items=" + is + " names=" + in + ", capacity=" + getCapacity());
            }
        }
        return in;
    }
        
    /** this should always equal size(), but it's here for testing purposes */
    protected int sizeItems() {
        int t = 0;
        for (final Level<E> l : level) {
            if (l!=null)
                t += l.size();
        }
        return t;
    }

    /**
     * Get the average priority of Items
     *
     * @return The average priority of Items in the bag
     */
    @Override
    public float getAveragePriority() {
        if (size() == 0) {
            return 0.0f;
        }
        return Math.min(mass / size(), 1.0f);
    }

    /**
     * Get an Item by key
     *
     * @param key The key of the Item
     * @return The Item with the given key
     */
    @Override
    public E get(final K key) {
        return nameTable.get(key);
    }

    final int distributorLength;
    
    /** look for a non-empty level */
    protected void nextNonEmptyLevel() {
        int cl = currentLevel;
        
        // we need to do this this way to avoid a overflow of levelIndex
        do {
            if( !levelEmpty(cl = DISTRIBUTOR[levelIndex % distributorLength]) ) {
                levelIndex++;
                break;
            }

            levelIndex = (levelIndex+1) % distributorLength;
        } while (true);
        
        currentLevel = cl;  
        if (currentLevel < fireCompleteLevelThreshold) { // for dormant levels, take one item
            currentCounter = 1;
        } else {                  // for active levels, take all current items
            currentCounter = this.level[currentLevel].size();
        }
    }
    
    @Override
    public E takeNext() {
        if (size() == 0) {
            return null; // empty bag                
        }
        if (levelEmpty(currentLevel) || (currentCounter == 0)) { // done with the current level
            nextNonEmptyLevel();
        }
        if (levelEmpty(currentLevel)) {
            throw new IllegalStateException("Empty level selected for takeNext");
        }
        final E selected = takeOutFirst(currentLevel); // take out the first item in the level
        currentCounter--;        
        return selected;
    }

    @Override public E take(final K name) {
        final E oldItem = nameTable.remove(name);
        if (oldItem == null) {
            return null;
        }
        final int expectedLevel = getLevel(oldItem);
        //TODO scan up/down iteratively, it is likely to be near where it was
        if (!levelEmpty(expectedLevel)) {
            if (level[expectedLevel].remove(oldItem)) {                
                removeMass(oldItem);
                return oldItem;
            }            
        }
        for (int l = 0; l < levels; l++) {
            if ((!levelEmpty(l)) && (l!=expectedLevel)) {
                if (level[l].remove(oldItem)) {
                    removeMass(oldItem);
                    return oldItem;
                }
            }
        }
        //If it wasn't found, it probably was removed already.  So this check is probably not necessary
            //search other levels for this item because it's not where we thought it was according to getLevel()
        if (MiscFlags.DEBUG) {
            final int ns = nameTable.size();
            final int is = sizeItems();
            if (ns == is)
                return null;
            throw new IllegalStateException("LevelBag inconsistency: " + nameTable.size() + "|" + sizeItems() + " Can not remove missing element: size inconsistency" + oldItem + " from " + this.getClass().getSimpleName());
        }
        return oldItem;
    }

    /**
     * Decide the put-in level according to priority
     *
     * @param item The Item to put in
     * @return The put-in level
     */
    private int getLevel(final E item) {
        final float fl = item.getPriority() * levels;
        final int level = (int) Math.ceil(fl) - 1;
        if (level < 0) return 0;
        if (level >= levels) return levels-1;
        return level;
    }
    
    /**
     * Insert an item into the itemTable, and return the overflow
     *
     * @param newItem The Item to put in
     * @return null if nothing overflowed, non-null if an overflow Item, which
     * may be the attempted input item (in which case it was not inserted)
     */
    @Override public E addItem(final E newItem) {
        E oldItem = null;
        final int inLevel = getLevel(newItem);
        if (size() >= capacity) {      // the bag will be full after the next 
            int outLevel = 0;
            while (levelEmpty(outLevel)) {
                outLevel++;
            }
            if (outLevel > inLevel) {           // ignore the item and exit
                return newItem;
            } else {                            // remove an old item in the lowest non-empty level
                oldItem = takeOutFirst(outLevel);
            }
        }
        if (this.level[inLevel] == null) {
            this.level[inLevel] = new Level<E>();
        }
        level[inLevel].add(newItem);        // FIFO
        nameTable.put(newItem.name(), newItem);        
        addMass(newItem);
        return oldItem;
    }

    /**
     * Take out the first or last E in a level from the itemTable
     *
     * @param level The current level
     * @return The first Item
     */
    private E takeOutFirst(final int level) {
        final E selected = this.level[level].removeFirst();
        if (selected!=null) {
            nameTable.remove(selected.name());
            removeMass(selected);
        }
        else {
            throw new IllegalStateException("Attempt to remove item from empty level: " + level);
        }
        return selected;
    }

    protected void removeMass(final E item) {
        mass -= item.getPriority();
    }
    protected void addMass(final E item) {
        mass += item.getPriority();
    }

    /**
     * TODO refactor : paste from preceding method
     */
    public String toStringLong(final int minLevel) {
        StringBuilder buf = new StringBuilder(32)
                .append(" BAG ").append(getClass().getSimpleName())
                .append(" ").append(showSizes());

        for (int i = levels; i >= minLevel; i--) {
            if (!levelEmpty(i - 1)) {
                buf = buf.append("\n --- LEVEL ").append(i).append(":\n ");
                for (final E e : level[i - 1]) {
                    buf = buf.append(e.toStringLong()).append('\n');
                }

            }
        }
        buf.append(">>>> end of Bag").append(getClass().getSimpleName());
        return buf.toString();
    }

    /**
     * show item Table Sizes
     */
    public String showSizes() {
        final StringBuilder buf = new StringBuilder(" ");
        int l = 0;
        for (final Level<E> items : level) {
            final int s = items.size();
            if ((items != null) && (s > 0)) {
                l++;
                buf.append(s).append(' ');
            }
        }
        return "Levels: " + Integer.toString(l) + ", sizes: " + buf;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    public Iterable<E> getLevel(final int i) {
        if (level[i] == null) {
            return Collections.emptyList();
        }
        return level[i];
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {
            int l = level.length - 1;
            private Iterator<E> levelIterator;
            private E next;
            final int size = size();
            int count = 0;

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasNext() {
                if (next != null) {
                    return true;
                }
                if (l >= 0 && levelIterator == null) {
                    while (levelEmpty(l)) {
                        if (--l == -1)
                            return false; //end of the levels
                    }
                    levelIterator = level[l].iterator();
                }
                if (levelIterator == null) {
                    return false;
                }
                next = levelIterator.next();
                count++;
                if (levelIterator.hasNext()) {
                    return true;
                } else {
                    levelIterator = null;
                    l--;
                    return count <= size;
                }
            }

            @Override
            public E next() {
                final E e = next;
                next = null;
                return e;
            }
        };
    }
}
