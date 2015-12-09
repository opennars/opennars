/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package objenome.op;

/**
 *
 * @author thorsten
 */
public class Scalar extends Literal<Double> implements DiffableFunction {

    private double upperBound = Double.POSITIVE_INFINITY;
    private double lowerBound = Double.NEGATIVE_INFINITY;
    private String name = "";

    public Scalar(double value, String name) {
        super(value);
        this.name = name;
    }

    @Override
    public double value() {
        return value;
    }

    @Override
    public double partialDerive(Scalar parameter) {
        return this == parameter ? 1 : 0;
    }


    public void setValue(double v) {
        if (v < lowerBound)
            value = lowerBound;
        else if (v > upperBound)
            value = upperBound;
        else
            value = v;
    }

    public void setUpperBound(double x) {
        upperBound = x;
    }

    public void setLowerBound(double x) {
        lowerBound = x;
    }

    public String getName() {
        return name;
    }
}
