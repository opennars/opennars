/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.qa;

import nars.Events.Answer;
import nars.NAR;
import nars.concept.Concept;
import nars.event.NARReaction;
import nars.narsese.InvalidInputException;
import nars.task.Sentence;
import nars.task.Task;

/**
 *
 * @author me
 */
public abstract class AnswerReaction extends NARReaction {
    
    private final Task question;
    private final NAR nar;
    


    /** reacts to all questions */
    public AnswerReaction(NAR n) {
        this(n, (Task)null);
    }

    public AnswerReaction(NAR n, String questionTask) throws InvalidInputException {
        this(n, n.ask(questionTask));
    }

    /** reacts to a specific question */
    public AnswerReaction(NAR n, Task question) {
        super(n, Answer.class);

        this.nar = n;
        this.question = question;
        reportAnyExistingSolutions();
    }


    protected void reportAnyExistingSolutions() {
        if (question != null) {
            Concept c = nar.memory.concept(question.getTerm());
            if (c == null) return;

            onSolution(c.getBeliefs().top());
            /*
            for (Task s : c.getBeliefs())
                onSolution(s);
                */
        }

    }
    
    @Override
    public void event(Class event, Object[] args) {                
        
        if (event == Answer.class) {
            Task questionTask = (Task)args[1];
            Task belief = ((Task)args[0]);

            if ((question == null) || questionTask.equals(question)) {
                onSolution(belief);
            }
            else if (questionTask.hasParent(question)) {
                onChildSolution(questionTask, belief);
            }
        }
    }
    
    /** called when the question task has been solved directly */
    abstract public void onSolution(Task belief);
    
    /** called when a subtask of the question has been solved */
    public void onChildSolution(Task question, Sentence belief) {

    }
    
    
}
