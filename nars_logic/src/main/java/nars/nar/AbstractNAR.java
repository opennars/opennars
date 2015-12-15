package nars.nar;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.bag.BagBudget;
import nars.bag.impl.CurveBag;
import nars.budget.Budget;
import nars.concept.AtomConcept;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.java.jclass;
import nars.nal.Deriver;
import nars.nal.PremiseRule;
import nars.nal.nal8.OperatorReaction;
import nars.nal.nal8.operator.NullOperator;
import nars.nal.nal8.operator.TermFunction;
import nars.op.data.Flat;
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
import nars.task.flow.FIFOTaskPerception;
import nars.task.flow.TaskPerception;
import nars.term.Term;
import nars.term.Termed;
import nars.term.atom.Atom;
import nars.term.compile.TermIndex;
import nars.time.Clock;
import nars.util.data.MutableInteger;
import nars.util.data.random.XorShift128PlusRandom;
import nars.util.event.Active;
import org.apache.commons.lang3.mutable.MutableFloat;

import java.io.Serializable;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;


/**
 * Default set of NAR parameters which have been classically used for development.
 * <p>
 * WARNING this Seed is not immutable yet because it extends Param,
 * which is supposed to be per-instance/mutable. So do not attempt
 * to create multiple NAR with the same Default seed model
 */
public abstract class AbstractNAR extends NAR {

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


//    /**
//     * Default DEFAULTS
//     */
//    public Default() {
//        this(1024, 1, 2, 3, new FrameClock());
//    }

    public AbstractNAR(int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle, Clock clock) {
        this(new Memory(clock,
                TermIndex.memory(activeConcepts)
        ), activeConcepts, conceptsFirePerCycle, termLinksPerCycle, taskLinksPerCycle);
    }

    public AbstractNAR(Memory memory, int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {
        super(memory);

        rng = new XorShift128PlusRandom(1);

        initDefaults(memory);

        the("input", input = initInput());

        the("core", core = initCore(
                activeConcepts,
                conceptsFirePerCycle,
                termLinksPerCycle, taskLinksPerCycle
        ));

        if (core!=null) {
            beforeNextFrame(this::initHigherNAL);
        }

    }

    protected void initHigherNAL() {
        if (nal() >= 7) {
            initNAL7();
            if(nal() >=8) {
                initNAL8();
                if (nal() >= 9) {
                    initNAL9();
                }
            }
        }
    }

    public void initNAL7() {
        //NAL7 plugins
        memory.the(new STMTemporalLinkage(this, core.deriver));
        memory.the(new Anticipate(this));
    }

    public void initNAL8() {
        /** derivation operators available at runtime */
        for (Class<? extends TermFunction> c : PremiseRule.Operators) {
            try {
                onExec(c.newInstance());
            } catch (Exception e) {
                error(e);
            }
        }

        for (OperatorReaction o : defaultOperators)
            onExec(o);

        for (OperatorReaction o : exampleOperators)
            onExec(o);
    }

    protected void initNAL9() {



        new FullInternalExperience(this);
        new Abbreviation(this);
        //onExec(Counting.class);

//                /*if (internalExperience == Minimal) {
//                    new InternalExperience(this);
//                    new Abbreviation(this);
//                } else if (internalExperience == Full)*/ {
//                    on(FullInternalExperience.class);
//                    on(Counting.class);
//                }
    }


    public TaskPerception initInput() {
        return new FIFOTaskPerception(this, null, this::process);
    }

    protected DefaultCycle initCore(int activeConcepts, int conceptsFirePerCycle, int termLinksPerCycle, int taskLinksPerCycle) {

        DefaultCycle c = initCore(
            getDeriver(),
            newConceptBag(activeConcepts)
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

    protected DefaultCycle initCore(Deriver deriver, Bag<Concept> conceptBag)    {
        return new Default.DefaultCycle2(this, deriver, conceptBag);
    }

    public void initDefaults(Memory m) {
        //parameter defaults

        setTaskLinkBagSize(24);
        setTermLinkBagSize(24);

        m.duration.set(5);

        m.conceptBeliefsMax.set(8);
        m.conceptGoalsMax.set(5);
        m.conceptQuestionsMax.set(3);

        m.conceptForgetDurations.setValue(2.0);
        m.taskLinkForgetDurations.setValue(3.0);
        m.termLinkForgetDurations.setValue(4.0);


        m.derivationThreshold.set(0);


        m.taskProcessThreshold.set(0); //warning: if this is not zero, it could remove un-TaskProcess-able tasks even if they are stored by a Concept

        //budget propagation thresholds
        m.termLinkThreshold.set(Global.BUDGET_EPSILON);
        m.taskLinkThreshold.set(Global.BUDGET_EPSILON);

        m.executionExpectationThreshold.set(0.5);

        m.shortTermMemoryHistory.set(5);
    }


    public static final OperatorReaction[] exampleOperators = {
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



    public final OperatorReaction[] defaultOperators = {

            //system control
            new echo(),
            //PauseInput.the,
            new reset(),
            //new eval(),
            //new Wait(),

//            new believe(),  // accept a statement with a default truth-value
//            new want(),     // accept a statement with a default desire-value
//            new wonder(),   // find the truth-value of a statement
//            new evaluate(), // find the desire-value of a statement
            //concept operations for internal perceptions
//            new remind(),   // create/activate a concept
//            new consider(),  // do one inference step on a concept
//            new name(),         // turn a compount term into an atomic term
            //new Abbreviate(),
            //new Register(),
            new doubt(),        // decrease the confidence of a belief
//            new hesitate(),      // decrease the confidence of a goal

            //Meta
            new reflect(),
            new jclass(),

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

            /*new json.jsonfrom(),
            new json.jsonto()*/
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

    public AbstractNAR nal(int maxNALlevel) {
        memory.nal(maxNALlevel);
        return this;
    }

    /** ConceptBuilder: */
    public Concept apply(Term t) {

        Bag<Task> taskLinks =
                new CurveBag<>(taskLinkBagSize, rng).mergePlus();

        Bag<Termed> termLinks =
                new CurveBag<>(termLinkBagSize, rng).mergePlus();

        //Budget b = new UnitBudget();
        //Budget b = new BagAggregateBudget(taskLinks);

        return t instanceof Atom ?

                new AtomConcept(
                    t,
                    termLinks, taskLinks) :

                new DefaultConcept(
                    t,
                    taskLinks, termLinks, memory);

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
    protected Concept doConceptualize(Term term, Budget b, float scale) {
        Termed c = memory.index.get(term);
        if (!(c instanceof Concept)) {
            c = apply(term);
            if (c == null)
                return null; //unconceptualizable
            memory.index.put(term, c);
        }

        return core.concepts().put(c, b, scale).get();
    }


    public Bag<Concept> newConceptBag(int initialCapacity) {
        return new CurveBag(initialCapacity, rng).mergePlus();
    }

    public AbstractNAR setTaskLinkBagSize(int taskLinkBagSize) {
        this.taskLinkBagSize = taskLinkBagSize;
        return this;
    }

    public AbstractNAR setTermLinkBagSize(int termLinkBagSize) {
        this.termLinkBagSize = termLinkBagSize;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '[' + nal() + ']';
    }

    protected Deriver getDeriver() {
        return Deriver.getDefaultDeriver();
    }

    public NAR forEachConcept(Consumer<Concept> recip) {
        core.active.forEach(recip);
        return this;
    }


    /**
     * The original deterministic memory cycle implementation that is currently used as a standard
     * for development and testing.
     */
    public abstract static class DefaultCycle implements Serializable {

        final Active handlers = new Active();

        /**
         * How many concepts to fire each cycle; measures degree of parallelism in each cycle
         */
        public final MutableInteger conceptsFiredPerCycle;


        public final Deriver  deriver;


        public final MutableInteger tasklinksSelectedPerFiredConcept = new MutableInteger(1);
        public final MutableInteger termlinksSelectedPerFiredConcept = new MutableInteger(1);

        public final MutableFloat activationFactor = new MutableFloat(1.0f);

//        final Function<Task, Task> derivationPostProcess = d -> {
//            return LimitDerivationPriority.limitDerivation(d);
//        };



        /**
         * concepts active in this cycle
         */
        public final Bag<Concept> active;


        @Deprecated
        public final transient NAR nar;

        public final MutableInteger capacity = new MutableInteger();

        public final MutableFloat conceptForget;




//        @Deprecated
//        int tasklinks = 2; //TODO use MutableInteger for this
//        @Deprecated
//        int termlinks = 3; //TODO use MutableInteger for this

        /* ---------- Short-term workspace for a single cycle ------- */

        public DefaultCycle(NAR nar, Deriver deriver, Bag<Concept> concepts) {

            this.nar = nar;

            this.deriver = deriver;

            conceptForget = nar.memory.conceptForgetDurations;

            conceptsFiredPerCycle = new MutableInteger(1);
            active = concepts;

            handlers.add(
                nar.memory.eventCycleEnd.on((m) -> fireConcepts(conceptsFiredPerCycle.intValue(), c->process(c))),
                nar.memory.eventReset.on((m) -> reset())
            );
        }


        abstract protected void process(ConceptProcess cp);

        /**
         * samples an active concept
         */
        public Concept next() {
            return active.peekNext().get();
        }


        public void reset() {


        }

        Predicate<BagBudget> simpleForgetDecay = (b) -> {
            float p = b.getPriority() * 0.99f;
            if (p > b.getQuality()*0.1f)
                b.setPriority(p);
            return true;
        };

        protected void fireConcepts(int conceptsToFire, Consumer<ConceptProcess> processor) {

            active.setCapacity(capacity.intValue()); //TODO share the MutableInteger so that this doesnt need to be called ever

            if (conceptsToFire == 0 || active.isEmpty()) return;

            active.next(conceptsToFire, cb -> {
                Concept c = (AtomConcept) cb.get();

                //c.getTermLinks().update(simpleForgetDecay);
                //c.getTaskLinks().update(simpleForgetDecay);

                float p =
                        //Math.max(
                       c.getTaskLinks().getPriorityMax()
                       // c.getTermLinks().getPriorityMax()
                        //)
                        ;

                cb.setPriority(p);
                cb.setDurability(p);
                cb.setQuality(p);

                //if above firing threshold
                //fireConcept(c);
                firePremiseSquare(nar, processor, c,
                    tasklinksSelectedPerFiredConcept.intValue(),
                    termlinksSelectedPerFiredConcept.intValue(),
                    simpleForgetDecay
                );

                c.getTermLinks().update();
                c.getTaskLinks().update();

                return true;
            });
            active.update();

        }

        /*{
            fireConcept(c, p -> {
                //direct: just input to nar
                deriver.run(p, nar::input);
            });
        }*/


        /** temporary re-usable array for batch firing */
        private final Set<BagBudget<Termed>> terms = Global.newHashSet(1);
        /** temporary re-usable array for batch firing */
        private final Set<BagBudget<Task>> tasks = Global.newHashSet(1);

        /**
         * iteratively supplies a matrix of premises from the next N tasklinks and M termlinks
         * (recycles buffers, non-thread safe, one thread use this at a time)
         */
        public void firePremiseSquare(
                NAR nar,
                Consumer<ConceptProcess> proc,
                Concept concept,
                int tasklinks, int termlinks, Predicate<BagBudget> each) {

            //Memory m = nar.memory;
            //int dur = m.duration();

            //long now = nar.time();

        /* dur, now,
                taskLinkForgetDurations * dur,
                tasks); */

            int tasksCount = concept.getTaskLinks().next(tasklinks, each, tasks);
            if (tasksCount == 0) return;


        /*int termsCount = concept.nextTermLinks(dur, now,
                m.termLinkForgetDurations.floatValue(),
                terms);*/
            int termsCount = concept.getTermLinks().next(termlinks, each, terms);
            if (termsCount == 0) return;

            /*System.out.println(tasks.size() + "," + terms.size() + ": "
                    + tasks + " " + terms);*/

            ConceptProcess.firePremises(concept,
                    tasks.toArray(new BagBudget[tasks.size()]),
                    terms.toArray(new BagBudget[terms.size()]),
                    proc, nar);

            tasks.clear();
            terms.clear();
        }


//        protected final void fireConceptSquare(Concept c, Consumer<ConceptProcess> withResult) {
//
//
//            {
//                int num = termlinksSelectedPerFiredConcept.intValue();
//                if (firingTermLinks == null ||
//                        firingTermLinks.length != num)
//                    firingTermLinks = new BagBudget[num];
//            }
//            {
//                int num = tasklinksSelectedPerFiredConcept.intValue();
//                if (firingTaskLinks == null ||
//                        firingTaskLinks.length != num)
//                    firingTaskLinks = new BagBudget[num];
//            }
//
//            firePremiseSquare(
//                nar,
//                withResult,
//                c,
//                firingTaskLinks,
//                firingTermLinks,
//                nar.memory.taskLinkForgetDurations.intValue()
//            );
//        }


//        public final Concept activate(Term term, Budget b) {
//            Bag<Term, Concept> active = this.active;
//            active.setCapacity(capacity.intValue());
//
////            ConceptActivator ca = conceptActivator;
////            ca.setActivationFactor( activationFactor.floatValue() );
////            return ca.update(term, b, nar.time(), 1.0f, active);
//        }

        public final Bag<Concept> concepts() {
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
