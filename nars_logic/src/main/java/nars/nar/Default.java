package nars.nar;

import nars.*;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.GuavaCacheBag;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.budget.ItemComparator;
import nars.clock.Clock;
import nars.clock.CycleClock;
import nars.clock.HardRealtimeClock;
import nars.clock.RealtimeMSClock;
import nars.concept.BeliefTable;
import nars.concept.Concept;
import nars.concept.ConceptBuilder;
import nars.concept.DefaultConcept;
import nars.cycle.SequentialCycle;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.nal.LogicStage;
import nars.nal.PremiseProcessor;
import nars.nal.nal8.OpReaction;
import nars.nal.nal8.operator.NullOperator;
import nars.nal.nal8.operator.eval;
import nars.op.app.STMInduction;
import nars.op.data.Flat;
import nars.op.data.json;
import nars.op.data.similaritree;
import nars.op.io.*;
import nars.op.math.add;
import nars.op.math.count;
import nars.op.mental.*;
import nars.op.meta.complexity;
import nars.op.meta.reflect;
import nars.op.software.js;
import nars.op.software.scheme.scheme;
import nars.premise.HashTableNovelPremiseGenerator;
import nars.premise.PremiseGenerator;
import nars.process.ConceptProcess;
import nars.process.CycleProcess;
import nars.process.TaskProcess;
import nars.process.concept.*;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.filter.DerivationFilter;
import nars.task.filter.FilterBelowConfidence;
import nars.task.filter.FilterDuplicateExistingBelief;
import nars.term.Compound;
import nars.term.Term;
import nars.util.data.random.XorShift1024StarRandom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static nars.op.mental.InternalExperience.InternalExperienceMode.Full;
import static nars.op.mental.InternalExperience.InternalExperienceMode.Minimal;

//import sun.tools.jstat.Operator;

/**
 * Default set of NAR parameters which have been classically used for development.
 *
 * WARNING this Seed is not immutable yet because it extends Param,
 * which is supposed to be per-instance/mutable. So do not attempt
 * to create multiple NAR with the same Default seed model
 */
public class Default extends Param implements NARSeed {

    public static final OpReaction[] exampleOperators = new OpReaction[]{
            //new Wait(),
            new NullOperator("break"),
            new NullOperator("drop"),
            new NullOperator("goto"),
            new NullOperator("open"),
            new NullOperator("pick"),
            new NullOperator("strike"),
            new NullOperator("throw"),
            new NullOperator("activate"),
            new NullOperator("deactivate")
    };
    //public final Random rng = new RandomAdaptor(new MersenneTwister(1));
    public final Random rng = new XorShift1024StarRandom(1);
    /**
     * How many concepts to fire each cycle; measures degree of parallelism in each cycle
     */
    public final AtomicInteger conceptsFiredPerCycle = new AtomicInteger();

    /**
     * max # of inputs to perceive per cycle; -1 means unlimited (attempts to drains input to empty each cycle)
     */
    public final AtomicInteger inputsMaxPerCycle = new AtomicInteger();

    /**
     * max # of novel tasks to process per cycle; -1 means unlimited (attempts to drains input to empty each cycle)
     */
    public final AtomicInteger novelMaxPerCycle = new AtomicInteger();
    public final OpReaction[] defaultOperators = new OpReaction[]{

            //system control
            echo.the,
            PauseInput.the,
            new reset(),

            new eval(),

            //new Wait(),
            new believe(),  // accept a statement with a default truth-value
            new want(),     // accept a statement with a default desire-value
            new wonder(),   // find the truth-value of a statement
            new evaluate(), // find the desire-value of a statement

            //concept operations for internal perceptions
            new remind(),   // create/activate a concept
            new consider(),  // do one inference step on a concept
            new name(),         // turn a compount term into an atomic term
            //new Abbreviate(),

            //new Register(),

            // truth-value operations
            new doubt(),        // decrease the confidence of a belief
            new hesitate(),      // decrease the confidence of a goal


            //Meta
            new reflect(),

            // feeling operations
            new feelHappy(),
            new feelBusy(),

            // math operations
            new count(),
            new add(),
            //new MathExpression(),


            new complexity(),

            //Term manipulation
            new Flat.flatProduct(),
            new similaritree(),

            //TODO move Javascript to a UnsafeOperators set, because of remote execution issues
            new scheme(),      // scheme evaluation


            //new NumericCertainty(),

            //io operations
            new say(),

            new schizo(),     //change Memory's SELF term (default: SELF)

            new js(), //javascdript evalaution

            new json.jsonfrom(),
            new json.jsonto()
         /*
+         *          I/O operations under consideration
+         * observe          // get the most active input (Channel ID: optional?)
+         * anticipate       // get the input matching a given statement with variables (Channel ID: optional?)
+         * tell             // output a judgment (Channel ID: optional?)
+         * ask              // output a question/quest (Channel ID: optional?)
+         * demand           // output a goal (Channel ID: optional?)
+         */

//        new Wait()              // wait for a certain number of clock cycle


        /*
         * -think            // carry out a working cycle
         * -do               // turn a statement into a goal
         *
         * possibility      // return the possibility of a term
         * doubt            // decrease the confidence of a belief
         * hesitate         // decrease the confidence of a goal
         *
         * feel             // the overall happyness, average solution quality, and predictions
         * busy             // the overall business
         *


         * do               // to turn a judgment into a goal (production rule) ??

         *
         * count            // count the number of elements in a set
         * arithmatic       // + - * /
         * comparisons      // < = >
         * logic        // binary logic
         *



         * -assume           // local assumption ???
         *
         * observe          // get the most active input (Channel ID: optional?)
         * anticipate       // get input of a certain pattern (Channel ID: optional?)
         * tell             // output a judgment (Channel ID: optional?)
         * ask              // output a question/quest (Channel ID: optional?)
         * demand           // output a goal (Channel ID: optional?)


        * name             // turn a compount term into an atomic term ???
         * -???              // rememberAction the history of the system? excutions of operatons?
         */

        /* operators for testing examples */
//        table.put("^goto", new GoTo("^go-to"));
//        table.put("^pick", new Pick("^pick"));
//        table.put("^open", new Open("^open"));
//        table.put("^break", new Break("^break"));
//        table.put("^drop", new Drop("^drop"));
//        table.put("^throw", new Throw("^throw"));
//        table.put("^strike", new Strike("^strike"));

    };
    public int cyclesPerFrame = 1;
    protected int maxNALLevel;
    /**
     * Size of TaskLinkBag
     */
    int taskLinkBagSize;
    /**
     * Size of TermLinkBag
     */
    int termLinkBagSize;
    /**
     * determines maximum number of concepts
     */
    int conceptBagSize;
    /**
     * Size of TaskBuffer
     */
    int taskBufferSize;
    InternalExperience.InternalExperienceMode internalExperience;


    /**
     * Default DEFAULTS
     */
    public Default() {
        this(1024, 1, 3);
    }


    public Default(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {

        setActiveConcepts(maxConcepts);

        conceptTaskTermProcessPerCycle.set(termLinksPerCycle);

        termLinkMaxMatched.set(5);

        //Build Parameters
        this.maxNALLevel = Global.DEFAULT_NAL_LEVEL;
        this.internalExperience =
                maxNALLevel >= 8 ? InternalExperience.InternalExperienceMode.Minimal : InternalExperience.InternalExperienceMode.None;

        setTaskLinkBagSize(16);

        setTermLinkBagSize(64);

        setNovelTaskBagSize(48);


        //Runtime Initial Values

        duration.set(5);

        shortTermMemoryHistory.set(1);
        temporalRelationsMax.set(4);

        conceptActivationFactor.set(1.0);
        conceptFireThreshold.set(0.0);

        conceptForgetDurations.set(3.0);
        taskLinkForgetDurations.set(4.0);
        termLinkForgetDurations.set(10.0);
        novelTaskForgetDurations.set(2.0);

        //param.budgetThreshold.set(0.01f);

        conceptBeliefsMax.set(11);
        conceptGoalsMax.set(8);
        conceptQuestionsMax.set(4);

        inputsMaxPerCycle.set(conceptsFirePerCycle);
        conceptsFiredPerCycle.set(conceptsFirePerCycle);
        novelMaxPerCycle.set(conceptsFirePerCycle);


        this.activeConceptThreshold.set(0.0);
        this.goalThreshold.set(0.01);

        this.taskProcessThreshold.set(Global.BUDGET_EPSILON);
        this.termLinkThreshold.set(Global.BUDGET_EPSILON);
        this.taskLinkThreshold.set(Global.BUDGET_EPSILON);

        this.executionThreshold.set(0.6);
        //executionThreshold.set(0.60);

        setClock(new CycleClock());
        outputVolume.set(100);

        reliance.set(Global.DEFAULT_JUDGMENT_CONFIDENCE);


        conceptCreationExpectation.set(0.66);

    }

    static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    protected DerivationFilter[] getDerivationFilters() {
        return new DerivationFilter[]{
                new FilterBelowConfidence(0.01),
                new FilterDuplicateExistingBelief()
                //param.getDefaultDerivationFilters().add(new BeRational());
        };
    }

    /** avoid calling this directly; use Default.simulationTime() which also sets the forgetting mode */
    public Default setClock(Clock clock) {
        this.clock = clock;
        return this;
    }

    public Default level(int maxNALlevel) {
        this.maxNALLevel = maxNALlevel;
        if (maxNALlevel < 8) {
            this.internalExperience = InternalExperience.InternalExperienceMode.None;
        }
        return this;
    }

    @Override
    public int getMaximumNALLevel() {
        return maxNALLevel;
    }

    @Override
    public ConceptBuilder getConceptBuilder() {
        return this;
    }

    /**
     * initialization after NAR is constructed
     */
    @Override
    public void init(NAR n) {

        n.setCyclesPerFrame(cyclesPerFrame);


        if (maxNALLevel >= 7) {
            n.on(PerceptionAccel.class);
            n.on(STMInduction.class);


            if (maxNALLevel >= 8) {

                for (OpReaction o : defaultOperators)
                    n.on(o);
                for (OpReaction o : exampleOperators)
                    n.on(o);


                //n.on(Anticipate.class);      // expect an event

                if (internalExperience == Minimal) {
                    n.on(InternalExperience.class, Abbreviation.class);
                } else if (internalExperience == Full) {
                    n.on(FullInternalExperience.class);
                    n.on(Counting.class);
                }
            }
        }

        //n.on(new RuntimeNARSettings());

    }

    @Override
    public Concept newConcept(final Term t, final Budget b, final Memory m) {

        Bag<Sentence, TaskLink> taskLinks =
                new CurveBag(rng, /*sentenceNodes,*/ getConceptTaskLinks());
        taskLinks.mergePlus();
        //new ChainBag(rng,  getConceptTaskLinks());

        Bag<TermLinkKey, TermLink> termLinks =
                new CurveBag(rng, /*termlinkKeyNodes,*/ getConceptTermLinks());
        //new ChainBag(rng, /*termlinkKeyNodes,*/ getConceptTermLinks());

        return newConcept(t, b, taskLinks, termLinks, m);
    }

    /**
     * rank function used for concept belief and goal tables
     */
    protected BeliefTable.RankBuilder newConceptBeliefGoalRanking() {
        return (c, b) ->
                BeliefTable.BeliefConfidenceOrOriginality;
        //new BeliefTable.BeliefConfidenceAndCurrentTime(c);

    }

    @Override
    public PremiseProcessor getPremiseProcessor(Param p) {

        return new PremiseProcessor(

                new LogicStage /* <ConceptProcess> */ []{

                        //A. concept fire tasklink derivation
                        new TransformTask(),
                        new Contraposition(),

                        //B. concept fire tasklink termlink (pre-filter)
                        new FilterEqualSubtermsAndSetPremiseBelief(),
                        new MatchTaskBelief(),

                        //C. concept fire tasklink termlink derivation ---------
                        new ForwardImplicationProceed(),

                        //temporalInduce(nal, task, taskSentence, memory);
                        //(new TemporalInductionChain()),
                        new TemporalInductionChain2(),

                        new PerceptionDetachment(),

                        new DeduceSecondaryVariableUnification(),
                        new DeduceConjunctionByQuestion(),

                        new TableDerivations()
                        //---------------------------------------------
                },

                getDerivationFilters()

        );
    }

    protected Concept newConcept(Term t, Budget b, Bag<Sentence, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, Memory mem) {
        return new DefaultConcept(t, b,
                taskLinks, termLinks,
                newConceptBeliefGoalRanking(),
                newPremiseGenerator(),
                mem
        );
    }

    /**
     * construct a new premise generator for a concept
     */
    public PremiseGenerator newPremiseGenerator() {
        int novelCycles = 1;
        return new HashTableNovelPremiseGenerator(termLinkMaxMatched, novelCycles);

//        return new BloomFilterNovelPremiseGenerator(termLinkMaxMatched, novelCycles /* cycle to clear after */,
//                novelCycles * conceptTaskTermProcessPerCycle.get(),
//                0.01f /* false positive probability */ );


    }

    public Bag<Term, Concept> newConceptBag() {
        CurveBag<Term, Concept> b = new CurveBag(rng, getActiveConcepts());
        b.mergeMax();
        return b;
    }

    @Override
    public CacheBag<Term, Concept> newIndex() {

        return new GuavaCacheBag();
        //return new TrieCacheBag();
    }

    @Override
    public CycleProcess newCycleProcess() {
        return new DefaultCycle(
                new ItemAccumulator(new ItemComparator.Plus()),
                newConceptBag(),
                newNovelTaskBag(),
                inputsMaxPerCycle, novelMaxPerCycle, conceptsFiredPerCycle
        );
    }

    public Bag<Sentence<Compound>, Task<Compound>> newNovelTaskBag() {
        return new CurveBag(rng, getNovelTaskBagSize());
        //return new ChainBag(rng, getNovelTaskBagSize());
    }

    public int getNovelTaskBagSize() {
        return taskBufferSize;
    }

    public Default setNovelTaskBagSize(int taskBufferSize) {
        this.taskBufferSize = taskBufferSize;
        return this;
    }

    public int getActiveConcepts() {
        return conceptBagSize;
    }

    public Default setActiveConcepts(int conceptBagSize) {
        this.conceptBagSize = conceptBagSize;
        return this;
    }

    public int getConceptTaskLinks() {
        return taskLinkBagSize;
    }

    public Default setTaskLinkBagSize(int taskLinkBagSize) {
        this.taskLinkBagSize = taskLinkBagSize;
        return this;
    }

    public int getConceptTermLinks() {
        return termLinkBagSize;
    }

    public Default setTermLinkBagSize(int termLinkBagSize) {
        this.termLinkBagSize = termLinkBagSize;
        return this;
    }

    public Default clock(Clock c) {
        setClock(c);
        return this;
    }

    public Default realTime() {
        return clock(new RealtimeMSClock(true));
    }

    public Default realTimeHard(int durationMS) {
        duration.set(durationMS);
        return clock(new HardRealtimeClock());
    }

    public NARSeed setCyclesPerFrame(int cyclesPerFrame) {
        this.cyclesPerFrame = cyclesPerFrame;
        return this;
    }

    @Override
    public Param newParam() {
        return this;
    }

    @Override
    public Random getRandom() {
        return rng;
    }

    public InternalExperience.InternalExperienceMode getInternalExperience() {
        return internalExperience;
    }

    public Default setInternalExperience(InternalExperience.InternalExperienceMode i) {
        if (i == null) i = InternalExperience.InternalExperienceMode.None;
        this.internalExperience = i;
        return this;
    }



    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + maxNALLevel +
                ((internalExperience == InternalExperience.InternalExperienceMode.None) || (internalExperience == null) ? "" : "+")
                + ']';
    }

    /**
     * The original deterministic memory cycle implementation that is currently used as a standard
     * for development and testing.
     */
    public static class DefaultCycle extends SequentialCycle {



        /** How many concepts to fire each cycle; measures degree of parallelism in each cycle */
        public final AtomicInteger conceptsFiredPerCycle;

        /** max # of inputs to perceive per cycle; -1 means unlimited (attempts to drains input to empty each cycle) */
        public final AtomicInteger inputsMaxPerCycle;

        /** max # of novel tasks to process per cycle; -1 means unlimited (attempts to drains input to empty each cycle) */
        public final AtomicInteger novelMaxPerCycle;



        /**
         * New tasks with novel composed terms, for delayed and selective processing
         */
        public final Bag<Sentence<Compound>, Task<Compound>> novelTasks;

        int numNovelTasksPerCycle = 1;

        /* ---------- Short-term workspace for a single cycle ------- */



        public DefaultCycle(ItemAccumulator<Task> newTasks, Bag<Term, Concept> concepts, Bag<Sentence<Compound>, Task<Compound>> novelTasks, AtomicInteger inputsMaxPerCycle, AtomicInteger novelMaxPerCycle, AtomicInteger conceptsFiredPerCycle) {
            super(concepts, newTasks);

            this.conceptsFiredPerCycle = conceptsFiredPerCycle;
            this.inputsMaxPerCycle = inputsMaxPerCycle;
            this.novelMaxPerCycle = novelMaxPerCycle;
            this.novelTasks = novelTasks;
        }


        @Override
        public void delete() {
            super.delete();
            novelTasks.delete();
        }

        @Override
        public void reset(Memory m) {
            super.reset(m);


            if (novelTasks!=null)
                novelTasks.clear();
        }




        /**
         * An atomic working cycle of the system:
         *  0) optionally process inputs
         *  1) optionally process new task(s)
         *  2) optionally process novel task(s)
         *  2) optionally fire a concept
         **/
        @Override
        public void cycle() {

            concepts.forgetNext(
                    memory.param.conceptForgetDurations,
                    Global.CONCEPT_FORGETTING_EXTRA_DEPTH,
                    memory);

            //inputs
            inputNextPerception(inputsMaxPerCycle.get());


            //all new tasks
            int numNewTasks = newTasks.size();
            if (numNewTasks > 0)
                runNewTasks();


            //1 novel tasks if numNewTasks empty
            if (newTasks.isEmpty() && !novelTasks.isEmpty())  {
                int nn = novelMaxPerCycle.get();
                if (nn < 0) nn = novelTasks.size(); //all
                if (nn > 0)
                    runNextNovelTasks(nn);
            }


            //1 concept if (memory.newTasks.isEmpty())*/
            final int conceptsToFire = newTasks.isEmpty() ? conceptsFiredPerCycle.get() : 0;
            if (conceptsToFire > 0) {

                final float conceptForgetDurations = memory.param.conceptForgetDurations.floatValue();

                ConceptProcess.forEachPremise(memory,
                        () ->  nextConceptToProcess(conceptForgetDurations),
                        conceptsToFire,
                        p->p.run()
                );

            }

            memory.runNextTasks();

        }



        /**
         * Select a novel task to process.
         */
        protected void runNextNovelTasks(int count) {

            queueNewTasks();

            for (int i = 0; i < count; i++) {

                final Task task = novelTasks.pop();
                if (task!=null)
                    TaskProcess.run(memory, task);
                else
                    break;
            }

            commitNewTasks();
        }

        private void runNewTasks() {

            queueNewTasks();

            for (int n = newTasks.size()-1;  n >= 0; n--) {
                Task highest = newTasks.removeHighest();
                if (highest == null) break;

                run(highest);
            }

            commitNewTasks();

        }



        /** returns whether the task was run */
        protected boolean run(Task task) {


            //memory.emotion.busy(task);

            if (task.isInput() || !(task.isJudgment())
                    || (memory.concept(task.getTerm()) != null)
                    ) {

                //it is a question/quest or a judgment for a concept which exists:
                return TaskProcess.run(memory, task)!=null;

            } else {
                //it is a judgment or goal which would create a new concept:



                //if (s.isJudgment() || s.isGoal()) {

                final double exp = task.getExpectation();
                if (exp > memory.param.conceptCreationExpectation.floatValue()) {//Global.DEFAULT_CREATION_EXPECTATION) {

                    // new concept formation
                    Task overflow = novelTasks.put(task);

                    if (overflow != null) {
                        if (overflow == task) {
                            memory.removed(task, "Ignored");
                            return false;
                        } else {
                            memory.removed(overflow, "Displaced novel task");
                        }
                    }

                    memory.logic.TASK_ADD_NOVEL.hit();
                    return true;

                } else {
                    memory.removed(task, "Neglected");
                }
                //}
            }
            return false;
        }





    }

    @Deprecated
    public static class CommandLineNARBuilder extends Default {

        List<String> filesToLoad = new ArrayList();

        public CommandLineNARBuilder(String[] args) {
            super();

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--silence".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);
                    outputVolume.set(100 - sl);
                } else if ("--noise".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);
                    outputVolume.set(sl);
                } else {
                    filesToLoad.add(arg);
                }

            }
        }

        /**
         * Decode the silence level
         *
         * @param param Given argument
         * @return Whether the argument is not the silence level
         */
        public static boolean isReallyFile(String param) {
            return !"--silence".equals(param);
        }

        @Override
        public void init(NAR n) {
            super.init(n);

            for (String x : filesToLoad) {
                n.memory.taskNext(() -> {
                    try {
                        n.input(new File(x));
                    } catch (FileNotFoundException fex) {
                        System.err.println(getClass() + ": " + fex.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                //n.run(1);
            }

        }
    }


}
