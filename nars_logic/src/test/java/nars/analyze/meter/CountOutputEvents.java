package nars.analyze.meter;

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

    public CountOutputEvents(NAR n, Metrics m) {
        super(n, ev);

        for (Class c : getEvents()) {
            HitMeter h = new HitMeter(c.getSimpleName());
            eventMeters.put(c, h);
            m.addMeter(h);
        }

    }

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
