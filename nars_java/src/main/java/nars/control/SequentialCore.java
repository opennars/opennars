package nars.control;

import nars.Core;
import nars.Events;
import nars.Memory;
import nars.budget.Budget;
import nars.nal.BudgetFunctions;
import nars.nal.TaskComparator;
import nars.nal.concept.Concept;
import nars.nal.ConceptProcess;
import nars.budget.Bag;
import nars.budget.bag.CacheBag;
import nars.budget.bag.LevelBag;
import nars.nal.tlink.TaskLink;
import nars.nal.term.Term;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * Base class for single-threaded Cores
 */
abstract public class SequentialCore implements Core {

    /* ---------- Long-term storage for multiple cycles ---------- */
    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Term, Concept> concepts;
    public final CacheBag<Term, Concept> subcon;


    protected List<Runnable> run = new ArrayList();

    protected Memory memory;

    public SequentialCore(Bag<Term, Concept> concepts, CacheBag<Term, Concept> subcon) {

        this.concepts = concepts;
        this.subcon = subcon;

    }

    @Override
    public double conceptMass() {
        return concepts.mass();
    }

    /** for removing a specific concept (if it's not putBack) */
    /*@Deprecated public Concept takeOut(Term t) {
        return concepts.remove(t);
    }*/

    @Override
    public void init(Memory m) {
        this.memory = m;
        if (concepts instanceof CoreAware)
            ((CoreAware)concepts).setCore(this);
        if (concepts instanceof Memory.MemoryAware)
            ((Memory.MemoryAware)concepts).setMemory(m);
        if (subcon!=null)
            subcon.setMemory(m);


    }

    protected static class DefaultConceptProcess extends ConceptProcess {

        private final Bag<Term, Concept> bag;

        public DefaultConceptProcess(Memory mem, Bag<Term, Concept> bag, Concept concept, TaskLink taskLink) {
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
        return new DefaultConceptProcess(memory, concepts, c, t);
    }

    protected Concept nextConceptToProcess() {
        Concept currentConcept = concepts.forgetNext(memory.param.conceptForgetDurations, memory);

        if (currentConcept==null)
            return null;

        if (currentConcept.getPriority() < memory.param.conceptFireThreshold.get()) {
            return null;
        }

        return currentConcept;
    }

    @Override
    public void reset() {
        concepts.clear();
        if (subcon!=null)
            subcon.clear();
    }


    public Iterable<Concept> getConcepts() {
        return concepts.values();
    }

    @Override
    public Concept concept(final Term term) {
        return concepts.get(term);
    }

    @Override
    public void conceptRemoved(Concept c) {
        memory.emit(Events.ConceptForget.class, c);

        if (subcon!=null) {
            subcon.add(c);
            //System.out.println("forget: " + c + "   con=" + concepts.size() + " subcon=" + subcon.size());
        }
        else {
            memory.emit(Events.ConceptForget.class, c);


            //explicitly destroy all concept data structures to free memory for GC
            //c.end();
        }


    }

    final ConceptActivator activator = new ConceptActivator() {
        @Override
        public Memory getMemory() {
            return memory;
        }

        @Override
        public CacheBag<Term, Concept> getSubConcepts() {
            return subcon;
        }

    };


    @Override
    public Concept conceptualize(Budget budget, final Term term, boolean createIfMissing) {

        activator.set(term, budget, createIfMissing, memory.time());
        return concepts.update(activator);

    }


    @Deprecated @Override public void activate(final Concept c, final Budget b, BudgetFunctions.Activating mode) {
        concepts.remove(c.name());
        BudgetFunctions.activate(c, b, mode);
        concepts.putBack(c, memory.param.cycles(memory.param.conceptForgetDurations), memory);
    }

//    @Override
//    public void forget(Concept c) {
//        concepts.take(c.name());
//        concepts.putBack(c, memory.param.conceptForgetDurations.getCycles(), memory);
//    }

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
    public void forEach(Consumer<? super Concept> action) {
        //use experimental consumer for levelbag to avoid allocating so many iterators within iterators
        if (concepts instanceof LevelBag)
            ((LevelBag)concepts).forEach(action);

        //use default iterator
        //iterator().forEachRemaining(action);
    }

}
