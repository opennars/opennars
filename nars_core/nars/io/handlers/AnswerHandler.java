/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.handlers;

import nars.util.EventEmitter.EventObserver;
import nars.util.Events.Answer;
import nars.main.NAR;
import nars.entity.Sentence;
import nars.entity.Task;

/**
 *
 * @author me
 */
public abstract class AnswerHandler implements EventObserver {
    
    private Task question;
    private NAR nar;
    
    final static Class[] events = new Class[] { Answer.class
 };
    
    public void start(Task question, NAR n) {
        this.nar = n;
        this.question = question;
                
        nar.event(this, true, events);
    }
    
    public void off() {
        nar.event(this, false, events);
    }

    @Override
    public void event(Class event, Object[] args) {                
        
        if (event == Answer.class) {
            Task task = (Task)args[0];
            Sentence belief = (Sentence)args[1];
            if (task.equals(question)) {
                onSolution(belief);
            }
        }
    }
    
    /** called when the question task has been solved directly */
    abstract public void onSolution(Sentence belief);
}
