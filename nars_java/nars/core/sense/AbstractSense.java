package nars.core.sense;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import nars.core.Memory;
import nars.core.sense.AbstractSense.UnknownSensorException;
import nars.util.meter.Sensor;
import nars.util.meter.data.DefaultDataSet;
import nars.util.meter.sensor.AbstractSpanTracker;
import nars.util.meter.sensor.AbstractSensor;
import nars.util.meter.sensor.EventSensor;
import nars.util.meter.sensor.SpanTracker;


abstract public class AbstractSense extends DefaultDataSet {

    final Map<String, Sensor> sensors = new HashMap<String, Sensor>();
    long lastUpdate = -1;
    int allSensorResetPeriodCycles = 2048; //how often to reset all sensors

    public AbstractSense() {
        super(new TreeMap<>());
    }

    abstract public void sense(Memory memory);
    
    protected void add(Sensor s) {
        sensors.put(s.name(), s);
    }

    public SpanTracker getSensorSpan(final String name) {
        Sensor s = sensors.get(name);
        if (s instanceof SpanTracker) {
            return (SpanTracker) s;
        }
        return null;
    }

    public EventSensor getSensorEvent(final String name) {
        Sensor s = sensors.get(name);
        if (s instanceof EventSensor) {
            return (EventSensor) s;
        }
        return null;
    }

    public void updateSensors(final boolean reset, long cyclesSinceLastUpdate) {
        for (final Sensor s : sensors.values()) {
            
            s.setCyclesSinceLastUpdate(cyclesSinceLastUpdate);
            
            if (reset) {
                if (s instanceof AbstractSpanTracker) {
                    if (((AbstractSensor) s).getSampleWindow() > 0) {
                        continue;
                    }
                }
                s.getSession().drainData();
            }
        }
    }

    public void incident(final String name) {
        final EventSensor e = getSensorEvent(name);
        if (e == null) {
            throw new UnknownSensorException(name);
        }
        e.event();
    }

    public void print() {
        for (Map.Entry<String, Object> a : this.dataMap.entrySet()) {
            System.out.println(a.getKey() + " = " + a.getValue());
        }
        System.out.println();
    }
    
    
    /** returns the same instance */
    public void update(final Memory memory) {
        long time = memory.getTime();
        if (time == lastUpdate) {
            //already updated
            return;
        }
        
        sense(memory);

        long timeSinceLastUpdate = time - lastUpdate;
        
        lastUpdate = time;                     
        
        updateSensors((time % allSensorResetPeriodCycles == 0), timeSinceLastUpdate);

        return;
    }
    

     
    public static class UnknownSensorException extends RuntimeException {

        public UnknownSensorException(final String name) {
            super("LogicState: Unknown EventSensor " + name);
        }    
        
    }
    
    
}
