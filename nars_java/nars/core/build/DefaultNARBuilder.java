package nars.core.build;

import java.util.concurrent.atomic.AtomicInteger;
import nars.core.NARBuilder;
import nars.core.Param;
import nars.core.Parameters;
import nars.core.control.SequentialMemoryCycle;
import nars.entity.Concept;
import nars.entity.ConceptBuilder;
import nars.entity.Task;
import nars.entity.TaskLink;
import nars.entity.TermLink;
import nars.language.Term;
import nars.storage.AbstractBag;
import nars.storage.Bag;
import nars.storage.Memory;
import nars.storage.MemoryModel;

/**
 * Default set of NAR parameters which have been classically used for development.
 */
public class DefaultNARBuilder extends NARBuilder implements ConceptBuilder {

    
    public DefaultNARBuilder() {
        super();
        
        setConceptBagLevels(100);
        setConceptBagSize(1000);        
        
        setTaskLinkBagLevels(100);        
        setTaskLinkBagSize(20);

        setTermLinkBagLevels(100);
        setTermLinkBagSize(100);
    }

    @Override
    public Param newParam() {
        Param p = new Param();
        p.noiseLevel.set(100);
        p.conceptForgettingRate.set(10);             
        p.taskForgettingRate.set(20);
        p.beliefForgettingRate.set(50);
        p.cycleInputTasks.set(1);
                
        
        //Experimental parameters - adjust at your own risk
        p.cycleMemory.set(1);                
        return p;
    }
    
    @Override
    public MemoryModel newMemoryModel(Param p, ConceptBuilder c) {
        return new SequentialMemoryCycle(newConceptBag(p), c);
    }

    @Override
    public ConceptBuilder getConceptBuilder() {
        return this;
    }
    
    
    

    @Override
    public Concept newConcept(final Term t, final Memory m) {
        
        AbstractBag<TaskLink> taskLinks = new Bag<>(getTaskLinkBagLevels(), getTaskLinkBagSize(), m.param.taskForgettingRate);
        AbstractBag<TermLink> termLinks = new Bag<>(getTermLinkBagLevels(), getTermLinkBagSize(), m.param.beliefForgettingRate);
        
        return new Concept(t, taskLinks, termLinks, m);        
    }

    
    protected AbstractBag<Concept> newConceptBag(Param p) {
        return new Bag(getConceptBagLevels(), getConceptBagSize(), p.conceptForgettingRate);
    }

    @Override
    public AbstractBag<Task> newNovelTaskBag(Param p) {
        return new Bag<>(getConceptBagLevels(), Parameters.TASK_BUFFER_SIZE, new AtomicInteger(Parameters.NEW_TASK_FORGETTING_CYCLE));
    }
 
    public int taskLinkBagLevels;
    
    /** Size of TaskLinkBag */
    public int taskLinkBagSize;
    
    public int termLinkBagLevels;
    
    /** Size of TermLinkBag */
    public int termLinkBagSize;
    
    /** determines maximum number of concepts */
    private int conceptBagSize;    
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
    
}
