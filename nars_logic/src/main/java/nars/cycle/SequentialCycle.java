package nars.cycle;

import nars.Memory;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.concept.ConceptActivator;
import nars.link.TaskLink;
import nars.process.ConceptProcess;
import nars.process.CycleProcess;
import nars.term.Term;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for single-threaded Cores based on the original NARS design
 */
abstract public class SequentialCycle extends ConceptActivator implements CycleProcess {

    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Term, Concept> concepts;


    protected List<Runnable> run = new ArrayList();

    protected Memory memory;

    public SequentialCycle(Bag<Term, Concept> concepts) {

        this.concepts = concepts;

    }


    @Override
    public void remember(Concept c) {
        concepts.put(c);
    }

    @Override
    public void forget(Concept c) {
        concepts.remove(c.getTerm());
    }

    protected static class DefaultConceptProcess extends ConceptProcess {

        private final Bag<Term, Concept> bag;

        public DefaultConceptProcess(Bag<Term, Concept> bag, Concept concept, TaskLink taskLink) {
            super(concept, taskLink);
            this.bag = bag;
        }

        @Override
        public void beforeFinish() {
        }
    }

    @Override
    public int size() {
        return concepts.size();
    }

    protected ConceptProcess newConceptProcess(Concept c, TaskLink t) {
        return new DefaultConceptProcess(concepts, c, t);
    }

    protected Concept nextConceptToProcess(float conceptForgetDurations) {
        Concept currentConcept = concepts.forgetNext(conceptForgetDurations, memory);

        if (currentConcept == null)
            return null;

        if (!currentConcept.isActive())
            return null;

        if (currentConcept.getPriority() < memory.param.conceptFireThreshold.get()) {
            return null;
        }

        return currentConcept;
    }

    @Override
    public void reset(Memory m, boolean delete) {

        this.memory = m;

        run.clear();

        if (delete)
            concepts.delete();
        else
            concepts.clear();

    }


    public Iterable<Concept> getConcepts() {
        return concepts.values();
    }




    /** returns a concept that is in this active concept bag only */
    @Override public Concept getActiveConcept(final Term term) {
        return concepts.get(term);
    }

//    /** @return true = deleted, false = forgotten */
//    @Override public boolean conceptRemoved(final Concept c) {
//        if ((subcon != null) && (!c.isDeleted())) {
//            subcon.put(c);
//
//            //it may have been set deleted inside the CacheBag processes's so check for it here
//            return (c.isDeleted());
//
//        }
//        return true;
//    }


    @Override
    public Concept conceptualize(Budget budget, final Term term, boolean createIfMissing) {
        return conceptualize(term, budget, createIfMissing, memory.time(), concepts);
    }

    @Override
    public Concept nextConcept() {
        return concepts.peekNext();
    }

    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }

    @Override
    public Memory getMemory() {
        return memory;
    }


    @Override
    public void forEach(final Consumer<? super Concept> action) {
        concepts.forEach(action);
    }

    public void conceptPriorityHistogram(double[] bins) {
        if (bins!=null)
            concepts.getPriorityHistogram(bins);
    }

    @Override
    public Concept remove(Concept cc) {
        Concept c = concepts.remove(cc.getTerm());
        return c;
    }

    //    @Deprecated
//    @Override
//    public void activate(final Concept c, final Budget b, BudgetFunctions.Activating mode) {
//        concepts.remove(c.name());
//        BudgetFunctions.activate(c.getBudget(), b, mode);
//        concepts.putBack(c, memory.param.cycles(memory.param.conceptForgetDurations), memory);
//    }

//    @Override
//    public void forget(Concept c) {
//        concepts.take(c.name());
//        concepts.putBack(c, memory.param.conceptForgetDurations.getCycles(), memory);
//    }

}