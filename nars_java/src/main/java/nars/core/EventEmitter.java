
package nars.core;

import reactor.core.Environment;
import reactor.event.Event;
import reactor.event.dispatch.Dispatcher;
import reactor.event.registry.Registration;
import reactor.event.selector.Selector;
import reactor.event.selector.Selectors;

import java.util.ArrayList;

import static reactor.event.selector.Selectors.T;

/**
 * Adapted from http://www.recursiverobot.com/post/86215392884/witness-a-simple-android-and-java-event-emitter
 * TODO separate this into a single-thread and multithread implementation
 */
public class EventEmitter extends Eventer<Object> {

    
    
    /** Observes events emitted by EventEmitter */
    public interface EventObserver<C> {
        public void event(Class<? extends C> event, Object[] args);
    }
                
    public EventEmitter(String type) {
        super(type);
    }

    public EventEmitter(Environment e, Dispatcher d) {
        super(e, d);
    }
    
    

    public final boolean isActive(final Class event) {
        return r.getConsumerRegistry().select(T(event)).isEmpty();
    }

    public Registration on(Class<?> channel, EventEmitter.EventObserver obs) {
        return on(Selectors.T(channel), obs);
    }
    
    public Registration on(Selector s, EventEmitter.EventObserver obs) {
        return on(s,  event -> {

            Class channel = (Class)(((Event)event).getKey());
            Object o = ((Event)event).getData();
            
            if (o instanceof Object[]) {
                obs.event(channel, (Object[]) o);
            } else {
                obs.event(channel, new Object[]{o});
            }
        });
    }

    /** for enabling many events at the same time */
    @Deprecated public void set(final EventObserver o, final boolean enable, final Class... events) {
        
        for (final Class c : events) {
            if (enable)
                on(c, o);
            else
                off(c, o);
        }
    }

    public static class Registrations extends ArrayList<Registration> {

        Registrations(int length) {
            super(length);
        }

        public void resume() {
            for (Registration r : this)                
                r.resume();            
        }
        public void pause() {
            for (Registration r : this)                
                r.pause();            
        }
        public void cancel() {
            for (Registration r : this)
                r.cancel();            
        }
        
        public void cancelAfterUse() {
            for (Registration r : this)
                r.cancelAfterUse();
        }        
    }
    
    public Registrations on(final EventObserver o, final Class... events) {        
        Registrations r = new Registrations(events.length);
    
        for (final Class c : events)            
            r.add( on(c, o) );
        
        return r;
    }
    
    @Override
    @Deprecated public void emit(Class channel, Object arg) {
        
        if (!(arg instanceof Object[]))
            super.emit(channel, new Object[] { arg });
        else
            super.emit(channel, arg);
    }

    
    public void emit(Class channel, Object... args) {
        emit(channel, (Object)args);
    }

    
            
 
    /**
     * @param event
     * @param o
     * @return  whether it was removed
     */
    public <C> void off(final Class<? extends C> event, final EventObserver<? extends C> o) {
        throw new RuntimeException("off() not supported; use the returned Registration object to .cancel()");
    }



}