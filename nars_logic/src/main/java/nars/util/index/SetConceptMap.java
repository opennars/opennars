package nars.util.index;

import nars.Global;
import nars.NAR;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** uses a predefined set of terms that will be mapped */
abstract public class SetConceptMap<T extends Term> extends MutableConceptMap<T> implements Iterable<T> {



    public final Map<T,Concept> values = new LinkedHashMap();

    public SetConceptMap(NAR nar) {
        super(nar);
    }

    @Override
    public Iterator<T> iterator() {
        return values.keySet().iterator();
    }

    public boolean include(Concept c) {
        Concept removed = values.put((T) c.term, c);
        return removed!=c; //different instance
    }
    public boolean exclude(Concept c) {
        return values.remove(c.term)!=null;
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
        values.put(a, null);
    }

    /** remove an inclusion, and/or add an exclusion */
    //TODO public void exclude(Term a) { }

    public int size() {
        return values.size();
    }
}
