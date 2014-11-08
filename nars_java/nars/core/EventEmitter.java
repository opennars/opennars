
package nars.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapted from http://www.recursiverobot.com/post/86215392884/witness-a-simple-android-and-java-event-emitter
 * TODO separate this into a single-thread and multithread implementation
 */
public class EventEmitter {

    
    /** Observes events emitted by EventEmitter */
    public interface Observer<C> {
        public void event(Class<? extends C> event, Object[] args);
    }

    private final Map<Class<?>, List<Observer>> events;
            
    
    private Deque<Object[]> pendingOps = new ArrayDeque();
    
    /** EventEmitter that allows unknown events; must use concurrent collection
     *  for multithreading since new event classes may be added at any time.
     */
    public EventEmitter() {
        /*if (Parameters.THREADS > 1)
            events = new ConcurrentHashMap<>();
        else*/
            events = new HashMap<>();
    }

    /** EventEmitter with a fixed set of known events; the 'events' map
     *  can then be made unmodifiable and non-concurrent for speed.    */
    public EventEmitter(Class... knownEventClasses) {
        events = new HashMap(knownEventClasses.length);
        for (Class c : knownEventClasses) {
            events.put(c, newObserverList());
        }
    }

    protected List<Observer> newObserverList() {
        return new ArrayList();
        /*return Parameters.THREADS == 1 ? 
                new ArrayList() : Collections.synchronizedList(new ArrayList());*/
    }
    
    public final boolean isActive(final Class event) {
        if (events.get(event)!=null)
            if (!events.get(event).isEmpty())
                return true;
        return false;
    }
    
    //apply pending on/off changes when synchronizing, ex: in-between memory cycles
    public void synch() {
        synchronized (pendingOps) {
            if (!pendingOps.isEmpty()) {
                for (Object[] o : pendingOps) {
                    Class c = (Class)o[1];
                    Observer d = (Observer)o[2];
                    if ((Boolean)o[0]) {                        
                        _on(c,d);
                    }
                    else {                        
                        _off(c,d);
                    }
                }
            }
            pendingOps.clear();
        }
    }
    
    public <C> void on(final Class<? extends C> event, final Observer<? extends C> o) {
        if (Parameters.THREADS == 1) {
            _on(event, o);
        }
        else {
            synchronized(pendingOps) {
                pendingOps.add(new Object[] { true, event, o });        
            }
        }
    }
            
    private <C> void _on(final Class<? extends C> event, final Observer<? extends C> o) {
        if (events.containsKey(event))
            events.get(event).add(o);
        else {
            List<Observer> a = newObserverList();
            a.add(o);
            events.put(event, a);
        }
                
    }
 
    /**
     * @param event
     * @param o
     * @return  whether it was removed
     */
    public <C> void off(final Class<? extends C> event, final Observer<? extends C> o) {
        if (Parameters.THREADS == 1) {
            _off(event, o);
        }
        else {
            synchronized(pendingOps) {
                pendingOps.add(new Object[] { false, event, o });        
            }
        }
    }
    
    private void _off(final Class<?> event, final Observer o) {
        if (null == event || null == o)
            throw new RuntimeException("Invalid parameter");
 
        if (!events.containsKey(event))
            throw new RuntimeException("Unknown event: " + event);
        
        events.get(event).remove(o);
        /*if (!removed) {
            throw new RuntimeException("Observer " + o + " was not registered for events");
        }*/        
    }

    /** for enabling many events at the same time */
    public void set(final Observer o, final boolean enable, final Class... events) {
        for (final Class c : events) {
            if (enable)
                on(c, o);
            else
                off(c, o);
        }
    }
    

    public void emit(final Class eventClass, final Object... params) {
        List<Observer> observers = events.get(eventClass);
        
        if ((observers == null) || (observers.isEmpty())) return;

        int n = observers.size();
        for (int i = 0; i < n; i++) {
            Observer m = observers.get(i);
            m.event(eventClass, params);
        }
    }
 
//    public void emitLater(final Class eventClass, final Object... params) {
//        if (hasAnyOn(eventClass)) {
//            Platform.runLater(new Runnable() {
//
//                @Override
//                public void run() {
//                    emit(eventClass, params);
//                }
//                
//            });
//        }
//    }
}