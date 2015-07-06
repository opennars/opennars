package nars.nar;

import nars.Memory;
import nars.NAR;
import nars.bag.Bag;
import nars.bag.impl.experimental.DelayBag;
import nars.bag.impl.experimental.FairDelayBag;
import nars.budget.Budget;
import nars.concept.Concept;
import nars.concept.DefaultConcept;
import nars.cycle.experimental.AntCore;
import nars.link.TaskLink;
import nars.link.TermLink;
import nars.link.TermLinkKey;
import nars.process.CycleProcess;
import nars.task.Sentence;
import nars.term.Term;

/**
 *
 * https://en.wikipedia.org/wiki/Neuromorphic_engineering
 */
public class Neuromorphic extends Curve {

    private int numAnts;

    /** defaults to all inputs */
    private int maxInputsPerCycle = -1;

    /** temporary: true=curve bag, false=fairdelaybag */
    private final boolean fairdelaybag = true;

    public Neuromorphic(int numAnts) {
        super();        
        this.numAnts = numAnts;
    }

    @Override
    public CycleProcess newControlCycle() {
        if (numAnts == -1)
            numAnts = conceptsFiredPerCycle.get();
        return new AntCore(numAnts, 2.0f, getActiveConcepts());
    }


    @Override
    public Bag<Term, Concept> newConceptBag() {
        /** created by AntAttention */
        return null;
    }

    /** set to -1 for unlimited */
    public Neuromorphic setMaxInputsPerCycle(int i) {
        this.maxInputsPerCycle = i;

        return this;
    }

    @Override
    public void init(NAR x) {
        super.init(x);
        x.param.inputsMaxPerCycle.set(maxInputsPerCycle);
    }

    @Override
    public Concept newConcept(final Term t, Budget b, final Memory m) {

        if (fairdelaybag) {
            DelayBag<Sentence, TaskLink> taskLinks = new FairDelayBag(m,
                    taskLinkForgetDurations, getConceptTaskLinks());
            DelayBag<TermLinkKey, TermLink> termLinks = new FairDelayBag(m,
                    termLinkForgetDurations, getConceptTermLinks());
            return new DefaultConcept(t, b, taskLinks, termLinks, getConceptRanking(), m);
        }
        else {
            return super.newConcept(t, b, m);
        }


    }
    /*
    @Override
    public Concept newConcept(BudgetValue b, Term t, Memory m) {
        
        DelayBag<TaskLink,Task> taskLinks = new FairDelayBag(
                param.taskLinkForgetDurations, getConceptTaskLinks()) {

            
        };
        taskLinks.setMemory(m);
        
        DelayBag<TermLink,TermLink> termLinks = new FairDelayBag(
                param.termLinkForgetDurations, getConceptTermLinks());
        
        termLinks.setMemory(m);
        
        return new Concept(b, t, taskLinks, termLinks, m);
    }*/

    /*
    @Override
    public Bag<Task<Term>, Sentence<Term>> newNovelTaskBag() {
        return new FairDelayBag(param.novelTaskForgetDurations, taskBufferSize);
    }*/

    
    
    
}