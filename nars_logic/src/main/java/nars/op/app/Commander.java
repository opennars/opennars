package nars.op.app;

import com.google.common.collect.Iterators;
import nars.NAR;
import nars.budget.TaskAccumulator;
import nars.concept.Concept;
import nars.nal.nal7.Tense;
import nars.task.Task;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Captures input goals and questions into a buffer
 * (ie. input by user) and re-processes them in a
 * controllable pattern, frequency,
 * and priority -- in addition to ordinary system activity.
 *
 * This guides inference according to the explicit
 * inputs that were input, focusing it towards those
 * outcomes.
 *
 * Analogous to a continuous echo/delay effect,
 * or a sustain effecct.
 */
public class Commander implements Consumer<NAR>, Supplier<Concept> {

    public final TaskAccumulator commands;
    public final Iterator<Task> commandIterator;
    public final LinkedHashSet<Concept> concepts = new LinkedHashSet();
    final Iterator<Concept> conceptsIterator = Iterators.cycle(concepts);


//    private final On cycleEnd;
//    private final NAR nar;

    /** how far away from the occurence time of a temporal belief before it is deleted */
    private final int maxTemporalBeliefAge;
    private final int maxTemporalBeliefDurations = 16 /* should be tuned */;
    private final NAR nar;

    int inputsPerFrame = 2;
    int cycleDivisor = 3;

//    float priorityPerCycle = 1,
//            priorityRemaining = 0; //change left over from last cycle

    public Commander(NAR nar, int capacity) {
        this(nar, new TaskAccumulator(capacity));
    }

    public Commander(NAR nar, TaskAccumulator buffer) {

        this.nar = nar;

        //TODO reset event
        //this.cycleEnd = active ?
                nar.memory.eventFrameStart.on(this);
                //: null;

        commands = buffer;
        commandIterator = Iterators.cycle(commands.getArrayBag());


        maxTemporalBeliefAge = nar.memory.duration() * maxTemporalBeliefDurations;


        nar.memory.eventInput.on((tp) -> {
            Task t = tp.getTask();
            if (t.isInput() && !commands.getArrayBag().contains(t))
                input(t);
        });
    }


//    @Override
//    public void setActive(boolean b) {
//        super.setActive(b);
//        if (!b) {
//            commands.clear();
//        }
//    }


    protected void input(Task t) {
        if (/*(t.isGoal() || t.isQuestOrQuestion()) && */ t.isInput()) {
            commands.getArrayBag().put(t);
        }
    }


    @Override
    public void accept(NAR nar) {

        //TODO iterate tasks until allotted priority has been reached,
        //  TaskProcess each

        int cs = commands.getArrayBag().size();
        if (cs == 0) return;


        long now = nar.time();
        if (now%cycleDivisor!= 0) return;

        Iterator<Task> commandIterator = this.commandIterator;
        for (int i = 0; i < inputsPerFrame; i++) {
            if (commandIterator.hasNext()) {
                Task next = commandIterator.next();
                if (valid(now, next)) {
                    Concept c = nar.process(next);
                    if (c!=null) {
                        concepts.add(c);
                        //TODO add recursive components?
                    }
                }
                else
                    commandIterator.remove();
            }
        }

    }

    public final boolean valid(long now, Task t) {

        if (t.getBudget().getDeleted())
            return false;

        if (!Tense.isEternal(t.getOccurrenceTime())) {
            long age = Math.abs( now - t.getOccurrenceTime() );
            if (age > maxTemporalBeliefAge)
                return false;
        }

        return true;
    }

    @Override
    public Concept get() {
        return conceptsIterator.next();
    }

    public boolean isEmpty() {
        return this.commands.getArrayBag().isEmpty();
    }

    public int size() {
        return this.commands.getArrayBag().size();
    }

    //TODO getBufferPrioritySum
    //TODO setPriorityPerCycle
    //TODO max tasks limit
    //TODO rebudgeting Function<Budget,Budget> for manipulating values
    //add with TTL?
}
