package nars.control;

import nars.core.Events;
import nars.core.Memory;
import nars.core.Parameters;
import nars.logic.BudgetFunctions;
import nars.logic.entity.BudgetValue;
import nars.logic.entity.Concept;
import nars.logic.entity.ConceptBuilder;
import nars.logic.entity.Term;
import nars.util.bag.impl.CacheBag;
import nars.util.bag.select.BagActivator;

/**
* Created by me on 3/15/15.
*/
abstract public class ConceptActivator extends BagActivator<Term,Concept> {

    final float relativeThreshold = Parameters.FORGET_QUALITY_RELATIVE;

    private boolean createIfMissing;
    private long now;

    public ConceptActivator() {
    }

    abstract public Memory getMemory();

    @Override
    public Concept updateItem(Concept c) {

        long cyclesSinceLastForgotten = now - c.budget.getLastForgetTime();
        getMemory().forget(c, cyclesSinceLastForgotten, relativeThreshold);

        if (budget!=null) {
            BudgetValue cb = c.budget;

            final float activationFactor = getMemory().param.conceptActivationFactor.floatValue();
            BudgetFunctions.activate(cb, getBudgetRef(), BudgetFunctions.Activating.TaskLink, activationFactor);
        }

        return c;
    }

    public ConceptActivator set(Term t, BudgetValue b, boolean createIfMissing, long now) {
        setKey(t);
        setBudget(b);
        this.createIfMissing = createIfMissing;
        this.now = now;
        return this;
    }

    abstract public CacheBag<Term,Concept> getSubConcepts();
    abstract public ConceptBuilder getConceptBuilder();

    @Override
    public Concept newItem() {

        //try remembering from subconscious
        if (getSubConcepts()!=null) {
            Concept concept = getSubConcepts().take(getKey());
            if (concept!=null) {

                //reset the forgetting period to zero so that its time while forgotten will not continue to penalize it during next forgetting iteration
                concept.budget.setLastForgetTime(now);

                getMemory().emit(Events.ConceptRemember.class, concept);

                return concept;
            }
        }

        //create new concept, with the applied budget
        if (createIfMissing) {

            Concept concept = getConceptBuilder().newConcept(budget, getKey(), getMemory());

            if (getMemory().logic!=null)
                getMemory().logic.CONCEPT_NEW.hit();

            getMemory().emit(Events.ConceptNew.class, concept);

            return concept;
        }

        return null;
    }

    @Override
    public void overflow(Concept overflow) {
        getMemory().concepts.conceptRemoved(overflow);
    }
}
