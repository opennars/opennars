package nars.logic.entity;

import nars.logic.Terms.Termable;

import java.io.PrintStream;

public interface TLink<T extends Termable> extends BudgetValue.Budgetable {

    public short getIndex(final int i);
    
    public T getTarget();
    
    public float getPriority();

    public static void print(TLink t, PrintStream out) {
        out.print(t.toString());
        out.print(' ');
        out.print(t.getBudget());

        if (t.getBudget().isNew()) {
            out.print(" (new) ");
        }
        if (t instanceof TaskLink) {
            out.print(((TaskLink)t).getRecords());
        }
    }
}
