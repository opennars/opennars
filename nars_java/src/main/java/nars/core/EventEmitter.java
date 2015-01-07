
package nars.core;

import reactor.event.Event;
import reactor.event.dispatch.Dispatcher;
import reactor.event.registry.Registration;
import reactor.event.selector.Selectors;
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

    public EventEmitter(Dispatcher d) {
        super(d);
    }
    
    

    public final boolean isActive(final Class event) {
        return r.getConsumerRegistry().select(T(event)).isEmpty();
    }

    public Registration on(Class<?> channel, EventEmitter.EventObserver obs) {
        return on(Selectors.T(channel),  event -> {
            
            Object o = ((Event)event).getData();
            
            if (o instanceof Object[]) {
                obs.event(channel, (Object[]) o);
            } else {
                obs.event(channel, new Object[]{o});
            }
        });
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