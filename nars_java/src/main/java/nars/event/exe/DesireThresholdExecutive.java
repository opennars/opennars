package nars.event.exe;

import nars.Global;
import nars.event.AbstractExecutive;
import nars.nal.Concept;
import nars.nal.Task;
import nars.nal.nal8.Operation;
import nars.nal.nal8.Operator;
import nars.nal.term.Term;


public class DesireThresholdExecutive extends AbstractExecutive {



    @Override
    protected boolean decide(Concept c, Task task) {
        Term term = task.getTerm();

        if ((term instanceof Operation) && (c.isDesired())) {

            Operation op = (Operation)term;
            op.setTask(task);

            Operator oper = op.getOperator();

            return oper.execute(op, c.memory);
        }

        return false;

        //emit(Events.UnexecutableGoal.class, t, this, nal);
    }


}
