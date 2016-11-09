/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io;

import nars.util.EventEmitter.EventObserver;
import nars.util.Events.Answer;
import nars.core.NAR;
import nars.entity.Concept;
import nars.entity.Sentence;
import nars.entity.Task;

/**
 *
 * @author me
 */
public abstract class Answered implements EventObserver {
    
    private Task question;
    private NAR nar;
    
    final static Class[] events = new Class[] { Answer.class
 };
    
    public void start(Task question, NAR n) {
        this.nar = n;
        this.question = question;
                
        nar.event(this, true, events);
        
        reportExistingSolutions();
    }
    
    public void off() {
        nar.event(this, false, events);
    }

    protected void reportExistingSolutions() {
        Concept c = (Concept)nar.memory.concept( question.getTerm() );
        if (c == null) return;        
        
        for (Task ts : c.beliefs) {
            onSolution(ts.sentence);
        }
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
