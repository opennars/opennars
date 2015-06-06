package nars.model.cycle;

import nars.Global;
import nars.Memory;
import nars.bag.impl.CacheBag;
import nars.bag.tx.BagActivator;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

/**
* Created by me on 3/15/15.
*/
abstract public class ConceptActivator extends BagActivator<Term,Concept> {

    final float relativeThreshold = Global.FORGET_QUALITY_RELATIVE;

    private boolean createIfMissing;
    private long now;

    public ConceptActivator() {
    }

    abstract public Memory getMemory();

    @Override
    public Concept updateItem(Concept c) {

        long cyclesSinceLastForgotten = now - c.getLastForgetTime();
        Memory.forget(now, c, cyclesSinceLastForgotten, relativeThreshold);

        //if (budget!=null) {
            

            final float activationFactor = getMemory().param.conceptActivationFactor.floatValue();
            BudgetFunctions.activate(c.getBudget(), getBudgetRef(), BudgetFunctions.Activating.TaskLink, activationFactor);
        //}

        return c;
    }

    public ConceptActivator set(Term t, Budget b, boolean createIfMissing, long now) {
        setKey(t);
        setBudget(b);
        this.createIfMissing = createIfMissing;
        this.now = now;
        return this;
    }

    abstract public CacheBag<Term,Concept> getSubConcepts();

    @Override
    public Concept newItem() {

        //try remembering from subconscious
        if (getSubConcepts()!=null) {
            Concept concept = getSubConcepts().take(getKey());
            if (concept!=null) {
                if (concept.isDeleted())
                    return null;

                //reactivate
                return concept.setState(Concept.State.Active);
            }
        }

        //create new concept, with the applied budget
        if (createIfMissing) {
            Concept concept = getMemory().newConcept(/*(Budget)*/this, getKey());

            if ( concept == null)
                throw new RuntimeException("No ConceptBuilder to build: " + getKey() + " " + this + ", builders=" + getMemory().getConceptBuilders());

            return concept;
        }

        return null;
    }

    @Override
    public void overflow(Concept c) {
        if (getMemory().concepts.conceptRemoved(c)) {
            //make sure it's deleted
            if (!c.isDeleted())
                c.delete();
        }
        else {
            if (c.isActive())
                c.setState(Concept.State.Forgotten);
        }
    }

}
