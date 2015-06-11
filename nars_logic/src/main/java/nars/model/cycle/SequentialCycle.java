package nars.model.cycle;

import nars.Memory;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.budget.Budget;
import nars.model.ControlCycle;
import nars.nal.concept.Concept;
import nars.nal.process.ConceptProcess;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for single-threaded Cores based on the original NARS design
 */
abstract public class SequentialCycle extends ConceptActivator implements ControlCycle {

    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Term, Concept> concepts;
    public final CacheBag<Term, Concept> subcon;


    protected List<Runnable> run = new ArrayList();

    protected Memory memory;

    public SequentialCycle(Bag<Term, Concept> concepts, CacheBag<Term, Concept> subcon) {

        this.concepts = concepts;
        this.subcon = subcon;

    }

    @Override
    public double conceptMass() {
        return concepts.mass();
    }

    @Override
    public void init(Memory m) {
        this.memory = m;
        if (concepts instanceof CoreAware)
            ((CoreAware) concepts).setCore(this);
        if (concepts instanceof Memory.MemoryAware)
            ((Memory.MemoryAware) concepts).setMemory(m);
        if (subcon != null)
            subcon.setMemory(m);


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

    protected Concept nextConceptToProcess() {
        Concept currentConcept = concepts.forgetNext(memory.param.conceptForgetDurations, memory);

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
    public void reset(boolean delete) {
        run.clear();

        if (delete)
            concepts.delete();
        else
            concepts.clear();

        if (subcon != null)
            subcon.clear();
    }


    public Iterable<Concept> getConcepts() {
        return concepts.values();
    }

    @Override
    public Concept concept(final Term term) {
        return concepts.get(term);
    }

    /** @return true = deleted, false = forgotten */
    @Override public boolean conceptRemoved(Concept c) {
        if ((subcon != null) && (!c.isDeleted())) {
            subcon.put(c);

            //it may have been set deleted inside the CacheBag processes's so check for it here
            return (c.isDeleted());

        }
        return true;
    }



    @Override
    public CacheBag<Term, Concept> getSubConcepts() {
        return subcon;
    }

    @Override
    public Concept conceptualize(Budget budget, final Term term, boolean createIfMissing) {
        return conceptualize(term, budget, createIfMissing, getMemory().time(), concepts);
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