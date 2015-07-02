/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.qa;

import nars.Events.Answer;
import nars.NAR;
import nars.event.NARReaction;
import nars.narsese.InvalidInputException;
import nars.nal.concept.Concept;
import nars.nal.Sentence;
import nars.nal.Task;

/**
 *
 * @author me
 */
public abstract class Answered extends NARReaction {
    
    private final Task question;
    private final NAR nar;
    


    /** reacts to all questions */
    public Answered(NAR n) {
        this(n, (Task)null);
    }

    public Answered(NAR n, String questionTask) throws InvalidInputException {
        this(n, n.ask(questionTask));
    }

    /** reacts to a specific question */
    public Answered(NAR n, Task question) {
        super(n, Answer.class);

        this.nar = n;
        this.question = question;
        reportExistingSolutions();
    }


    protected void reportExistingSolutions() {
        if (question != null) {
            Concept c = nar.memory.concept(question.getTerm());
            if (c == null) return;

            for (Task s : c.getBeliefs())
                onSolution(s.sentence);
        }

    }
    
    @Override
    public void event(Class event, Object[] args) {                
        
        if (event == Answer.class) {
            Task questionTask = (Task)args[1];
            Sentence belief = ((Task)args[0]).sentence;

            if ((question == null) || questionTask.equals(question)) {
                onSolution(belief);
            }
            else if (questionTask.hasParent(question)) {
                onChildSolution(questionTask, belief);
            }
        }
    }
    
    /** called when the question task has been solved directly */
    abstract public void onSolution(Sentence belief);
    
    /** called when a subtask of the question has been solved */
    public void onChildSolution(Task child, Sentence belief) {

    }
    
    
}
