package nars.model.impl;

import nars.*;
import nars.Memory.Forgetting;
import nars.Memory.Timing;
import nars.bag.Bag;
import nars.bag.impl.CacheBag;
import nars.bag.impl.experimental.ChainBag;
import nars.budget.Budget;
import nars.nal.nal8.DesireThresholdExecutive;
import nars.model.ControlCycle;
import nars.model.cycle.DefaultCycle;
import nars.nal.*;
import nars.nal.concept.Concept;
import nars.nal.concept.DefaultConcept;
import nars.nal.filter.ConstantDerivationLeak;
import nars.nal.filter.FilterBelowConfidence;
import nars.nal.filter.FilterOperationWithSubjOrPredVariable;
import nars.nal.nal8.NullOperator;
import nars.nal.nal8.Operator;
import nars.nal.rule.*;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;
import nars.op.app.STMInduction;
import nars.op.data.Flat;
import nars.op.io.Say;
import nars.op.io.Schizo;
import nars.op.math.Add;
import nars.op.math.Count;
import nars.op.mental.*;
import nars.op.meta.Reflect;
import nars.op.software.Javascript;
import nars.op.software.Scheme;

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
public class Default extends NARSeed implements NARSeed.ConceptBuilder {




    final LogicPolicy policy;

    /** Size of TaskLinkBag */
    int taskLinkBagSize;


    /** Size of TermLinkBag */
    int termLinkBagSize;
    
    /** determines maximum number of concepts */
    int conceptBagSize;


    /** max # subconscious "subconcept" concepts */
    int subconceptBagSize;

    /** Size of TaskBuffer */
    int taskBufferSize;
    
    int taskBufferLevels;
    protected int maxNALLevel;

    InternalExperience.InternalExperienceMode internalExperience;
    private int cyclesPerFrame = 1;


    public NARSeed level(int maxNALlevel) {
        this.maxNALLevel = maxNALlevel;
        if (maxNALLevel < 8)
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

        setConceptBagSize(maxConcepts);
        setSubconceptBagSize(maxConcepts * 2);
        conceptsFiredPerCycle.set(conceptsFirePerCycle);
        termLinkMaxReasoned.set(termLinksPerCycle);
        termLinkMaxMatched.set((termLinksPerCycle*3));

        //Build Parameters
        this.maxNALLevel = Global.DEFAULT_NAL_LEVEL;
        this.internalExperience =
                maxNALLevel >= 8 ? InternalExperience.InternalExperienceMode.Minimal :  InternalExperience.InternalExperienceMode.None;

        setDerivationMerging(TaskComparator.Merging.Or);

        setTaskLinkBagSize(32);

        setTermLinkBagSize(96);

        setNovelTaskBagSize(32);




        //Runtime Initial Values

        duration.set(5);
        duration.set(5);

        confidenceThreshold.set(0.01);

        shortTermMemoryHistory.set(1);
        temporalRelationsMax.set(7);

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

        termLinkRecordLength.set(10);
        noveltyHorizon.set(7); //should probably be less than and not a multiple of other termlink parameters

        setForgetting(Forgetting.Periodic);
        setTiming(Timing.Cycle);
        outputVolume.set(100);

        reliance.set(0.9f);

        decisionThreshold.set(0.60);


        policy = new LogicPolicy(

                new LogicRule /* <ConceptProcess> */ [] {

                    //A. concept fire tasklink derivation
                    new TransformTask(),
                    new Contraposition(),

                    //B. concept fire tasklink termlink (pre-filter)
                    new FilterEqualSubtermsInRespectToImageAndProduct(),
                    new MatchTaskBelief(),

                    //C. concept fire tasklink termlink derivation
                    new ForwardImplicationProceed(),
                        //(new TemporalInductionChain()),
                    new TemporalInductionChain2(),
                    new DeduceSecondaryVariableUnification(),
                    new DeduceConjunctionByQuestion(),
                    new TableDerivations()
                } ,

                new DerivationFilter[] {
                    new FilterBelowConfidence(),
                    new FilterOperationWithSubjOrPredVariable()
                    //param.getDefaultDerivationFilters().add(new BeRational());
                }

        );

    }

    final Operator[] exampleOperators = new Operator[] {
        //new Wait(),
        new NullOperator("^break"),
        new NullOperator("^drop"),
        new NullOperator("^goto"),
        new NullOperator("^open"),
        new NullOperator("^pick"),
        new NullOperator("^strike"),
        new NullOperator("^throw"),
        new NullOperator("^activate"),
        new NullOperator("^deactivate")
    };

    public Operator[] newDefaultOperators(NAR n) {

        return new Operator[] {


                //new Wait(),
                new Believe(),  // accept a statement with a default truth-value
                new Want(),     // accept a statement with a default desire-value
                new Wonder(),   // find the truth-value of a statement
                new Evaluate(), // find the desire-value of a statement

                //concept operations for internal perceptions
                new Remind(),   // create/activate a concept
                new Consider(),  // do one inference step on a concept
                new Name(),         // turn a compount term into an atomic term
                //new Abbreviate(),

                //new Register(),

                // truth-value operations
                new Doubt(),        // decrease the confidence of a belief
                new Hesitate(),      // decrease the confidence of a goal


                //Meta
                new Reflect(),

                // feeling operations
                new FeelHappy(),
                new FeelBusy(),

                // math operations
                new Count(),
                new Add(),
                //new MathExpression(),

                //Term manipulation
                new Flat.AsProduct(),

                //TODO move Javascript to a UnsafeOperators set, because of remote execution issues
                //new Javascript(),  // javascript evaluation
                new Scheme(),      // scheme evaluation


                //new NumericCertainty(),

                //io operations
                new Say(),

                new Schizo(),     //change Memory's SELF term (default: SELF)

                new Javascript()
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

    }



    /** initialization after NAR is constructed */
    @Override public void init(NAR n) {

        n.setCyclesPerFrame(cyclesPerFrame);

        if (maxNALLevel >= 7) {
            n.on(STMInduction.class);
        }

        if (maxNALLevel >= 8) {

            for (Operator o : newDefaultOperators(n))
                n.on(o);
            for (Operator o : exampleOperators)
                n.on(o);

            //n.on(new Anticipate());      // expect an event

            if (internalExperience == Minimal) {
                n.on(new InternalExperience());
            } else if (internalExperience == Full) {
                n.on(new FullInternalExperience());
                n.on(new Abbreviation());
                n.on(new Counting());
            }
        }


        n.on(new Events.OUT(n));

        //n.on(new RuntimeNARSettings());

        initDerivationFilters();

    }

    protected void initDerivationFilters() {
        final float DERIVATION_PRIORITY_LEAK=0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
        final float DERIVATION_DURABILITY_LEAK=0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
        getLogicPolicy().derivationFilters.add(new ConstantDerivationLeak(DERIVATION_PRIORITY_LEAK, DERIVATION_DURABILITY_LEAK));
    }

    //public final DDNodePool<Sentence> sentenceNodes = new DDNodePool(1024);
    //public final DDNodePool<TermLinkKey> termlinkKeyNodes = new DDNodePool(1024);

    @Override
    public Concept newConcept(final Term t, final Budget b, final Memory m) {

        Bag<Sentence, TaskLink> taskLinks = new ChainBag(/*sentenceNodes,*/ getConceptTaskLinks());
        Bag<TermLinkKey, TermLink> termLinks = new ChainBag(/*termlinkKeyNodes,*/ getConceptTermLinks());

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
        return new ChainBag(getConceptBagSize());
    }
    
    CacheBag<Term,Concept> newSubconceptBag() {        
        if (getSubconceptBagSize() < 1) return null;
        return new CacheBag(getSubconceptBagSize());
    }

    @Override
    public ControlCycle newControlCycle() {
        return new DefaultCycle(newConceptBag(), newSubconceptBag(), newNovelTaskBag());
    }
    
    public Bag<Sentence<Compound>, Task<Compound>> newNovelTaskBag() {
        return new ChainBag(getNovelTaskBagSize());
    }

    public Default setSubconceptBagSize(int subconceptBagSize) {
        this.subconceptBagSize = subconceptBagSize;
        return this;
    }

    public int getSubconceptBagSize() {
        return subconceptBagSize;
    }



    public int getNovelTaskBagSize() {
        return taskBufferSize;
    }
    
    
    public int getConceptBagSize() { return conceptBagSize; }    
    public Default setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    

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
        setTiming(Timing.RealMS);
        setForgetting(Forgetting.Periodic);
        return this;
    }
    public Default simulationTime() {
        setTiming(Timing.Simulation);
        setForgetting(Forgetting.Periodic);
        return this;
    }


    @Override
    protected Memory newMemory(Param narParam, LogicPolicy policy) {
        Memory m = super.newMemory(narParam, policy);
        m.on((ConceptBuilder) this); //default conceptbuilder
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
