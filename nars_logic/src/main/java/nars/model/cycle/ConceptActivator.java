package nars.model.cycle;

import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.tx.BagActivator;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.concept.Concept;
import nars.nal.term.Term;

/**
 * Created by me on 3/15/15.
 */
abstract public class ConceptActivator extends BagActivator<Term, Concept> {

    final float relativeThreshold = Global.FORGET_QUALITY_RELATIVE;

    private boolean createIfMissing;
    private long now;

    /**
     * last created
     */
    private Concept lastRememberance;

    abstract public Memory getMemory();

    @Override
    public Concept updateItem(Concept c) {

        long cyclesSinceLastForgotten = now - c.getLastForgetTime();
        Memory.forget(now, c, cyclesSinceLastForgotten, relativeThreshold);


        final float activationFactor = getMemory().param.conceptActivationFactor.floatValue();
        BudgetFunctions.activate(c.getBudget(), getBudgetRef(), BudgetFunctions.Activating.TaskLink, activationFactor);

        return c;
    }

    public ConceptActivator set(Term t, Budget b, boolean createIfMissing, long now) {
        setKey(t);
        setBudget(b);
        this.createIfMissing = createIfMissing;
        this.now = now;
        return this;
    }

    public CacheBag<Term, Concept> index() {
        return getMemory().getConcepts();
    }

    @Override
    public synchronized Concept newItem() {

        lastRememberance = null;

        boolean hasSubconcepts = (index() != null);

        boolean belowThreshold = getPriority() <= getMemory().param.activeConceptThreshold.floatValue();

        //try remembering from subconscious if activation is sufficient
        if (hasSubconcepts) {
            Concept concept = index().take(getKey());
            if (concept != null) {
                if (concept.isDeleted())
                    return null;

                if (!belowThreshold) {
                    //reactivate
                    return concept;
                } else {
                    //remember but dont reactivate
                    lastRememberance = concept;
                    return null;
                }
            }
        }

        //create new concept, with the applied budget
        if (createIfMissing)       {
            if (!belowThreshold || (belowThreshold && hasSubconcepts)) {

                Concept concept = lastRememberance = getMemory().newConcept(/*(Budget)*/this, getKey());

                if (concept == null)
                    throw new RuntimeException("No ConceptBuilder to build: " + getKey() + " " + this + ", builders=" + getMemory().getConceptBuilders());

                if (belowThreshold && hasSubconcepts) {
                    //attempt insert the latent concept into subconcepts but return null
                    index().put(concept);
                    return null;
                } else
                    return concept;
            }
        }

        return null;
    }

    /**
     * threshold priority for allowing a new concept into the main memory
     */
    public float getMinimumActivePriority() {
        return 0f;
    }


    public void onRemoved(Concept c) {

    }

    @Override
    public void overflow(Concept c) {
        onRemoved(c);

        if (c.isActive())
            c.setState(Concept.State.Forgotten);
    }

    public synchronized Concept conceptualize(Term term, Budget budget, boolean b, long time, Bag<Term, Concept> concepts) {
        lastRememberance = null;
        set(term, budget, true, getMemory().time());
        Concept c = concepts.update(this);
        if (c != null) {

            if (!c.isActive())
                c.setState(Concept.State.Active);

        } else if (lastRememberance != null) {
            //see if a concept was created but inserted into subconcepts
            c = lastRememberance;

            if (!c.isForgotten())
                c.setState(Concept.State.Forgotten);

        }

        return c;
    }
}
