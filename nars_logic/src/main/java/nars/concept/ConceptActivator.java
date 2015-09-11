package nars.concept;

import nars.Memory;
import nars.NAR;
import nars.Param;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.tx.BagActivator;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.term.Term;
import nars.term.Termed;

/**
 * Created by me on 3/15/15.
 */
abstract public class ConceptActivator extends BagActivator<Term, Concept> implements ConceptBuilder {

    //static final float relativeThreshold = Global.MIN_FORGETTABLE_PRIORITY;

    private boolean createIfMissing;
    private long now;
    private static final nars.budget.BudgetFunctions.Activating activationFunction = BudgetFunctions.Activating.Accum;
    private float conceptForgetCycles;
    private float activationFactor;

    public final NAR nar;

    public ConceptActivator(NAR nar) {
        this.nar = nar;
    }

    /** gets a concept from Memory, even if forgotten */
    public Concept concept(Term t) {
        return nar.concept(t);
    }


    @Override
    public final float getForgetCycles() {
        return conceptForgetCycles;
    }


    @Override
    public final long time() {
        return now;
    }

    @Override
    public final float getActivationFactor() {
        return activationFactor;
    }


    public Concept update(Term term, Budget b, boolean createIfMissing, long now, float activationFactor, Bag<Term,Concept> bag) {

        setKey(term);


        setBudget(b);

        final Param param = nar.memory;
        this.conceptForgetCycles = param.durationToCycles( (param.conceptForgetDurations ));
        this.activationFactor = activationFactor;
        this.createIfMissing = createIfMissing;
        this.now = now;

        return bag.update(this);
    }

    public final CacheBag<Term, Concept> index() {
        return nar.concepts();
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
        final Memory memory = nar.memory;
        boolean belowThreshold = getBudget().getPriority() < memory.newConceptThreshold.floatValue();

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

            concept = newConcept(/*(Budget)*/getKey(), getBudget(), memory);

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

                nar.memory.logic.CONCEPT_REMEMBER.hit();

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

    public Concept conceptualize(Termed term, Budget budget, boolean createIfMissing, long time, Bag<Term, Concept> bag) {

        float activationFactor = 1.0f;

        Concept c = update(term.getTerm(), budget, createIfMissing, time, activationFactor, bag);


        if (c == null) {

            c = forgottenOrNewConcept();

            remember(c);

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



    protected boolean isActivatable(Concept c) {
        //return budget.summaryGreaterOrEqual(getMemory().param.activeConceptThreshold);
        return c.getBudget().getPriority() > nar.memory.activeConceptThreshold.floatValue();
    }
}
