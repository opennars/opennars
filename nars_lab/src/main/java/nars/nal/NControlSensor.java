package nars.nal;

public class NControlSensor extends ControlSensor {

    final Number a;

    public NControlSensor(Number a, int quantization) {
        super(quantization);
        this.a = a;
        range.set(a.doubleValue());
    }

    public NControlSensor(Number a, double min, double max, int quantization) {
        super(min, max, quantization);
        this.a = a;
    }

    public NControlSensor(Number a, double radius, int quantization) {
        this(a, a.doubleValue() - radius, a.doubleValue() + radius, quantization);
    }

    @SuppressWarnings("RedundantMethodOverride")
    @Override
    public void update() {
    }

    @Override
    public double get() {
        double aa = a.doubleValue();
        range.set(aa);
        return aa;
    }
}
