/* 
 * The MIT License
 *
 * Copyright 2018 The OpenNARS authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.opennars.io.events;

import java.util.*;

/**
 *
 */
// Adapted from http://www.recursiverobot.com/post/86215392884/witness-a-simple-android-and-java-event-emitter
// TODO separate this into a single-thread and multithread implementation
public class EventEmitter {

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


    /** Observes events emitted by EventEmitter */
    public interface EventObserver<C> {
        void event(Class<? extends C> event, Object[] args);
    }
}
