
package nars.util.event;

import objenome.op.cas.E;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * TODO separate this into a single-thread and multithread implementation
 */
abstract public class EventEmitter<K>  {

    public interface EventRegistration {
        public void off();
    }

//    /** more sophisticated event emitter which uses reactor.io */
//    public static class ReactorEventEmitter<E> extends EventEmitter<E> {
//
//
//        public final Reactor r;
//
//        public static class ReactorEventRegistration implements EventRegistration {
//
//            private final Registration registration;
//
//            ReactorEventRegistration(Registration r) {
//                this.registration = r;
//            }
//
//            @Override
//            public void off() {
//                registration.cancel();
//            }
//        }
//
//        public ReactorEventEmitter() {
//            this(Reactors.reactor().synchronousDispatcher().broadcastEventRouting().get());
//        }
//
//        public ReactorEventEmitter(Reactor r) {
//            this.r = r;
//
//            if (Global.DEBUG_TRACE_EVENTS) {
//            /*
//            System.out.println(r);
//            System.out.println(r.getDispatcher());
//            System.out.println(r.getRouter());
//            */
//
//                r.on(Selectors.matchAll(), x -> {
//                    System.out.println(x + " --> " +
//                            r.getConsumerRegistry().select(x.getKey()).size());
//                });
//            }
//        /*
//        r.on(T(Throwable.class), t -> {
//            Throwable e = (Throwable)t.getData();
//            //if (Parameters.DEBUG) {
//                if (e.getCause()!=null)
//                    e.getCause().printStackTrace();
//                else
//                    e.printStackTrace();
//
//                //throw new RuntimeException(e);
//            //}
//
//            //else {
//            //    System.err.println(e);
//            }///
//
//            //throw new RuntimeException(e.getCause());
//
//        });*/
//
//        }
//
//
//
//    /*public static Eventer newWorkQueue() {
//        return new Eventer(Reactors.reactor(new Environment(), Environment.WORK_QUEUE));
//    }*/
//
//        @Override
//        public void cycle() {
//        }
//
//
//
//        /** new Eventer with a dispatcher mode that runs in the same thread */
//        public static ReactorEventEmitter newSynchronous() {
//            return new ReactorEventEmitter(Reactors.reactor().env(new Environment()).synchronousDispatcher().firstEventRouting().get());
//        }
//
//        public void synch() {
//            r.getDispatcher().awaitAndShutdown();
//        }
//
//        public void shutdown() {
//            r.getDispatcher().shutdown();
//        }
//
//        public <X extends Event<?>> Registration on(Selector s, Consumer<X> c) {
//            return r.on(s, c);
//        }
//
//        @Override
//        public void notify(final Class channel, final Object[] arg) {
//            r.notify(channel, Event.wrap(arg));
//        }
//
//        public void notify(Object channel) {
//            r.notify(channel);
//        }
//
//        public void fire(Object event) {
//            r.notify(Event.wrap(event));
//        }
//
//        public void fire(Object channel, Event event) {
//            r.notify(channel, event);
//        }
//
//
//        @Override
//        public final boolean isActive(final Class event) {
//            return !r.getConsumerRegistry().select(event).isEmpty();
//        }
//
//        @Override
//        public EventRegistration on(Class<?> channel, Reaction obs) {
//
//            return new ReactorEventRegistration(on(Selectors.T(channel), obs));
//        }
//
//        Registration on(Selector s, Reaction obs) {
//            return on(s,  new Consumer<Event>() {
//                @Override public void accept(Event event) {
//                    try {
//                        Class channel = (Class) (event.getKey());
//                        Object o = event.getData();
//                        obs.event(channel, (Object[]) o);
//                    }
//                    catch (Throwable t) {
//                        if (Global.DEBUG) {
//                            t.printStackTrace();
//                        }
//                        emit(Events.ERR.class, t);
//                    }
//                }
//            });
//        }
//    }

    /** simple single-thread synchronous (in-thread) event emitter.
     * stores lists of reactions as array for fast iteration
     *
     * NOTE: event reactions will not be available until cycle() is called (ex: at the beginning of each memory cycle)
     * this could be surprising if you expect the handler to be immediately available.
     *
     * TODO investigate if CopyOnWriteArrayList can eliminate the need for the pending addition/removal queues
     * */
    public static class DefaultEventEmitter<K> extends EventEmitter<K> {

        final Map<K,List<Reaction<K>>> reactions = new HashMap(16);


        public class DefaultEventRegistration<K> implements EventRegistration {

            final K key;
            final Reaction reaction;

            DefaultEventRegistration(K key, Reaction o) {
                this.key= key;
                this.reaction = o;
            }

            @Override
            public void off() {
                reactions.get(key).remove(reaction);
            }
        }



        @Override
        public void notify(final K channel, final Object... arg) {
            List<Reaction<K>> c = reactions.get(channel);
            if (c!=null) {
                for (Reaction r : c) {
                    r.event(channel, arg);
                }
            }
        }

        @Override
        public EventRegistration on(K channel, Reaction o) {
            DefaultEventRegistration d = new DefaultEventRegistration(channel, o);
            List<Reaction<K>> cl = reactions.get(channel);
            if (cl == null)
                reactions.put(channel, cl = new CopyOnWriteArrayList());

            cl.add(o);
            return d;
        }

        @Override
        public void delete() {
            reactions.clear();
        }

        @Override
        public boolean isActive(Class event) {
            return reactions.containsKey(event);
        }
    }

    abstract void notify(K channel, Object[] arg);

    abstract public EventRegistration on(K k, Reaction o);

    abstract public boolean isActive(final Class event);


    /** for enabling many events at the same time */
    @Deprecated public void set(final Reaction<E> o, final boolean enable, final K... events) {
        
        for (final K c : events) {
            if (enable)
                on(c, o);
            else
                off(c, o);
        }
    }


    public static class Registrations extends ArrayList<EventRegistration> {

        Registrations(int length) {
            super(length);
        }

//        public void resume() {
//            for (Registration r : this)
//                r.resume();
//        }
//        public void pause() {
//            for (Registration r : this)
//                r.pause();
//        }
//        public void cancelAfterUse() {
//            for (Registration r : this)
//                r.cancelAfterUse();
//        }

        public void off() {
            for (EventRegistration r : this)
                r.off();
        }
        
    }


    
    public Registrations on(final Reaction o, final K... events) {
        Registrations r = new Registrations(events.length);
    
        for (final K c : events)
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


    public void emit(final K channel) {
        notify(channel, null);
    }

    public void emit(final K channel, final Object... args) {
//        if (args.length == 0) {
//            //notify(channel);
//            throw new RuntimeException("event to " + channel + " with zero arguments");
//        }
//        else
            notify(channel, args);
    }

    
            
 
    /**
     * @param event
     * @param o
     * @return  whether it was removed
     */
    @Deprecated public void off(final K event, final Reaction<? extends E> o) {
        throw new RuntimeException("off() not supported; use the returned Registration object to .cancel()");
    }

    public void delete() {

    }


}