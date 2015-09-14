package nars.link;

import nars.budget.Budgeted;
import nars.term.Term;
import nars.term.Termed;

import java.io.PrintStream;
import java.io.Serializable;

public interface TLink<T extends Termed> extends Budgeted, Serializable, Termed {

    public short getIndex(final int i);
    
    public Term getTerm();
    
    @Override
    public float getPriority();

    public static void print(TLink t, PrintStream out) {
        out.print(t.toString());
    }

//    /** accessor for getIndex which will throw an exception if the index is not available for use as a figure value (0,1) */
//    default public int getFigureIndex(final int i) {
//        final short v = getIndex(i);
//        if ((v < 0) || (v > 1))
//            throw new RuntimeException("Figure index " + i + " not available in " + this);
//        return v;
//    }
}
