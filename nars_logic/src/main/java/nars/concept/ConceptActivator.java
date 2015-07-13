package nars.concept;

import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.tx.BagActivator;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.term.Term;

/**
 * Created by me on 3/15/15.
 */
abstract public class ConceptActivator extends BagActivator<Term, Concept> {

    final float relativeThreshold = Global.MIN_FORGETTABLE_PRIORITY;

    private boolean createIfMissing;
    private long now;
    private static final nars.budget.BudgetFunctions.Activating activationFunction = BudgetFunctions.Activating.Accum;


    abstract public Memory getMemory();

    /** gets a concept from Memory, even if forgotten */
    public Concept concept(Term t) {
        return getMemory().concept(t);
    }

    @Override
    public Concept updateItem(Concept c) {

        long cyclesSinceLastForgotten = now - c.getLastForgetTime();
        Memory.forget(now, c, cyclesSinceLastForgotten, relativeThreshold);


        final float activationFactor = getMemory().param.conceptActivationFactor.floatValue();
        BudgetFunctions.activate(c.getBudget(), getBudgetRef(), activationFunction, activationFactor);

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

    /** returns non-null if the Concept is available for entry into active
     *  Concept bag. attempts to retrieve an existing concept from the index
     *  first  otherwise it may attempt to create a new concept and at least
     *  insert it into the index for potential later activation.
     */
    @Override public Concept newItem() {
        //default behavior overriden; a new item will be maanually inserted into the bag under certain conditons to be determined by this class
        return null;
    }


    public Concept forgottenOrNewConcept() {
        boolean belowThreshold = getPriority() < getMemory().param.newConceptThreshold.floatValue();

        //try remembering from subconscious if activation is sufficient
        Concept concept = index().get(getKey());
        if (concept != null) {
            if (concept.isDeleted()) {
                throw new RuntimeException("deleted concept should not have been returned by index");
            }

            if (!belowThreshold) {
                //reactivate
                return concept;
            } else {
                //exists in subconcepts, but is below threshold to activate
                return null;
            }
        }

        //create new concept, with the applied budget
        if (createIfMissing) {

            //create it regardless, even if this returns null because it wasnt active enough

            concept = getMemory().newConcept(/*(Budget)*/getKey(), this);

            if (concept == null)
                throw new RuntimeException("No ConceptBuilder to build: " + getKey() + " " + this + ", builders=" + getMemory().getConceptBuilders());

            if (!belowThreshold)
                return concept;
        }

        return null;
    }


    /** called when a Concept enters attention. its state should be set active prior to call */
    abstract public void remember(Concept c);

    /** called when a Concept leaves attention. its state should be set forgotten prior to call */
    abstract public void forget(Concept c);


    @Override
    public void overflow(Concept c) {
        if (c.isActive()) {
            c.setState(Concept.State.Forgotten);
        }

        forget(c);
    }

    public Concept conceptualize(Term term, Budget budget, boolean b, long time, Bag<Term, Concept> concepts) {

        set(term, budget, true, getMemory().time());
        Concept c = concepts.update(this);



        if (c == null) {

            c = forgottenOrNewConcept();

            if (!c.isActive()) {
                if (isActivatable(budget)) {
                    c.setState(Concept.State.Active);
                    remember(c);
                } else {
                    if (!c.isForgotten())
                        c.setState(Concept.State.Forgotten);
                }
            }

        }
        else {
            if (c.isDeleted()) {
                throw new RuntimeException("deleted concept should not have been returned by index");
            }
        }

        return c;

//        if (c != null) {
//
//            if (c.isDeleted()) {
//                throw new RuntimeException("deleted concept should not have been returned by index");
//                //throw new RuntimeException(c + " is invalid state " + c.getState() + " after conceptualization");
//                //return null;
//            }
//
//            if (!c.isActive()) {
//
//                if (c.getBudget().summaryGreaterOrEqual(getMemory().param.activeConceptThreshold) ) {
//                    c.setState(Concept.State.Active);
//                    remember(c);
//                }
//                else {
//                    if (c.getState()!= Concept.State.Forgotten)
//                        c.setState(Concept.State.Forgotten);
//                }
//
//            }
//
//
//            return c;
//
//        }
//        return null;
    }

    protected boolean isActivatable(Budget budget) {
        //return budget.summaryGreaterOrEqual(getMemory().param.activeConceptThreshold);
        return budget.getPriority() > getMemory().param.activeConceptThreshold.floatValue();
    }
}
