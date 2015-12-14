///*
// * Bag.java
// *
// * Copyright (C) 2008  Pei Wang
// *
// * This file is part of Open-NARS.
// *
// * Open-NARS is free software; you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 2 of the License, or
// * (at your option) any later version.
// *
// * Open-NARS is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
// */
//package nars.bag.impl;
//
//import com.google.common.base.Function;
//import com.google.common.collect.Iterators;
//import com.google.common.collect.Lists;
//import nars.Global;
//import nars.bag.Bag;
//import nars.bag.BagBudget;
//import nars.budget.Budget;
//import nars.budget.Itemized;
//import nars.util.data.ReversibleRecyclableArrayIterator;
//import nars.util.data.linkedlist.DD;
//import nars.util.data.linkedlist.DDList;
//import nars.util.data.linkedlist.DDNodePool;
//import nars.util.math.Distributor;
//
//import java.util.*;
//import java.util.function.Consumer;
//import java.util.stream.Collectors;
//
///**
// * Original Bag implementation which distributes items into
// * discrete levels (queues) according to priority
// *
// * TODO recycle Level[] arrays in an object pool when a LevelBag is finished, or just recycle anentire Concept
// */
//public class LevelBag<K, E extends Itemized<K>> extends Bag<K, E> {
//
//
//    /**
//     * priority levels
//     */
//    public final int levels;
//    /**
//     * firing threshold
//     */
//    public final int fireCompleteLevelThreshold;
//
//    /**
//     * shared DISTRIBUTOR that produce the probability distribution
//     */
//    final short[] DISTRIBUTOR;
//
//
//
//    /**
//     * defined in different bags
//     */
//    int capacity;
//
//    private final boolean ownsNodePool;
//
//    /**
//     * index to get next level, kept in individual objects
//     */
//    int levelIndex;
//    /**
//     * current take out level
//     */
//    int currentLevel;
//    /**
//     * maximum number of items to be taken out at current level
//     */
//
//    int currentCounter;
//
//    protected final boolean[] levelEmpty;
//
//    private final DDNodePool<E> nodePool;
//
//
//    /**
//     * mapping from key to item
//     */
//    public final Map<K, DD<E>> index;
//
//    /**
//     * array of lists of items, for items on different level
//     */
//    public final Level[] level;
//
//    public enum NextNonEmptyLevelMode {
//        Default, Fast
//    }
//
//    NextNonEmptyLevelMode nextNonEmptyMode = NextNonEmptyLevelMode.Default;
//
//
//    public LevelBag(int levels, int capacity) {
//        this(levels, capacity, 0);
//    }
//
//
//    public LevelBag(int levels, int capacity, int thresholdLevel) {
//        this(Global.newHashMap(capacity*2), levels, capacity, thresholdLevel);
//    }
//    /**
//     * thresholdLevel = 0 disables "fire level completely" threshold effect
//     */
//    public LevelBag(Map<K, DD<E>> indexMap, int levels, int capacity, int thresholdLevel) {
//        this.levels = levels;
//
//        nodePool = new DDNodePool(capacity / 8);
//        ownsNodePool = true;
//
//        fireCompleteLevelThreshold = thresholdLevel;
//        //THRESHOLD = levels + 1; //fair/flat takeOut policy
//
//        this.capacity = capacity;
//
//        index = indexMap;
//
//        levelEmpty = new boolean[levels];
//
//        level = new Level[levels];
//
//        //TODO use inverted condition to avoid needing to always setting this to true here
//        Arrays.fill(levelEmpty, true);
//
//        DISTRIBUTOR = Distributor.get(levels).order;
//
//        clear();
//    }
//
//
//    /** high performance linkedhashset/deque for use as a levelbag level */
//    public static class Level<KK,EE extends Itemized<KK>> extends DDList<EE> {
//
//
//        private final boolean[] levelEmpty;
//
//        public Level(boolean[] levelEmpty, DDNodePool<EE> nodePool, int level) {
//            super(level, nodePool);
//            this.levelEmpty = levelEmpty;
//        }
//
//        @Override
//        public void changed() {
//            levelEmpty[getID()] = isEmpty();
//        }
//
//        public EE removeFirst() {
//            DD<EE> first = getFirstNode();
//            if (first == null) return null;
//            return remove(first);
//        }
//
//        public void print() {
//            System.out.println("head=" + super.getFirst() + ", tail=" + super.getLast() + ", ");
//            System.out.println("  " + Lists.newArrayList(iterator()));
//        }
//
//        /*public E removeFirst() {
//            E e = getFirst();
//            if (e == null) return null;
//            return remove(e);
//        }*/
//
//        public DD<EE> peekFirst() {
//            return getFirstNode();
//        }
//
//        public Iterator<EE> descendingIterator() {
//            //order wont matter within the level
//            return iterator();
//            //return items.descendingIterator();
//        }
//
//
//    }
//
//    public final Level<K,E> level(int l) {
//        return (Level<K,E>)level[l];
//    }
//
//    @Override
//    public final void clear() {
//
//        index.clear();
//        for (int i = 0; i < levels; i++) {
//            if (level[i] != null) {
//                level[i].clear();
//            }
//        }
//
//        currentLevel = levels - 1;
//        levelIndex = capacity % levels; // so that different bags start at different point
//        currentCounter = 0;
//    }
//
//    @Override public void delete() {
//
//        if (ownsNodePool)
//            nodePool.delete();
//
//        index.clear();
//        for (int i = 0; i < levels; i++) {
//            if (level[i] != null) {
//                level[i].delete();
//                level[i] = null;
//            }
//        }
//
//    }
//
//    /**
//     * The number of items in the bag
//     *
//     * @return The number of items
//     */
//    @Override
//    public int size() {
//
//        int in = index.size();
//
//        if (Global.DEBUG) {
//
//            int is = sizeItems();
//            if (Math.abs(is - in) > 1) {
//                printAll(System.err);
//                System.err.println("index: " + index);
//                throw new RuntimeException(getClass() + " inconsistent index: items=" + is + " names=" + in + ", capacity=" + capacity());
//
//            }
//        }
//
//        return in;
//    }
//
//    /**
//     * this should always equal size(), but it's here for testing purposes
//     */
//    protected int sizeItems() {
//        int t = 0;
//        for (int i = 0; i < levels; i++) {
//            if (level[i] != null)
//                t += level[i].size();
//        }
//        return t;
//    }
//
//
//    @Override
//    public Set<K> keySet() {
//        return index.keySet();
//    }
//
//
//    @Override
//    public BagBudget<K> remove(K name) {
//        DD<E> t = index.get(name);
//        if (t == null) return null;
//        return extract(t);
//    }
//
//
//    /**
//     * Get an Item by key
//     *
//     * @param key The key of the Item
//     * @return The Item with the given key
//     */
//    @Override
//    public E get(K key) {
//        DD<E> b = index.get(key);
//        if (b==null) return null;
//        return b.item;
//    }
//
//
////    /**
////     * Check whether a level is empty
////     *
////     * @param n The level index
////     * @return Whether that level is empty
////     */
////    final public boolean levelEmpty[final int l) {
////        final Deque<E> level = this.level[l];
////        return (level == null) || (level.isEmpty());
////    }
//
//    public LevelBag setNextNonEmptyMode(NextNonEmptyLevelMode nextNonEmptyMode) {
//        this.nextNonEmptyMode = nextNonEmptyMode;
//        return this;
//    }
//
//
//    /** returns whether there is available item; if so then currentLevel will be set to the next level of it */
//    protected boolean nextNonEmptyLevel() {
//        if (size() == 0)
//            return false; // empty bag
//
//        if (levelEmpty[currentLevel] || (currentCounter <= 0)) {
//            switch (nextNonEmptyMode) {
//                case Default:
//                    nextNonEmptyLevelDefault();
//                    break;
//                case Fast:
//                    nextNonEmptyLevelFast();
//                    break;
//            }
//            if (Global.DEBUG) {
//                if (levelEmpty[currentLevel] || (level[currentLevel] == null) || (level(currentLevel).isEmpty())) {
//                    if (Global.THREADS == 1) {
//                        throw new RuntimeException("Empty setLevel selected for takeNext");
//                    }
//                }
//            }
//        }
//        currentCounter--;
//        return true;
//    }
//
//
//    /**
//     * look for a non-empty level
//     */
//    protected final void nextNonEmptyLevelDefault() {
//
//        //cache class fields as local variables for speed in the iteration
//        short[] D = DISTRIBUTOR;
//        int numLevels = D.length;
//        int li = levelIndex;
//        boolean[] le = levelEmpty;
//
//        if (li < 0)
//            throw new RuntimeException("levelIndex is < 0, may need additional check for overflow");
//
//        while (le[ D[li++ % numLevels]]);
//
//        currentLevel = D[(li-1) % numLevels];
//        levelIndex = li;
//
//        currentCounter = (currentLevel < fireCompleteLevelThreshold) ?
//             1 : // for dormant levels, take one item
//             getNonEmptyLevelSize(currentLevel); // for active levels, take all current items
//    }
//
//    /**
//     * Variation of LevelBag which follows a different distributor policy but
//     * runs much faster.  The policy should be approximately equally fair as LevelBag
//     */
//    protected boolean nextNonEmptyLevelFast() {
//
//        if (Global.DEBUG) {
//            boolean actuallyAnyNonEmpty = false;
//            for (boolean aLevelEmpty : levelEmpty) {
//                if (!aLevelEmpty) {
//                    actuallyAnyNonEmpty = true;
//                    break;
//                }
//            }
//            if (!actuallyAnyNonEmpty) {
//                //throw new RuntimeException("inconsistent empty state");
//                new RuntimeException("inconsistent empty state").printStackTrace();
//                return false;
//            }
//        }
//
//        int cl = DISTRIBUTOR[(levelIndex++) % DISTRIBUTOR.length];
//        if (cl % 2 == 0) {
//            //up
//            while (levelEmpty[cl]) {
//                cl++;
//                cl %= levels;
//            }
//        } else {
//            //down
//            while (levelEmpty[cl]) {
//                cl--;
//                if (cl < 0) cl = levelEmpty.length - 1;
//            }
//        }
//
//
//        currentLevel = cl;
//
//        currentCounter = currentLevel < fireCompleteLevelThreshold ? 1 : getNonEmptyLevelSize(currentLevel);
//
//        return true;
//    }
//
//    //TODO handle deleted items like Bag.update(..)
////    @Override
////    public E update(final BagTransaction<K, E> selector) {
////
////        final K key = selector.name();
////        final DD<E> bx;
////        if (key != null) {
////            bx = index.get(key);
////        }
////        else {
////            bx = rotateNext();
////        }
////
////
////        if ((bx == null) || (bx.item == null)) {
////            //allow selector to provide a new instance
////            E n = selector.newItem();
////            if (n!=null) {
////                E overflow = put(n);
////
////                if (overflow!=null)
////                    selector.overflow(overflow);
////
////                if (overflow == n)
////                    return null;
////
////                return n; //return the new instance
////            }
////            //no instance provided, nothing to do
////            return null;
////        }
////
////        E b = bx.item;
////
////        final float priPrev = b.getPriority();
////
////        //allow selector to modify it, then if it returns non-null, reinsert
////
////        Budget c = selector.updateItem(b, temp.budget( b.getBudget() ));
////        if ((c!=null && !c.equalsByPrecision(b.getBudget()))) {
////
////            b.getBudget().budget(c);
////            relevel(bx, b);
////        }
////
////        return b;
////    }
//
//    protected DD<E> rotateNext() {
//        if (!nextNonEmptyLevel())
//            return null;
//        Level<K,E> cl = level[currentLevel];
//        DD<E> r = cl.peekFirst();
//        cl.rotate();
//        return r;
//    }
//
//
//    @Override
//    public E peekNext() {
//        DD<E> e = rotateNext();
//        if (e == null) return null;
//        return e.item;
//    }
//
//
//    @Override
//    public E pop() {
//        if (size() == 0)
//            return null;
//
//        if (!nextNonEmptyLevel())
//            return null;
//
//        // take out the first item in the level
//        return extract(currentLevel);
//    }
//
//
//    private /*synchronized*/ E extract(int outLevel) {
//
//        if (level[outLevel] == null)
//            throw new RuntimeException("Attempted TAKE from empty (null) level " + outLevel);
//
//        Level l = level[outLevel];
//        if (l.isEmpty())
//            throw new RuntimeException("Attempted TAKE from empty level " + outLevel);
//
//
//        DD<E> fn = l.getFirstNode();
//        if (fn == null)
//            throw new RuntimeException("Attempted TAKE from empty level " + outLevel);
//
//        return extract(fn);
//    }
//
//
//    public final int getNonEmptyLevelSize(int l) {
//        return level[l].size();
//    }
//
//    public final int getLevelSize(int l) {
//        return level[l].size();
//    }
//
//
//
//
//
//    /**
//     * Decide the put-in level according to priority
//     *
//     * @param item The Item to put in
//     * @return The put-in level
//     */
//    public int getLevel(E item) {
//        float fl = item.getPriority() * levels;
//        int level = (int)fl; //(int) Math.ceil(fl) - 1;
//        if (level < 0) return 0;
//        if (level >= levels) return levels - 1;
//        return level;
//    }
//
//
//    /** removes from existing level and adds to new one */
//    protected DD<E> relevel(DD<E> x, E newValue) {
//        int prevLevel = x.owner();
//        int nextLevel = getLevel(newValue);
//
//        E prevValue = x.item;
//
//        boolean keyChange = !newValue.name().equals(prevValue.name());
//
//        if (keyChange) {
//            //name changed, must be rehashed
//            extract(x);
//            insert(newValue);
//        }
//        else {
//            if (prevLevel != nextLevel) {
//                level[prevLevel].detach(x);
//                x.owner = nextLevel;
//                if (x.item!=newValue) {
//                    x.item = newValue;
//                }
//                ensureLevelExists(nextLevel).add(x);
//
//            }
//        }
//
//        return x;
//    }
//
//    /** removal of the bagged item from its level and the index */
//    public E extract(DD<E> node) {
//        if (node == null)
//            throw new RuntimeException("OUT must not be null");
//        int lev = node.owner();
//        if (lev == -1)
//            throw new RuntimeException(node + " has invalid level");
//        E i = node.item;
//        level[lev].remove(node);
//        index.remove(i.name());
//        return i;
//    }
//
//    /** addition of the item to its level and the index */
//    protected  DD<E> insert(E newItem, int inLevel) {
//        if (newItem == null)
//            throw new RuntimeException("IN must not be null");
//        DD<E> dd = ensureLevelExists(inLevel).add(newItem);
//        index.put(newItem.name(), dd);
//        return dd;
//    }
//
//    protected /* synchronized */ DD<E> insert(E newItem) {
//        return insert(newItem, getLevel(newItem));
//    }
//
//
//    @Override
//    public E put(E newItem) {
//        if (newItem==null)
//            throw new RuntimeException("PUT item muts be non-null");
//
//        //if (Parameters.DEBUG_BAG) size();
//        DD<E> existing = index.get(newItem.name());
//        if (existing != null) {
//            Budget prevBudget = existing.item.getBudget();
//            existing.item = newItem;
//            merge(newItem.getBudget(), prevBudget);
////            if (merge(newItem, prevBudget)) {
////                //budget changed, adjust level
//                relevel(existing, newItem);
//            //}
//            return null;
//        }
//
//        //1. ensure capacity
//        int inLevel = getLevel(newItem);
//
//        E overflow = null;
//        if (size() >= capacity) {      // the bag will be full after the next
//            int outLevel = 0;
//            while (levelEmpty[outLevel]) {
//                outLevel++;
//            }
//            if (outLevel > inLevel) {           // ignore the item due to insufficent budget and exit
//                return newItem;
//            } else {                            // remove an old item in the lowest non-empty level
//                overflow = extract(outLevel);
//            }
//        }
//
//        //2.
//        ensureLevelExists(inLevel);
//
//        //3. insert
//        /*Bagged dd = */
//        insert(newItem, inLevel);
//
//        //4.
//
//        if (Global.DEBUG)
//            size();
//
//        return overflow;
//    }
//
//
//
//    protected final Level ensureLevelExists(int l) {
//        Level existing = level(l);
//        if (existing == null) {
//            return (level[l] = new Level(levelEmpty, nodePool, l));
//        }
//        return existing;
//    }
//
//
////    /**
////     * Take out the first or last E in a level from the itemTable
////     *
////     * @param level The current level
////     * @return The first Item
////     */
////    private E takeOutFirst(final int level) {
////        final E selected = this.level[level].removeFirst();
////        if (selected != null) {
////            E removed = index.remove(selected.name());
////            removeMass(selected);
////        } else {
////            if (Parameters.THREADS == 1) {
////                throw new RuntimeException("Attempt to remove item from empty setLevel: " + level);
////            }
////        }
////        return selected;
////    }
//
//
////    /**
////     * Refresh display
////     */
////    @Deprecated protected void refresh() {
////        if (bagObserver!=null)
////            if (bagObserver.isActive()) {
////                bagObserver.refresh(toString());
////            }
////    }
//
//
//    /**
//     * Collect Bag content into a String for display
//     */
////    @Override
////    public String toString() {
////        final StringBuilder buf = new StringBuilder(" ");
////        for (int i = levels - 1; i >= 0; i--) {
////            if (itemTable[i] != null && !itemTable[i].isEmpty()) {
////                buf.append("\n --- Level ").append((i + 1)).append(":\n");
////                for (final E e : itemTable[i]) {
////                    buf.append(e.toStringExternal()).append('\n');
////                }
////            }
////        }
////        return buf.toString();
////    }
//
//    /**
//     * TODO refactor : paste from preceding method
//     */
//    public String toStringLong(int minLevel) {
//        StringBuilder buf = new StringBuilder(32)
//                .append(" BAG ").append(getClass().getSimpleName())
//                .append(' ').append(showSizes());
//
//        for (int i = levels; i >= minLevel; i--) {
//            if (!levelEmpty[i - 1]) {
//                buf.append("\n --- LEVEL ").append(i).append(":\n ");
//                for (Object e : level[i - 1]) {
//                    buf.append(e).append('\n');
//                }
//
//            }
//        }
//        buf.append(">>>> end of Bag").append(getClass().getSimpleName());
//        return buf.toString();
//    }
//
//    /**
//     * show item Table Sizes
//     */
//    public String showSizes() {
//        StringBuilder buf = new StringBuilder(" ");
//        int l = 0;
//        for (Object items : level) {
//            int s = ((Level)items).size();
//            if (s > 0) {
//                l++;
//                buf.append(s).append(' ');
//            }
//        }
//        return "Levels: " + Integer.toString(l) + ", sizes: " + buf;
//    }
//
//
//    public float getAverageItemsPerLevel() {
//        return ((float) capacity) / levels;
//    }
//
//    public float getMaxItemsPerLevel() {
//        int max = getLevelSize(0);
//        for (int i = 1; i < levels; i++) {
//            int s = getLevelSize(i);
//            if (s > max) {
//                max = s;
//            }
//        }
//        return max;
//    }
//
//    public float getMinItemsPerLevel() {
//        int min = getLevelSize(0);
//        for (int i = 1; i < levels; i++) {
//            int s = getLevelSize(i);
//            if (s < min) {
//                min = s;
//            }
//        }
//        return min;
//    }
//
//    @Override
//    public int capacity() {
//        return capacity;
//    }
//
//    Iterable<E> getLevel(int i) {
//        if (level[i] == null) {
//            return Collections.emptyList();
//        }
//        return level[i];
//    }
//
//    @Override
//    public void setCapacity(int c) {
//        capacity = c;
//    }
//
//
//    @Override
//    @Deprecated public Iterable<E> values() {
//        return index.values().stream().map((d) -> d.item).collect(Collectors.toList());
//
//        //throw new RuntimeException("Coming soon");
//    }
//
//
//    final Function<Level, Iterator<E>> levelIteratorFunc = o -> {
//        if (o == null) return Collections.emptyIterator();
//        return o.iterator();
//    };
//
//
//    @Override
//    @Deprecated public Iterator<E> iterator() {
//        return Iterators.concat(
//                Iterators.transform(
//                        new ReversibleRecyclableArrayIterator(level, true), levelIteratorFunc)  );
//        //return new ItemIterator();
//        //return new DDIterators();
//    }
//
//    @Override
//    public void forEach(Consumer<? super E> c) {
//        int count = size();
//
//        if (count == 0) return;
//
//        for (int l = level.length-1; l >= 0; l--) {
//            if (levelEmpty[l]) continue;
//            level[l].forEach(c);
////            DD<E> node = level[l].getFirstNode();
////            while (node!=null) {
////                if (node.item!=null)
////                    c.accept( node.item );
////                node = node.next;
////            }
//        }
//    }
//
//
////    // assumes no calls to DDList.add() during iteration
////    private class DDIterators implements Iterator<E> {
////        private DD<E> current;  // the node that is returned by next()
////        private DD<E> lastAccessed = null;      // the last node to be returned by prev() or next()
////        // reset to null upon intervening remove() or add()
////        private int index;
////        private Level currentLevel;
////        int currentLevelNum = 0;
////
////        DDIterators() {
////            nextLevel();
////        }
////
////        public void nextLevel() {
////            currentLevel = level[currentLevelNum];
////            current = currentLevel.pre.next;
////            lastAccessed = null;
////            index = 0;
////        }
////
////        public boolean hasNext() {
////            while (!((index < currentLevel.size))) {
////            if  return true;
////            else {
////                currentLevelNum++;
////                nextLevel();
////
////            }
////        }
////
////        public boolean hasPrevious() {
////            return index > 0;
////        }
////
////        public int previousIndex() {
////            return index - 1;
////        }
////
////        public int nextIndex() {
////            return index;
////        }
////
////        public E next() {
////            if (!hasNext()) throw new NoSuchElementException();
////            lastAccessed = current;
////            E item = current.item;
////            current = current.next;
////            index++;
////            return item;
////        }
////
////        public E previous() {
////            if (!hasPrevious()) throw new NoSuchElementException();
////            current = current.prev;
////            index--;
////            lastAccessed = current;
////            return current.item;
////        }
////
////
////
////
////    }
//
//
//    public int numEmptyLevels() {
//        int empty = 0;
//        for (int i = 0; i < level.length; i++) {
//            if (levelEmpty[i]) {
//                empty++;
//            }
//        }
//        return empty;
//    }
//
//    //TODO move this to a "bag metrics" class
////    private void stat() {
////        int itsize = 0;
////        Set<CharSequence> items = new HashSet();
////        for (Deque<E> x : itemTable) {
////            if (x != null) {
////                itsize += x.size();
////                for (E e : x)
////                    if (e!=null) items.add(e.name());
////            }
////        }
////        int nsize = nameTable.size();
////        if (nsize != itsize) {
////            System.err.println("nameTable=" + nameTable.size() + " , itemTable=" + itsize);
//////            Set<CharSequence> named = new HashSet(nameTable.keySet());
//////            if (itsize > nsize) {
//////                System.err.println("  itemTable extras: " + items.removeAll(named));
//////            }
//////            else {
//////                System.err.println("  nameTable extras: " + named.removeAll(items));
//////            }
////
////        }
////
////    }
//
//
//}
//
//
//
////    final private class ItemIterator implements Iterator<E> {
////
////        int l = level.length - 1;
////        private Iterator<E> levelIterator;
////        private E next;
////        final int size = size();
////        int count = 0;
////
////        @Override
////        public boolean hasNext() {
////            if (next != null) {
////                return true;
////            }
////
////            if (l >= 0 && levelIterator == null) {
////                while (levelEmpty[l]) {
////                    if (--l == -1)
////                        return false; //end of the levels
////                }
////                levelIterator = level[l].descendingIterator();
////            }
////
////            if (levelIterator == null) {
////                return false;
////            }
////
////            next = levelIterator.next();
////            count++;
////
////            if (levelIterator.hasNext()) {
////                return true;
////            } else {
////                levelIterator = null;
////                l--;
////                return count <= size;
////            }
////        }
////
////        @Override
////        public E next() throws NoSuchElementException {
////            if (next == null) throw new NoSuchElementException();
////            E e = next;
////            next = null;
////            return e;
////        }
////
////    }
//
//
