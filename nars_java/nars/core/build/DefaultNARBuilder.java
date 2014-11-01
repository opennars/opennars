package nars.core.build;

import nars.core.Attention;
import nars.core.Memory;
import nars.core.Memory.Forgetting;
import nars.core.Memory.Timing;
import nars.core.NAR;
import nars.core.NARBuilder;
import nars.core.Param;
import nars.core.Parameters;
import nars.core.control.DefaultAttention;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.plugin.mental.Abbreviation;
import nars.plugin.mental.FullInternalExperience;
import nars.plugin.mental.TemporalParticlePlanner;
import nars.storage.Bag;
import nars.storage.CacheBag;
import nars.storage.LevelBag;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class DefaultNARBuilder extends NARBuilder implements ConceptBuilder {

    
    public int taskLinkBagLevels;
    
    /** Size of TaskLinkBag */
    public int taskLinkBagSize;
    
    public int termLinkBagLevels;
    
    /** Size of TermLinkBag */
    public int termLinkBagSize;
    
    /** determines maximum number of concepts */
    private int conceptBagSize;    
    
    /** max # subconscious "subconcept" concepts */
    private int subconceptBagSize;

    /** Size of TaskBuffer */
    private int taskBufferSize = 10;
    private Memory.Timing timing = Timing.Iterative;
    private Memory.Forgetting forgetMode = Forgetting.Iterative;
    private int taskBufferLevels;
    
    
    public DefaultNARBuilder() {
        super();
        
        setConceptBagSize(1000);        
        setSubconceptBagSize(0);
        setConceptBagLevels(100);
        
        setTaskLinkBagSize(20);
        setTaskLinkBagLevels(100);

        setTermLinkBagSize(100);
        setTermLinkBagLevels(100);
        
        setNovelTaskBagSize(10);
        setNovelTaskBagLevels(100);
    }

    @Override
    public Param newParam() {
        Param p = new Param();
        p.setTiming(timing);
        p.noiseLevel.set(100);
        
        p.decisionThreshold.set(0.30);
        
        p.duration.set(5);
        p.conceptForgetDurations.set(2.0);
        p.taskForgetDurations.set(4.0);
        p.beliefForgetDurations.set(10.0);
        p.newTaskForgetDurations.set(2.0);
                
        p.conceptBeliefsMax.set(7);
        p.conceptGoalsMax.set(7);
        p.conceptQuestionsMax.set(5);
        
        p.contrapositionPriority.set(30);
                
        p.termLinkMaxReasoned.set(3);
        p.termLinkMaxMatched.set(10);
        p.termLinkRecordLength.set(10);
        
        p.setForgetMode(forgetMode);
        
        return p;
    }

    @Override
    public NAR build() {
        NAR n = super.build();
        
        //the only plugin which is dependent on a parameter
        //because it enriches NAL8 performance a lot:
        if(Parameters.TEMPORAL_PARTICLE_PLANNER) {
            TemporalParticlePlanner planner=new TemporalParticlePlanner();
            n.addPlugin(planner);
        }
        if(Parameters.INTERNAL_EXPERIENCE_FULL) {
            FullInternalExperience nal9=new FullInternalExperience();
            n.addPlugin(nal9);
        }
        if(Parameters.INTERNAL_EXPERIENCE_FULL) {
            Abbreviation nal9abr=new Abbreviation();
            n.addPlugin(nal9abr);
        }
        
        return n;
    }
    
    
    
    @Override
    public Attention newAttention(Param p, ConceptBuilder c) {
        return new DefaultAttention(newConceptBag(p), newSubconceptBag(p), c);
    }

    @Override
    public ConceptBuilder getConceptBuilder() {
        return this;
    }

    @Override
    public Concept newConcept(BudgetValue b, Term t, Memory m) {        
        Bag<TaskLink,Task> taskLinks = new LevelBag<>(getTaskLinkBagLevels(), getTaskLinkBagSize());
        Bag<TermLink,TermLink> termLinks = new LevelBag<>(getTermLinkBagLevels(), getTermLinkBagSize());
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }

    
    protected Bag<Concept,Term> newConceptBag(Param p) {
        return new LevelBag(getConceptBagLevels(), getConceptBagSize());
    }
    
    protected CacheBag<Term,Concept> newSubconceptBag(Param p) {        
        if (getSubconceptBagSize() == 0) return null;
        return new CacheBag(getSubconceptBagSize());
    }

    @Override
    public Bag<Task<Term>,Sentence<Term>> newNovelTaskBag(Param p) {
        return new LevelBag<>(getNovelTaskBagLevels(), getNovelTaskBagSize());
    }

    public DefaultNARBuilder setSubconceptBagSize(int subconceptBagSize) {
        this.subconceptBagSize = subconceptBagSize;
        return this;
    }
    public int getSubconceptBagSize() {
        return subconceptBagSize;
    }
 
    
    
    public int getConceptBagSize() { return conceptBagSize; }    
    public DefaultNARBuilder setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    /** Level granularity in Bag, usually 100 (two digits) */    
    private int conceptBagLevels;
    public int getConceptBagLevels() { return conceptBagLevels; }    
    public DefaultNARBuilder setConceptBagLevels(int bagLevels) { this.conceptBagLevels = bagLevels; return this;  }
        
    /**
     * @return the taskLinkBagLevels
     */
    public int getTaskLinkBagLevels() {
        return taskLinkBagLevels;
    }
       
    public DefaultNARBuilder setTaskLinkBagLevels(int taskLinkBagLevels) {
        this.taskLinkBagLevels = taskLinkBagLevels;
        return this;
    }

    public DefaultNARBuilder setNovelTaskBagSize(int taskBufferSize) {
        this.taskBufferSize = taskBufferSize;
        return this;
    }

    public int getNovelTaskBagSize() {
        return taskBufferSize;
    }
    
    public DefaultNARBuilder setNovelTaskBagLevels(int l) {
        this.taskBufferLevels = l;
        return this;
    }

    public int getNovelTaskBagLevels() {
        return taskBufferLevels;
    }
    

    public int getTaskLinkBagSize() {
        return taskLinkBagSize;
    }

    public DefaultNARBuilder setTaskLinkBagSize(int taskLinkBagSize) {
        this.taskLinkBagSize = taskLinkBagSize;
        return this;
    }

    public int getTermLinkBagLevels() {
        return termLinkBagLevels;
    }

    public DefaultNARBuilder setTermLinkBagLevels(int termLinkBagLevels) {
        this.termLinkBagLevels = termLinkBagLevels;
        return this;
    }

    public int getTermLinkBagSize() {
        return termLinkBagSize;
    }

    public DefaultNARBuilder setTermLinkBagSize(int termLinkBagSize) {
        this.termLinkBagSize = termLinkBagSize;
        return this;
    }

    
    public static class CommandLineNARBuilder extends DefaultNARBuilder {
        private final Param param;

        @Override public Param newParam() {        
            return param;
        }

        public CommandLineNARBuilder(String[] args) {
            super();

            param = super.newParam();

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--silence".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);                
                    param.noiseLevel.set(100-sl);
                }
                if ("--noise".equals(arg)) {
                    arg = args[++i];
                    int sl = Integer.parseInt(arg);                
                    param.noiseLevel.set(sl);
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
    }
    
    public DefaultNARBuilder realTime() {
        timing = Timing.Real;
        forgetMode = Forgetting.Periodic;
        return this;
    }
    public DefaultNARBuilder simulationTime() {
        timing = Timing.Simulation;        
        forgetMode = Forgetting.Periodic;
        return this;
    }
    

    /* ---------- initial values of run-time adjustable parameters ---------- */
//    /** Concept decay rate in ConceptBag, in [1, 99]. */
//    private static final int CONCEPT_CYCLES_TO_FORGET = 10;
//    /** TaskLink decay rate in TaskLinkBag, in [1, 99]. */
//    private static final int TASK_LINK_CYCLES_TO_FORGET = 20;
//    /** TermLink decay rate in TermLinkBag, in [1, 99]. */
//    private static final int TERM_LINK_CYCLES_TO_FORGET = 50;        
//    /** Task decay rate in TaskBuffer, in [1, 99]. */
//    private static final int NEW_TASK_FORGETTING_CYCLE = 10;

    public void setForgetMode(Forgetting forgetMode) {
        this.forgetMode = forgetMode;
    }

}
