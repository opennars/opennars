package ca.nengo.model;

import ca.nengo.model.impl.ObjectTarget;
import com.google.common.util.concurrent.AtomicDouble;

/**
* Created by me on 3/2/15.
*/
public class AtomicDoubleTarget extends ObjectTarget {

    protected final AtomicDouble val;

    public AtomicDoubleTarget(Node parent, String name, AtomicDouble d) {
        super(parent, name, Object.class);
        this.val = d;
    }

    @Override
    public boolean applies(Object value) {
        if (value instanceof Number) return true;
        if (value instanceof RealSource) {
            return ((RealSource)value).getDimension() > 0;
        }
        return false;
    }

    @Override
    public void apply(Object v) throws SimulationException {
        if (v instanceof Number) {
            val.set(((Number)v).doubleValue());
        }
        else if (v instanceof RealSource) {
            val.set(((RealSource)v).getValues()[0]);
        }
    }


}
