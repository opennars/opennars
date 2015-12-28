package nars.util.version;

import nars.util.data.list.FasterIntArrayList;
import nars.util.data.list.FasterList;

/**
 * Maintains a versioned snapshot history (stack) of a changing value
 */
public class Versioned<X> extends FasterIntArrayList /*Comparable<Versioned>*/ {

    public final FasterList<X> value;
    private final Versioning context;

    /**
     * id, unique within the context this has registered with
     */
    private final int id;

    public Versioned(Versioning context) {
        this(context, context.newIntStack(), context.newValueStack());
    }

    public Versioned(Versioning context, int[] buffer, FasterList<X> value) {
        super(buffer);
        this.context = context;
        this.value = value;
        id = context.track();
    }

    /** called when this versioned is removed/deleted from a context */
    void delete() {
        context.onDeleted(this);
    }

    @Override
    public final boolean equals(Object otherVersioned) {
        return this == otherVersioned;
    }

    @Override
    public final int hashCode() {
        return id;
    }


    boolean revertNext(int before) {
        int p = size - 1;
        if (p >= 0) {
            int[] a = items;
            if (a[p--] > before) {
                popTo(p);
                value.popTo(p);
                return true;
            }
        }
        return false;
    }


    public int lastUpdatedAt() {
        if (isEmpty()) return -1;
        return getLast();
    }

    /*@Override
    public int compareTo(Versioned o) {
        return Integer.compare(o.now(), now());
    }*/

    /**
     * gets the latest value
     */
    public final X get() {
        if (size == 0) return null;
        return value.getLast();
    }

//    /**
//     * gets the latest value at a specific time, rolling back as necessary
//     */
//    public X revertThenGet(int now) {
//        revert(now);
//        return latest();
//    }

    /**
     * sets thens commits
     */
    public void set(X nextValue) {
        set(context.newChange(this), nextValue);
    }

    /**
     * set but does not commit;
     * a commit should precede this call otherwise it will have the version of a previous commit
     */
    public Versioning thenSet(X nextValue) {
        Versioning ctx = context;
        set(ctx.continueChange(this), nextValue);
        return ctx;
    }

    /**
     * sets at a specific time but does not commit;
     * make sure to call commit on the returned context after
     * all concurrent set() are finished
     */
    final Versioning set(int now, X nextValue) {

        add(now);
        value.add(nextValue);

        return context;
    }


    @Override
    public void clear() {
        super.clear();
        value.clear();
    }

    @Override
    public final String toString() {
        X v = get();
        if (v != null)
            return v.toString();
        return "null";
    }

    public final String toStackString() {
        StringBuilder sb = new StringBuilder("(");
        int s = size();
        for (int i = 0; i < s; i++) {
            //sb.append('(');
            sb.append(get(i));
            sb.append(':');
            sb.append(value.get(i));
            //sb.append(')');
            if (i < s - 1)
                sb.append(", ");
        }
        sb.append(')');
        return sb.toString();

    }

//    public int setInt(IntToIntFunction f) {
//        Integer x = (Integer) get();
//        Integer y = f.valueOf(x);
//        set(y);
//    }

    public X getIfAbsent(X valueIfMissing) {
        if (isEmpty()) return valueIfMissing;
        X x = get();
        if (x == null) return valueIfMissing;
        return x;
    }

//    public long getIfAbsent(long valueIfMissing) {
//        if (isEmpty()) return valueIfMissing;
//        return ((Long) get());
//    }

    @Deprecated public int getIfAbsent(int valueIfMissing) {
        if (isEmpty()) return valueIfMissing;
        Integer i  = (Integer) get();
        if (i == null) return valueIfMissing;
        return i;
    }

//    public char getIfAbsent(char valueIfMissing) {
//        if (isEmpty()) return valueIfMissing;
//        return ((Character) get());
//    }

}
