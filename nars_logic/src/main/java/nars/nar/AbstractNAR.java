package nars.nar;

import nars.Global;
import nars.Memory;
import nars.NAR;
import nars.concept.Concept;
import nars.nal.Deriver;
import nars.nal.PremiseRule;
import nars.nal.nal8.AbstractOperator;
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
import nars.term.compile.TermIndex;
import nars.time.Clock;
import nars.util.data.random.XorShift128PlusRandom;

import java.util.Random;
import java.util.function.Consumer;


/**
 * Default set of NAR parameters which have been classically used for development.
 * <p>
 * WARNING this Seed is not immutable yet because it extends Param,
 * which is supposed to be per-instance/mutable. So do not attempt
 * to create multiple NAR with the same Default seed model
 */
public abstract class AbstractNAR extends NAR {


    /** default for new concepts */
    @Deprecated int taskLinkBagSize;
    /** default for new concepts */
    @Deprecated int termLinkBagSize;


    //public final Random rng = new RandomAdaptor(new MersenneTwister(1));
    public final Random rng;

    public AbstractNAR(Clock clock) {
        this(new Memory(clock, TermIndex.memory(1024) ));
    }

    public AbstractNAR(Memory memory) {
        super(memory);

        rng = new XorShift128PlusRandom(1);

        initDefaults(memory);


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
        memory.the(new STMTemporalLinkage(this));
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

        for (AbstractOperator o : defaultOperators)
            onExec(o);

        for (AbstractOperator o : exampleOperators)
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



    public void initDefaults(Memory m) {
        //parameter defaults

        setTaskLinkBagSize(12);
        setTermLinkBagSize(16);

        m.duration.set(5);

        m.conceptBeliefsMax.set(12);
        m.conceptGoalsMax.set(7);
        m.conceptQuestionsMax.set(5);

        m.conceptForgetDurations.setValue(2.0);
        m.linkForgetDurations.setValue(3.0);

        m.derivationDurabilityThreshold.setValue(Global.BUDGET_DERIVATION_DURABILITY_THRESHOLD);

        m.taskProcessThreshold.setValue(0); //warning: if this is not zero, it could remove un-TaskProcess-able tasks even if they are stored by a Concept

        //budget propagation thresholds
        m.termLinkThreshold.setValue(Global.BUDGET_PROPAGATION_EPSILON);
        m.taskLinkThreshold.setValue(Global.BUDGET_PROPAGATION_EPSILON);

        m.executionExpectationThreshold.setValue(0.5);

        m.shortTermMemoryHistory.set(5);
    }


    public static final AbstractOperator[] exampleOperators = {
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





    public final AbstractOperator[] defaultOperators = {

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
            //new jclass(),

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



    //    /**
//     * rank function used for concept belief and goal tables
//     */
//    public BeliefTable.RankBuilder newConceptBeliefGoalRanking() {
//        return (c, b) ->
//                BeliefTable.BeliefConfidenceOrOriginality;
//        //new BeliefTable.BeliefConfidenceAndCurrentTime(c);
//
//    }



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

    protected Deriver newDeriver() {
        return Deriver.getDefaultDeriver();
    }

    /** reports all active concepts or those which can be reached */
    public abstract NAR forEachConcept(Consumer<Concept> recip);




}
