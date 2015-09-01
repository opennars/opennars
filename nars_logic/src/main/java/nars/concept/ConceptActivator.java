package nars.concept;

import nars.AbstractMemory;
import nars.Events;
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

    //static final float relativeThreshold = Global.MIN_FORGETTABLE_PRIORITY;

    private boolean createIfMissing;
    private long now;
    private static final nars.budget.BudgetFunctions.Activating activationFunction = BudgetFunctions.Activating.Accum;
    private float conceptForgetCycles;
    private float activationFactor;


    abstract public Memory getMemory();

    /** gets a concept from Memory, even if forgotten */
    public Concept concept(Term t) {
        return getMemory().concept(t);
    }


    @Override
    public float getForgetCycles() {
        return conceptForgetCycles;
    }


    @Override
    public long time() {
        return now;
    }

    @Override
    public float getActivationFactor() {
        return activationFactor;
    }


    public ConceptActivator set(Budget b, boolean createIfMissing, long now) {

        setBudget(b);

        this.conceptForgetCycles = getMemory().param.cycles( (getMemory().param.conceptForgetDurations ));
        this.activationFactor = getMemory().param.conceptActivationFactor.floatValue();
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
        final Memory memory = getMemory();
        boolean belowThreshold = getBudget().getPriority() < memory.param.newConceptThreshold.floatValue();

        //try remembering from subconscious if activation is sufficient
        Concept concept = index().get(getKey());
        if (concept != null) {

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

            concept = memory.newConcept(/*(Budget)*/getKey(), getBudget());

            if (concept == null)
                throw new RuntimeException("No ConceptBuilder to build: " + getKey() + " " + this);
            else {
                //memory.emit(Events.ConceptNew.class, this);
                if (memory.logic != null)
                    memory.logic.CONCEPT_NEW.hit();
            }

            if (!belowThreshold)
                return concept;
        }

        return null;
    }


    protected final boolean remember(Concept c) {

            if (isActivatable(c)) {

                on(c);

                getMemory().logic.CONCEPT_REMEMBER.hit();

                return true;
            }

        return false;
    }



    /** called when a Concept enters attention. its state should be set active prior to call */
    abstract protected void on(Concept c);

    /** called when a Concept leaves attention. its state should be set forgotten prior to call */
    abstract protected void off(Concept c);


    @Override
    public final void overflow(Concept c) {
        //getMemory().logic.CONCEPT_FORGET.hit();
        off(c);
    }

    public Concept conceptualize(Term term, Budget budget, boolean b, long time, Bag<Term, Concept> concepts) {

        set(budget, true, time);

        return conceptualize(term, concepts);

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

    protected Concept conceptualize(Term term, Bag<Term, Concept> concepts) {
        setKey(term);
        Concept c = concepts.update(this);



        if (c == null) {

            c = forgottenOrNewConcept();

            remember(c);

        }

        return c;
    }

    abstract protected boolean active(Term t);

    protected boolean isActivatable(Concept c) {
        //return budget.summaryGreaterOrEqual(getMemory().param.activeConceptThreshold);
        return c.getBudget().getPriority() > getMemory().param.activeConceptThreshold.floatValue();
    }
}
