package objenome.op;

/**
 * Fast double-only literal which stores an unboxed copy of the value for fast math
 */
public class Doubliteral extends Literal<Double> {

    private double v;

    public Doubliteral(double x) {
        super(x);
        v = x;
    }

    @Override
    protected void setValue(Double value) {
        super.setValue(value);
        v = value;
    }

    @Override
    public double asDouble() {
        return v;
    }

    @Override
    public Doubliteral clone() {
        return new Doubliteral(asDouble());
    }
}
