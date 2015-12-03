package nars.term.transform;

import com.gs.collections.impl.list.mutable.primitive.IntArrayList;
import nars.Op;
import nars.nal.meta.TermPattern;
import nars.term.Term;
import nars.util.data.list.FasterList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;


public abstract class Subst extends Frame {

    public Subst(Random random, Op type) {
        super(random, type);
    }

    /** standard matching */
    public abstract boolean next(Term x, Term y, int power);

    /** compiled matching */
    public abstract boolean next(TermPattern x, Term y, int power);

    public abstract void putXY(Term x, Term y);
    public abstract void putYX(Term x, Term y);


    //public abstract Term resolve(Term t, Substitution s);


    final static int defaultInitialVersionedCapacity = 16;

    final public boolean containsKey(Object k) {
        return values.containsKey(k);
    }

    public int size() {
        return values.size();
    }

    @Override
    public String toString() {
        return "subst:{" +
                "now:" + now +
                ", values:" + values +
                ", " + super.toString() +
                '}';
    }


    public static final class Versioned extends IntArrayList implements Comparable<Versioned> {
        public final FasterList value;

        public Versioned(int capacity) {
            super(capacity);
            value = new FasterList(capacity);
        }

        public final void moveTo(int newSize) {
            if (newSize < 0)
                throw new RuntimeException("negative index");
            this.size = newSize;
            value.moveTo(newSize);
        }

        /** gets the latest value */
        public Object getLatest() {
            if (isEmpty()) return null;
            return value.getLast();
        }

        public int now() {
            if (isEmpty()) return -1;
            return getLast();
        }

        @Override
        public int compareTo(Versioned o) {
            return Integer.compare(o.now(), now());
        }

        /** gets the latest value */
        public Object current() {
            if (value.isEmpty()) return null;
            return value.getLast();
        }

        /** gets the latest value at a specific time, rolling back as necessary */
        public Object current(int now) {
            revert(now);
            if (value.isEmpty()) return null;
            return value.getLast();
        }

        /** returns true if value changed */
        public boolean commit(Object nextValue, int now) {

            revert(now); //in case this is further ahead

            //final int current = now();


            //if (current == -1 || !Objects.equals(nextValue, getLatest())) {

                //if (current < now) {
                    add(now);
                    value.add(nextValue);
                //}
//                else {
//                    //update value but dont add a new version
//                    value.set(size-1, nextValue);
//                    return false;
//                }
                return true;
//            }
//            return false;
        }

        public void revert(int before) {
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
        public void clear() {
            super.clear();
            value.clear();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("(");
            int s = size();
            for (int i = 0; i < s; i++) {
                sb.append('(');
                sb.append(get(i));
                sb.append(':');
                sb.append(value.get(i));
                sb.append(')');
                if (i < s-1)
                    sb.append(", ");
            }
            sb.append(")");
            return sb.toString();

        }
    }

    public int now() {
        return now;
    }


    /** current version */
    int now = 0;

    public final Map<Object,Versioned> values = new LinkedHashMap();

    /** TODO stores a sorted list according to their version #, newest are last */
    //SortedList<Versioned> recent = new SortedList();

    /** records an assignment operation */
    public void set(Object key, Object value) {
        Versioned v = values.computeIfAbsent(key, (k) -> new Versioned(defaultInitialVersionedCapacity) );
        if (v.commit(value, now)) {
            now++;
        }
    }

    public <O> O get(Object key) {
        Versioned v = values.get(key);
        if (v == null) return null;
        return (O)v.current(now);
    }

    public <O> O get(Object key, Supplier<O> ifAbsentPut) {
        //TODO use compute... Map methods
        O o = get(key);
        if (o == null) {
            set(key, o = ifAbsentPut.get());
        }
        return o;
    }

    /** reverts/undo to previous state */
    public void revert(int when) {
        now = when;
    }


}
