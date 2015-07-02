package nars.nar;

import nars.*;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.CurveBag;
import nars.bag.impl.GuavaCacheBag;
import nars.bag.impl.LevelBag;
import nars.budget.Budget;
import nars.clock.CycleClock;
import nars.clock.RealtimeMSClock;
import nars.process.CycleProcess;
import nars.cycle.DefaultCycle;
import nars.nal.*;
import nars.concept.Concept;
import nars.concept.ConceptBuilder;
import nars.concept.DefaultConcept;
import nars.nal.nal8.Operator;
import nars.nal.nal8.operator.NullOperator;
import nars.nal.nal8.operator.eval;
import nars.process.concept.*;
import nars.task.Sentence;
import nars.task.Task;
import nars.task.TaskComparator;
import nars.task.filter.*;
import nars.term.Compound;
import nars.term.Term;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.op.app.STMInduction;
import nars.op.data.Flat;
import nars.op.data.json;
import nars.op.data.similaritree;
import nars.op.io.say;
import nars.op.io.schizo;
import nars.op.math.add;
import nars.op.math.count;
import nars.op.math.lessThan;
import nars.op.mental.*;
import nars.op.meta.complexity;
import nars.op.meta.reflect;
import nars.op.software.js;
import nars.op.software.scheme.scheme;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static nars.op.mental.InternalExperience.InternalExperienceMode.Full;
import static nars.op.mental.InternalExperience.InternalExperienceMode.Minimal;

//import sun.tools.jstat.Operator;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Default extends NARSeed implements ConceptBuilder {




    final LogicPolicy policy;

    /** Size of TaskLinkBag */
    int taskLinkBagSize;


    /** Size of TermLinkBag */
    int termLinkBagSize;
    
    /** determines maximum number of concepts */
    int conceptBagSize;


    /** Size of TaskBuffer */
    int taskBufferSize;
    
    int taskBufferLevels;

    InternalExperience.InternalExperienceMode internalExperience;
    private int cyclesPerFrame = 1;


    public Default level(int maxNALlevel) {
        super.level(maxNALlevel);
        if (maxNALlevel < 8)
            setInternalExperience(null);
        return this;
    }


    @Override
    protected int getMaximumNALLevel() {
        return maxNALLevel;
    }

    /** Default DEFAULTS */
    public Default() {
        this(1024, 1, 3);
    }

    public Default(int maxConcepts, int conceptsFirePerCycle, int termLinksPerCycle) {

        setActiveConcepts(maxConcepts);
        conceptsFiredPerCycle.set(conceptsFirePerCycle);

        termLinkMaxReasoned.set(termLinksPerCycle);
        noveltyHorizon.set(0.9f/termLinksPerCycle);

        termLinkMaxMatched.set(3);

        //Build Parameters
        this.maxNALLevel = Global.DEFAULT_NAL_LEVEL;
        this.internalExperience =
                maxNALLevel >= 8 ? InternalExperience.InternalExperienceMode.Minimal :  InternalExperience.InternalExperienceMode.None;

        setInputMerging(TaskComparator.Merging.Or);

        setTaskLinkBagSize(16);

        setTermLinkBagSize(16);

        setNovelTaskBagSize(32);




        //Runtime Initial Values

        duration.set(5);

        confidenceThreshold.set(0.01);

        shortTermMemoryHistory.set(1);
        temporalRelationsMax.set(4);

        conceptActivationFactor.set(1.0);
        conceptFireThreshold.set(0.0);

        conceptForgetDurations.set(2.0);
        taskLinkForgetDurations.set(4.0);
        termLinkForgetDurations.set(10.0);
        novelTaskForgetDurations.set(2.0);

        //param.budgetThreshold.set(0.01f);

        conceptBeliefsMax.set(7);
        conceptGoalsMax.set(7);
        conceptQuestionsMax.set(5);

        inputsMaxPerCycle.set(1);

        termLinkRecordLength.set(8);


        this.perceptThreshold.set(0.0);
        this.taskProcessThreshold.set(0.0);
        this.activeConceptThreshold.set(0.01);
        this.goalThreshold.set(0.01);

        this.termLinkThreshold.set(0.0);
        this.taskLinkThreshold.set(0.0);

        this.executionThreshold.set(0.01);

        setClock(new CycleClock());
        outputVolume.set(100);

        reliance.set(Global.DEFAULT_JUDGMENT_CONFIDENCE);

        executionThreshold.set(0.60);

        conceptCreationExpectation.set(0.66);

        policy = new LogicPolicy(

                new LogicRule /* <ConceptProcess> */ [] {

                    //A. concept fire tasklink derivation
                    new TransformTask(),
                    new Contraposition(),

                    //B. concept fire tasklink termlink (pre-filter)
                    new FilterEqualSubtermsInRespectToImageAndProduct(),
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
                } ,

                new DerivationFilter[] {
                    new FilterBelowConfidence(),
                    new FilterOperationWithSubjOrPredVariable(),
                    new FilterDuplicateExistingBelief(),
                    //param.getDefaultDerivationFilters().add(new BeRational());
                }

        );

    }

    public static final Operator[] exampleOperators = new Operator[] {
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

    public ConceptBuilder[] defaultConceptBuilders = new ConceptBuilder[] {
            new lessThan()
    };



    public final Operator[] defaultOperators  = new Operator[] {

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





    /** initialization after NAR is constructed */
    @Override public void init(NAR n) {

        n.setCyclesPerFrame(cyclesPerFrame);


        

        if (maxNALLevel >= 7) {
            n.on(PerceptionAccel.class);
            n.on(STMInduction.class);


            if (maxNALLevel >= 8) {

                for (Operator o : defaultOperators)
                    n.on(o);
                for (Operator o : exampleOperators)
                    n.on(o);

                for (ConceptBuilder c : defaultConceptBuilders) {
                    n.on(c);
                }

                n.on(Anticipate.class);      // expect an event

                if (internalExperience == Minimal) {
                    new InternalExperience(n);
                    new Abbreviation(n);
                } else if (internalExperience == Full) {
                    new FullInternalExperience(n);
                    n.on(new Counting());
                }
            }
        }

        //n.on(new RuntimeNARSettings());

        initDerivationFilters();

    }

    protected void initDerivationFilters() {
        final float DERIVATION_PRIORITY_LEAK=0.9f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
        final float DERIVATION_DURABILITY_LEAK=0.9f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
        getLogicPolicy().derivationFilters.add(new ConstantDerivationLeak(DERIVATION_PRIORITY_LEAK, DERIVATION_DURABILITY_LEAK));
    }

    @Override
    public Concept newConcept(final Term t, final Budget b, final Memory m) {

        Bag<Sentence, TaskLink> taskLinks = new CurveBag(rng, /*sentenceNodes,*/ getConceptTaskLinks(), true);
        Bag<TermLinkKey, TermLink> termLinks =
                new CurveBag(rng, /*termlinkKeyNodes,*/ getConceptTermLinks(), true);
                //new ChainBag(rng, /*termlinkKeyNodes,*/ getConceptTermLinks());

        return newConcept(t, b, taskLinks, termLinks, m);
    }

    @Override
    public LogicPolicy getLogicPolicy() {
        return policy;
    }

    protected Concept newConcept(Term t, Budget b, Bag<Sentence, TaskLink> taskLinks, Bag<TermLinkKey, TermLink> termLinks, Memory m) {
        return new DefaultConcept(t, b, taskLinks, termLinks, m);
    }
    
    public Bag<Term, Concept> newConceptBag() {
        //return new ChainBag(rng, getActiveConcepts());
        return new LevelBag((int)Math.sqrt(getActiveConcepts()), getActiveConcepts());
    }

    @Override
    public CacheBag<Term,Concept> newIndex() {
        return new GuavaCacheBag();
    }

    @Override
    public CycleProcess newControlCycle() {
        return new DefaultCycle(newConceptBag(), newNovelTaskBag());
    }
    
    public Bag<Sentence<Compound>, Task<Compound>> newNovelTaskBag() {
        return new CurveBag(rng, getNovelTaskBagSize(), true);
    }



    public int getNovelTaskBagSize() {
        return taskBufferSize;
    }
    
    
    public int getActiveConcepts() { return conceptBagSize; }
    public Default setActiveConcepts(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    

    public Default setNovelTaskBagSize(int taskBufferSize) {
        this.taskBufferSize = taskBufferSize;
        return this;
    }


    public int getNovelTaskBagLevels() {
        return taskBufferLevels;
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

    
    public Default realTime() {
        setClock(new RealtimeMSClock(true));
        return this;
    }



    @Override
    protected Memory newMemory(Param narParam, LogicPolicy policy) {
        Memory m = super.newMemory(narParam, policy);
        m.on(this); //default conceptbuilder
        return m;
    }

    public void setCyclesPerFrame(int cyclesPerFrame) {
        this.cyclesPerFrame = cyclesPerFrame;
    }

    public static class CommandLineNARBuilder extends Default {
        
        List<String> filesToLoad = new ArrayList();
        
        public CommandLineNARBuilder(String[] args) {
            super();

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--silence".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);                
                    outputVolume.set(100-sl);
                }
                else if ("--noise".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);                
                    outputVolume.set(sl);
                }    
                else {
                    filesToLoad.add(arg);
                }
                
            }        
        }

        @Override
        public void init(NAR n) {
            super.init(n);
            
            for (String x : filesToLoad) {
                try {
                    n.input(new File(x));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
        return getClass().getSimpleName()+ '[' + maxNALLevel +
                ((internalExperience== InternalExperience.InternalExperienceMode.None) || (internalExperience==null) ? "" : "+")
                + ']';
    }



//    public static Default fromJSON(String filePath) {
//
//        try {
//            String c = readFile(filePath, Charset.defaultCharset());
//            return Param.json.fromJson(c, Default.class);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            return null;
//        }
//    }
    
    static String readFile(String path, Charset encoding) 
        throws IOException  {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
      }

}
