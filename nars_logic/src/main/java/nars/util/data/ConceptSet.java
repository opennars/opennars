package nars.util.data;

import nars.NAR;
import nars.concept.Concept;
import nars.term.Term;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** uses a predefined set of terms that will be mapped */
public abstract class ConceptSet<T extends Term> extends MutableConceptMap<T> {

    public final Map<T,Concept> values = new LinkedHashMap();


    protected ConceptSet(NAR nar) {
        super(nar);
    }

    @Override
    public Iterator<T> iterator() {
        return values.keySet().iterator();
    }

    @Override
    public boolean include(Concept c) {
        Concept removed = values.put((T) c.get(), c);
        return removed!=c; //different instance
    }
    @Override
    public boolean exclude(Concept c) {
        return values.remove(c.get())!=null;
    }
//    public boolean exclude(Term t) {
//        return values.remove(t)!=null;
//    }


    @Override
    public boolean contains(T t) {
        if (!values.containsKey(t)) {
            return super.contains(t);
        }
        return true;
    }


    /** set a term to be present always in this map, even if the conept disappears */
    @Override
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
