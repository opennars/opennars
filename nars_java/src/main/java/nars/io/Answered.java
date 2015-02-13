/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io;

import nars.core.Events.Answer;
import nars.core.NAR;
import nars.event.Reaction;
import nars.logic.entity.Concept;
import nars.logic.entity.Sentence;
import nars.logic.entity.Task;
import reactor.event.registry.Registration;

/**
 *
 * @author me
 */
public abstract class Answered implements Reaction {
    
    private Task question;
    private NAR nar;
    
    final static Class[] events = new Class[] { Answer.class
 };
    private Registration answering;
    
    public void start(Task question, NAR n) {
        this.nar = n;
        this.question = question;
                
        answering = nar.memory.event.on(Answer.class, this);
        
        reportExistingSolutions();
    }
    
    public void off() {
        answering.cancel();
    }

    protected void reportExistingSolutions() {
        Concept c = nar.memory.concept( question.getTerm() );
        if (c == null) return;        
        
        for (Sentence s : c.beliefsEternal)
            onSolution(s);
        for (Sentence s : c.beliefsTemporal)
            onSolution(s);

    }
    
    @Override
    public void event(Class event, Object[] args) {                
        
        if (event == Answer.class) {
            Task task = (Task)args[0];
            Sentence belief = (Sentence)args[1];
            if (task.equals(question)) {
                onSolution(belief);
            }
            else if (task.hasParent(question)) {
                onChildSolution(task, belief);
            }
        }
    }
    
    /** called when the question task has been solved directly */
    abstract public void onSolution(Sentence belief);
    
    /** called when a subtask of the question has been solved */
    abstract public void onChildSolution(Task child, Sentence belief);
    
    
}
