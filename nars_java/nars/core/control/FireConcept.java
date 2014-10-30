/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.core.control;

import nars.core.Events;
import nars.core.Memory;
import nars.entity.Concept;
import nars.entity.Task;
import nars.entity.TermLink;
import nars.inference.RuleTables;

abstract public class FireConcept extends NAL {
    private final int numTaskLinks;

    public FireConcept(Memory mem, Concept concept, int numTaskLinks) {
        super(mem);
        this.currentConcept = concept;
        this.currentTaskLink = currentTaskLink;
        this.numTaskLinks = numTaskLinks;
    }

    abstract public void onFinished();
    
    @Override
    public void run() {        
        fire();        
        onFinished();                
    }
    
    protected void fire() {
        
        for (int i = 0; i < numTaskLinks; i++) {
            
            if (currentConcept.taskLinks.size() == 0) 
                return;
            
            currentTaskLink = currentConcept.taskLinks.takeNext();        

            if (currentTaskLink.budget.aboveThreshold()) {
                try {
                    fireTaskLink();
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            currentConcept.taskLinks.putBack(currentTaskLink, 
                    mem.param.taskForgetDurations.getCycles(), mem);        
        }
        
    }

    protected void fireTaskLink() {
        final Task task = currentTaskLink.getTargetTask();
        setCurrentTerm(currentConcept.term);
        setCurrentTaskLink(currentTaskLink);
        setCurrentBeliefLink(null);
        setCurrentTask(task); // one of the two places where this variable is set
        mem.logic.TASKLINK_FIRE.commit(currentTaskLink.budget.getPriority());
        emit(Events.ConceptFire.class, currentConcept, currentTaskLink);
        if (currentTaskLink.type == TermLink.TRANSFORM) {
            setCurrentBelief(null);
            RuleTables.transformTask(currentTaskLink, this); // to turn this into structural inference as below?
        } else {
            int termLinkCount = mem.param.termLinkMaxReasoned.get();
            while (termLinkCount > 0) {
                final TermLink termLink = currentConcept.selectTermLink(currentTaskLink, mem.time());
                if (termLink != null) {
                    emit(Events.TermLinkSelect.class, termLink, currentConcept);
                    setCurrentBeliefLink(termLink);
                    RuleTables.reason(currentTaskLink, termLink, this);
                    currentConcept.returnTermLink(termLink);
                    termLinkCount--;
                } else {
                    break;
                }
            }
        }
    }
    
}
