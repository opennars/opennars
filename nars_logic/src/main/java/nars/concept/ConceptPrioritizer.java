package nars.concept;

import nars.Memory;
import nars.bag.Bag;
import nars.term.Term;

/**
 * Created by me on 7/12/15.
 */
public class ConceptPrioritizer extends ConceptActivator {

    private final ConceptActivator activator;
    private float newPriority;

    public ConceptPrioritizer(ConceptActivator proxy) {
        this.activator = proxy;
    }

    @Override
    public Concept updateItem(Concept c) {
        c.getBudget().setPriority(newPriority);
        return c;
    }

    public boolean update(Term c, float newPriority, Bag<Term, Concept> bag) {
        if (newPriority < 0) newPriority = 0;
        else if (newPriority > 1f) newPriority = 1f;
        this.newPriority = newPriority;


        Concept result = conceptualize(c, bag);
        if (result!=null)
            return true;

        //deactivated as a result
        return false;
    }

    @Override
    public Memory getMemory() {
        return activator.getMemory();
    }

    @Override
    public void onRemembered(Concept c) {
        activator.remember(c);
    }

    @Override
    public void onForgotten(Concept c) {
        activator.forget(c);
    }

    @Override
    protected boolean isActive(Term t) {
        return activator.isActive(t);
    }

}
