package nars.util.index;


import nars.Global;
import nars.NAR;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

import java.util.Set;

abstract public class MutableConceptMap<T extends Term> extends ConceptMap implements Iterable<T> {

    public final Set<T> inclusions = Global.newHashSet(16);

    public MutableConceptMap(NAR n) {
        super(n);
    }

    public boolean contains(final T t) {
        return inclusions.contains(t);
    }

    public void include(T t) {
        inclusions.add(t);
    }

    abstract public boolean include(Concept c);
    abstract public boolean exclude(Concept c);

    @Override
    protected boolean onConceptActive(Concept c) {
        return include(c);
    }

    @Override
    protected boolean onConceptForget(Concept c) {
        if (inclusions.contains(c.getTerm())) return false;
        return exclude(c);
    }

}