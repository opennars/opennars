package nars.nar;

import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.task.Task;
import nars.task.flow.FIFOTaskPerception;
import nars.term.Termed;
import nars.term.compile.TermIndex;
import nars.time.Clock;
import nars.time.FrameClock;

import java.util.function.Consumer;
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

    public Terminal(TermIndex termIndex, Clock c) {
        super(new Memory(
                c,
                termIndex
                //new TrieCacheBag()
        ));

        the("input", initInput());

    }

    public Terminal() {
        this(
            new FrameClock()
        );
        //new RealtimeMSClock());
    }

    public Terminal(TermIndex i) {
        this (i, new FrameClock());
    }

    public Terminal(Clock c) {
        this(TermIndex.memory(1024), c);
    }

    @Override
    public Concept conceptualize(Termed termed, Budget activation, float scale) {
        return memory.concept(termed); //ignore activation
    }

    @Override
    public float conceptPriority(Termed termed, float priIfNonExistent) {
        return 0;
    }

    //    @Override
//    protected Concept doConceptualize(Term term, Budget b, float scale) {
//        Concept exists = memory.concept(term);
//        if (exists!=null) {
//            return exists;
//        }
//        else {
//            Concept c = apply(term);
//            memory.index.put(term, c);
//            return c;
//        }
//
//    }


    @Override
    public NAR forEachConcept(Consumer<Concept> recip) {
        return null;
    }

    public FIFOTaskPerception initInput() {
        return new FIFOTaskPerception(this,
                taskFilter,
                this::process
        );
    }
}
