//package nars.control.experimental;
//
//import com.google.common.collect.Multimap;
//import com.google.common.collect.MultimapBuilder;
//import nars.control.UniCore;
//import nars.core.Core;
//import nars.core.Events;
//import nars.core.Memory;
//import nars.core.Parameters;
//import nars.io.Symbols;
//import nars.logic.FireConcept;
//import nars.logic.ImmediateProcess;
//import nars.logic.entity.*;
//import nars.util.bag.Bag;
//import nars.util.bag.impl.CacheBag;
//
//import java.util.*;
//
///**
// * Similar to default, except that new tasks are buffered per concept
// * and applied prior to that concept's firing
// */
//public class BufferCore extends UniCore {
//
//    final Multimap<Concept,Task> bufferedTasks = MultimapBuilder.hashKeys().hashSetValues().build();
//
//    //novelTasks doesnt need to be a bag unless it reaches capacity, in which it acts as a priority queue. so bag is not necessary at all if they will all be processed
//    //TODO use a sorted set, sorted by budget so that highest priority are executed first
//    final Set<Task> immediateTasks = new LinkedHashSet();
//
//    public BufferCore(Bag<Term, Concept> concepts, CacheBag<Term, Concept> subcon, ConceptBuilder conceptBuilder) {
//        super(concepts, subcon, conceptBuilder);
//    }
//
//    @Override
//    public void addTask(Task t) {
//        Concept c;
//        if (t.isInput() || t.getPunctuation() == Symbols.JUDGMENT || ( (c = concept(t.getTerm())) == null)) {
//            immediateTasks.add(t);
//        }
//        else {
//            bufferedTasks.put(c, t);
//        }
//    }
//
//    @Override
//    public void cycle() {
//
//        memory.nextPercept(memory.param.inputsMaxPerCycle.get());
//
//
//        ArrayList<Task> nextImmediates = new ArrayList(immediateTasks); //copy to new collection because the immediate processes will add to noveltasks, otherwise causing concurrentmodificationexceptions
//        immediateTasks.clear();
//
//        for (Task n : nextImmediates) {
//            new ImmediateProcess(memory, n).run();
//        }
//
//
//        FireConcept f = nextConcept();
//        if (f != null) {
//            f.run();
//        }
//
//        memory.dequeueOtherTasks(run);
//        Core.run(run);
//        run.clear();
//
//    }
//
//    /** all supplied tasks pertain to the same concept */
//    public static class BufferedImmediateProcess extends ImmediateProcess {
//
//        private final Collection<Task> tasks;
//
//
//        public BufferedImmediateProcess(Memory mem, Collection<Task> tasks) {
//            super(mem);
//            this.tasks = tasks;
//        }
//
//        @Override
//        public void rule() {
//            Concept c = null;
//            for (Task t : tasks) {
//
//                setCurrentTask(t);
//
//                if (c == null) {
//                    c = memory.conceptualize(currentTask.budget, currentTask.getTerm());
//                    if (c == null) return;
//                }
//                else {
//                    if (Parameters.DEBUG) {
//                        if (!getCurrentConcept().getTerm().equals(t.getTerm()))
//                            throw new RuntimeException("term mismatch");
//                    }
//                }
//
//                boolean processed = c.directProcess(this, currentTask);
//                if (!processed) return;
//
//
//                emit(Events.TaskImmediateProcessed.class, currentTask, this);
//                memory.logic.TASK_IMMEDIATE_PROCESS.hit();
//
//            }
//
//            if (c!=null)
//                c.link(tasks);
//
//        }
//
//
//    }
//
//
//    public static class BufferedFireConcept extends DefaultFireConcept {
//
//        private final Collection<Task> preTasks;
//
//        public BufferedFireConcept(Memory mem, Bag<Term, Concept> bag, Concept concept, int numTaskLinks, Collection<Task> preTasks) {
//            super(mem, bag, concept);
//            this.preTasks = preTasks;
//        }
//
//        @Override
//        protected void onStart() {
//            new BufferedImmediateProcess(memory, preTasks).run();
//        }
//    }
//
//    @Override
//    protected FireConcept newFireConcept(Concept c) {
//        Collection<Task> p = bufferedTasks.removeAll(c);
//        return new BufferedFireConcept(memory, concepts, c, 1, p);
//    }
//
//    @Override
//    public void reset() {
//        super.reset();
//        bufferedTasks.clear();
//    }
//}
