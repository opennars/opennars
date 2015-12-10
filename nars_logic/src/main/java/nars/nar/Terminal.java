package nars.nar;

import nars.Memory;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.task.Task;
import nars.task.flow.FIFOTaskPerception;
import nars.term.Term;
import nars.term.compile.TermIndex;
import nars.time.RealtimeMSClock;

import java.util.function.Predicate;

/**
 * Terminal only executes commands and does not
 * reason.  however it will produce an event
 * stream which can be delegated to other
 * components like other NAR's
 */
public class Terminal extends AbstractNAR {

    final Predicate<Task> taskFilter =
            Task::isCommand;

    public Terminal(TermIndex termIndex) {
        super(new Memory(
                new RealtimeMSClock(),
                termIndex
                //new TrieCacheBag()
        ), 0,0,0,0);
    }

    public Terminal() {
        this(TermIndex.memory(1024));
    }

    @Override
    public Concept apply(Term t) {
        return new DefaultConcept(t, memory);
    }

    @Override
    public DefaultCycle initCore(int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {
        //nothing
        return null;
    }


    @Override
    protected Concept doConceptualize(Term term, Budget b) {
        Concept exists = memory.concept(term);
        if (exists!=null) {
            exists.getBudget().mergePlus(b);
            return exists;
        }
        else {
            Concept c = apply(term);
            c.getBudget().budget(b);
            memory.index.put(term, c);
            return c;
        }
    }



    @Override
    public FIFOTaskPerception initInput() {
        FIFOTaskPerception input = new FIFOTaskPerception(this,
                taskFilter,
                this::process
        );
        return input;
    }
}
