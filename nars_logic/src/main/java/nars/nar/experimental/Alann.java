package nars.nar.experimental;

import com.google.common.collect.Iterators;
import com.gs.collections.api.block.procedure.Procedure2;
import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Param;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.MapCacheBag;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.budget.Itemized;
import nars.concept.Concept;
import nars.concept.ConceptBuilder;
import nars.io.out.TextOutput;
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
import nars.term.Term;
import nars.util.data.random.XorShift1024StarRandom;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * "Adaptive Logic And Neural Networks" Spiking continuous-time model
 * designed by TonyLo
 */
public class Alann extends AbstractNARSeed<CacheBag<Term,Concept>,Param> {

    private MapCacheBag<Term, Concept> index;
    private Iterator<Concept> indexIterator;
    private List<Task> sorted = Global.newArrayList();
    @Deprecated final Default param = new NewDefault(); // shadow defaults, will replace once refactored
    final Random rng = new XorShift1024StarRandom(1);
    final ItemAccumulator<Task> newTasks = new ItemAccumulator(Budget.plus);

    final static Procedure2<Budget,Budget> budgetMerge = Budget.plus;

    Commander commander;
    public final List<Derivelet> derivers = Global.newArrayList();
    final int maxNewTasksPerCycle = 10;
    final int maxNewTaskHistory = 20;

    public static void main(String[] args) {
        //temporary testing shell

        Global.DEBUG = true;
        Global.EXIT_ON_EXCEPTION = true;

        NAR n = new NAR(new Alann(1));
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
    class Derivelet {

        /** current location */
        public Concept concept;


        public ConceptProcess nextPremise() {

            final Concept concept = this.concept;

            TaskLink tl = concept.getTaskLinks().forgetNext();
            if (tl == null) return null;

            //TODO better probabilities
            if (Math.random() < 0.1) {
                return new ConceptTaskLinkProcess(concept, tl);
            }
            else {
                TermLink tm = concept.getTermLinks().forgetNext();
                if (tm == null)
                    return new ConceptTaskLinkProcess(concept, tl);

                return new ConceptTaskTermLinkProcess(concept, tl, tm) {

                    @Override protected void inputDerivations() {
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

                };
            }

        }

        /** determines a next concept to move adjacent to
         *  the concept it is currently at
         */
        public Concept nextConcept() {
            if (concept == null) {
                return null;
            }

            if (Math.random() < 0.5) {
                //stay here
                return concept;
            }
            if (Math.random() < 0.25) {
                TermLink tl = concept.getTermLinks().forgetNext();
                Concept tlConcept = get(tl.getTerm());
                if (tlConcept!=null) return tlConcept;
            }
            if (Math.random() < 0.25) {
                TaskLink tl = concept.getTaskLinks().forgetNext();
                Concept tkConcept = get(tl.getTerm());
                if (tkConcept != null) return tkConcept;
            }

            //stay here
            return concept;
        }

        /** run next iteration; true if still alive by end, false if died and needs recycled */
        final public boolean cycle() {
            if ( (concept = nextConcept()) == null)
                return false;

            final ConceptProcess p = nextPremise();
            p.run();

            return true;
        }

        public TaskLink taskLink;

        public Task belief;

        public void start(Concept concept) {
            this.concept = concept;
        }
    }




    public Alann(int initialDerivers) {
        for (int i = 0; i < initialDerivers; i++)
            derivers.add( new Derivelet() );
    }

    @Override
    public void cycle() {

        final int size = newTasks.size();
        if (size!=0) {

            int toDiscard = Math.max(0, size - maxNewTaskHistory);
            int remaining = newTasks.update(maxNewTaskHistory, sorted);

            if (size > 0) {

                int toRun = Math.min( maxNewTasksPerCycle, remaining);

                final TaskProcess[] x = TaskProcess.run(memory, sorted, toRun, toDiscard);


                for (final TaskProcess y : x)
                    y.run();

                //System.out.print("newTasks size=" + size + " run=" + toRun + "=(" + x.length + "), discarded=" + toDiscard + "  ");
            }
        }


        if (index.size() > 0) {
            int numDerivers = derivers.size();
            for (int i = 0; i < numDerivers; i++) {
                final Derivelet d = derivers.get(i);

                if (!d.cycle()) {

                    //recycle this derivelet
                    Concept c = nextConcept();
                    if (c != null)
                        d.start(c);
                    //else ?
                }
            }
        }

        //System.out.println("cycle " + memory.time());
    }

    @Override
    public Concept put(Concept concept) {
        return index.put(concept);
    }

    @Override
    public Concept get(Term key) {
        return index.get(key);
    }

    @Override
    public Concept conceptualize(Term term, Budget budget, boolean createIfMissing) {
        Concept existing = get(term);
        if (existing == null) {
            put(existing = newConcept(term, budget, memory));
        }
        else {
            budgetMerge.value(existing.getBudget(), budget);
        }
        return existing;
    }

    @Override
    public Concept nextConcept() {
        final Concept c = indexIterator.next();
        if (c == null)
            return null;
        return c;
    }

    @Override
    public boolean reprioritize(Term term, float newPriority) {
        throw new RuntimeException("N/A");
    }

    @Override
    public Concept remove(Concept c) {
        Itemized removed = index.remove(c.getTerm());
        if ((removed==null) || (removed!=c))
            throw new RuntimeException("concept unknown");

        c.getBudget().zero(); return c;
    }



    @Override
    public Param newParam() {
        param.the(Deriver.class, NewDefault.der);
        return param;
    }

    @Override
    public Random getRandom() {
        return rng;
    }

    @Override
    public CacheBag<Term, Concept> newConceptIndex() {
        this.index = new MapCacheBag(Global.newHashMap());
        this.indexIterator = Iterators.cycle(index);
        return this.index;
    }

    public Memory newMemory() {

        final Param p = newParam();

        return new Memory(
                getRandom(),
                getMaximumNALLevel(),
                p,
                getConceptBuilder(),
                getPremiseProcessor(p),
                newConceptIndex()
        );
    }


    @Override
    public void init(NAR nar) {

        param.init(nar);

        //param.taskProcessThreshold.set(0); //process everything, even if budget is zero

        commander = new Commander(nar);


    }

    @Override
    public PremiseProcessor getPremiseProcessor(Param p) {
        return param.getPremiseProcessor(p);
    }

    @Override
    public ConceptBuilder getConceptBuilder() {
        return param.getConceptBuilder();
    }

    @Override
    public Concept newConcept(final Term t, final Budget b, final Memory m) {

        Bag<Sentence, TaskLink> taskLinks =
                new CurveBag(rng, /*sentenceNodes,*/ param.getConceptTaskLinks());
        taskLinks.mergeAverage();

        Bag<TermLinkKey, TermLink> termLinks =
                new CurveBag(rng, /*termlinkKeyNodes,*/ param.getConceptTermLinks());
        termLinks.mergeAverage();

        return param.newConcept(t, b, taskLinks, termLinks, m);
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
