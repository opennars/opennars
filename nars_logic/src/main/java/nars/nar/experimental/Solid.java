package nars.nar.experimental;

import com.gs.collections.impl.list.mutable.FastList;
import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.GuavaCacheBag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.concept.ConceptActivator;
import nars.concept.ConceptBagActivator;
import nars.io.Perception;
import nars.io.in.Input;
import nars.link.TaskLink;
import nars.nar.Default;
import nars.process.ConceptProcess;
import nars.process.CycleProcess;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.budget.ItemComparator;
import nars.term.Term;
import nars.util.sort.ArraySortedIndex;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * processes every concept fairly, according to priority, in each cycle
 * <p>
 * TODO eliminate ConcurrentSkipListSet like is implemented in DefaultCore
 */
public class Solid extends Default implements CycleProcess {


    private final int maxConcepts;
    private int maxTasksPerCycle = -1; //if ==-1, no limit
    private final int minTaskLink;
    private final int maxTaskLink;
    private final int minTermLink;
    private final int maxTermLink;
    private final int inputsPerCycle;
    private Memory memory;

    public final Bag<Term, Concept> concepts;

    ConceptActivator activator;

    final SortedSet<Task> tasks = new ConcurrentSkipListSet<>(new ItemComparator.Plus());
        /*final SortedSet<Task> tasks = new FastSortedSet(new WrapperComparatorImpl(new TaskComparator(TaskComparator.Duplication.Or))).atomic();*/

    int tasksAddedThisCycle = 0;


    public Solid(int inputsPerCycle, int activeConcepts, int minTaskLink, int maxTaskLink, int minTermLink, int maxTermLink) {
        super();
        this.inputsPerCycle = inputsPerCycle;
        this.maxConcepts = activeConcepts;

        //this.maxTasks = activeConcepts * maxTaskLink * maxTermLink * 2;
        this.maxTasksPerCycle = -1;

        this.minTaskLink = minTaskLink;
        this.maxTaskLink = maxTaskLink;
        this.minTermLink = minTermLink;
        this.maxTermLink = maxTermLink;
        duration.set(1);
        termLinkForgetDurations.set(1);
        taskLinkForgetDurations.set(1);
        conceptForgetDurations.set(1);

        conceptCreationExpectation.set(0);

        setTermLinkBagSize(16);
        setTaskLinkBagSize(32);



        concepts = new CurveBag(rng, activeConcepts, new CurveBag.Power6BagCurve(),
                new ArraySortedIndex<>(activeConcepts, new FastList<>(activeConcepts)/*.asSynchronized()*/)
        );
        //concepts = new ChainBag(rng, activeConcepts);
        //concepts = new BubbleBag(rng, activeConcepts);
        //concepts = new HeapBag(rng, activeConcepts);
        //concepts = new LevelBag(32, activeConcepts);
    }

    @Override
    public void init(NAR n) {
        super.init(n);
        this.memory = n.memory;

        activator = new ConceptBagActivator(memory, concepts);
    }


    @Override
    public void conceptPriorityHistogram(double[] bins) {
        throw new RuntimeException("not impl yet");
    }

    @Override
    public Memory getMemory() {
        return memory;
    }


    @Override
    public Iterator<Concept> iterator() {
        return concepts.iterator();
    }

    @Override
    public boolean accept(Task t) {
        if (tasks.add(t)) {
            tasksAddedThisCycle++;
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return concepts.size();
    }

    protected int num(float p, int min, int max) {
        return Math.round((p * (max - min)) + min);
    }


    @Override
    public void onInput(Input ii) {
        //TODO use perception buffer, but for now, just flush it all into memory
        ii.inputAll(memory);
    }

    protected void processNewTasks() {
        int t = 0;
        final int mt = maxTasksPerCycle;

        //int nt = tasks.size();
        //long now = memory.time();

        float maxPriority = -1, currentPriority;
        float maxQuality = Float.MIN_VALUE, minQuality = Float.MAX_VALUE;
        for (Task task : tasks) {

            currentPriority = task.getPriority();
            if (maxPriority == -1) maxPriority = currentPriority; //first one is highest

            float currentQuality = task.getQuality();
            if (currentQuality < minQuality) minQuality = currentQuality;
            else if (currentQuality > maxQuality) maxQuality = currentQuality;

            if (TaskProcess.run(memory, task) != null) {
                t++;
                if (mt != -1 && t >= mt) break;
            }
        }

            /*
            System.out.print(tasksAddedThisCycle + " added, " + nt + " unique  ");
            System.out.print("pri=[" + currentPriority + " .. " + maxPriority + "]  ");
            System.out.print("qua=[" + minQuality + " .. " + maxQuality + "]  ");
            System.out.println();
            */

        tasks.clear();
        tasksAddedThisCycle = 0;
    }

    @Override
    public synchronized void cycle() {
        //System.out.println("\ncycle " + memory.time() + " : " + concepts.size() + " concepts");


        processNewTasks();

        //2. fire all concepts
        for (Concept c : concepts) {

            if (c == null) break;

            int conceptTaskLinks = c.getTaskLinks().size();
            if (conceptTaskLinks == 0) continue;

            float p = c.getPriority();
            int fires = num(p, minTaskLink, maxTaskLink);
            if (fires < 1) continue;
            int termFires = num(p, minTermLink, maxTermLink);
            if (termFires < 1) continue;

            for (int i = 0; i < fires; i++) {
                TaskLink tl = c.getTaskLinks().forgetNext(taskLinkForgetDurations, memory);
                if (tl == null) break;
                new ConceptProcess(memory, c, tl, termFires).run();
            }

        }

        memory.runNextTasks();
    }

    @Override
    public void reset(Memory memory, Perception perception) {

        tasks.clear();

        concepts.clear();

    }

    @Override
    public void delete() {
        concepts.delete();
    }

    @Override
    public CacheBag<Term, Concept> newIndex() {
        return new GuavaCacheBag();
    }

    @Override
    public Concept concept(Term term) {
        return concepts.get(term);
    }

    @Override
    public Concept conceptualize(Term term, Budget budget, boolean createIfMissing) {
        //synchronized(activator) {
        //if (budget.getPriority() >= (memory.param.newConceptThreshold).floatValue() ) {
            return activator.conceptualize(term, budget, true, memory.time(), concepts);
        //}
        //return null;
        //}
    }

    @Override
    public Concept nextConcept() {
        return concepts.peekNext();
    }



    @Override
    public CycleProcess newCycleProcess() {
        return this;
    }

    public void setMaxTasksPerCycle(int maxTasksPerCycle) {
        this.maxTasksPerCycle = maxTasksPerCycle;
    }

//    @Override
//    public Concept newConcept(Term t, Budget b, Memory m) {
//        super.newConcept()
//        Bag<Sentence, TaskLink> taskLinks =
//                new CurveBag(rng, getConceptTaskLinks());
//
//        Bag<TermLinkKey, TermLink> termLinks =
//                //new ChainBag(rng, getConceptTermLinks());
//                new CurveBag(rng, getConceptTermLinks());
//
//        return new DefaultConcept(t, b, taskLinks, termLinks, getConceptBeliefGoalRanking(),
//                new DirectPremiseSelector(), m);
//        //return super.newConcept(b, t, m);
//    }


    /*
    static final Comparator<Item> budgetComparator = new Comparator<Item>() {
        //almost...
        //> Math.pow(2.0,32.0) * 0.000000000001
        //0.004294967296

        //one further is below 0.001 resolution
        //> Math.pow(2.0,32.0) * 0.0000000000001
        //0.0004294967296

        @Override
        public int compare(final Item o1, final Item o2) {
            if (o1.equals(o2)) return 0; //is this necessary?
            float p1 = o1.getPriority();
            float p2 = o2.getPriority();
            if (p1 == p2) {
                float d1 = o1.getDurability();
                float d2 = o2.getDurability();
                if (d1 == d2) {
                    float q1 = o1.getQuality();
                    float q2 = o2.getQuality();
                    if (q1 == q2) {
                        return Integer.compare(o1.hashCode(), o2.hashCode());
                    }
                    else {
                        return q1 < q2 ? -1 : 1;
                    }
                }
                else {
                    return d1 < d2 ? -1 : 1;
                }
            }
            else {
                return p1 < p2 ? -1 : 1;
            }
        }
    };
    */

    @Override
    public Concept remove(Concept c) {
        return concepts.remove(c.getTerm());
    }

    @Override
    public boolean reprioritize(Term term, float newPriority) {
        //TODO
        return false;
    }
}
