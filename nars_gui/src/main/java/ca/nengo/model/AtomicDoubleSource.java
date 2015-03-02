package ca.nengo.model;

import ca.nengo.model.impl.ObjectSource;
import reactor.jarjar.jsr166e.extra.AtomicDouble;


public class AtomicDoubleSource extends ObjectSource<Double> {

    private final AtomicDouble val;

    public AtomicDoubleSource(Node parent, String name, AtomicDouble d) {
        super(parent, name);
        this.val = d;
    }

    @Override
    public Double get() {
        return val.doubleValue();
    }
}
