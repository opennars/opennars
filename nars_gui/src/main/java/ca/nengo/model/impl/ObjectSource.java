package ca.nengo.model.impl;

import ca.nengo.model.Node;
import ca.nengo.model.Resettable;
import ca.nengo.model.NSource;

/**
 * Default source implementation which can provide any type of object
 */
public class ObjectSource<V> implements NSource<V>, Resettable {

    public final String name;
    private final Node node;
    V value = null;

    public ObjectSource(Node parent, String name) {
        this.name = name;
        this.node = parent;
    }

    @Override
    public void reset(boolean randomize) {
        value = null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int getDimensions() {
        return 1;
    }

    @Override
    public V get() {
        return value;
    }

    @Override
    public void accept(V val) {
        this.value = val;
    }

    @Override
    public void setRequiredOnCPU(boolean val) {

    }

    @Override
    public boolean getRequiredOnCPU() {
        return false;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public NSource clone() throws CloneNotSupportedException {
        return null;
    }

    @Override
    public NSource clone(Node node) throws CloneNotSupportedException {
        return null;
    }
}
