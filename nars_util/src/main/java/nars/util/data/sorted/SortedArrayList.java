//package nars.util.data.sorted;
//
//import java.util.ArrayList;
//
///**
// * UNTESTED
// * @author http://stackoverflow.com/a/4249132
// */
//public class SortedArrayList<T> extends ArrayList<T> {
//
//    public SortedArrayList(int capacity) {
//        super(capacity);
//    }
//    public SortedArrayList() {
//        super();
//    }
//
//    @SuppressWarnings("unchecked")
//    public void insertSorted(T value) {
//        add(value);
//        Comparable<T> cmp = (Comparable<T>) value;
//        for (int i = size()-1; i > 0 && cmp.compareTo(get(i-1)) < 0; i--) {
//            T tmp = get(i);
//            set(i, get(i-1));
//            set(i-1, tmp);
//        }
//    }
//
//
//    public void insertSorted(Iterable<T> i) {
//        for (T t : i) insertSorted(t);
//    }
// }