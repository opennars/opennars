package nars.io.numeric;

import com.gs.collections.impl.map.mutable.primitive.LongDoubleHashMap;
import nars.*;
import nars.nal.nal8.Operation;
import nars.nal.nal8.operator.NullOperator;
import nars.nar.experimental.Equalized;
import nars.task.Task;
import nars.term.Term;
import org.apache.commons.math3.util.Precision;

import java.util.List;
import java.util.Map;

/**
 * Created by me on 8/14/15.
 */
public class NumberPerception extends NullOperator {

    final double epsilon = 0.001;
    int windowHistory = 16;

    final Map<Term, LongDoubleHashMap> data = Global.newHashMap();

    public NumberPerception() {
        super("observe");
    }

    @Override
    protected List<Task> execute(Operation o, Memory memory) {
        Term[] x = o.getArgs();
        if (x.length == 2) {
            Term variable = x[0];
            Term value = x[1];
            double n = getValue(value);
            if (!Double.isFinite(n)) {
                observe(variable, n);
            }
            else {
                memory.emit(Events.ERR.class, "Invalid number for observation: " + o);
            }
        }
        return null;
    }

    private void observe(Term variable, final double n) {
        final LongDoubleHashMap h = data.computeIfAbsent(variable, k -> new LongDoubleHashMap());
        final long now = getMemory().time();
        h.updateValue(now, n, (p) -> {
            if (!Precision.equals(p, n, epsilon)) {
                onRevised(variable, now, p, n);
            }
            return n;
        });

        onObserved(variable, now, n);
    }

    private void onObserved(Term variable, long now, double value) {

    }

    private void onRevised(Term variable, long now, double prev, double value) {

    }

    private double getValue(Term value) {
        if (value.operator() != Op.ATOM)
            return Double.NaN;
        try {
            return Double.valueOf(value.toStringCompact());
        }
        catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    public static void main(String[] args) {
        Equalized e = new Equalized(1024, 3, 3);
        NAR n = new NAR(e);
        n.on(new NumberPerception());

        n.input("observe(x,0)!");
        n.frame();
        n.input("observe(x,1)!");
        n.frame();
        n.input("observe(x,0.5)!");
        n.frame();

    }

    @Override
    protected void noticeExecuted(Operation operation, Memory memory) {
        //no reaction
        operation.getTask().delete();
    }




}
