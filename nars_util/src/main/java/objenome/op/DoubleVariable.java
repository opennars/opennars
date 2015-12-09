package objenome.op;

/** optimized for math */
public class DoubleVariable extends Variable<Double> {

    protected double value;

    public DoubleVariable(String name) {
        super(name, Double.class);
    }

    public void setValue(Double value) {
        //throw new RuntimeException("use set(double v) to avoid boxing");
        this.value = value;
    }

    public Double getValue() {
        throw new RuntimeException("use get() to avoid boxing");
        //return value;
    }

    public void set(double v) {
        value = v;
    }
    public double get() {
        return value;
    }

    public float getFloat() {
        return (float)get();
    }
}
