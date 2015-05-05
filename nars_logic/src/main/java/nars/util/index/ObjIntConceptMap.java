package nars.util.index;

import com.gs.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import nars.NAR;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** uses a predefined set of terms that will be mapped */
abstract public class ObjIntConceptMap<T extends Term> extends MutableConceptMap<T> implements Iterable<T> {


    public final ObjectIntHashMap<T> values = new ObjectIntHashMap();


    public ObjIntConceptMap(NAR nar) {
        super(nar);
    }

    @Override
    public Iterator<T> iterator() {
        return values.keySet().iterator();
    }

    public boolean include(Concept c) {
        T t = (T)c.term;
        return values.getIfAbsentPut(t, 0) == 0;
    }

    public boolean exclude(Concept c) {
        return exclude(c.term);
    }

    public boolean exclude(Term t) {
        int size = values.size();
        values.remove(t);
        return (values.size() != size);
    }


    public boolean contains(final T t) {
        if (!values.containsKey(t)) {
            return super.contains(t);
        }
        return true;
    }


    /** set a term to be present always in this map, even if the conept disappears */
    public void include(T a) {
        super.include(a);
        values.getIfAbsentPut(a, 0);
    }

    /** remove an inclusion, and/or add an exclusion */
    //TODO public void exclude(Term a) { }

    public int size() {
        return values.size();
    }



    public int add(T t, int dx) {
        int c = values.getIfAbsent(t, Integer.MIN_VALUE);
        if (c == Integer.MIN_VALUE) {
            if (dx < 0)
                return -1; //subtracting from an entry non-existint, return -1
            else
                c = 0; //begin at zero
        }
        c = c + dx;
        if (c < 0)
            c = 0;
        values.put(t, c);
        return c;
    }

    public int set(T t, int n) {
        values.put(t, n);
        return n;
    }
}
