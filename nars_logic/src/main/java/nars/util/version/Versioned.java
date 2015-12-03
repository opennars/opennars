package nars.util.version;

import com.gs.collections.impl.list.mutable.primitive.IntArrayList;
import nars.util.data.list.FasterList;

/**
 * Maintains a versioned snapshot history (stack) of a changing value
 */
public final class Versioned<X> extends IntArrayList implements Versionable /*Comparable<Versioned>*/ {

    public final FasterList<X> value;
    private final Versioning context;

    public Versioned(Versioning context) {
        this(context, 1);
    }

    public Versioned(Versioning context, int capacity) {
        super(capacity);
        this.context = context;
        this.value = new FasterList(capacity);
    }

    final void moveTo(int newSize) {
        if (newSize < 0)
            throw new RuntimeException("negative index");
        this.size = newSize;
        value.moveTo(newSize);
    }


    /**
     * gets the latest value
     */
    public X current() {
        if (isEmpty()) return null;
        return value.getLast();
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
    public X get() {
        if (value.isEmpty()) return null;
        return value.getLast();
    }

//    /**
//     * gets the latest value at a specific time, rolling back as necessary
//     */
//    public X at(int now) {
//        revert(now);
//        return latest();
//    }

    /** sets and commits */
    public void set(X nextValue) {
        set(context.now(), nextValue);
        context.onChanged(this, true);
    }

    public Versioning setWith(X nextValue) {
        Versioning v = set(context.now(), nextValue);
        context.onChanged(this, false);
        return v;
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

    void revert(int before) {
        int[] a = this.items;
        int p = this.size;
        int b = 0;
        while (p > 0) {
            if ((b = a[p]) <= before)
                break;
            p--;
        }
        moveTo(b);
    }

    @Override
    public final void clear() {
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
            sb.append('(');
            sb.append(get(i));
            sb.append(':');
            sb.append(value.get(i));
            sb.append(')');
            if (i < s - 1)
                sb.append(", ");
        }
        sb.append(")");
        return sb.toString();

    }
}
