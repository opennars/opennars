package nars.nar;

import nars.LocalMemory;
import nars.bag.impl.TrieCacheBag;
import nars.clock.RealtimeMSClock;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.term.Term;

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
        super(new LocalMemory(
                        new RealtimeMSClock(true),
                        new TrieCacheBag()
                ),
                0,0,0,0);
    }

    @Override
    public Concept apply(Term t) {
        return new DefaultConcept(t, memory);
    }

    @Override
    public FIFOTaskPerception initInput() {
        FIFOTaskPerception input = new FIFOTaskPerception(this,
                taskFilter,
                task -> TaskProcess.run(Terminal.this, task)
        );
        return input;
    }
}
