/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.event;

import com.gs.collections.api.tuple.Twin;
import nars.NAR;
import nars.concept.Concept;
import nars.narsese.InvalidInputException;
import nars.task.Sentence;
import nars.task.Task;
import nars.util.event.On;

import java.util.function.Consumer;

/**
 *
 * @author me
 */
public abstract class AnswerReaction implements Consumer<Twin<Task>> {
    
    private final Task question;
    private final NAR nar;
    private On reg;


    /** reacts to all questions */
    public AnswerReaction(NAR n) {
        this(n, (Task)null);
    }


    public AnswerReaction(NAR n, String questionTask) throws InvalidInputException {
        this(n, n.task(questionTask));
    }

    /** reacts to a specific question */
    public AnswerReaction(NAR n, Task question) {

        this.nar = n;
        this.question = question;

        reg = n.memory.eventAnswer.on(this);

        if (question!=null) {
            reportAnyExistingSolutions();
            n.input(question);
        }
    }

    public synchronized void off() {
        if (reg!=null) {
            reg.off();
            reg = null;
        }
    }

    protected boolean reportAnyExistingSolutions() {
        Concept c = nar.memory.concept(question.getTerm());
        if (c == null) return false;

        Task top = c.getBeliefs().top();
        if (top!=null) {
            onSolution(top);
            /*
            for (Task s : c.getBeliefs())
                onSolution(s);
                */

            return true;
        }
        return false;
    }

    @Override
    public void accept(Twin<Task> taskTwin) {
        Task questionTask = taskTwin.getOne();
        Task belief = taskTwin.getTwo();

        if ((question == null) || questionTask.equals(question)) {
            onSolution(belief);
        }
        else if (questionTask.hasParent(question)) {
            onChildSolution(questionTask, belief);
        }

    }


    /** called when the question task has been solved directly */
    abstract public void onSolution(Task belief);
    
    /** called when a subtask of the question has been solved */
    public void onChildSolution(Task question, Sentence belief) {

    }
    
    
}
