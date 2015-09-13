package nars.meter;

import nars.NAR;
import nars.util.event.Topic;
import nars.util.meter.event.HitMeter;

import java.util.HashMap;
import java.util.Map;

/**
* Created by me on 2/10/15.
*/
public class EventCount {

    public final Map<Object, HitMeter> eventMeters = new HashMap();
    private Topic.Registrations sub;

    public EventCount(NAR n) {
        this.sub = Topic.all(n.memory, (event,value) -> {
            final HitMeter h = getHitMeter(event);
            h.hit();
        });
    }

    protected HitMeter getHitMeter(String event) {
        return eventMeters.computeIfAbsent(event, k -> new HitMeter(event));
    }

    public void off() {
        if (sub!=null) {
            sub.off();
            sub = null;
        }
    }


    public long numOutputs() { return getHitMeter("eventDerived").count(); }
    public long numInputs() { return getHitMeter("eventInput").count(); }
    public long numExecutions() { return getHitMeter("eventExecute").count(); }
    public long numErrors() { return getHitMeter("eventError").count(); }
    public long numAnswers() { return getHitMeter("eventAnswer").count(); }


    public void reset() {
        eventMeters.values().forEach(HitMeter::reset);
    }
}
