/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.io.qa;

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
        this(n, n.ask(questionTask));
    }

    /** reacts to a specific question */
    public AnswerReaction(NAR n, Task question) {

        reg = n.memory.eventAnswer.on(this);

        this.nar = n;
        this.question = question;
        reportAnyExistingSolutions();
    }

    public synchronized void off() {
        if (reg!=null) {
            reg.off();
            reg = null;
        }
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
