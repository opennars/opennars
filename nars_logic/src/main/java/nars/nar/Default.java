package nars.nar;

import com.google.common.util.concurrent.AtomicDouble;
import nars.Global;
import nars.LocalMemory;
import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.bag.impl.CurveBag;
import nars.budget.Budget;
import nars.budget.ItemAccumulator;
import nars.clock.CycleClock;
import nars.concept.AtomConcept;
import nars.concept.Concept;
import nars.concept.ConceptActivator;
import nars.concept.DefaultConcept;
import nars.event.CycleReaction;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.nal.SimpleDeriver;
import nars.nal.nal8.OperatorReaction;
import nars.nal.nal8.operator.NullOperator;
import nars.op.app.STMTemporalLinkage;
import nars.op.data.Flat;
import nars.op.data.json;
import nars.op.data.similaritree;
import nars.op.io.echo;
import nars.op.io.reset;
import nars.op.io.say;
import nars.op.io.schizo;
import nars.op.math.add;
import nars.op.math.count;
import nars.op.mental.*;
import nars.op.meta.complexity;
import nars.op.meta.reflect;
import nars.op.software.js;
import nars.op.software.scheme.scheme;
import nars.process.ConceptProcess;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.term.Atom;
import nars.term.Term;
import nars.util.data.MutableInteger;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.event.On;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Default set of NAR parameters which have been classically used for development.
 * <p/>
 * WARNING this Seed is not immutable yet because it extends Param,
 * which is supposed to be per-instance/mutable. So do not attempt
 * to create multiple NAR with the same Default seed model
 */
public class Default extends NAR {

    public static final OperatorReaction[] exampleOperators = new OperatorReaction[]{
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

    public final OperatorReaction[] defaultOperators = new OperatorReaction[]{

            //system control
            new echo(),
            //PauseInput.the,
            new reset(),
            //new eval(),
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
    };
    public final DefaultCycle core;
    public int cyclesPerFrame = 1;

    /**
     * Size of TaskLinkBag
     */
    int taskLinkBagSize;
    /**
     * Size of TermLinkBag
     */
    int termLinkBagSize;


    /**
     * Default DEFAULTS
     */
    public Default() {
        this(768, 1, 2, 2);
    }

    public Default(int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {
        this(new LocalMemory(new CycleClock()), activeConcepts, conceptsFirePerCycle, termLinksPerCycle, taskLinksPerCycle);
    }

    public Default(Memory m, int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {
        super(m);

        //termLinkMaxMatched.set(5);


        //Build Parameters
//        this.internalExperience = InternalExperience.InternalExperienceMode.None; //much too early, this is nonsensical without working NAL

        setTaskLinkBagSize(8);
        setTermLinkBagSize(16);


        m.duration.set(5);
        m.shortTermMemoryHistory.set(5);
        m.conceptActivationFactor.set(1.0);
        m.conceptFireThreshold.set(0.0);

        m.conceptForgetDurations.set(1.0);
        m.taskLinkForgetDurations.set(2.0);
        m.termLinkForgetDurations.set(3.0);

        m.conceptBeliefsMax.set(11);
        m.conceptGoalsMax.set(5);
        m.conceptQuestionsMax.set(1);
        m.activeConceptThreshold.set(0.0);
        m.questionFromGoalThreshold.set(0.35);
        m.taskProcessThreshold.set(Global.BUDGET_EPSILON * 2);
        m.termLinkThreshold.set(0); //Global.BUDGET_EPSILON);
        m.taskLinkThreshold.set(0); //Global.BUDGET_EPSILON);
        m.executionThreshold.set(0.5);

        //m.reliance.set(Global.DEFAULT_JUDGMENT_CONFIDENCE);
        m.conceptCreationExpectation.set(0);//.66);

        setCyclesPerFrame(cyclesPerFrame);

        //core loop
        {
            DefaultCycle c = this.core = new DefaultCycle(
                    m.the("defaultCore", this),
                    m.the("logic", getDeriver()),
                    new ConceptBagActivator(this),
                    m.the("inputBuffer", new ItemAccumulator(Budget.max)),
                    newConceptBag()
            );
            m.the("core", c);

            c.termlinksSelectedPerFiredConcept.set(termLinksPerCycle);
            c.tasklinksSelectedPerFiredConcept.set(taskLinksPerCycle);
            c.inputsMaxPerCycle.set(conceptsFirePerCycle);
            c.conceptsFiredPerCycle.set(conceptsFirePerCycle);

            c.capacity.set(activeConcepts);

        }

        if (nal() >= 7) {

            //scope: control
            m.the(new STMTemporalLinkage(this, core.deriver));

            if (nal() >= 8) {

                for (OperatorReaction o : defaultOperators)
                    on(o);
                /*for (OperatorReaction o : exampleOperators)
                    on(o);*/

                //n.on(Anticipate.class);      // expect an event

                new FullInternalExperience(this);
                new Abbreviation(this);
                on(Counting.class);

//                /*if (internalExperience == Minimal) {
//                    new InternalExperience(this);
//                    new Abbreviation(this);
//                } else if (internalExperience == Full)*/ {
//                    on(FullInternalExperience.class);
//                    on(Counting.class);
//                }
            }
        }
        //n.on(new RuntimeNARSettings());

    }


//    static String readFile(String path, Charset encoding)
//            throws IOException {
//        byte[] encoded = Files.readAllBytes(Paths.get(path));
//        return new String(encoded, encoding);
//    }

//    protected DerivationFilter[] getDerivationFilters() {
//        return new DerivationFilter[]{
//                new FilterBelowConfidence(0.01),
//                new FilterDuplicateExistingBelief()
//                //param.getDefaultDerivationFilters().add(new BeRational());
//        };
//    }

    public Default nal(int maxNALlevel) {
        memory.nal(maxNALlevel);
        return this;
    }

    public Concept newConcept(final Term t, final Budget b) {

        Bag<Sentence, TaskLink> taskLinks =
                new CurveBag<>(rng, /*sentenceNodes,*/ getConceptTaskLinks());
        taskLinks.mergePlus();

        Bag<TermLinkKey, TermLink> termLinks =
                new CurveBag<>(rng, /*termlinkKeyNodes,*/ getConceptTermLinks());
        termLinks.mergePlus();

        return newConcept(t, b, taskLinks, termLinks, memory());
    }

    public class ConceptAttentionEnhancer {

        /**
         * called by concept before it fires to update any pending changes
         */
        public void updateLinks(Concept c) {


            final Memory memory = memory();

            if (Global.TERMLINK_FORGETTING_EXTRA_DEPTH > 0)
                c.getTermLinks().forgetNext(
                        memory.termLinkForgetDurations,
                        Global.TERMLINK_FORGETTING_EXTRA_DEPTH,
                        memory);


            if (Global.TASKLINK_FORGETTING_EXTRA_DEPTH > 0)
                c.getTaskLinks().forgetNext(
                        memory.taskLinkForgetDurations,
                        Global.TASKLINK_FORGETTING_EXTRA_DEPTH,
                        memory);


            //linkTerms(null, true);

        }
    }

//    /**
//     * rank function used for concept belief and goal tables
//     */
//    public BeliefTable.RankBuilder newConceptBeliefGoalRanking() {
//        return (c, b) ->
//                BeliefTable.BeliefConfidenceOrOriginality;
//        //new BeliefTable.BeliefConfidenceAndCurrentTime(c);
//
//    }

    public Concept newConcept(Term t, Budget b, Bag<Sentence, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, Memory m) {

        if (t instanceof Atom) {
            return new AtomConcept(t, b, termLinks, taskLinks, m
            );
        } else {
            return new DefaultConcept(t, b,
                    taskLinks, termLinks,
                    m
            );
        }

    }

    @Override
    protected final Concept doConceptualize(Term term, Budget b) {
        return core.update(term, b, true, 1f, core.active);
    }


    public Bag<Term, Concept> newConceptBag() {
        CurveBag<Term, Concept> b = new CurveBag(rng, 1);
        b.mergePlus();
        return b;
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + nal() + +']';
    }

    protected SimpleDeriver getDeriver() {
        return SimpleDeriver.standardDeriver;
    }

    protected boolean process(Task t) {
        return true;
    }

    /**
     * The original deterministic memory cycle implementation that is currently used as a standard
     * for development and testing.
     */
    public static class DefaultCycle extends CycleReaction implements Serializable, Function<ConceptProcess, Stream<Task>> {

        public final Deque<Task> percepts = new ArrayDeque();

        /**
         * How many concepts to fire each cycle; measures degree of parallelism in each cycle
         */
        public final AtomicInteger conceptsFiredPerCycle;


        /**
         * max # of inputs to perceive per cycle; -1 means unlimited (attempts to drains input to empty each cycle)
         */
        public final AtomicInteger inputsMaxPerCycle;

        public final SimpleDeriver deriver;


        public final MutableInteger tasklinksSelectedPerFiredConcept = new MutableInteger(1);
        public final MutableInteger termlinksSelectedPerFiredConcept = new MutableInteger(1);

//        final Function<Task, Task> derivationPostProcess = d -> {
//            return LimitDerivationPriority.limitDerivation(d);
//        };


        /**
         * samples an active concept
         */
        public Concept next() {
            return active.peekNext();
        }

        /**
         * New tasks with novel composed terms, for delayed and selective processing
         */
        public final ItemAccumulator<Task> newTasks;

        /**
         * concepts active in this cycle
         */
        public final Bag<Term, Concept> active;

        public final On onInput;

        @Deprecated
        transient public final NAR nar;

        public final MutableInteger capacity = new MutableInteger();

        public final ConceptBagActivator ca;

        public final AtomicDouble conceptForget;

//        @Deprecated
//        int tasklinks = 2; //TODO use MutableInteger for this
//        @Deprecated
//        int termlinks = 3; //TODO use MutableInteger for this

        /* ---------- Short-term workspace for a single cycle ------- */

        public DefaultCycle(NAR nar, SimpleDeriver deriver, ConceptBagActivator ca, ItemAccumulator<Task> newTasks, Bag<Term, Concept> concepts) {
            super(nar);

            nar.memory.eventReset.on((m) -> {
                reset();
            });

            this.nar = nar;
            this.ca = ca;

            this.deriver = deriver;


            this.conceptForget = nar.memory().conceptForgetDurations;

            this.newTasks = newTasks;
            this.inputsMaxPerCycle = new AtomicInteger(1);
            this.conceptsFiredPerCycle = new AtomicInteger(1);
            this.active = concepts;


            onInput = nar.memory().eventInput.on(new InputConsumer(newTasks, percepts));
        }

        public void reset() {

            percepts.clear();

            newTasks.clear();

        }

        /**
         * An atomic working cycle of the system:
         * 0) optionally process inputs
         * 1) optionally process new task(s)
         * 2) optionally process novel task(s)
         * 2) optionally fire a concept
         **/
        @Override
        public void onCycle() {
            enhanceAttention();
            runInputTasks(inputsMaxPerCycle.get());
            runNewTasks(/*inputsMaxPerCycle.get()*/);
            fireConcepts(conceptsFiredPerCycle.get());
        }

        protected void fireConcepts(int max) {

            active.setCapacity(capacity.intValue());

            //1 concept if (memory.newTasks.isEmpty())*/
            final int conceptsToFire = newTasks.isEmpty() ? max : 0;
            if (conceptsToFire == 0) return;

            final float conceptForgetDurations = nar.memory().conceptForgetDurations.floatValue();

            //final float tasklinkForgetDurations = nar.memory().taskLinkForgetDurations.floatValue();

            //final int termLinkSelections = termLinksPerConcept.getValue();

//            Concept[] buffer = new Concept[conceptsToFire];
//            int n = active.forgetNext(conceptForgetDurations, buffer, time());
//            if (n == 0) return;

            final long now = nar.time();

            for (int i = 0; i < conceptsToFire; i++) {
                //active.forgetNext(conceptForgetDurations, nar.memory(), 1)
                Concept[] buffer = new Concept[]{active.forgetNext(conceptForgetDurations, nar.memory())};

                for (final Concept c : buffer) {
                    if (c == null) break;
                    fireConcept(conceptForgetDurations, now, c);
                }
            }
        }


        private void fireConcept(float conceptForgetDurations, long now, Concept c) {

            /*ConceptProcess.nextPremise(nar, c,
                    conceptForgetDurations,
                    conceptProcessor, now );*/

            nar.input(ConceptProcess.nextPremiseSquare(nar, c,
                    conceptForgetDurations,
                    this,
                    termlinksSelectedPerFiredConcept.intValue(), tasklinksSelectedPerFiredConcept.intValue()));
        }


        //TODO move this to separate enhance plugin
        protected void enhanceAttention() {
            active.forgetNext(
                    conceptForget,
                    Global.CONCEPT_FORGETTING_EXTRA_DEPTH,
                    nar.memory());
        }

        protected void runNewTasks() {
            runNewTasks(newTasks.size()); //all
        }

        protected void runNewTasks(int max) {

            int numNewTasks = Math.min(max, newTasks.size());
            if (numNewTasks == 0) return;

            //queueNewTasks();

            for (int n = newTasks.size() - 1; n >= 0; n--) {
                Task highest = newTasks.removeHighest();
                if (highest == null) break;

                run(highest);
            }
            //commitNewTasks();
        }

        protected void runInputTasks(int max) {

            int m = Math.min(percepts.size(),
                    max);

            for (int n = m; n > 0; n--) {
                run(percepts.removeFirst());
            }
        }

        protected boolean run(Task task) {
            return TaskProcess.run(nar, task) != null;
        }

        public final long time() {
            return nar.time();
        }

        public Concept update(Term term, Budget b, boolean b1, float v, Bag<Term, Concept> active) {
            active.setCapacity(capacity.intValue());
            return ca.update(term.getTerm(), b, true, time(), 1f, active);
        }

        public Iterable<?> concepts() {
            return active;
        }

        @Override
        public Stream<Task> apply(ConceptProcess premise) {

            //used to estimate the fraction this batch should be scaled but this is not accurate
            //final int numPremises = termlinks*tasklinks;

            return Task.normalize(
                    premise.derive(deriver).collect(Collectors.toList()),
                    premise.getMeanSummary() /*/numPremises*/
            ).stream();

            //OPTION 1: re-input to input buffers
            //t.input(nar, deriver, derivationPostProcess);

            //OPTION 2: immediate process
                /*t.apply(deriver).forEach(r -> {
                    run(r);
                });*/

        }


        //try to implement some other way, this is here because of serializability
        @Deprecated
        static class InputConsumer implements Consumer<Task>, Serializable {
            public final ItemAccumulator<Task> newTasks;
            public final Deque<Task> percepts;

            public InputConsumer(ItemAccumulator<Task> newTasks, Deque<Task> percepts) {
                this.newTasks = newTasks;
                this.percepts = percepts;
            }

            @Override
            public void accept(Task t) {
                if (t.isInput())
                    percepts.add(t);
                else {
                    if (t.getParentTask() != null && t.getParentTask().getTerm().equals(t.getTerm())) {
                    } else {
                        newTasks.add(t);
                    }
                }
            }
        }
    }

//    @Deprecated
//    public static class CommandLineNARBuilder extends Default {
//
//        List<String> filesToLoad = new ArrayList();
//
//        public CommandLineNARBuilder(String[] args) {
//            super();
//
//            for (int i = 0; i < args.length; i++) {
//                String arg = args[i];
//                if ("--silence".equals(arg)) {
//                    arg = args[++i];
//                    int sl = Integer.parseInt(arg);
//                    //outputVolume.set(100 - sl);
//                } else if ("--noise".equals(arg)) {
//                    arg = args[++i];
//                    int sl = Integer.parseInt(arg);
//                    //outputVolume.set(sl);
//                } else {
//                    filesToLoad.add(arg);
//                }
//            }
//
//            for (String x : filesToLoad) {
//                taskNext(() -> {
//                    try {
//                        input(new File(x));
//                    } catch (FileNotFoundException fex) {
//                        System.err.println(getClass() + ": " + fex.toString());
//                    } catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                });
//
//                //n.run(1);
//            }
//        }
//
//        /**
//         * Decode the silence level
//         *
//         * @param param Given argument
//         * @return Whether the argument is not the silence level
//         */
//        public static boolean isReallyFile(String param) {
//            return !"--silence".equals(param);
//        }
//    }

    public static class ConceptBagActivator extends ConceptActivator implements Serializable {


        public ConceptBagActivator(NAR n) {
            super(n);
        }

        @Override
        @Deprecated
        public Concept newConcept(Term t, Budget b, @Deprecated Memory m) {
            return ((Default) nar).newConcept(t, b);
        }

        @Override
        public Concept newItem() {
            //default behavior overriden; a new item will be maanually inserted into the bag under certain conditons to be determined by this class
            Concept c = ((Default) nar).newConcept(getKey(), getBudget());
            nar.memory().put(c);

            return c;
        }

        @Override
        protected void on(Concept c) {

        }

        @Override
        protected void off(Concept c) {

        }

//        public Concept update(Bag<Term,Concept> bag) {
//            return bag.update(this);
//        }
    }
}
