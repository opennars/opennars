package nars.nal.concept;

import nars.Memory;
import nars.budget.Budget;
import nars.nal.Truth;
import nars.nal.term.Compound;
import nars.nal.term.Term;

/** creates a new concept using the default concept builder and inserts a default belief of a given truth */
abstract public class BelievedConceptBuilder implements ConceptBuilder {

    @Override
    public Concept newConcept(Term t, Budget b, Memory m) {
        Truth tt = truth(t);
        if (tt == null) return null;

        Concept d = m.getConceptBuilderDefault().newConcept(t, b, m);
        d.beliefs.add(m.task((Compound) t).truth(tt).judgment().eternal().get());
        return d;
    }

    /** determine the truth of a given term, or return null to prevent the concept being created */
    protected abstract Truth truth(Term t);
}
