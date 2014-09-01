
package nars.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Adapted from http://www.recursiverobot.com/post/86215392884/witness-a-simple-android-and-java-event-emitter
 */
public class EventEmitter {
    
    public interface Observer<O> {
        public void event(Class event, Object... arguments);
    }

    private final ConcurrentMap<Class<?>, ConcurrentMap<Observer, String>> events
            = new ConcurrentHashMap<Class<?>, ConcurrentMap<Observer, String>>();
 
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
    //private final ExecutorService executorService = new ThreadPoolExecutor(1, 10, 30, TimeUnit.SECONDS, queue);
 

    public boolean hasAnyOn(final Class event) {
        if (events.get(event)!=null)
            if (events.get(event).size() > 0)
                return true;
        return false;
    }
    
    public <C> void on(final Class<? extends C> event, final Observer<? extends C> o) {
        if (null == event || null == o)
            return;
 
        events.putIfAbsent(event, new ConcurrentHashMap<Observer, String>());
        events.get(event).putIfAbsent(o, "");
    }
 
    public void off(final Class<?> event, final Observer o) {
        if (null == event || null == o)
            return;
 
        if (!events.containsKey(event))
            return;
 
        events.get(event).remove(o);
    }
 

    public void emit(final Class eventClass, final Object... params) {
        if (events.get(eventClass)==null) return;
        
        ConcurrentMap<Observer, String> observers = events.get(eventClass);
        
        if (observers.size() == 0) return;
        
        
        if (!events.containsKey(eventClass))
            return;

        for (final Observer m : events.get(eventClass).keySet()) {
            m.event(eventClass, params);

            /*executorService.execute(new Runnable() {
                @Override public void run() {
                    m.event(eventClass, params);
                }
            });*/
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