package nars.rl.horde.functions;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.LinkedHashMap;
import java.util.Map;


public class TabularAction<A> implements StateToStateAction<A>, Cloneable {
    private final A[] actions;
    private final int stateVectorSize;
    private RealVector nullVector;
    private final double vectorNorm;
    private boolean includeActiveFeature = false;
    private RealVector buffer;
    private final Map<A, Integer> actionToIndex;

    public TabularAction(A[] actions, double vectorNorm, int vectorSize) {
        this.actions = actions;
        this.vectorNorm = vectorNorm + 1;
        this.stateVectorSize = vectorSize;
        this.nullVector = new ArrayRealVector(vectorSize());
        actionToIndex = createActionIntMap(actions);
    }

    static public <A> Map<A, Integer> createActionIntMap(A[] actions) {
        Map<A, Integer> actionToIndex = new LinkedHashMap(actions.length);
        for (int i = 0; i < actions.length; i++)
            actionToIndex.put(actions[i], i);
        return actionToIndex;
    }

    protected int atoi(A a) {
        return actionToIndex.get(a);
    }

    public void includeActiveFeature() {
        includeActiveFeature = true;
        this.nullVector = new ArrayRealVector(vectorSize());
    }

    @Override
    public int vectorSize() {
        int result = stateVectorSize * actions.length;
        if (includeActiveFeature)
            result += 1;
        return result;
    }

    @Override
    public RealVector stateAction(RealVector s, A a) {
        if (s == null)
            return nullVector;
        if (buffer == null)
            buffer = new ArrayRealVector(vectorSize());
        int offset = atoi(a) * stateVectorSize;
//        if (s instanceof BinaryVector)
//            return stateAction(s, offset);
        RealVector phi_sa = buffer;
        phi_sa.set(0.0);
        if (includeActiveFeature)
            phi_sa.setEntry(vectorSize() - 1, 1);
        for (int s_i = 0; s_i < s.getDimension(); s_i++)
            phi_sa.setEntry(s_i + offset, s.getEntry(s_i));
        return phi_sa;
    }

    private RealVector stateAction(ArrayRealVector s, int offset) {
        ArrayRealVector phi_sa = (ArrayRealVector) buffer;
        phi_sa.set(0.0);
        phi_sa.setSubVector(offset, s);
        if (includeActiveFeature)
            phi_sa.setEntry(phi_sa.getDimension() - 1, 1);
        return phi_sa;
    }

    public A[] actions() {
        return actions;
    }

    @Override
    public double vectorNorm() {
        return vectorNorm;
    }

    @Override
    public StateToStateAction clone() throws CloneNotSupportedException {
        return new TabularAction(actions, vectorNorm - 1, stateVectorSize);
    }
}
