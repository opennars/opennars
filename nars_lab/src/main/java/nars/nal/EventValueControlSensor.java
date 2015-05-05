package nars.nal;

import nars.NAR;
import nars.util.meter.event.DoubleMeter;


public class EventValueControlSensor extends ControlSensor {

    DoubleMeter e;
    final NAR nar;
    final String logicSensor;
    final double adaptContrast;

    /*default value if not exist */
    public EventValueControlSensor(NAR n, String logicSensor, int quantization, int sampleWindow, double adaptContrast) {
        super(quantization);
        e = new DoubleMeter("_");
        this.nar = n;
        this.logicSensor = logicSensor;
        this.adaptContrast = adaptContrast;
    }
    public EventValueControlSensor(NAR n, String logicSensor, int min, int max, int quantization, int sampleWindow) {
        super(min, max, quantization);
        e = new DoubleMeter("_");
        this.nar = n;
        this.logicSensor = logicSensor;
        this.adaptContrast = 0;
    }

    @Override
    public void update() {
        //e.commit(nar.memory.logic.d(logicSensor, 0 /*default value if not exist */ ));
    }

    @Override
    public double get() {
        //double v = e.signalFirst().get();
//        if (adaptContrast > 0) {
//            adaptContrast(adaptContrast, v);
//        }
        return Double.NaN;
    }
}
