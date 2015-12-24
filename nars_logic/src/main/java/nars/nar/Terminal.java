package nars.nar;

import nars.Memory;
import nars.NAR;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.task.Task;
import nars.task.flow.FIFOTaskPerception;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compile.TermIndex;
import nars.time.RealtimeMSClock;

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

    public Terminal(TermIndex termIndex) {
        super(new Memory(
                new RealtimeMSClock(),
                termIndex
                //new TrieCacheBag()
        ));

        the("input", initInput());

    }

    public Terminal() {
        this(TermIndex.memory(1024));
    }

    @Override
    public Concept apply(Term t) {
        return new DefaultConcept(t, memory);
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
    protected Concept doConceptualize(Termed c, Budget b, float scale) {
        if (c instanceof Concept) return ((Concept)c);
        return null;
    }

    @Override
    public NAR forEachConcept(Consumer<Concept> recip) {
        return null;
    }

    public FIFOTaskPerception initInput() {
        FIFOTaskPerception input = new FIFOTaskPerception(this,
                taskFilter,
                this::process
        );
        return input;
    }
}
