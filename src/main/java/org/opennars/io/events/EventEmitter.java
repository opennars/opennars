/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opennars.io.events;

import java.util.*;

/**
 * Adapted from http://www.recursiverobot.com/post/86215392884/witness-a-simple-android-and-java-event-emitter
 * TODO separate this into a single-thread and multithread implementation
 */
public class EventEmitter {

    
    /** Observes events emitted by EventEmitter */
    public interface EventObserver<C> {
        void event(Class<? extends C> event, Object[] args);
    }

    private final Map<Class<?>, List<EventObserver>> events;
            
    
    private final Deque<Object[]> pendingOps = new ArrayDeque();
    
    /** EventEmitter that allows unknown events; must use concurrent collection
     *  for multithreading since new event classes may be added at any time.
     */
    public EventEmitter() {
        /*if (Parameters.THREADS > 1)
            events = new ConcurrentHashMap<>();
        else*/
            //events = new HashMap<>();
        events = new HashMap<>();
    }

    /** EventEmitter with a fixed set of known events; the 'events' map
     *  can then be made unmodifiable and non-concurrent for speed.    */
    public EventEmitter(final Class... knownEventClasses) {
        events = new HashMap(knownEventClasses.length);
        for (final Class c : knownEventClasses) {
            events.put(c, newObserverList());
        }
    }

    protected List<EventObserver> newObserverList() {
        return new ArrayList();
        /*return Parameters.THREADS == 1 ? 
                new ArrayList() : Collections.synchronizedList(new ArrayList());*/
    }
    
    public final boolean isActive(final Class event) {
        if (events.get(event)!=null)
            return !events.get(event).isEmpty();
        return false;
    }
    
    //apply pending on/off changes when synchronizing, ex: in-between memory cycles
    public void synch() {
        synchronized (pendingOps) {
            if (!pendingOps.isEmpty()) {
                for (final Object[] o : pendingOps) {
                    final Class c = (Class)o[1];
                    final EventObserver d = (EventObserver)o[2];
                    if ((Boolean)o[0]) {                        
                        on(c,d);
                    }
                    else {                        
                        off(c,d);
                    }
                }
            }
            pendingOps.clear();
        }
    }  
    public void on(final Class<?> event, final EventObserver o) {
        if (events.containsKey(event))
            events.get(event).add(o);
        else {
            final List<EventObserver> a = newObserverList();
            a.add(o);
            events.put(event, a);
        }       
    }
 
    /**
     * @param event
     * @param o
     * @return  whether it was removed
     */
    public void off(final Class<?> event, final EventObserver o) {
        if (null == event || null == o)
            throw new IllegalStateException("Invalid parameter");
 
        if (!events.containsKey(event))
            throw new IllegalStateException("Unknown event: " + event);

        events.get(event).remove(o);
        /*if (!removed) {
            throw new IllegalStateException("EventObserver " + o + " was not registered for events");
        }*/        
    }

    /** for enabling many events at the same time */
    public void set(final EventObserver o, final boolean enable, final Class... events) {
        for (final Class c : events) {
            if (enable)
                on(c, o);
            else
                off(c, o);
        }
    }
    

    public void emit(final Class eventClass, final Object... params) {
        final List<EventObserver> observers = events.get(eventClass);
        
        if ((observers == null) || (observers.isEmpty())) return;

        final int n = observers.size();
        for (final EventObserver m : observers) {
            m.event(eventClass, params);
        }
        
    }
}
