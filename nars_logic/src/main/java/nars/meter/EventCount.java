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
            final HitMeter h = eventMeters.computeIfAbsent(event, k -> new HitMeter(event));
            h.hit();
        });
    }

    public void off() {
        if (sub!=null) {
            sub.off();
            sub = null;
        }
    }


    public long numOutputs() { return eventMeters.get("eventDerived").count(); }
    public long numInputs() { return eventMeters.get("eventInput").count(); }
    public long numExecutions() { return eventMeters.get("eventExecute").count(); }
    public long numErrors() { return eventMeters.get("eventError").count(); }
    public long numAnswers() { return eventMeters.get("eventAnswer").count(); }


    public void reset() {
        eventMeters.values().forEach(HitMeter::reset);
    }
}
