package nars.util.index;

import nars.NAR;
import nars.concept.Concept;
import nars.term.Term;

import java.util.Set;

/** uses a predefined set of terms that will be mapped */
abstract public class SeedConceptMap extends ConceptMap {

    public final Set<Term> terms;

    public SeedConceptMap(NAR nar, Set<Term> terms) {
        super(nar);
        this.terms = terms;
    }


    @Override
    public boolean contains(Concept c) {
        Term s = c.getTerm();
        return terms.contains(s);
    }

    public boolean contains(Term t) { return terms.contains(t); }
}
