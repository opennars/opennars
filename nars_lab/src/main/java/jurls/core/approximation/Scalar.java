/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jurls.core.approximation;

/**
 *
 * @author thorsten
 */
//TODO make 2 subclasses of Scalar like I had it originally; one for raw double value and another for index within a parameter array
public class Scalar implements DiffableFunctionSource {

    private double upperBound = Double.POSITIVE_INFINITY;
    private double lowerBound = Double.NEGATIVE_INFINITY;
    private final int index;
    private double value;

    public Scalar() {
        index = -1;
    }
    public Scalar(int index) {
        this.index = index;
    }

    public void setValue(double v) {
        if (index != -1) throw new RuntimeException("Array must be used with " + this);
        value = v;
    }

    public void setValue(double[] xs, double v) {
        if (index == -1) throw new RuntimeException("Array not used with " + this);
        if (v < lowerBound) {
            v = lowerBound;
        } else if (v > upperBound) {
            v = upperBound;
        }

        xs[index] = v;
    }

    public double getValue() {
        if (index != -1) throw new RuntimeException("Array must be used with " + this);
        return value;
    }
    public double getValue(double[] xs) {
        if (index == -1) throw new RuntimeException("Array not used with " + this);
        return xs[index];
    }

    public void setUpperBound(double x) {
        upperBound = x;
    }

    public void setLowerBound(double x) {
        lowerBound = x;
    }

    @Override
    public String valueToSource(SourceEnvironment se) {
        String y = se.allocateVariable();
        se.assign(y).append("xValues[").append(index).append("];").nl();

        return y;
    }

    @Override
    public String partialDeriveToSource(SourceEnvironment se) {
        String y = se.allocateVariable();

        se.assign(y).append("parameterIndex == ").append(index).append(" ? 1 : 0;").nl();

        return y;
    }

}
