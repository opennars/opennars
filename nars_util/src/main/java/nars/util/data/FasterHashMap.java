//package nars.util.data;
//
//
//import org.apache.commons.collections.map.AbstractHashedMap;
//
////EXPERIMENTAL, probably not useful
//public class FasterHashMap extends AbstractHashedMap {
//
//    final public static class CachedHashEntry extends HashEntry {
//
//        protected CachedHashEntry() {
//            super(null, 0, null, null);
//        }
//
//        final public CachedHashEntry set(HashEntry next, int hashCode, Object key, Object value) {
//            this.next = next;
//            this.hashCode = hashCode;
//            this.key = key;
//            this.value = value;
//            return this;
//        }
//
//        public final void clear() {
//            this.key = this.value = null; //this.next = null;
//        }
//    }
//
//
//    final DequePool<CachedHashEntry> pool;
//
//
//    public FasterHashMap(int size) {
//        super(Math.max(1,size));
//
//        pool = new CachedHashEntryDequePool(size+1);
//    }
//
//    @Override
//    final protected boolean isEqualKey(final Object key1, final Object key2) {
//        return key1.equals(key2);
//    }
//
//    @Override
//    final protected boolean isEqualValue(final Object value1, final Object value2) {
//        return value1.equals(value2);
//    }
//
//    @Override
//    final protected int hash(final Object key) {
//        return key.hashCode();
//    }
//
//
//
//    @Override
//    protected HashEntry createEntry(HashEntry next, int hashCode, Object key, Object value) {
//        return pool.get().set(next, hashCode, key, value);
//    }
//
//
//    public final void clear() {
//        modCount++;
//
//        HashEntry[] data = this.data;
//        for (int i = data.length - 1; i >= 0; i--) {
//            final HashEntry e = data[i];
//            if (e != null) {
//                destroyEntry(e);
//                data[i] = null;
//            }
//        }
//        size = 0;
//    }
//
//    @Override
//    final protected void destroyEntry(final HashEntry entry) {
//        pool.put((CachedHashEntry) entry);
//    }
//
//
//    public static void main(String[] args) {
//        FasterHashMap h = new FasterHashMap(8);
//        h.put("x", "y");
//        System.out.println(h);
//        h.clear();
//        h.put("x", "y");
//        h.put("y", "z");
//        System.out.println(h);
//        h.clear();
//        System.out.println(h);
//    }
//
//    private static class CachedHashEntryDequePool extends DequePool<CachedHashEntry> {
//
//        public CachedHashEntryDequePool(int size) {
//            super(size);
//        }
//
//        @Override
//        public CachedHashEntry create() {
//            return new CachedHashEntry();
//        }
//
//        @Override
//        public void put(final CachedHashEntry i) {
//            i.clear();
//            data.offer(i);
//        }
//    }
// }