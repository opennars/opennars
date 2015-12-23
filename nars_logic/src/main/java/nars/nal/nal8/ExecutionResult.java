package nars.nal.nal8;

import nars.task.Task;
import nars.task.Tasked;

import java.io.Serializable;

/**
 * Holds an operation task and any generated feedback
 */
public class ExecutionResult implements Serializable, Tasked {

    public final Task operation;
    public final Object feedback;

    public ExecutionResult(Task op, Object feedback) {
        operation = op;
        this.feedback = feedback;
    }

    @Override
    public Task getTask() {
        return operation;
    }

    @Override
    public String toString() {
        Task t = operation;
        //if (t == null) return "";

        /*if (operation instanceof ImmediateOperation) {
            return operation.toString();
        } else */
        //Term[] args = operation.argArray();
        //Term operator = operation.getOperator();
        StringBuilder sb = new StringBuilder();

        t.appendTo(sb, null);

//                Budget b = getTask();
//                if (b!=null)
//                    sb.append(b.toStringExternal()).append(' ');

        //sb.append(operator).append('(');

            /*
            if (args.length > 0) {
                String argString = Arrays.toString(args);
                sb.append(argString.substring(1, argString.length()-1)); //remove '[' and ']'
            }
            */

        //sb.append(')');
        sb.append(' ');

        if (feedback != null)
            sb.append(feedback);
        else
            sb.append("void");

        return sb.toString();
    }


}
