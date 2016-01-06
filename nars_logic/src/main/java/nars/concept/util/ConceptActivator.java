//package nars.concept.util;
//
//import nars.NAR;
//import nars.Param;
//import nars.bag.Bag;
//import nars.bag.tx.BagActivator;
//import nars.budget.Budget;
//import nars.concept.Concept;
//import nars.term.Term;
//import nars.term.Termed;
//import nars.term.compile.TermIndex;
//
///**
// * Created by me on 3/15/15.
// */
//public class ConceptActivator extends BagActivator<Term, Concept> implements ConceptBuilder {
//
//    //static final float relativeThreshold = Global.MIN_FORGETTABLE_PRIORITY;
//
//    private long now;
//
//    private float conceptForgetCycles;
//    private float activationFactor;
//
//    public final NAR nar;
//    private final ConceptBuilder builder;
//
//    public ConceptActivator(NAR nar, ConceptBuilder builder) {
//        this.nar = nar;
//        this.builder = builder;
//    }
//
//    /** gets a concept from Memory, even if forgotten */
//    public Concept concept(Term t) {
//        return nar.concept(t);
//    }
//
//
//    @Override
//    public final float getForgetCycles() {
//        return conceptForgetCycles;
//    }
//
//
//    @Override
//    public final long time() {
//        return now;
//    }
//
//    @Override
//    public float getActivationFactor() {
//        return activationFactor;
//    }
//
//
//    @Override
//    public final Concept newItem() {
//        TermIndex i = nar.index();
//        Term n = name();
//        Termed c = i.get(n);
//        if (!(c instanceof Concept)) {
//            Concept newConcept = builder.apply(n);
//            i.put(n, newConcept);
//            return newConcept;
//        }
//        return ((Concept)c);
//    }
//
//    @Override
//    public final Concept apply(Term t) {
//        return builder.apply(t);
//    }
//
//
//    public Concept update(Term term, Budget b, long now, float activationFactor, Bag<Term, Concept> bag) {
//
//        setKey(term);
//
//        setBudget(b);
//
//        Param param = nar.memory;
//        conceptForgetCycles = param.durationToCycles( param.conceptForgetDurations );
//        this.activationFactor = activationFactor;
//        this.now = now;
//
//        //Concept c = bag.update(this);
//        return null;
//    }
//
////    public final CacheBag<Term, Concept> index() {
////        return nar.concepts();
////    }
////
//
////
////    public Concept forgottenOrNewConcept() {
////        final Memory memory = nar.memory;
////        boolean belowThreshold = getBudget().summaryLessThan(memory.newConceptThreshold.floatValue();
////
////        //try remembering from subconscious if activation is sufficient
////        Concept concept = index().get(getKey());
////        if (concept != null) {
////
////            if (!belowThreshold) {
////                //reactivate
////                return concept;
////            } else {
////                //exists in subconcepts, but is below threshold to activate
////                return null;
////            }
////        }
////
////        //create new concept, with the applied budget
////        if (createIfMissing) {
////
////            //create it regardless, even if this returns null because it wasnt active enough
////
////            concept = newConcept(/*(Budget)*/getKey(), getBudget(), memory);
////
////            if (concept == null)
////                throw new RuntimeException("No ConceptBuilder to build: " + getKey() + " " + this);
////            else {
////                //memory.emit(Events.ConceptNew.class, this);
////                if (memory.logic != null)
////                    memory.logic.CONCEPT_NEW.hit();
////            }
////
////            if (!belowThreshold)
////                return concept;
////        }
////
////        return null;
////    }
////
////
////    protected final boolean remember(Concept c) {
////
////            if (isActivatable(c)) {
////
////                on(c);
////
////                nar.memory.logic.CONCEPT_REMEMBER.hit();
////
////                return true;
////            }
////
////        return false;
////    }
//
//
//
//
//
//
//    @Override
//    public final void overflow(Concept c) {
//        //getMemory().logic.CONCEPT_FORGET.hit();
//    }
//
//    public void setActivationFactor(float activationFactor) {
//        this.activationFactor = activationFactor;
//    }
//
////    public Concept conceptualize(Termed term, Budget budget, boolean createIfMissing, long time, Bag<Term, Concept> bag) {
////
////        float activationFactor = 1.0f;
////
////        Concept c = update(term.getTerm(), budget, createIfMissing, time, activationFactor, bag);
////
////
////        if (c == null) {
////
////            c = forgottenOrNewConcept();
////
////            remember(c);
////
////        }
////
////
////
////        return c;
////
////
//////        if (c != null) {
//////
//////            if (c.isDeleted()) {
//////                throw new RuntimeException("deleted concept should not have been returned by index");
//////                //throw new RuntimeException(c + " is invalid state " + c.getState() + " after conceptualization");
//////                //return null;
//////            }
//////
//////            if (!c.isActive()) {
//////
//////                if (c.getBudget().summaryGreaterOrEqual(getMemory().param.activeConceptThreshold) ) {
//////                    c.setState(Concept.State.Active);
//////                    remember(c);
//////                }
//////                else {
//////                    if (c.getState()!= Concept.State.Forgotten)
//////                        c.setState(Concept.State.Forgotten);
//////                }
//////
//////            }
//////
//////
//////            return c;
//////
//////        }
//////        return null;
////    }
//
//
////
////    protected boolean isActivatable(Concept c) {
////        //return budget.summaryGreaterOrEqual(getMemory().param.activeConceptThreshold);
////        return c.getBudget().getPriority() > nar.memory.activeConceptThreshold.floatValue();
////    }
// }
