package nars.prototype;

import nars.*;
import nars.Memory.Forgetting;
import nars.Memory.Timing;
import nars.budget.Bag;
import nars.budget.Budget;
import nars.budget.bag.CacheBag;
import nars.budget.bag.experimental.ChainBag;
import nars.control.DefaultCore;
import nars.event.AbstractExecutive;
import nars.event.exe.DesireThresholdExecutive;
import nars.nal.*;
import nars.nal.concept.Concept;
import nars.nal.concept.DefaultConcept;
import nars.nal.nal8.Operator;
import nars.nal.term.Compound;
import nars.nal.term.Term;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.tlink.TermLinkKey;
import nars.operate.app.STMInduction;
import nars.operate.mental.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import nars.nal.filter.ConstantDerivationLeak;

import static nars.operate.mental.InternalExperience.InternalExperienceMode.Full;
import static nars.operate.mental.InternalExperience.InternalExperienceMode.Minimal;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Default extends ProtoNAR implements ConceptBuilder {

    


    final NALParam nalParam = new NALParam();

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
        
    

    public ProtoNAR level(int maxNALlevel) {
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
        setSubconceptBagSize(maxConcepts * 8);
        param.conceptsFiredPerCycle.set(conceptsFirePerCycle);
        param.termLinkMaxReasoned.set(termLinksPerCycle);
        param.termLinkMaxMatched.set((termLinksPerCycle*3));

        //Build Parameters
        this.maxNALLevel = Global.DEFAULT_NAL_LEVEL;
        this.internalExperience =
                maxNALLevel >= 8 ? InternalExperience.InternalExperienceMode.Minimal :  InternalExperience.InternalExperienceMode.None;

        param.setDerivationDuplicationMode(TaskComparator.Duplication.Or);

        setTaskLinkBagSize(32);

        setTermLinkBagSize(96);

        setNovelTaskBagSize(32);




        //Runtime Initial Values

        param.duration.set(5);

        param.confidenceThreshold.set(0.01);

        param.shortTermMemoryHistory.set(1);
        param.temporalRelationsMax.set(7);

        param.conceptActivationFactor.set(1.0);
        param.conceptFireThreshold.set(0.0);

        param.conceptForgetDurations.set(2.0);
        param.taskLinkForgetDurations.set(4.0);
        param.termLinkForgetDurations.set(10.0);
        param.novelTaskForgetDurations.set(2.0);

        //param.budgetThreshold.set(0.01f);

        param.conceptBeliefsMax.set(7);
        param.conceptGoalsMax.set(7);
        param.conceptQuestionsMax.set(5);

        param.inputsMaxPerCycle.set(1);

        param.termLinkRecordLength.set(10);
        param.noveltyHorizon.set(7); //should probably be less than and not a multiple of other termlink parameters

        param.setForgetting(Forgetting.Periodic);
        param.setTiming(Timing.Cycle);
        param.outputVolume.set(100);

        param.reliance.set(0.9f);

        param.decisionThreshold.set(0.60);

        //add derivation filters here:
        //param.getDefaultDerivationFilters().add(new BeRational());

    }

    public AbstractExecutive getExecutive() {
        return new DesireThresholdExecutive();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+ '[' + maxNALLevel +
                ((internalExperience== InternalExperience.InternalExperienceMode.None) || (internalExperience==null) ? "" : "+")
                + ']';
    }



    @Override
    public void init(NAR n) {


        for (Operator o : DefaultOperators.get())
            n.on(o);
        for (Operator o : ExampleOperators.get())
            n.on(o);



        if (maxNALLevel >= 7) {
            n.on(new STMInduction());

        }

        if (maxNALLevel >= 8) {
            n.on(getExecutive());

            //n.on(new Anticipate());      // expect an event

            if (internalExperience == Minimal) {
                n.on(new InternalExperience());
            } else if (internalExperience == Full) {
                n.on(new FullInternalExperience());
                n.on(new Abbreviation());
                n.on(new Counting());
            }
        }

        final float DERIVATION_PRIORITY_LEAK=0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
        final float DERIVATION_DURABILITY_LEAK=0.4f; //https://groups.google.com/forum/#!topic/open-nars/y0XDrs2dTVs
        nalParam.derivationFilters.add(new ConstantDerivationLeak(DERIVATION_PRIORITY_LEAK, DERIVATION_DURABILITY_LEAK));


        n.on(new Events.OUT());

        n.on(new RuntimeNARSettings());


    }


    @Override
    public Concept newConcept(final Term t, final Budget b, final Memory m) {
        Bag<Sentence, TaskLink> taskLinks = new ChainBag<>(getConceptTaskLinks());
        Bag<TermLinkKey, TermLink> termLinks = new ChainBag<>(getConceptTermLinks());

        return newConcept(t, b, taskLinks, termLinks, m);
    }

    @Override
    public NALParam getNALParam() {
        return nalParam;
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
    public Core newCore() {
        return new DefaultCore(newConceptBag(), newSubconceptBag(), newNovelTaskBag());
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
        param.setTiming(Timing.RealMS);
        param.setForgetting(Forgetting.Periodic);
        return this;
    }
    public Default simulationTime() {
        param.setTiming(Timing.Simulation);
        param.setForgetting(Forgetting.Periodic);
        return this;
    }


    @Override
    protected Memory newMemory(Param narParam, NALParam nalParam) {
        Memory m = super.newMemory(narParam, nalParam);
        m.on((ConceptBuilder) this); //default conceptbuilder
        return m;
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
                    param.outputVolume.set(100-sl);
                }
                else if ("--noise".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);                
                    param.outputVolume.set(sl);
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

    
    
    public static Default fromJSON(String filePath) {
        
        try {
            String c = readFile(filePath, Charset.defaultCharset());                        
            return Param.json.fromJson(c, Default.class);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    static String readFile(String path, Charset encoding) 
        throws IOException  {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
      }

    public static class DefaultMicro extends Default {

        public DefaultMicro() {
            super();

            setInternalExperience(null);

            setConceptBagSize(128);
            setSubconceptBagSize(16);

            setTaskLinkBagSize(16);

            setTermLinkBagSize(16);

            setNovelTaskBagSize(16);




            //Runtime Initial Values

            param.confidenceThreshold.set(0.05);

            param.temporalRelationsMax.set(4);


            param.conceptBeliefsMax.set(7);
            param.conceptGoalsMax.set(5);
            param.conceptQuestionsMax.set(3);



            param.termLinkMaxReasoned.set(3);
            param.termLinkMaxMatched.set(5);
            param.termLinkRecordLength.set(6);
            param.noveltyHorizon.set(6);

            param.setForgetting(Forgetting.Periodic);
            param.setTiming(Timing.Cycle);
            param.outputVolume.set(100);

            param.reliance.set(0.9f);

            param.decisionThreshold.set(0.60);

            //add derivation filters here:
            //param.getDefaultDerivationFilters().add(new BeRational());

        }

    }
}
