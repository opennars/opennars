package nars.io.meter;

import java.util.HashMap;
import java.util.Map;
import nars.core.Memory;
import nars.io.meter.AbstractMeter.UnknownSensorException;
import nars.util.meter.Meter;
import nars.util.meter.data.DefaultDataSet;
import nars.util.meter.sensor.AbstractSensor;
import nars.util.meter.sensor.AbstractSpanTracker;
import nars.util.meter.sensor.EventMeter;
import nars.util.meter.sensor.SpanTracker;


abstract public class AbstractMeter extends DefaultDataSet {

    final Map<String, Meter> sensors = new HashMap<>();
    long lastUpdate = -1;
    int allSensorResetPeriodCycles = -1; //how often to reset all sensors, or -1 to disable
    boolean active = false;

    public AbstractMeter(Map<String,Object> map) {
        super(map);
        setActive(false); //default, set inactive
    }

    public AbstractMeter() {
        this(new HashMap<>());
    }

    /** samples the data */
    abstract public void sense(Memory memory);
    
    protected void add(Meter s) {
        sensors.put(s.name(), s);
        s.setActive(active);
    }

    public SpanTracker getSensorSpan(final String name) {
        Meter s = sensors.get(name);
        if (s instanceof SpanTracker) {
            return (SpanTracker) s;
        }
        return null;
    }

    public EventMeter getSensorEvent(final String name) {
        Meter s = sensors.get(name);
        if (s instanceof EventMeter) {
            return (EventMeter) s;
        }
        return null;
    }

    protected void updateSensors(final boolean reset, long cyclesSinceLastUpdate) {
        for (final Meter s : sensors.values()) {
            
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
        final EventMeter e = getSensorEvent(name);
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
        if (!active)
            return;
        
        long time = memory.time();
        if (time == lastUpdate) {
            //already updated
            return;
        }
        
        sense(memory);

        long timeSinceLastUpdate = time - lastUpdate;
        
        lastUpdate = time;                     
        
        updateSensors( 
                (allSensorResetPeriodCycles!=-1) && (time % allSensorResetPeriodCycles == 0), 
                timeSinceLastUpdate);

        return;
    }
    

     
    public static class UnknownSensorException extends RuntimeException {

        public UnknownSensorException(final String name) {
            super("LogicState: Unknown EventSensor " + name);
        }    
        
    }
    
    public void setActive(final boolean b) {
        this.active = b;
        for (final Meter s : sensors.values())
            s.setActive(b);
    }
    
}
