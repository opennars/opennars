package nars.event.exe;

import nars.event.AbstractExecutive;
import nars.logic.entity.Concept;
import nars.logic.entity.Task;
import nars.logic.entity.Term;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;


public class DesireThresholdExecutive extends AbstractExecutive {



    @Override
    protected boolean decide(Concept c, Task executableTask) {
        Term term = executableTask.getTerm();
        if ((term instanceof Operation) && (c.isDesired())) {

            Operation op = (Operation)term;
            op.setTask(executableTask);

            Operator oper = op.getOperator();

            return oper.execute(op, c.memory);
        }

        return false;

        //emit(Events.UnexecutableGoal.class, t, this, nal);
    }


}
