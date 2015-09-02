package nars.nar.experimental;

import com.gs.collections.impl.list.mutable.FastList;
import nars.*;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.GuavaCacheBag;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.concept.Concept;
import nars.concept.ConceptActivator;
import nars.concept.ConceptBagActivator;
import nars.concept.ConceptBuilder;
import nars.io.in.Input;
import nars.link.TaskLink;
import nars.nal.PremiseProcessor;
import nars.nar.Default;
import nars.process.ConceptProcess;
import nars.process.CycleProcess;
import nars.process.TaskProcess;
import nars.task.Task;
import nars.term.Term;
import nars.util.data.random.XORShiftRandom;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.sort.ArraySortedIndex;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * processes every concept fairly, according to priority, in each cycle
 * <p>
 * TODO eliminate ConcurrentSkipListSet like is implemented in DefaultCore
 */
public class Solid extends AbstractNARSeed<Bag<Term,Concept>,Param> {

    private final int maxConcepts;

    private final int termLinkBagSize;
    private final int taskLinkBagSize;

    private int maxTasksPerCycle = -1; //if ==-1, no limit
    private final int minTaskLink;
    private final int maxTaskLink;
    private final int minTermLink;
    private final int maxTermLink;
    private final int inputsPerCycle;


    public final Bag<Term, Concept> concepts;

    ConceptActivator activator;

    final ItemAccumulator<Task> tasks = new ItemAccumulator(Budget.plus);
    //final SortedSet<Task> tasks = new ConcurrentSkipListSet<>(new ItemComparator.Plus());
        /*final SortedSet<Task> tasks = new FastSortedSet(new WrapperComparatorImpl(new TaskComparator(TaskComparator.Duplication.Or))).atomic();*/

    /** stores sorted tasks temporarily */
    private final List<Task> temporary = Global.newArrayList();
    private final List<Concept> temporaryC = Global.newArrayList();


    int tasksAddedThisCycle = 0;
    private final boolean normalizePriority = false;

    /** proxy parameter/seed, temporary */
    @Deprecated private Default param;

    private final Random rng = new XorShift1024StarRandom(1);


    @Override
    public Param newParam() {
        this.param = new Default().setInternalExperience(null);
        param.duration.set(2);
        param.termLinkForgetDurations.set(4);
        param.taskLinkForgetDurations.set(10);
        param.conceptForgetDurations.set(2);
        param.conceptCreationExpectation.set(0);

        return param;
    }

    @Override
    public Random getRandom() {
        return rng;
    }

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

        termLinkBagSize = 32;
        taskLinkBagSize = 32;


        concepts = new CurveBag<Term,Concept>(getRandom(), activeConcepts, new CurveBag.Power6BagCurve(),
                new ArraySortedIndex(activeConcepts, new FastList(activeConcepts)/*.asSynchronized()*/)
        );

        reset(newMemory());

        //concepts = new ChainBag(rng, activeConcepts);
        //concepts = new BubbleBag(rng, activeConcepts);
        //concepts = new HeapBag(rng, activeConcepts);
        //concepts = new LevelBag(32, activeConcepts);
    }

//    /** construct a new premise generator for a concept */
//    @Override
//    public BloomFilterNovelPremiseGenerator newPremiseGenerator() {
//        return new BloomFilterNovelPremiseGenerator(termLinkMaxMatched, 1 /* cycle to clear after */,
//                maxTaskLink * maxTermLink,
//                0.01f /* false positive probability */ );
//    }

    @Override
    public void init(NAR n) {
        ((Default)n.param).init(n); //temporary hack

        activator = new ConceptBagActivator(n.memory, concepts);
    }

    @Override
    public PremiseProcessor getPremiseProcessor(Param p) {
        return null;
    }

    @Override
    public ConceptBuilder getConceptBuilder() {
        return null;
    }


    @Override
    public boolean accept(Task t) {
        if (tasks.add(t)) {
            tasksAddedThisCycle++;
            return true;
        }
        return false;
    }



    protected int num(float p, int min, int max) {
        if ((max == min) || (p == 0)) return min;
        return Math.round((p * (max - min)))+min;
    }


    protected int processNewTasks() {



        int t = 0;
        final int mt = maxTasksPerCycle;

        //int nt = tasks.size();
        //long now = memory.time();

        //float maxPriority = -1;
        //float maxQuality = Float.MIN_VALUE, minQuality = Float.MAX_VALUE;

        Iterator<Task> ii = tasks.iterateHighestFirst(temporary);
        while (ii.hasNext()) {

            Task task = ii.next();

            //float currentPriority = task.getPriority();
            //if (maxPriority == -1) maxPriority = currentPriority; //first one is highest

            //float currentQuality = task.getQuality();
            //if (currentQuality < minQuality) minQuality = currentQuality;
            //else if (currentQuality > maxQuality) maxQuality = currentQuality;

            if (TaskProcess.run(memory, task) != null) {
                t++;
                if (mt != -1 && t >= mt) break;
            }
        }
        temporary.clear();

            /*
            System.out.print(tasksAddedThisCycle + " added, " + nt + " unique  ");
            System.out.print("pri=[" + currentPriority + " .. " + maxPriority + "]  ");
            System.out.print("qua=[" + minQuality + " .. " + maxQuality + "]  ");
            System.out.println();
            */

        tasks.clear();
        tasksAddedThisCycle = 0;
        return t;
    }



    @Override
    public void cycle() {
        //System.out.println("\ncycle " + memory.time() + " : " + concepts.size() + " concepts");


        //System.out.println("before: " + Arrays.toString(concepts.getPriorityHistogram(4)));

        int newTasks = processNewTasks();

        final float tlfd = memory.param.cycles(param.termLinkForgetDurations);

        float maxPriority = concepts.getPriorityMax();
        float minPriority = concepts.getPriorityMin();

        //2. fire all concepts

        temporaryC.addAll((Collection)concepts.values());


        /*
        {
            //clear the bag, it will be repopulated by task activity to follow
            concepts.clear();

            //crude forgetting, TODO improve
            final float forgetScale = 1.0f - newTasks * (1.0f / memory.param.cycles(memory.param.conceptForgetDurations));
            final long now = memory.time();
            System.out.println(forgetScale);
            for (final Concept c : temporaryC) {
                c.getBudget().mulPriority(forgetScale);
                c.getBudget().setLastForgetTime(now);
            }
        }
        */

        for (final Concept c : temporaryC) {

            int conceptTaskLinks = c.getTaskLinks().size();
            if (conceptTaskLinks == 0)
                continue;

            float cp = c.getPriority();
            float p = normalizePriority ? normalize(cp, minPriority, maxPriority) : cp;
            //the concept can become activated by other concepts during this iteration
            if (p < minPriority) p = minPriority;
            if (p > maxPriority) p = maxPriority;


            int fires = num(p, minTaskLink, maxTaskLink);
            if (fires < 1) continue;
            int termFires = num(p, minTermLink, maxTermLink);
            if (termFires < 1) continue;


            for (int i = 0; i < fires; i++) {
                TaskLink tl = c.getTaskLinks().forgetNext(param.taskLinkForgetDurations, memory);
                if (tl == null) break;

                ConceptProcess.forEachPremise(c, tl,
                        termFires,
                        tlfd,
                        proc -> proc.run()
                );
            }

        }

        temporaryC.clear();

    }


    @Override
    public void reset(Memory memory) {
        super.reset(memory);
        tasks.clear();

    }


    static float normalize(final float p, final float min, final float max) {
        if (max == min) return 0f;
        return (p - min)/(max-min);
    }



    @Override
    public void delete() {
        concepts.delete();
    }

    @Override
    public CacheBag<Term, Concept> newConceptIndex() {
        return new GuavaCacheBag();
    }

    @Override
    public int getMaximumNALLevel() {
        return 0;
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

    @Override
    final public Concept newConcept(Term t, Budget b, Memory m) {
        return param.newConcept(t, b, m);
    }

    public Solid level(int i) {
        memory.setLevel(i);
        return this;
    }
}
