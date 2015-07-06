package nars.meter;

import nars.Events;
import nars.NAR;
import nars.event.NARReaction;
import nars.util.meter.Metrics;
import nars.util.meter.event.HitMeter;

import java.util.HashMap;
import java.util.Map;

/**
* Created by me on 2/10/15.
*/
public class CountOutputEvents extends NARReaction {

//        public static final DoubleMeter numIn = new DoubleMeter("IN");
//        public static final DoubleMeter numOut = new DoubleMeter("OUT");

    final Map<Class, HitMeter> eventMeters = new HashMap();

    public CountOutputEvents(NAR n) {
        this(n, null);
    }

    public CountOutputEvents(NAR n, Metrics m) {
        super(n, ev);

        for (Class c : getEvents()) {
            HitMeter h = new HitMeter(c.getSimpleName());
            eventMeters.put(c, h);
            if (m!=null)
                m.addMeter(h);
        }

    }

    public long numOutputs() { return eventMeters.get(Events.OUT.class).count(); }
    public long numInputs() { return eventMeters.get(Events.IN.class).count(); }
    public long numExecutions() { return eventMeters.get(Events.EXE.class).count(); }
    public long numErrors() { return eventMeters.get(Events.ERR.class).count(); }
    public long numAnswers() { return eventMeters.get(Events.Answer.class).count(); }

    public static final Class[] ev = new Class[] {
            Events.IN.class,
            Events.EXE.class,
            Events.OUT.class,
            Events.ERR.class,
            Events.Answer.class,
    };

    @Override
    public Class[] getEvents() {
        return ev;
    }



    @Override
    public void event(Class event, Object[] args) {

        eventMeters.get(event).hit();
    }

    public void reset() {
        for (HitMeter h : eventMeters.values())
            h.reset();
    }
}
