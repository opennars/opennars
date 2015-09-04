package nars.nar.experimental;

import com.google.common.collect.Iterators;
import com.gs.collections.api.block.procedure.Procedure2;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Param;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.MapCacheBag;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.budget.Itemized;
import nars.concept.AtomConcept;
import nars.concept.Concept;
import nars.concept.ConceptBuilder;
import nars.concept.DefaultConcept;
import nars.io.Texts;
import nars.io.out.TextOutput;
import nars.link.TLink;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.meter.NARTrace;
import nars.nal.Deriver;
import nars.nal.PremiseProcessor;
import nars.nar.Default;
import nars.nar.NewDefault;
import nars.op.app.Commander;
import nars.process.ConceptProcess;
import nars.process.ConceptTaskLinkProcess;
import nars.process.ConceptTaskTermLinkProcess;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.filter.DerivationFilter;
import nars.task.filter.FilterBelowConfidence;
import nars.task.filter.FilterDuplicateExistingBelief;
import nars.task.filter.LimitDerivationPriority;
import nars.term.Atom;
import nars.term.Term;
import nars.util.data.map.nbhm.NonBlockingHashMap;
import nars.util.data.random.XorShift1024StarRandom;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.infinispan.commons.equivalence.AnyEquivalence;
import org.infinispan.util.concurrent.BoundedConcurrentHashMap;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * "Adaptive Logic And Neural Networks" Spiking continuous-time model
 * designed by TonyLo
 */
public class Alann extends AbstractNARSeed<MapCacheBag<Term,Concept>,Param> {

    private final int deriversPerThread;
    final int threads;

    private final List<Task> sorted = Global.newArrayList();

    @Deprecated
    public final Default param = new NewDefault() {

        @Override
        protected DerivationFilter[] getDerivationFilters() {
            return new DerivationFilter[]{
                    new FilterBelowConfidence(0.01),
                    new FilterDuplicateExistingBelief(),
                    new LimitDerivationPriority()
                    //param.getDefaultDerivationFilters().add(new BeRational());
            };
        }

    }; // shadow defaults, will replace once refactored

    final Random rng = new XorShift1024StarRandom(1);
    final ItemAccumulator<Task> newTasks = new ItemAccumulator(Budget.plus);

    final static Procedure2<Budget,Budget> budgetMerge = Budget.plus;

    private final Map<Term, Concept> conceptsMap;

    final static int maxConcepts = 128*1024;


    float maxCyclesPerSecond = -1;


    Commander commander;

    final int maxNewTasksPerCycle = 10;
    final int maxNewTaskHistory = 100;

    ExecutorService exe;


    public static void main(String[] args) {
        //temporary testing shell

        Global.DEBUG = true;
        Global.EXIT_ON_EXCEPTION = true;

        NAR n = new NAR(new Alann(4, 2));
        TextOutput.out(n);
        NARTrace.out(n);

        n.input("<x --> y>.\n" +
                "<y --> z>.\n" +
                "<x --> z>?\n");

        n.frame(8);

    }



    @Override final public boolean accept(final Task t) {
        return newTasks.add(t);
    }

    /** particle that travels through the graph,
     * responsible for deciding what to derive */
    public static class Derivelet  {


        private DeriveletsThread runner;

        /** current location */
        public Concept concept;


        public Derivelet() {
        }


        public ConceptProcess nextPremise(long now) {

            final Concept concept = this.concept;



            concept.getBudget().forget(now, 4f, 0);



            TaskLink tl = concept.getTaskLinks().forgetNext();
            if ((tl == null) || (tl.getTask().isDeleted()))
                return null;

            /*if (runner.nextFloat() < 0.1) {
                return new ALANNConceptTaskLinkProcess(concept, tl);
            }
            else*/ {
                TermLink tm = concept.getTermLinks().forgetNext();
                if ((tm != null) && (tl.type != TermLink.TRANSFORM)) {
                    return new ALANNConceptTaskTermLinkProcess(concept, tl, tm);
                }
                else {
                    return new ALANNConceptTaskLinkProcess(concept, tl);
                }
            }

        }

        protected void inputDerivations(final Set<Task> derived) {
            if (derived!=null) {
                //transform this ConceptProcess's derivation to a TaskProcess and run it
                final Memory mem = concept.getMemory();

                derived.forEach(/*newTaskProcess*/ t -> {

                    if (t.init(mem)) {
                        //System.err.println("direct input: " + t);
                        TaskProcess.run(mem, t);
                    }

                });
            }
        }

        /** determines a next concept to move adjacent to
         *  the concept it is currently at
         */
        public Concept nextConcept() {

            final Concept concept = this.concept;

            if (concept == null) {
                return null;
            }


            final float x = runner.nextFloat();

            //calculate probability it will stay at this concept
            final float stayProb = (concept.getPriority()) * 0.8f;
            if (x < stayProb ) {
                //stay here
                return concept;
            }
            else {
                final TLink tl;
                float rem = 1.0f - stayProb;
                if (x > stayProb + rem/2 ) {
                    tl = concept.getTermLinks().forgetNext();
                } else {
                    tl = concept.getTaskLinks().forgetNext();
                }
                if (tl != null) {
                    Concept c = runner.concept(tl.getTerm());
                    if (c != null) return c;
                }
            }

            return null;
        }

        /** run next iteration; true if still alive by end, false if died and needs recycled */
        final public boolean cycle(long now) {

            final Concept current = this.concept;

            if ( (this.concept = nextConcept()) == null) {
                return false;
            }

            final ConceptProcess p = nextPremise(now);
            if (p!=null)
                p.run();

            return true;
        }


        public final void start(final Concept concept, final DeriveletsThread runner) {
            this.runner = runner;
            this.concept = concept;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '@' + concept;
        }

        private class ALANNConceptTaskTermLinkProcess extends ConceptTaskTermLinkProcess {

            public ALANNConceptTaskTermLinkProcess(Concept concept, TaskLink tl, TermLink tm) {
                super(concept, tl, tm);
            }

            @Override protected void inputDerivations() {
                Derivelet.this.inputDerivations(derived);
            }

            @Override
            protected synchronized void derive() {
                super.derive();
            }
        }

        private class ALANNConceptTaskLinkProcess extends ConceptTaskLinkProcess {
            public ALANNConceptTaskLinkProcess(Concept concept, TaskLink tl) {
                super(concept, tl);
            }

            @Override protected void inputDerivations() {
                Derivelet.this.inputDerivations(derived);
            }

            @Override
            protected synchronized void derive() {
                super.derive();
            }
        }
    }



    public class DeriveletsThread implements Runnable {

        final static long ifNoConceptsWaitMS = 1;

        /** random # generator local to this thread */
        private final Random rng;

        /** current concept, next concept */
        private Supplier<Concept> conceptSupply;

        public Derivelet[] d;
        private long now;

        public DeriveletsThread(Collection<Derivelet> d, Supplier<Concept> conceptSupply) {
            this.conceptSupply = conceptSupply;
            this.d = d.toArray(new Derivelet[d.size()]);
            this.rng = newRandom();
        }


        @Override
        public String toString() {
            return "DeriveletsThread:" + '/' + Thread.currentThread().getId() + '*' +
                    d.length;
                    //Arrays.toString(d);
        }

        protected void cycle() {
            Derivelet current = null;

            try {

                this.now = time();

                boolean active = false;

                for (final Derivelet x : d) {
                    active |= cycleDerivelet(current = x);
                }

                if (!active) {
                    try {
                        Thread.sleep(ifNoConceptsWaitMS);
                    } catch (InterruptedException e) { }
                }

            } catch (Exception e) {
                e.printStackTrace();
                kill(current);
            }
        }

        final boolean cycleDerivelet(final Derivelet d) {
            //System.out.println(d + " cycle");

            if (!d.cycle(now)) {

                //recycle this derivelet
                Concept next = conceptSupply.get();
                if (next != null) {
                    d.start(next, this);
                } else {
                    return false;
                }
            }

            return true;
        }

        /** kill a derivelet if it was interrupted or exception,
         * freeing any hostage concept it may have (which will be locked)  */
        protected void kill(final Derivelet d) {
            final Concept currentConcept = d.concept;
            if (currentConcept!=null) {
                d.concept = null;
            }
        }

        public void run() {

            //System.out.println(this + " start");

            while (true /*running*/) {
                cycle();
            }
        }


        public final float nextFloat() {
            return this.rng.nextFloat();
        }

        public final Concept concept(final Term term) {
            return memory.concept(term);
        }
    }

    public class InstrumentedDeriveletsThread extends DeriveletsThread {

        final Mean m = new Mean();
        final int reportPeriod = 50;
        private long prevCyc;

        public InstrumentedDeriveletsThread(Collection<Derivelet> d, Supplier<Concept> concepts) {
            super(d, concepts);
        }

        @Override
        protected void cycle() {

            long cyc = memory.time();
            if (cyc == prevCyc) {
                sleepWait();
                return;
            }
            this.prevCyc = cyc;

            long start = System.nanoTime();
            super.cycle();
            long end = System.nanoTime();

            double timeMS = (end-start)/1.0e6;
            m.increment(timeMS);

            if (cyc % reportPeriod == 0) {
                System.out.println(this + " @ " + cyc + " (" + Texts.n4(timeMS) + "ms avg),  " + concepts.size() + " concepts" );

                m.clear();

            }
        }

        private void sleepWait() {
            try {
                Thread.sleep(ifNoConceptsWaitMS);
            } catch (InterruptedException e) {            }
        }
    }

    public Alann(int deriversPerThread, int threads) {
        super(new MapCacheBag(
                //new ConcurrentHashMap(maxConcepts)


                new BoundedConcurrentHashMap(
                        /* capacity */ maxConcepts,
                        /* concurrency */ threads,
                        /* key equivalence */
                        AnyEquivalence.getInstance(Term.class),
                        AnyEquivalence.getInstance(Concept.class))

                //new NonBlockingHashMap<>(maxConcepts)

                //Global.newHashMap()
        ));
        this.conceptsMap = concepts.data;

        this.deriversPerThread = deriversPerThread;
        this.threads = threads;


    }


    @Deprecated @Override public Concept nextConcept() {
        throw new RuntimeException("should not be called, this method will be deprecated");
    }

    @Override
    public void cycle() {

        final int size = newTasks.size();
        if (size!=0) {

            int toDiscard = Math.max(0, size - maxNewTaskHistory);
            int remaining = newTasks.update(maxNewTaskHistory, sorted);

            if (size > 0) {

                int toRun = Math.min( maxNewTasksPerCycle, remaining);

                TaskProcess.run(memory, sorted, toRun, toDiscard);

                //System.out.print("newTasks size=" + size + " run=" + toRun + "=(" + x.length + "), discarded=" + toDiscard + "  ");
            }
        }

        {
            float maxCPS = maxCyclesPerSecond;
            if (maxCPS > 0) {
                long ms = (long) (1000f / maxCPS);
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException e) {

                }
            }

            Thread.yield();
        }


//        if (index.size() > 0) {
//            int numDerivers = derivers.size();
//            for (int i = 0; i < numDerivers; i++) {
//                final Derivelet d = derivers.get(i);
//
//
//            }
//        }

        //System.out.println("cycle " + memory.time());
    }

    @Override
    public final Concept put(final Concept concept) {
        return concepts.put(concept);
    }

    @Override
    public final Concept get(final Term key) {
        return concepts.get(key);
    }


    @Override
    public Concept conceptualize(final Term term, final Budget budget, final boolean createIfMissing) {
        return conceptsMap.compute(term, (k,existing) -> {
            if (existing!=null) {
                budgetMerge.value(existing.getBudget(), budget);
                return existing;
            }
            else {
                return newConcept(term, budget, memory);
            }
        });
    }

//    @Override
//    public Concept conceptualize(final Term term, final Budget budget, final boolean createIfMissing) {
//        Concept existing = get(term);
//        if (existing == null) {
//            put(existing = newConcept(term, budget, memory));
//        }
//        else {
//            budgetMerge.value(existing.getBudget(), budget);
//        }
//        return existing;
//    }



    @Override
    public boolean reprioritize(Term term, float newPriority) {
        throw new RuntimeException("N/A");
    }

    @Override
    public Concept remove(Concept c) {
        Itemized removed = concepts.remove(c.getTerm());
        if ((removed==null) || (removed!=c))
            throw new RuntimeException("concept unknown");

        return c;
    }



    @Override
    public Param getParam() {
        param.the(Deriver.class, NewDefault.der);
        param.setTermLinkBagSize(32);
        return param;
    }

    @Override
    public Random getRandom() {
        return rng;
    }

    final Random newRandom() {
        return new XorShift1024StarRandom(1);
    }

    /** this will be invoked in the DeriveletsThread.
     *  Concept.runQueued will thus be called in-between
     *  derivelet cycles.
     * */
    public final Supplier<Concept> newConceptSupply() {
        final Iterator<Concept> indexIterator = Iterators.cycle(concepts);
        return () -> {

            if (!indexIterator.hasNext())
                return null;

            return indexIterator.next();
        };
    }



    @Override
    final public CacheBag<Term, Concept> getConceptIndex() {
        return concepts;
    }

    public Memory newMemory() {

        final Param p = getParam();

        return new Memory(
                getRandom(),
                getMaximumNALLevel(),
                p,
                getConceptBuilder(),
                getPremiseProcessor(p),
                concepts
        );
    }


    @Override
    public void init(NAR nar) {



        param.init(nar);

        //param.taskProcessThreshold.set(0); //process everything, even if budget is zero

        commander = new Commander(nar);

        initDerivelets();
    }

    private void initDerivelets() {

        exe = Executors.newFixedThreadPool(threads);

        //shared by all threads

        for (int j = 0; j < threads; j++) {

            final List<Derivelet> d = Global.newArrayList();

            for (int i = 0; i < deriversPerThread; i++) {
                d.add( new Derivelet() );
            }

            /** each thread gets its own iterator */
            final Supplier<Concept> cs = newConceptSupply();

            DeriveletsThread dt
                    //= new DeriveletsThread(d);
                    = new InstrumentedDeriveletsThread(d, cs);

            exe.execute(dt);

        }


        /** set this thread priority to zero to allow derivelet threads more priority */
        //Thread.currentThread().setPriority(1);
    }

    @Override
    public PremiseProcessor getPremiseProcessor(final Param p) {
        return param.getPremiseProcessor(p);
    }



    public Concept newConcept(Term t, Budget b, Bag<Sentence, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, Memory m) {

        if (t instanceof Atom) {
            return new AtomConcept(t, b,
                    termLinks, taskLinks,
                    null, m
            );
        }
        else {
            return new DefaultConcept(t, b,
                    taskLinks, termLinks,
                    null,
                    param.newConceptBeliefGoalRanking(),
                    m
            );
        }

    }

    @Override
    public ConceptBuilder getConceptBuilder() {
        return param.getConceptBuilder();
    }

    @Override
    public Concept newConcept(final Term t, final Budget b, final Memory m) {

        Bag<Sentence, TaskLink> taskLinks =
                new SynchronizedCurveBag<>(rng, /*sentenceNodes,*/ param.getConceptTaskLinks());
        taskLinks.mergeAverage();

        Bag<TermLinkKey, TermLink> termLinks =
                new SynchronizedCurveBag<>(rng, /*termlinkKeyNodes,*/ param.getConceptTermLinks());
        termLinks.mergeAverage();

        return newConcept(t, b, taskLinks, termLinks, m);
    }

//
//    @Override
//    protected Memory newMemory(Param narParam, ConceptProcessor policy) {
//        Memory m = super.newMemory(narParam, policy);
//        m.on(this); //default conceptbuilder
//        return m;
//    }
//
//    @Override
//    public CycleProcess newCycleProcess() {
//        return new AlannCycle();
//    }
//
//    @Override
//    protected CacheBag<Term, Concept> newIndex() {
//        return new GuavaCacheBag<>();
//    }
//
//    @Override
//    protected int getMaximumNALLevel() {
//        return 8;
//    }
//
//
//
//
//    @Override
//    public Concept newConcept(final Term t, final Budget b, final Memory m) {
//        return new AlannConcept(t, b, m);
//    }
//
//
//
//    public static class AlannConcept extends AbstractConcept {
//
//        final ArrayListBeliefTable beliefs;
//
//        final TermLinkBuilder termLinkBuilder;
//
//        final ObjectFloatHashMap<TermLink> termLinks = new ObjectFloatHashMap<>();
//
//        float activation = 0;
//        Task lastTask;
//
//        public AlannConcept(Term term, Budget budget, Memory memory) {
//            super(term, budget, memory);
//            beliefs = new ArrayListBeliefTable(4, new BeliefTable.BeliefConfidenceAndCurrentTime(this));
//            termLinkBuilder = new TermLinkBuilder(this);
//
//            if (termLinkBuilder.templates()!=null) {
//                for (TermLinkTemplate tlt : termLinkBuilder.templates())
//                    termLinks.put(termLinkBuilder.out(tlt), 0);
//            }
//        }
//
//        @Override
//        public boolean processBelief(Premise nal, Task task) {
//            System.out.println(this + " processBelief " + task);
//
//            //final TaskLink taskLink = new TaskLink(task, task.getBudget());
//
//
//            //believe(taskLink);
////
////            if (task.isInput()) {
////                //trigger spike event (spike const * truth.confidence)
////                onSpike(taskLink, 1f);
////            }
//
//
//            return false;
//        }
//
//        @Override
//        public Task processQuestion(Premise nal, Task task) {
//            System.out.println(this + " processQuestion " + task);
//
////
////            final TaskLink taskLink = new TaskLink(task, task.getBudget());
////            onSpike(taskLink, 1f);
////
////            lastTask = task;
//
//
//            return null;
//        }
//
//        protected void believe(TaskLink beliefTask) {
//            final Task theTask = beliefTask.getTask();
//            BeliefTable.Ranker belifeRanker = new BeliefTable.Ranker() {
//
//                @Override
//                public float rank(Task t, float bestToBeat) {
//                    return t.getTruth().getConfidence();
//                }
//            };
//
//            if (beliefs.add(theTask, belifeRanker, this, null) == theTask) {
//                inferLocal(beliefTask);
//                System.out.println(this + " added belief " + theTask);
//            }
//        }
//
//        /**  Local Inference on belief (revision/Choice/Decision) */
//        protected void inferLocal(TaskLink t) {
//
//            //TODO ???
//
//
//
//        }
//
//
//        /*onTask ( task)
//        {
//            // just store/replace the task in the concept
//        }*/
//
//        @Override
//        public boolean processGoal(Premise nal, Task task) {
//            System.out.println(this + " processGoal " + task);
//            lastTask = task;
//            return false;
//        }
//
//
//        protected void forget() {
//            //TODO make sure # cycles relative to duration are appropriately used
//            /*switch (param.forgetting) {
//                case Iterative:
//                    BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
//                    break;
//                case Periodic:*/
//            BudgetFunctions.forgetPeriodic(this, memory.param.conceptForgetDurations.floatValue(), (float) 0, memory.time());
//        }
//        protected void forget(TermLink tl) {
//            //TODO make sure # cycles relative to duration are appropriately used
//            /*switch (param.forgetting) {
//                case Iterative:
//                    BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
//                    break;
//                case Periodic:*/
//            BudgetFunctions.forgetPeriodic(tl, memory.param.termLinkForgetDurations.floatValue(), 0, memory.time());
//        }
//        protected void forget(TaskLink tl) {
//            //TODO make sure # cycles relative to duration are appropriately used
//            /*switch (param.forgetting) {
//                case Iterative:
//                    BudgetFunctions.forgetIterative(x.budget, forgetCycles, relativeThreshold);
//                    break;
//                case Periodic:*/
//            BudgetFunctions.forgetPeriodic(tl, memory.param.termLinkForgetDurations.floatValue(), 0, memory.time());
//        }
//
//        protected void onSpike(TaskLink taskLink, float multiplier) {
//            // adjust activation level for decay
//            //forget();
//
//
//
//            float conf =
//                    taskLink.getTask().isQuestOrQuestion() ?
//                            1f :
//                            taskLink.getTask().getTruth().getConfidence();
//            float spikeActivation = taskLink.getPriority() * conf * multiplier;
//
//            // if activation > threshold then this.Activate()
//            if (getPriority() > activationThreshold.floatValue()) {
//
//                taskLink.addPriority(spikeActivation);
//                taskLink.orDurability(getDurability());
//                taskLink.orQuality(getQuality());
//
//                for (TermLink tl : termLinks.keySet()) {
//
//                    tl.addPriority(spikeActivation);
//                    tl.orDurability(getDurability());
//                    tl.orQuality(getQuality());
//                    activate(taskLink, tl, multiplier);
//
//                }
//            }
//            else {
//                //System.out.println(this + " at " + toBudgetString());
//
//                //accumulate difference
//                addPriority(spikeActivation);
//            }
//
//
//
//        }
//
//        /** not used currently */
//        public void forEachSubterm(TermVisitor consumeTerm) {
//
//            final Term t = getTerm();
//            if (t instanceof Compound) {
//                Compound c = (Compound)t;
//                c.recurseTerms(consumeTerm);
//            }
//
//        }
//
//        public void forEachBelief(Consumer<Task> beliefConsumer) {
//            beliefs.forEach(beliefConsumer);
//        }
//
//        protected void activate(TaskLink taskLink, TermLink termLink, float multiplier) {
//
//
//            System.out.println(this + " activated by " + taskLink + "," + termLink + " @ " + getPriority() );
//
//            //reset activationLevel because it has fired
//
//
//
//            final Term thisTerm = getTerm();
//
//            //for each outbound link
//
//                Term term = termLink.getTerm();
//
//                if (term.equals(thisTerm)) return;
//
//                System.out.println("  spiking: " + term);
//
//                AlannConcept c = (AlannConcept)memory.conceptualize(term, taskLink.getBudget());
//
//                //trigger spike event (spike const * truth.confidence)
//                c.onSpike(taskLink, multiplier * spikeDecay);
//
//            //for each activeBelief
//            //  activeBeliefs = getActivatedBeliefs() // belief links with active src and destination
//            forEachBelief(b -> {
//                /*
//                new_tasks = do inference (lastTaskRx, activeBelief)
//                for each newtask
//                addTask(newTask)
//                */
//
//                //TODO
//
//            });
//
//            //setPriority(0);
//            forget();
//        }
//
//
//
//
//        // the following should not be necessary and will be removed:
//
//
//        @Override
//        public Bag<Sentence, TaskLink> getTaskLinks() {
//            return null;
//        }
//
//        @Override
//        public Bag<TermLinkKey, TermLink> getTermLinks() {
//            return null;
//        }
//
//        @Override
//        public TaskLink activateTaskLink(TaskLinkBuilder taskLinkBuilder) {
//            return null;
//        }
//
//        @Override
//        public boolean linkTerms(Budget budgetRef, boolean b) {
//            return false;
//        }
//
//        @Override
//        public TermLink activateTermLink(TermLinkBuilder termLinkBuilder) {
//            return null;
//        }
//
//        @Override
//        public void updateLinks() {
//
//        }
//
//        @Override
//        public boolean link(Task currentTask) {
//            return false;
//        }
//
//        @Override
//        public TermLinkBuilder getTermLinkBuilder() {
//            return null;
//        }
//
//        @Override
//        public TermLink nextTermLink(TaskLink taskLink) {
//            return null;
//        }
//
//        @Override
//        public BeliefTable getBeliefs() {
//            return beliefs;
//        }
//
//        @Override
//        public BeliefTable getGoals() {
//            return null;
//        }
//
//        @Override
//        public TaskTable getQuestions() {
//            return null;
//        }
//
//        @Override
//        public TaskTable getQuests() {
//            return null;
//        }
//
//
//
//    }
//
////    static class AlannConceptProcess extends ConceptProcess {
////
////        public AlannConceptProcess(Memory memory, Concept concept, TaskLink taskLink) {
////            super(memory, concept, taskLink);
////        }
////
////        @Override
////        protected void afterDerive() {
////            super.afterDerive();
////
////            if (derived!=null && !derived.isEmpty())
////                System.out.println(this + " derived " + derived);
////        }
////
////        public void run(TermLink tl) {
////
////            beforeDerive();
////
////            derive();
////
////            if (tl != null)
////                processTerm(tl);
////
////            afterDerive();
////
////        }
////
////        @Override
////        protected void processTerms() {
////            //nothing
////        }
////    }
//
//    class AlannCycle extends AbstractCycle {
//
//        /**
//         * holds original (user-input) goals and question tasks
//         */
//        protected final ItemAccumulator<Task> commands = new ItemAccumulator(new ItemComparator.Plus());
//
//        Iterator<Task> commandCycle;
//
//        /**
//         * this is temporary if it can be is unified with the concept's main index
//         */
//        Bag<Term, Concept> concepts;
//
//
//        @Override
//        public boolean accept(Task t) {
//
//            if (t.isInput() && !t.isJudgment()) {
//                //match against commands or goals and adjust as necessary
//                return commands.add(t);
//            } else {
//                return add(t);
//            }
//
//            /*
//            {
//                Update UI as required
//                decrement priority of derived tasks
//                Create new concepts if required
//                trigger onTask event for respective concepts
//            }
//            */
//
//        }
//
//        public double getConceptActivationThreshold() {
//            return conceptActivationThreshold;
//        }
//
//        @Override
//        public void reset(Memory m) {
//            super.reset(m);
//
//            if (concepts == null)
//                concepts = new CurveBag(memory.random, MAX_CONCEPTS);
//            else
//                concepts.clear();
//
//            commands.clear();
//            commandCycle = Iterators.cycle(commands.items);
//        }
//
//        /**
//         * an input belief, or a derived task of any punctuation
//         */
//        protected boolean add(Task t) {
//            Concept c = memory.conceptualize(t.getTerm(), t.getBudget());
//            if (c == null)
//                return false;
//
//            TaskProcess.run(memory, t);
//
//            return true;
//        }
//
//        @Override
//        public int size() {
//            return concepts.size();
//        }
//
//        @Override
//        public void conceptPriorityHistogram(double[] bins) {
//            concepts.getPriorityHistogram(bins);
//        }
//
//        /** forever (slow cycle) “cycle” */
//        @Override public void cycle() {
//
//
//
//            // it needs to be thought of as a parallel system
//            // if you process it sequentially then you have to store all the spikes for each cycle for each concept
//            // However, if you think of it an an event based model then it is much easier
//            // So concepts are, ideally, independent processing elements that receive events (spike/tasks)
//            // It does not really translate to a loop.
//            // So no ‘real’ cycles
//
//
//            /*
//                inject tasks and goals into respective concepts only questions and goals
//
//                insert task into each relevant concept and replace existing as necessary
//            */
//
//            inputNextPerception();
//
//
//            if (commandCycle.hasNext()) {
//                for (int i = 0; i < commandsPerCycle; i++) {
//                    Task nextCommand = commandCycle.next();
//                    add(nextCommand);
//                    //TaskProcess.run(memory, nextCommand);
//                }
//            }
//
//            // for fast cycle in 1 to n “subcycle”
//            for (int i = 0; i < subcyclesPerCycle; i++)
//                subcycle();
//
//        }
//
//        protected void subcycle() {
//            /*insert input tasks into respective concepts */
//
//            for (Concept c : concepts) {
//
//                AlannConcept a = (AlannConcept)c;
//
//                //if level > threshold {
//                /*if (a.active())*/ {
//
//                    a.forEachBelief(b -> {
//
//                        /*
//                        send spike to all outbound belief links modulating for truth.confidence
//                        reset activation
//                        do inference on all beliefs that have a matching activated concept on the other end of the link
//                        send generated (derived?) tasks to relevant concepts
//                        */
//
//                        float bconf = b.getTruth().getConfidence();
//
//                        //a.onSpike(new TaskLink(b, b.getBudget()), bconf);
//
//                    });
//
//                }
//
//                /*
//
//
//
//
//
//                }
//
//                */
//            }
//
//        }
//
//
//        @Override
//        public Concept conceptualize(final Term term, Budget budget, boolean createIfMissing) {
//            return conceptualize(term, budget, createIfMissing, memory.time(), concepts);
//        }
//
//
//        @Override
//        public Concept nextConcept() {
//            return null;
//        }
//
//        @Override
//        public boolean reprioritize(Term term, float newPriority) {
//            return false;
//        }
//
//        @Override
//        public Concept remove(Concept c) {
//            return concepts.remove(c.getTerm());
//        }
//
//        @Override
//        protected void on(Concept c) {
//
//        }
//
//        @Override
//        protected void off(Concept c) {
//
//        }
//
//        @Override
//        protected boolean active(Term t) {
//            return concepts.get(t)!=null;
//        }
//
//    }
}
