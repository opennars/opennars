package nars.util.meter;

import nars.Global;
import nars.NAR;
import nars.util.event.Active;
import nars.util.event.Topic;
import nars.util.meter.event.HitMeter;

import java.util.Map;

/**
* Created by me on 2/10/15.
*/
public class EventCount {

    public final Map<Object, HitMeter> eventMeters;
    private Active sub;

    public EventCount(NAR n) {

        Map<Object, HitMeter> eventMeters
                = this.eventMeters = Global.newHashMap();

        Topic.each(n.memory, (field) -> {
            String nn = field.getName();
            eventMeters.put(nn, new HitMeter(nn));
        });

        sub = Topic.all(n.memory, (event, value) ->
            eventMeters.get(event).hit());
    }

    public void off() {
        if (sub!=null) {
            sub.off();
            sub = null;
        }
    }


    public long numTaskProcesses() {
        return eventMeters.get("eventTaskProcess").count(); }
    public long numOutputs() { return eventMeters.get("eventDerived").count(); }
    public long numInputs() { return eventMeters.get("eventInput").count(); }
    public long numExecutions() { return eventMeters.get("eventExecute").count(); }
    public long numErrors() { return eventMeters.get("eventError").count(); }
    public long numAnswers() { return eventMeters.get("eventAnswer").count(); }


    public void reset() {
        eventMeters.values().forEach(HitMeter::reset);
    }
}
