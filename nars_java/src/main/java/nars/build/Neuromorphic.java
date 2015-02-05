package nars.build;

import nars.control.experimental.AntCore;
import nars.core.Core;
import nars.core.Memory;
import nars.core.NAR;
import nars.logic.entity.*;
import nars.util.bag.Bag;
import nars.util.bag.impl.experimental.DelayBag;
import nars.util.bag.impl.experimental.FairDelayBag;

/**
 *
 * https://en.wikipedia.org/wiki/Neuromorphic_engineering
 */
public class Neuromorphic extends Curve {

    private int numAnts;

    /** defaults to all inputs */
    private int maxInputsPerCycle = -1;

    /** temporary: true=curve bag, false=fairdelaybag */
    private boolean fairdelaybag = true;

    public Neuromorphic(int numAnts) {
        super();        
        this.numAnts = numAnts;
    }

    @Override
    public Core newCore() {
        if (numAnts == -1)
            numAnts = param.conceptsFiredPerCycle.get();
        return new AntCore(numAnts, 2.0f, getConceptBagSize(), getConceptBuilder());
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
    public Concept newConcept(BudgetValue b, final Term t, final Memory m) {

        if (fairdelaybag) {
            DelayBag<TaskLink, Sentence> taskLinks = new FairDelayBag(
                    param.taskLinkForgetDurations, getConceptTaskLinks());
            taskLinks.setMemory(m);
            DelayBag<TermLink, String> termLinks = new FairDelayBag(
                    param.termLinkForgetDurations, getConceptTermLinks());
            termLinks.setMemory(m);
            return new Concept(b, t, taskLinks, termLinks, m);
        }
        else {
            return super.newConcept(b, t, m);
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