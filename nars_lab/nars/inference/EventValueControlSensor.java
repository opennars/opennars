package nars.inference;

import nars.core.NAR;
import nars.util.meter.sensor.EventValueSensor;


public class EventValueControlSensor extends ControlSensor {

    EventValueSensor e;
    final NAR nar;
    final String logicSensor;
    final double adaptContrast;

    /*default value if not exist */
    public EventValueControlSensor(NAR n, String logicSensor, int quantization, int sampleWindow, double adaptContrast) {
        super(quantization);
        e = new EventValueSensor("_", true, sampleWindow);
        this.nar = n;
        this.logicSensor = logicSensor;
        this.adaptContrast = adaptContrast;
    }

    @Override
    public void update() {
        e.commit(nar.memory.logic.d(logicSensor, 0 /*default value if not exist */ ));
    }

    @Override
    public double get() {
        double v = e.get().mean();
        if (adaptContrast > 0) {
            adaptContrast(adaptContrast, v);
        }
        return v;
    }
}
