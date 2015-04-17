package nars.nal.tlink;

import nars.budget.Budget;
import nars.nal.Terms.Termable;

import java.io.PrintStream;

public interface TLink<T extends Termable> extends Budget.Budgetable {

    public short getIndex(final int i);
    
    public T getTarget();
    
    public float getPriority();

    public static void print(TLink t, PrintStream out) {
        out.print(t.toString());
        out.print(' ');
        out.print(Budget.toString(t.getBudget()));

        if (t.getBudget().isNew()) {
            out.print(" (new) ");
        }
        if (t instanceof TaskLink) {
            out.print(((TaskLink)t).getRecords());
        }
    }

    /** accessor for getIndex which will throw an exception if the index is not available for use as a figure value (0,1) */
    default public int getFigureIndex(final int i) {
        final short v = getIndex(i);
        if ((v < 0) || (v > 1))
            throw new RuntimeException("Figure index " + i + " not available in " + this);
        return v;
    }
}
