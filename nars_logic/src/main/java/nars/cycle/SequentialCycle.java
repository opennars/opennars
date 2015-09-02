package nars.cycle;

import nars.Global;
import nars.Memory;
import nars.bag.Bag;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.concept.Concept;
import nars.concept.ConceptPrioritizer;
import nars.task.Task;
import nars.term.Term;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Base class for single-threaded Cores based on the original NARS design
 */
abstract public class SequentialCycle extends AbstractCycle<Bag<Term,Concept>> {



    /**
     * List of new tasks accumulated in one cycle, to be processed in the next
     * cycle
     */
    public final ItemAccumulator<Task> newTasks;
    protected Set<Task> newTasksTemp = Global.newHashSet(8);
    protected boolean executingNewTasks = false;




    public SequentialCycle(Bag<Term,Concept> concepts, ItemAccumulator<Task> newTasksBuffer) {
        super(concepts);
        this.newTasks = newTasksBuffer;
    }


    /** should be followed by a 'commitNewTasks' call after finishing */
    protected void queueNewTasks() {
        executingNewTasks = true;
    }

    /** @return how many new tasks added */
    protected int commitNewTasks() {

        executingNewTasks = false;

        //add the generated tasks back to newTasks
        int ns = newTasksTemp.size();
        if (ns > 0) {
            newTasks.addAll( newTasksTemp );
            newTasksTemp.clear();
        }

        return ns;
    }

    @Override
    public boolean accept(Task t) {
        if (executingNewTasks) {
            return newTasksTemp.add(t); //buffer it
        }
        else {
            return newTasks.add(t); //add it directly to the newtasks set
        }
    }

    @Override
    public void reset(Memory m) {
        super.reset(m);

        newTasksTemp.clear();
        newTasks.clear();
    }




    @Override
    public boolean reprioritize(Term c, float newPriority) {
        return new ConceptPrioritizer(this).update(c, newPriority, concepts);
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


    public void forEach(final Consumer<? super Concept> action) {
        concepts.forEach(action);
    }



    @Override
    public Concept remove(Concept cc) {

        getMemory().eventConceptForget.emit(cc);

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

    @Override public void forEachConcept(int max, Consumer<Concept> action) {
        concepts.forEach(max, action);
    }
}