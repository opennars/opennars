//package nars.util.bag;
//
///** Wraps an item in a level; simultaneously used as the nameTable value associated with a given key */
//public class Bagged<F> extends DD<F> {
//
//    public int level = -1;
//
//    //public K name() { return item!= null ? item.name() : null; }
//    //public E item() { return item; }
//
//    @Override
//    public String toString() {
//        return item + ":" + level;
//    }
//
//    @Override
//    public int hashCode() {
//        if (item!=null) return item.hashCode();
//        return 0;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) return true;
//        if (!(obj instanceof DD)) return false;
//        Bagged b = (Bagged)obj;
//        if (item == null) return b.item == null;
//        else
//            return item.equals(((DD) obj).item);
//    }
//}
