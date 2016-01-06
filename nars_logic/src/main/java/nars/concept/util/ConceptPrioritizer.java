//package nars.concept;
//
//import nars.Memory;
//import nars.bag.Bag;
//import nars.budget.Budget;
//import nars.term.Term;
//
///**
// * NOT TESTED
// */
//public class ConceptPrioritizer extends ConceptActivator {
//
//    private final ConceptActivator activator;
//    private float newPriority;
//
//    public ConceptPrioritizer(ConceptActivator proxy) {
//        this.activator = proxy;
//    }
//
//    @Override
//    public Budget updateItem(Concept concept, Budget result) {
//        result.setPriority(newPriority);
//        return result;
//    }
//
//    public boolean update(Term c, float newPriority, Bag<Term, Concept> bag) {
//        if (newPriority < 0) newPriority = 0;
//        else if (newPriority > 1f) newPriority = 1f;
//        this.newPriority = newPriority;
//
//
//        Concept result = conceptualize(c, bag);
//        if (result!=null)
//            return true;
//
//        //deactivated as a result
//        return false;
//    }
//
//    @Override
//    public Memory getMemory() {
//        return activator.getMemory();
//    }
//
//    @Override
//    public void on(Concept c) {
//        activator.remember(c);
//    }
//
//    @Override
//    public void off(Concept c) {
//        activator.overflow(c);
//    }
//
//    @Override
//    protected boolean active(Term t) {
//        return activator.active(t);
//    }
//
// }
