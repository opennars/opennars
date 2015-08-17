package nars.op.io;

import nars.nal.nal8.ImmediateOperator;
import nars.nal.nal8.Operation;
import nars.task.Task;

/**
 * Input perception command to queue 'stepLater' cycles in Memory
 * TODO wrap as Operator
 */
public class PauseInput extends ImmediateOperator {

    public static final PauseInput the = new PauseInput();

    protected PauseInput() {
        super();
    }

    public static Task make(int cycles) {
        return the.newTask(Integer.toString(cycles));
    }

    @Override
    public void accept(Operation o) {
        int cycles = Integer.parseInt( o.args()[0].toString() );
        o.getMemory().think(cycles);
    }
}
