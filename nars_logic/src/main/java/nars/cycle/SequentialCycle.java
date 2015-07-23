package nars.cycle;

import nars.Events;
import nars.Memory;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.concept.ConceptActivator;
import nars.concept.ConceptPrioritizer;
import nars.concept.DefaultConcept;
import nars.io.Perception;
import nars.io.in.Input;
import nars.link.TaskLink;
import nars.process.ConceptProcess;
import nars.process.CycleProcess;
import nars.task.Task;
import nars.term.Term;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * Base class for single-threaded Cores based on the original NARS design
 */
abstract public class SequentialCycle extends ConceptActivator implements CycleProcess {

    /**
     * Concept bag. Containing all Concepts of the system
     */
    public final Bag<Term, Concept> concepts;

    protected Perception percepts;

    protected Memory memory;

    public SequentialCycle(Bag<Term, Concept> concepts) {
        this.concepts = concepts;
    }

    @Override
    public void onRemembered(Concept c) {
        Concept overflown = concepts.put(c);
        if (overflown!=null)
            overflow(overflown);
    }

    @Override
    public void onForgotten(Concept c) {
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

    @Override
    public void reset(Memory m, Perception p) {

        concepts.clear();

        memory = m;
        percepts = p;

    }

    @Override public void delete() {
        concepts.delete();
    }

    @Override
    public void onInput(Input perception) {
        percepts.accept(perception);
    }

    public Iterable<Concept> getConcepts() {
        return concepts.values();
    }


    /** attempts to perceive the next input from perception, and
     *  handle it by immediately acting on it, or
     *  adding it to the new tasks queue for future reasoning.
     * @return how many tasks were generated as a result of perceiving (which can be zero), or -1 if no percept is available */
    protected int inputNextPerception() {
        if (!memory.isInputting()) return -1;

        Task t = percepts.get();
        if (t != null)
            return memory.add(t) ? 1 : 0;

        return -1;
    }

    /** attempts to perceive at most N perceptual tasks.
     *  this allows Attention to regulate input relative to other kinds of mental activity
     *  if N == -1, continue perceives until perception buffer is emptied
     *  @return how many tasks perceived
     */
    public int inputNextPerception(int maxPercepts) {
        //if (!perceiving()) return 0;

        boolean inputEverything;

        if (maxPercepts == -1) { inputEverything = true; maxPercepts = 1; }
        else inputEverything = false;

        int perceived = 0;
        while (perceived < maxPercepts) {
            int p = inputNextPerception();
            if (p == -1) break;
            else if (!inputEverything) perceived += p;
        }
        return perceived;
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