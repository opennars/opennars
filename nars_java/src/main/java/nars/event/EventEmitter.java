
package nars.event;

import nars.core.Events;
import nars.core.Parameters;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.Selector;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;

import java.util.ArrayList;

/**
 * TODO separate this into a single-thread and multithread implementation
 */
public class EventEmitter<E>  {

    public final Reactor r;

    public EventEmitter() {
        this(Reactors.reactor().synchronousDispatcher().broadcastEventRouting().get());
    }

    public EventEmitter(Reactor r) {
        this.r = r;

        if (Parameters.DEBUG_TRACE_EVENTS) {
            /*
            System.out.println(r);
            System.out.println(r.getDispatcher());
            System.out.println(r.getRouter());
            */

            r.on(Selectors.matchAll(), x -> {
                System.out.println(x + " --> " +
                        r.getConsumerRegistry().select(x.getKey()).size());
            });
        }
        /*
        r.on(T(Throwable.class), t -> {
            Throwable e = (Throwable)t.getData();
            //if (Parameters.DEBUG) {
                if (e.getCause()!=null)
                    e.getCause().printStackTrace();
                else
                    e.printStackTrace();

                //throw new RuntimeException(e);
            //}

            //else {
            //    System.err.println(e);
            }///

            //throw new RuntimeException(e.getCause());

        });*/

    }



    /*public static Eventer newWorkQueue() {
        return new Eventer(Reactors.reactor(new Environment(), Environment.WORK_QUEUE));
    }*/


    /** new Eventer with a dispatcher mode that runs in the same thread */
    public static EventEmitter newSynchronous() {
        return new EventEmitter(Reactors.reactor().env(new Environment()).synchronousDispatcher().firstEventRouting().get());
    }

    public void synch() {
        r.getDispatcher().awaitAndShutdown();
    }

    public void shutdown() {
        r.getDispatcher().shutdown();
    }

    public <X extends Event<?>> Registration on(Selector s, Consumer<X> c) {
        return r.on(s, c);
    }


    /** a pre-allocated event to re-use if it's available */
    Event theEvent = Event.wrap(null);

    public void notify(Class channel, Object arg) {
        final Event e;
//        if ((Parameters.THREADS == 1) && (theEvent != null)) {
//            e = theEvent;
//            theEvent = null;
//            e.setData(arg);
//        }
//        else {
            e = Event.wrap(arg);
        //}

        r.notify(channel, e);

        if (theEvent == null) {
            e.recycle();
            theEvent = e;
        }
    }

    public void notify(Object channel) {
        r.notify(channel);
    }
    public void fire(Object event) {
        r.notify(Event.wrap(event));
    }
    public void fire(Object channel, Event event) {
        r.notify(channel, event);
    }


    public final boolean isActive(final Class event) {
        return !r.getConsumerRegistry().select(event).isEmpty();
    }

    public Registration on(Class<?> channel, Reaction obs) {
        return on(Selectors.T(channel), obs);
    }
    
    public Registration on(Selector s, Reaction obs) {
        return on(s,  new Consumer<Event>() {
            @Override public void accept(Event event) {
                try {
                    Class channel = (Class) (event.getKey());
                    Object o = event.getData();
                    obs.event(channel, (Object[]) o);
                }
                catch (Throwable t) {
                    if (Parameters.DEBUG) {
                        t.printStackTrace();
                    }
                    emit(Events.ERR.class, t);
                }
            }
        });
    }

    /** for enabling many events at the same time */
    @Deprecated public void set(final Reaction o, final boolean enable, final Class... events) {
        
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
    
    public Registrations on(final Reaction o, final Class... events) {
        Registrations r = new Registrations(events.length);
    
        for (final Class c : events)            
            r.add( on(c, o) );
        
        return r;
    }

//
//    @Override
//    @Deprecated public void emit(Class channel, Object arg) {
//
//        if (!(arg instanceof Object[]))
//            super.emit(channel, new Object[] { arg });
//        else
//            super.emit(channel, arg);
//    }

    
    public void emit(Class channel, Object... args) {
        if (args.length == 0)
            notify(channel);
        else
            notify(channel, args);
    }

    
            
 
    /**
     * @param event
     * @param o
     * @return  whether it was removed
     */
    @Deprecated public <C> void off(final Class<? extends C> event, final Reaction<? extends C> o) {
        throw new RuntimeException("off() not supported; use the returned Registration object to .cancel()");
    }



}