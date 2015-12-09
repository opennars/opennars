package nars.nal;

import nars.NAR;
import nars.util.meter.FunctionMeter;
import nars.util.meter.event.DoubleMeter;


public class EventValueControlSensor extends ControlSensor {

    DoubleMeter e;
    final NAR nar;
    final FunctionMeter<? extends Number> logicSensor;
    final double adaptContrast;

    /*default value if not exist */
    public EventValueControlSensor(NAR n, FunctionMeter logicSensor, int quantization, int sampleWindow, double adaptContrast) {
        super(quantization);
        e = new DoubleMeter("_");
        nar = n;
        this.logicSensor = logicSensor;
        this.adaptContrast = adaptContrast;
    }
    public EventValueControlSensor(NAR n, DoubleMeter signal, int min, int max, int quantization, int sampleWindow) {
        super(min, max, quantization);
        e = new DoubleMeter("_");
        nar = n;
        logicSensor = signal;
        adaptContrast = 0;
    }

    @Override
    public void update() {
        e.set( logicSensor.getValue(null, 0).doubleValue() );
    }

    @Override
    public double get() {
        return e.get();
        //double v = e.signalFirst().get();
//        if (adaptContrast > 0) {
//            adaptContrast(adaptContrast, v);
//        }
        //return Double.NaN;
    }
}
