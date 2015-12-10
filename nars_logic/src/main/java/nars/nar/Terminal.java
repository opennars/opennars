package nars.nar;

import nars.Memory;
import nars.bag.impl.TrieCacheBag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.task.Task;
import nars.task.flow.ImmediateTaskPerception;
import nars.term.Term;
import nars.time.RealtimeMSClock;

import java.util.function.Predicate;

/**
 * Terminal only executes commands and does not
 * reason.  however it will produce an event
 * stream which can be delegated to other
 * components like other NAR's
 *
 * TODO extend AbstractNAR, not Default
 */
public class Terminal extends Default {

    final Predicate<Task> taskFilter =
            task -> task.isCommand();

    public Terminal() {
        super(new Memory(
                    new RealtimeMSClock(),
                    new TrieCacheBag()
        ), 0,0,0,0);
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
            memory.concepts.put(c);
            return c;
        }
    }



    @Override
    public ImmediateTaskPerception initInput() {
        ImmediateTaskPerception input = new ImmediateTaskPerception(this,
                taskFilter,
                task -> process(task)
        );
        return input;
    }
}
