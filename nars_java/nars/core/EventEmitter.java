
package nars.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adapted from http://www.recursiverobot.com/post/86215392884/witness-a-simple-android-and-java-event-emitter
 */
public class EventEmitter {
    
    /** Observes events emitted by EventEmitter */
    public interface Observer<C> {
        public void event(Class<? extends C> event, Object[] arguments);
    }

    private final Map<Class<?>, List<Observer>> events;
            

    /** EventEmitter that allows unknown events; must use concurrent collection
     *  for multithreading since new event classes may be added at any time.
     */
    public EventEmitter() {
        events = new ConcurrentHashMap<Class<?>, List<Observer>>();
    }

    /** EventEmitter with a fixed set of known events; the 'events' map
     *  can then be made unmodifiable and non-concurrent for speed.    */
    public EventEmitter(Class... eventClasses) {
        events = new HashMap(eventClasses.length);
        for (Class c : eventClasses) {
            events.put(c, newObserverList());
        }
    }

    protected List<Observer> newObserverList() {
        return new CopyOnWriteArrayList<Observer>();
    }
    
    public boolean hasAnyOn(final Class event) {
        if (events.get(event)!=null)
            if (events.get(event).size() > 0)
                return true;
        return false;
    }
    
    public <C> void on(final Class<? extends C> event, final Observer<? extends C> o) {
        if (null == event || null == o)
            return;
         
        if (events.containsKey(event))
            events.get(event).add(o);
        else {
            List<Observer> a = newObserverList();
            a.add(o);
            events.put(event, a);
        }
                
    }
 
    public void off(final Class<?> event, final Observer o) {
        if (null == event || null == o)
            throw new RuntimeException("Invalid parameter");
 
        if (!events.containsKey(event))
            throw new RuntimeException("Unknown event: " + event);
        
        boolean removed = events.get(event).remove(o);
        if (!removed) {
            throw new RuntimeException("Observer " + o + " was not registered for events");
        }
    }
 

    public void emit(final Class eventClass, final Object... params) {
        List<Observer> observers = events.get(eventClass);
        
        if ((observers == null) || (observers.isEmpty())) return;

        int n = observers.size();        
        for (int i = 0; i < n; i++) {
            final Observer m = observers.get(i);
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