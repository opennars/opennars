package nars.cycle;

import nars.Events;
import nars.Memory;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.concept.ConceptPrioritizer;
import nars.io.Perception;
import nars.term.Term;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Base class for single-threaded Cores based on the original NARS design
 */
abstract public class SequentialCycle extends AbstractCycle {

    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Term, Concept> concepts;

    public SequentialCycle(Bag<Term, Concept> concepts) {
        this.concepts = concepts;
    }

    @Override
    public void reset(Memory m, Perception p) {
        super.reset(m, p);
        concepts.clear();
    }

    @Override
    public void on(Concept c) {
        Concept overflown = concepts.put(c);
        if (overflown!=null)
            overflow(overflown);
    }

    @Override
    public void off(Concept c) {
        //will have already bbe
    }


    @Override
    public boolean reprioritize(Term c, float newPriority) {
        return new ConceptPrioritizer(this).update(c, newPriority, concepts);
    }

    @Override
    public int size() {
        return concepts.size();
    }

    protected Concept nextConceptToProcess(float conceptForgetDurations) {
        Concept currentConcept = concepts.forgetNext(conceptForgetDurations, memory);

        if (currentConcept == null)
            return null;

        if (currentConcept.getPriority() < memory.param.conceptFireThreshold.get()) {
            return null;
        }

        return currentConcept;
    }

    @Override public void delete() {
        concepts.delete();
    }

    public Iterable<Concept> getConcepts() {
        return concepts.values();
    }


    /** returns a concept that is in this active concept bag only */
    @Override public Concept concept(final Term term) {
        return concepts.get(term);
    }


    @Override
    public Concept conceptualize(final Term term, Budget budget, boolean createIfMissing) {
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
    public void forEach(final Consumer<? super Concept> action) {
        concepts.forEach(action);
    }

    @Override
    public void conceptPriorityHistogram(double[] bins) {
        if (bins!=null)
            concepts.getPriorityHistogram(bins);
    }

    @Override
    public Concept remove(Concept cc) {

        getMemory().emit(Events.ConceptForget.class, this);


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