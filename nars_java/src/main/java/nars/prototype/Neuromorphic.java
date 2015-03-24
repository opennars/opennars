package nars.prototype;

import nars.control.experimental.AntCore;
import nars.Core;
import nars.Memory;
import nars.NAR;
import nars.energy.Budget;
import nars.nal.entity.*;
import nars.nal.entity.tlink.TermLinkKey;
import nars.energy.Bag;
import nars.energy.bag.experimental.DelayBag;
import nars.energy.bag.experimental.FairDelayBag;

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
    public Concept newConcept(Budget b, final Term t, final Memory m) {

        if (fairdelaybag) {
            DelayBag<String, TaskLink> taskLinks = new FairDelayBag(
                    param.taskLinkForgetDurations, getConceptTaskLinks());
            taskLinks.setMemory(m);
            DelayBag<TermLinkKey, TermLink> termLinks = new FairDelayBag(
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