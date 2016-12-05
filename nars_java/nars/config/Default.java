package nars.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import nars.storage.Memory;
import nars.NAR;
import static nars.config.Default.InternalExperienceMode.Full;
import static nars.config.Default.InternalExperienceMode.Minimal;
import nars.control.DefaultAttention;
import nars.entity.BudgetValue;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Sentence;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.io.DefaultTextPerception;
import nars.io.TextInput;
import nars.language.Term;
import nars.operator.Operator;
import nars.operator.mental.Anticipate;
//import nars.lab.plugin.app.plan.TemporalParticlePlanner;
//import nars.lab.plugin.input.PerceptionAccel;
import nars.plugin.mental.Abbreviation;
import nars.lab.plugin.mental.Counting;
import nars.plugin.mental.FullInternalExperience;
import nars.plugin.mental.InternalExperience;
import nars.plugin.mental.RuntimeNARSettings;
import nars.storage.Bag;
import nars.storage.LevelBag;
import nars.plugin.mental.Emotions;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class Default extends Parameters implements ConceptBuilder {

    
    public Param param = new Param();
    
    int taskLinkBagLevels;
    
    /** Size of TaskLinkBag */
    int taskLinkBagSize;
    
    int termLinkBagLevels;
    
    /** Size of TermLinkBag */
    int termLinkBagSize;
    
    /** determines maximum number of concepts */
    int conceptBagSize;    
    
    int conceptBagLevels;

    /** Size of TaskBuffer */
    int novelTaskBagSize;
    
    int sequenceTaskBagSize;
    
    int novelTaskBagLevels;
    
    int sequenceBagLevels;
    
    public static enum InternalExperienceMode {
        None, Minimal, Full
    }
    
    InternalExperienceMode internalExperience = InternalExperienceMode.Minimal;
        
    
   //transient TemporalParticlePlanner pluginPlanner = null;

    
    public Default() {
        super();
        
       // temporalPlanner(8, 64, 16);
        
        setConceptBagSize(Parameters.CONCEPT_BAG_SIZE);        
        setConceptBagLevels(Parameters.CONCEPT_BAG_LEVELS);
        
        setTaskLinkBagSize(Parameters.TASK_LINK_BAG_SIZE);
        setTaskLinkBagLevels(10);

        setTermLinkBagSize(Parameters.TERM_LINK_BAG_SIZE);
        setTermLinkBagLevels(Parameters.TERM_LINK_BAG_LEVELS);
        
        setNovelTaskBagSize(Parameters.TASK_BUFFER_BAG_SIZE);
        setNovelTaskBagLevels(Parameters.TASK_BUFFER_BAG_LEVELS);
        
        setSequenceTaskBagSize(Parameters.SEQUENCE_BAG_SIZE);
        setSequenceTaskBagLevels(Parameters.SEQUENCE_BAG_LEVELS);
    
        //add derivation filters here:
        //param.getDefaultDerivationFilters().add(new BeRational());
    }

    public Memory newMemory(Param p) {        
        return new Memory(p, 
                new DefaultAttention(newConceptBag(), getConceptBuilder()), 
                new LevelBag<>(getNovelTaskBagLevels(), getNovelTaskBagSize()),
                new LevelBag<>(getNovelTaskBagLevels(), getNovelTaskBagSize()));
    }


    public NAR init(NAR n) {
        
        for (Operator o : DefaultOperators.get(n))
            n.memory.addOperator(o);
        for (Operator o : ExampleOperators.get())
            n.memory.addOperator(o);
        
                
        n.addPlugin(new DefaultTextPerception());
        
        n.addPlugin(new RuntimeNARSettings());
        
        n.addPlugin(new Emotions());
        
        n.addPlugin(new Anticipate());      // expect an event
        
        if (internalExperience==Minimal) {            
            n.addPlugin(new InternalExperience());
        }
        else if (internalExperience==Full) {            
            n.addPlugin(new FullInternalExperience());
            n.addPlugin(new Abbreviation());
            n.addPlugin(new Counting());
        }
        
        return n;
    }


    ConceptBuilder getConceptBuilder() {
        return this;
    }

    @Override
    public Concept newConcept(BudgetValue b, Term t, Memory m) {        
        Bag<TaskLink,Task> taskLinks = new LevelBag<>(getTaskLinkBagLevels(), getConceptTaskLinks());
        Bag<TermLink,TermLink> termLinks = new LevelBag<>(getTermLinkBagLevels(), getConceptTermLinks());
        
        return new Concept(b, t, taskLinks, termLinks, m);        
    }

    
    public Bag<Concept,Term> newConceptBag() {
        return new LevelBag(getConceptBagLevels(), getConceptBagSize());
    }
    
    public int getConceptBagSize() { return conceptBagSize; }    
    public Default setConceptBagSize(int conceptBagSize) { this.conceptBagSize = conceptBagSize; return this;   }

    
    
    public int getConceptBagLevels() { return conceptBagLevels; }    
    public Default setConceptBagLevels(int bagLevels) { this.conceptBagLevels = bagLevels; return this;  }
        
    /**
     * @return the taskLinkBagLevels
     */
    public int getTaskLinkBagLevels() {
        return taskLinkBagLevels;
    }
       
    public Default setTaskLinkBagLevels(int taskLinkBagLevels) {
        this.taskLinkBagLevels = taskLinkBagLevels;
        return this;
    }

    public Default setNovelTaskBagSize(int taskBufferSize) {
        this.novelTaskBagSize = taskBufferSize;
        return this;
    }
    
    public Default setSequenceTaskBagSize(int taskBufferSize) {
        this.sequenceTaskBagSize = taskBufferSize;
        return this;
    }

    public int getNovelTaskBagSize() {
        return novelTaskBagSize;
    }
    
    public int getSequenceTaskBagSize() {
        return sequenceTaskBagSize;
    }
    
    public Default setNovelTaskBagLevels(int l) {
        this.novelTaskBagLevels = l;
        return this;
    }
    
    public Default setSequenceTaskBagLevels(int l) {
        this.sequenceBagLevels = l;
        return this;
    }

    public int getNovelTaskBagLevels() {
        return novelTaskBagLevels;
    }
    

    public int getConceptTaskLinks() {
        return taskLinkBagSize;
    }

    public Default setTaskLinkBagSize(int taskLinkBagSize) {
        this.taskLinkBagSize = taskLinkBagSize;
        return this;
    }

    public int getTermLinkBagLevels() {
        return termLinkBagLevels;
    }

    public Default setTermLinkBagLevels(int termLinkBagLevels) {
        this.termLinkBagLevels = termLinkBagLevels;
        return this;
    }

    public int getConceptTermLinks() {
        return termLinkBagSize;
    }

    public Default setTermLinkBagSize(int termLinkBagSize) {
        this.termLinkBagSize = termLinkBagSize;
        return this;
    }

    public InternalExperienceMode getInternalExperience() {
        return internalExperience;
    }

    public Default setInternalExperience(InternalExperienceMode internalExperience) {
        this.internalExperience = internalExperience;
        return this;
    }
    
    static String readFile(String path, Charset encoding) 
        throws IOException  {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
      }
}
