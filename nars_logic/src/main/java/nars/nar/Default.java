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
import nars.concept.*;
import nars.event.CycleReaction;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.nal.SimpleDeriver;
import nars.nal.nal8.OpReaction;
import nars.nal.nal8.operator.NullOperator;
import nars.nal.nal8.operator.eval;
import nars.op.app.STMEventInference;
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
import nars.premise.HashTableNovelPremiseGenerator;
import nars.premise.PremiseGenerator;
import nars.process.ConceptProcess;
import nars.process.TaskProcess;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.filter.DerivationFilter;
import nars.task.filter.FilterBelowConfidence;
import nars.task.filter.FilterDuplicateExistingBelief;
import nars.term.Atom;
import nars.term.Term;
import nars.util.data.MutableInteger;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.event.DefaultTopic;
import org.apache.commons.lang3.mutable.MutableInt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static nars.op.mental.InternalExperience.InternalExperienceMode.Full;
import static nars.op.mental.InternalExperience.InternalExperienceMode.Minimal;


/**
 * Default set of NAR parameters which have been classically used for development.
 * <p>
 * WARNING this Seed is not immutable yet because it extends Param,
 * which is supposed to be per-instance/mutable. So do not attempt
 * to create multiple NAR with the same Default seed model
 */
public class Default extends NAR {

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

    public final OpReaction[] defaultOperators = new OpReaction[]{

            //system control
            echo.the,
            //PauseInput.the,
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

    InternalExperience.InternalExperienceMode internalExperience;

    /**
     * Default DEFAULTS
     */
    public Default() {
        this(1024, 1, 3);
    }

    public Default(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {
        this(new LocalMemory(new CycleClock()), maxConcepts, conceptsFirePerCycle, termLinksPerCycle);
    }

    public Default(Memory m, int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {
        super(m);

        //termLinkMaxMatched.set(5);

        //Build Parameters
        this.maxNALLevel = Global.DEFAULT_NAL_LEVEL;
        this.internalExperience =
                maxNALLevel >= 8 ? InternalExperience.InternalExperienceMode.Minimal : InternalExperience.InternalExperienceMode.None;

        setTaskLinkBagSize(32);

        setTermLinkBagSize(64);

        //Runtime Initial Values

        m.duration.set(5);
        m.shortTermMemoryHistory.set(1);
        m.temporalRelationsMax.set(4);
        m.conceptActivationFactor.set(1.0);
        m.conceptFireThreshold.set(0.0);
        m.conceptForgetDurations.set(3.0);
        m.taskLinkForgetDurations.set(4.0);
        m.termLinkForgetDurations.set(10.0);
        //param.budgetThreshold.set(0.01f);
        m.conceptBeliefsMax.set(11);
        m.conceptGoalsMax.set(8);
        m.conceptQuestionsMax.set(4);
        m.activeConceptThreshold.set(0.0);
        m.questionFromGoalThreshold.set(0.35);
        m.taskProcessThreshold.set(Global.BUDGET_EPSILON);
        m.termLinkThreshold.set(Global.BUDGET_EPSILON);
        m.taskLinkThreshold.set(Global.BUDGET_EPSILON);
        m.executionThreshold.set(0.6);
        //executionThreshold.set(0.60);
        m.reliance.set(Global.DEFAULT_JUDGMENT_CONFIDENCE);
        m.conceptCreationExpectation.set(0.66);

        setCyclesPerFrame(cyclesPerFrame);

        //core loop
        {
            DefaultCycle c = this.core = new DefaultCycle(
                    this,
                    new ConceptBagActivator(this),
                    new ItemAccumulator(Budget.max),
                    newConceptBag()
            );
            m.the("core", c);
            //run this on the end half cycle
            memory.eventCycleEnd.on(c);
            c.capacity.set(maxConcepts);
            c.inputsMaxPerCycle.set(conceptsFirePerCycle);
            c.conceptsFiredPerCycle.set(conceptsFirePerCycle);
            c.termLinksPerConcept.setValue(termLinksPerCycle);
        }

        if (maxNALLevel >= 7) {

            //scope: control
            m.the(new PerceptionAccel(this, () -> core.next()));
            new STMEventInference(this, core.deriver );

            if (maxNALLevel >= 8) {

                for (OpReaction o : defaultOperators)
                    on(o);
                for (OpReaction o : exampleOperators)
                    on(o);

                //n.on(Anticipate.class);      // expect an event

                if (internalExperience == Minimal) {
                    new InternalExperience(this);
                    new Abbreviation(this);
                } else if (internalExperience == Full) {
                    on(FullInternalExperience.class);
                    on(Counting.class);
                }
            }
        }
        //n.on(new RuntimeNARSettings());

    }

    public DefaultCycle getCycleProcess() {
        return core;
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

    public Default nal(int maxNALlevel) {
        this.maxNALLevel = maxNALlevel;
        if (maxNALlevel < 8) {
            this.internalExperience = InternalExperience.InternalExperienceMode.None;
        }
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

    /**
     * rank function used for concept belief and goal tables
     */
    public BeliefTable.RankBuilder newConceptBeliefGoalRanking() {
        return (c, b) ->
                BeliefTable.BeliefConfidenceOrOriginality;
        //new BeliefTable.BeliefConfidenceAndCurrentTime(c);

    }

    public Concept newConcept(Term t, Budget b, Bag<Sentence, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, Memory m) {

        if (t instanceof Atom) {
            return new AtomConcept(t, b, termLinks, taskLinks, newPremiseGenerator(), m
            );
        }
        else {
            return new DefaultConcept(t, b,
                    taskLinks, termLinks,
                    newPremiseGenerator(), newConceptBeliefGoalRanking(),
                    m
            );
        }

    }

    @Override
    protected final Concept doConceptualize(Term term, Budget b) {
        return core.update(term.getTerm(), b, true, 1f, core.active);
    }

    /**
     * construct a new premise generator for a concept
     */
    public PremiseGenerator newPremiseGenerator() {
        int novelCycles = 1;
        return new HashTableNovelPremiseGenerator(memory().termLinkMaxMatched, novelCycles);
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
        return getClass().getSimpleName() + '[' + maxNALLevel +
                ((internalExperience == InternalExperience.InternalExperienceMode.None) || (internalExperience == null) ? "" : "+")
                + ']';
    }

    protected boolean process(Task t) {
        return true;
    }

    /**
     * The original deterministic memory cycle implementation that is currently used as a standard
     * for development and testing.
     */
    public static class DefaultCycle extends CycleReaction  /*extends SequentialCycle*/ {

        final Deque<Task> percepts = new ArrayDeque();

        /**
         * How many concepts to fire each cycle; measures degree of parallelism in each cycle
         */
        public final AtomicInteger conceptsFiredPerCycle;

        public final MutableInt termLinksPerConcept = new MutableInt();

        /**
         * max # of inputs to perceive per cycle; -1 means unlimited (attempts to drains input to empty each cycle)
         */
        public final AtomicInteger inputsMaxPerCycle;
        private SimpleDeriver deriver = new SimpleDeriver(SimpleDeriver.standard);


            /** samples an active concept */
        public Concept next() {
            return active.peekNext();
        }

        /**
         * New tasks with novel composed terms, for delayed and selective processing
         */
        private final ItemAccumulator<Task> newTasks;

        /** concepts active in this cycle */
        private final Bag<Term, Concept> active;

        private final DefaultTopic.On onInput;

        private final NAR nar;

        public final MutableInteger capacity = new MutableInteger();

        private final ConceptBagActivator ca;

        private final AtomicDouble conceptForget;


        /* ---------- Short-term workspace for a single cycle ------- */

        public DefaultCycle(NAR nar, ConceptBagActivator ca, ItemAccumulator<Task> newTasks, Bag<Term, Concept> concepts) {
            super(nar);

            nar.memory.eventReset.on((m) -> {
                reset();
            });

            this.nar = nar;
            this.ca = ca;


            this.conceptForget = nar.memory().conceptForgetDurations;

            this.newTasks = newTasks;
            this.inputsMaxPerCycle = new AtomicInteger(1);
            this.conceptsFiredPerCycle = new AtomicInteger(1);
            this.active = concepts;

            onInput = nar.memory().eventInput.on(t-> {
                if (t.isInput())
                    percepts.add(t);
                else
                    newTasks.add(t);
            });
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
        @Override public void onCycle() {
            enhanceAttention();
            runInputTasks(inputsMaxPerCycle.get());
            runNewTasks(inputsMaxPerCycle.get());
            fireConcepts(conceptsFiredPerCycle.get());
        }

        protected void fireConcepts(int max) {

            active.setCapacity(capacity.intValue());

            //1 concept if (memory.newTasks.isEmpty())*/
            final int conceptsToFire = newTasks.isEmpty() ? max : 0;
            if (conceptsToFire == 0) return;

            final float conceptForgetDurations = nar.memory().conceptForgetDurations.floatValue();

            //final float tasklinkForgetDurations = nar.memory().taskLinkForgetDurations.floatValue();

            final int termLinkSelections = termLinksPerConcept.getValue();

//            Concept[] buffer = new Concept[conceptsToFire];
//            int n = active.forgetNext(conceptForgetDurations, buffer, time());
//            if (n == 0) return;

            final long now = nar.time();

            Concept[] buffer = new Concept[] { active.forgetNext(conceptForgetDurations, nar.memory()) };

            for (final Concept c : buffer) {
                if (c == null) break;
                ConceptProcess.forEachPremise(nar, c, termLinkSelections, conceptForgetDurations, (t) -> {
                    t.input(nar, deriver);
                }, now );
            }
        }

        protected void enhanceAttention() {
            active.forgetNext(
                    conceptForget,
                    Global.CONCEPT_FORGETTING_EXTRA_DEPTH,
                    nar.memory());
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

        public final long time() { return nar.time(); }

        public Concept update(Term term, Budget b, boolean b1, float v, Bag<Term, Concept> active) {
            active.setCapacity(capacity.intValue());
            return ca.update(term.getTerm(), b, true, time(), 1f, active);
        }

        public Iterable<?> concepts() {
            return active;
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
                    //outputVolume.set(100 - sl);
                } else if ("--noise".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);
                    //outputVolume.set(sl);
                } else {
                    filesToLoad.add(arg);
                }
            }

            for (String x : filesToLoad) {
                taskNext(() -> {
                    try {
                        input(new File(x));
                    } catch (FileNotFoundException fex) {
                        System.err.println(getClass() + ": " + fex.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

                //n.run(1);
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
    }

    private class ConceptBagActivator extends ConceptActivator {


        public ConceptBagActivator(NAR n) {
            super(n);
        }

        @Override
        @Deprecated public Concept newConcept(Term t, Budget b, @Deprecated Memory m) {
            return Default.this.newConcept(t, b);
        }

        @Override public Concept newItem() {
            //default behavior overriden; a new item will be maanually inserted into the bag under certain conditons to be determined by this class
            Concept c = Default.this.newConcept(getKey(),getBudget());
            memory().put(c);

            return c;
        }

        @Override
        protected void on(Concept c) {

        }

        @Override
        protected void off(Concept c) {

        }

        public Concept update(Bag<Term,Concept> bag) {
            return bag.update(this);
        }
    }
}
