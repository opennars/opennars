package nars.nal;


public abstract class ControlSensor {

    public final NumericRange range;
    public final int quantization;

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public ControlSensor(int quantization) {
        range = new NumericRange();
        this.quantization = quantization;
    }

    @SuppressWarnings("ConstructorNotProtectedInAbstractClass")
    public ControlSensor(double min, double max, int quantization) {
        range = new NumericRange((min + max) / 2, (max - min) / 2);
        this.quantization = quantization;
    }
    //called each cycle
    //called during learning cycle to get the value
    /** returns next index */

    //called each cycle
    public void update() { }

    //called during learning cycle to get the value
    public abstract double get();

    /** returns next index */
    public int vectorize(double[] d, int index) {
        range.vectorizeSmooth(d, index, get(), quantization);
        return quantization;
    }

    public void adaptContrast(double rate, double center) {
        range.adaptiveContrast(rate, center);
    }
}
