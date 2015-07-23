package nars.concept;

import nars.Memory;
import nars.bag.Bag;
import nars.term.Term;

/**
 * Created by me on 7/22/15.
 */
public class ConceptBagActivator extends ConceptActivator {

    private final Memory memory;
    private final Bag<Term, Concept> conceptBag;

    public ConceptBagActivator(Memory memory, Bag<Term, Concept> conceptBag) {
        this.memory = memory;
        this.conceptBag = conceptBag;
    }

    @Override
    public Memory getMemory() {
        return memory;
    }

    @Override
    protected boolean isActive(Term t) {
        return conceptBag.get(t) != null;
    }

    @Override
    public void onRemembered(Concept c) {

        Concept overflown = conceptBag.put(c);
        if (overflown!=null)
            overflow(overflown);
    }

    @Override
    public void onForgotten(Concept c) {
        Concept x = conceptBag.remove(c.getTerm());
        if (x!=null)
            throw new RuntimeException("should have been removed already but not here");
//        if (x != c) {
//            throw new RuntimeException("removed concept was not in bag; but was " + x);
//        }
    }

}
