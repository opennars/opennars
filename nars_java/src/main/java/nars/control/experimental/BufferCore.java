package nars.control.experimental;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import nars.control.UniCore;
import nars.core.Core;
import nars.core.Memory;
import nars.io.Symbols;
import nars.logic.FireConcept;
import nars.logic.ImmediateProcess;
import nars.logic.entity.*;
import nars.util.bag.Bag;
import nars.util.bag.CacheBag;

import java.util.*;

/**
 * Similar to default, except that new tasks are buffered per concept
 * and applied prior to that concept's firing
 */
public class BufferCore extends UniCore {

    final Multimap<Concept,Task> bufferedTasks = MultimapBuilder.hashKeys().hashSetValues().build();

    //novelTasks doesnt need to be a bag unless it reaches capacity, in which it acts as a priority queue. so bag is not necessary at all if they will all be processed
    //TODO use a sorted set, sorted by budget so that highest priority are executed first
    final Set<Task> immediateTasks = new LinkedHashSet();

    public BufferCore(Bag<Term, Concept> concepts, CacheBag<Term, Concept> subcon, ConceptBuilder conceptBuilder) {
        super(concepts, subcon, conceptBuilder);
    }

    @Override
    public void addTask(Task t) {
        Concept c;
        if (t.isInput() || t.getPunctuation() == Symbols.JUDGMENT || ( (c = concept(t.getTerm())) == null)) {
            immediateTasks.add(t);
        }
        else {
            bufferedTasks.put(c, t);
        }
    }

    @Override
    public void cycle() {

        memory.nextPercept(memory.param.inputsMaxPerCycle.get());


        ArrayList<Task> nextNovelTasks = new ArrayList(immediateTasks); //copy to new collection because the immediate processes will add to noveltasks, otherwise causing concurrentmodificationexceptions
        immediateTasks.clear();

        for (Task n : nextNovelTasks) {
            new ImmediateProcess(memory, n).run();
        }


        FireConcept f = nextConcept();
        if (f != null) {
            f.run();
        }

        memory.dequeueOtherTasks(run);
        Core.run(run);
        run.clear();

    }


    public static class BufferedFireConcept extends DefaultFireConcept {

        private final Collection<Task> preTasks;

        public BufferedFireConcept(Memory mem, Bag<Term, Concept> bag, Concept concept, int numTaskLinks, Collection<Task> preTasks) {
            super(mem, bag, concept, numTaskLinks);
            this.preTasks = preTasks;
        }

        @Override
        protected void onStart() {
            //System.err.println(currentConcept + " buffered " + preTasks);
            for (Task p : preTasks) {
                new ImmediateProcess(memory, p).run();
            }
        }
    }

    @Override
    protected FireConcept newFireConcept(Concept c) {
        Collection<Task> p = bufferedTasks.removeAll(c);
        return new BufferedFireConcept(memory, concepts, c, 1, p);
    }

    @Override
    public void reset() {
        super.reset();
        bufferedTasks.clear();
    }
}
