package nars.nal.nal8;

import nars.nal.Task;
import nars.nal.term.Term;

/**
 * Created by me on 5/16/15.
 */
public class ExecutionResult {
    public final Operation operation;
    public final Object feedback;

    public ExecutionResult(Operation op, Object feedback) {
        this.operation = op;
        this.feedback = feedback;
    }

    public Task getTask() {
        return operation.getTask();
    }

    public Operation getOperation() {
        return operation;
    }

    @Override
    public String toString() {
        if (operation instanceof ImmediateOperation) {
            return operation.toString();
        } else {
            Term[] args = operation.getArgumentsRaw();
            Operator operator = operation.getOperator();
            StringBuilder sb = new StringBuilder();

            getTask().appendToString(sb, operator.getMemory());

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

            if (feedback != null)
                sb.append("  ").append(feedback);

            return sb.toString();
        }
    }


}
