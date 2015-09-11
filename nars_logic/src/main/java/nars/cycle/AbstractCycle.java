//package nars.cycle;
//
//import nars.Memory;
//import nars.bag.impl.CacheBag;
//import nars.concept.Concept;
//import nars.concept.ConceptActivator;
//import nars.process.CycleProcess;
//import nars.term.Term;
//
//import java.util.Iterator;
//import java.util.function.Consumer;
//
///**
// * Basic CycleProcess that can buffer perceptions
// */
//public abstract class AbstractCycle<C extends CacheBag<Term,Concept>> extends ConceptActivator implements CycleProcess<Memory> {
//
//
//
//    protected Memory memory;
//
//    /**
//     * Concept bag. Containing all Concepts of the system
//     */
//    public final C concepts;
//
//
//    @Override public void delete() {
//        concepts.delete();
//    }
//
//
//}
