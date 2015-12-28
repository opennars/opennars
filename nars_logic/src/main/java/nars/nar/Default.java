package nars.nar;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.Premise;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.CurveBag;
import nars.budget.Budget;
import nars.concept.AtomConcept;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.concept.util.ConceptActivator;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.nal.Deriver;
import nars.nal.nal8.OperatorReaction;
import nars.nal.nal8.operator.NullOperator;
import nars.op.data.Flat;
import nars.op.data.json;
import nars.op.data.similaritree;
import nars.op.io.echo;
import nars.op.io.reset;
import nars.op.io.say;
import nars.op.io.schizo;
import nars.op.math.add;
import nars.op.math.length;
import nars.op.mental.*;
import nars.op.meta.complexity;
import nars.op.meta.reflect;
import nars.op.software.js;
import nars.op.software.scheme.scheme;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.task.flow.ImmediateTaskPerception;
import nars.task.flow.TaskPerception;
import nars.term.Atom;
import nars.term.Term;
import nars.time.Clock;
import nars.time.FrameClock;
import nars.util.data.MutableInteger;
import nars.util.data.random.XorShift1024StarRandom;
import nars.util.event.Active;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.io.Serializable;
import java.util.Random;
import java.util.function.Consumer;


/**
 * Default set of NAR parameters which have been classically used for development.
 * <p>
 * WARNING this Seed is not immutable yet because it extends Param,
 * which is supposed to be per-instance/mutable. So do not attempt
 * to create multiple NAR with the same Default seed model
 */
public class Default extends NAR {

    public final DefaultCycle core;
    public final TaskPerception input;

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
        this(1024, 1, 2, 3, new FrameClock());
    }

    public Default(int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle, Clock clock) {
        this(new Memory(clock, CacheBag.memory(activeConcepts)), activeConcepts, conceptsFirePerCycle, termLinksPerCycle, taskLinksPerCycle);
    }

    public Default(Memory memory, int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {
        super(memory);

        getDeriver().load(memory);

        rng = new XorShift1024StarRandom(1);

        initDefaults(memory);

        the("input", input = initInput());

        the("core", core = initCore(
                activeConcepts,
                conceptsFirePerCycle,
                termLinksPerCycle, taskLinksPerCycle
        ));

        if (core!=null) {
            beforeNextFrame(() -> {
                initTime();
            });
        }

    }

    public void initTime() {
        if (nal() >= 7) {

            //NAL7 plugins
            memory.the(new STMTemporalLinkage(this, core.deriver));
            memory.the(new Anticipate(this));

            if(nal() >=8) {
                for (OperatorReaction o : exampleOperators)
                    onExec(o);
            }

            if (nal() > 8) {
                initNAL9();
            }
        }
    }

    protected void initNAL9() {
        //NAL8 plugins

        for (OperatorReaction o : defaultOperators)
            onExec(o);

        new FullInternalExperience(this);
        new Abbreviation(this);
        onExec(Counting.class);

//                /*if (internalExperience == Minimal) {
//                    new InternalExperience(this);
//                    new Abbreviation(this);
//                } else if (internalExperience == Full)*/ {
//                    on(FullInternalExperience.class);
//                    on(Counting.class);
//                }
    }

    public TaskPerception initInput() {
        ImmediateTaskPerception input = new ImmediateTaskPerception(false, this,
            task -> true /* allow everything */,
            task -> process(task)
        );
        return input;
    }

    protected DefaultCycle initCore(int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {

        DefaultCycle c = initCore(
            activeConcepts,
            getDeriver(),
            newConceptBag(activeConcepts),
            new ConceptActivator(this, this)
        );

        //TODO move these to a PremiseGenerator which supplies
        // batches of Premises
        c.termlinksSelectedPerFiredConcept.set(termLinksPerCycle);
        c.tasklinksSelectedPerFiredConcept.set(taskLinksPerCycle);

        //tmpConceptsFiredPerCycle[0] = c.conceptsFiredPerCycle;
        c.conceptsFiredPerCycle.set(conceptsFirePerCycle);

        c.capacity.set(activeConcepts);

        return c;
    }

    protected DefaultCycle initCore(int activeConcepts, Deriver deriver, Bag<Term, Concept> conceptBag, ConceptActivator activator) {
        return new DefaultCycle(this, deriver, conceptBag, activator);
    }

    public void initDefaults(Memory m) {
        //parameter defaults

        setTaskLinkBagSize(12);
        setTermLinkBagSize(16);


        m.duration.set(5);

        m.conceptBeliefsMax.set(12);
        m.conceptGoalsMax.set(7);
        m.conceptQuestionsMax.set(5);

        m.conceptForgetDurations.setValue(2.0);
        m.taskLinkForgetDurations.setValue(2.0);
        m.termLinkForgetDurations.setValue(3.0);


        m.derivationThreshold.set(0);


        m.taskProcessThreshold.set(0); //warning: if this is not zero, it could remove un-TaskProcess-able tasks even if they are stored by a Concept

        //budget propagation thresholds
        m.termLinkThreshold.set(Global.BUDGET_EPSILON);
        m.taskLinkThreshold.set(Global.BUDGET_EPSILON);

        m.executionExpectationThreshold.set(0.5);

        m.shortTermMemoryHistory.set(4);
    }


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
    public final Random rng;

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
            new length(),
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

    /** ConceptBuilder: */
    public Concept apply(final Term t) {

        Bag<Task, TaskLink> taskLinks =
                new CurveBag<>(taskLinkBagSize, rng).mergePlus();

        Bag<TermLinkKey, TermLink> termLinks =
                new CurveBag<>(termLinkBagSize, rng).mergePlus();

        if (t instanceof Atom) {
            return new AtomConcept(t, termLinks, taskLinks);
        } else {
            return new DefaultConcept(t, taskLinks, termLinks, memory);
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

    @Override
    protected Concept doConceptualize(Term term, Budget b) {
        return core.activate(term, b);
    }


    public Bag<Term, Concept> newConceptBag(int initialCapacity) {
        return new CurveBag<>(initialCapacity, rng).mergePlus();
    }

    public Default setTaskLinkBagSize(int taskLinkBagSize) {
        this.taskLinkBagSize = taskLinkBagSize;
        return this;
    }

    public Default setTermLinkBagSize(int termLinkBagSize) {
        this.termLinkBagSize = termLinkBagSize;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + nal() + ']';
    }

    protected Deriver getDeriver() {
        return Deriver.standardDeriver;
    }

    public NAR forEachConcept(Consumer<Concept> recip) {
        this.core.active.forEach(recip);
        return this;
    }


    /**
     * The original deterministic memory cycle implementation that is currently used as a standard
     * for development and testing.
     */
    public static class DefaultCycle implements Serializable {

        final Active handlers = new Active();

        /**
         * How many concepts to fire each cycle; measures degree of parallelism in each cycle
         */
        public final MutableInteger conceptsFiredPerCycle;


        public final Deriver  deriver;


        public final MutableInteger tasklinksSelectedPerFiredConcept = new MutableInteger(1);
        public final MutableInteger termlinksSelectedPerFiredConcept = new MutableInteger(1);

        public final MutableFloat activationFactor = new MutableFloat(1f);

//        final Function<Task, Task> derivationPostProcess = d -> {
//            return LimitDerivationPriority.limitDerivation(d);
//        };



        /**
         * concepts active in this cycle
         */
        public final Bag<Term, Concept> active;


        @Deprecated
        transient public final NAR nar;

        public final MutableInteger capacity = new MutableInteger();

        public final ConceptActivator conceptActivator;

        public final MutableFloat conceptForget;


        /** temporary re-usable array for batch firing */
        private TermLink[] firingTermLinks = null;

        /** temporary re-usable array for batch firing */
        private TaskLink[] firingTaskLinks = null;

//        @Deprecated
//        int tasklinks = 2; //TODO use MutableInteger for this
//        @Deprecated
//        int termlinks = 3; //TODO use MutableInteger for this

        /* ---------- Short-term workspace for a single cycle ------- */

        public DefaultCycle(NAR nar, Deriver deriver, Bag<Term, Concept> concepts, ConceptActivator ca) {
            super();

            this.nar = nar;
            this.conceptActivator = ca;

            this.deriver = deriver;

            this.conceptForget = nar.memory.conceptForgetDurations;

            this.conceptsFiredPerCycle = new MutableInteger(1);
            this.active = concepts;

            handlers.add(
                nar.memory.eventCycleEnd.on((m) -> {
                    fireConcepts(conceptsFiredPerCycle.intValue());
                }),
                nar.memory.eventReset.on((m) -> {
                    reset();
                })
            );
        }


        /**
         * samples an active concept
         */
        public Concept next() {
            return active.peekNext();
        }


        public void reset() {


        }

        protected void fireConcepts(int conceptsToFire) {

            active.setCapacity(capacity.intValue()); //TODO share the MutableInteger so that this doesnt need to be called ever

            //1 concept if (memory.newTasks.isEmpty())*/
            if (conceptsToFire == 0) return;

            final float conceptForgetDurations = nar.memory.conceptForgetDurations.floatValue();

            //final float tasklinkForgetDurations = nar.memory().taskLinkForgetDurations.floatValue();

            //final int termLinkSelections = termLinksPerConcept.getValue();

//            Concept[] buffer = new Concept[conceptsToFire];
//            int n = active.forgetNext(conceptForgetDurations, buffer, time());
//            if (n == 0) return;

            for (int i = 0; i < conceptsToFire; i++) {
                Concept c = active.forgetNext(conceptForgetDurations, nar.memory);
                if (c == null) break;
                fireConcept(c);
            }
        }

        protected void fireConcept(Concept c) {
            fireConcept(c, p -> {
                //direct: just input to nar
                deriver.run(p, nar::input);
            });
        }


        protected final void fireConcept(Concept concept, Consumer<Premise> withResult) {

            {
                int num = termlinksSelectedPerFiredConcept.intValue();
                if (firingTermLinks == null ||
                        firingTermLinks.length != num)
                    firingTermLinks = new TermLink[num];
            }
            {
                int num = tasklinksSelectedPerFiredConcept.intValue();
                if (firingTaskLinks == null ||
                        firingTaskLinks.length != num)
                    firingTaskLinks = new TaskLink[num];
            }

            ConceptProcess.firePremiseSquare(
                nar,
                withResult,
                concept,
                firingTaskLinks,
                firingTermLinks,
                nar.memory.taskLinkForgetDurations.intValue()
            );
        }


        public final Concept activate(final Term term, final Budget b) {
            final Bag<Term, Concept> active = this.active;
            active.setCapacity(capacity.intValue());

            final ConceptActivator ca = this.conceptActivator;
            ca.setActivationFactor( activationFactor.floatValue() );
            return ca.update(term, b, nar.time(), 1f, active);
        }

        public final Bag<Term,Concept> concepts() {
            return active;
        }




        //try to implement some other way, this is here because of serializability

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


}
