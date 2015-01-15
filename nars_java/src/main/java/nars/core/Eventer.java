/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.core;

import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.event.registry.Registration;
import reactor.event.selector.Selector;
import reactor.event.selector.Selectors;
import reactor.function.Consumer;

import static reactor.event.selector.Selectors.T;

/**
 *
 * @author me
 */
public class Eventer<E> {
    public final Reactor r;

    public Eventer(Reactor r) {
        this.r = r;

        if (Parameters.DEBUG_TRACE_EVENTS) {

            System.out.println(r);
            System.out.println(r.getDispatcher());
            System.out.println(r.getEventRouter());

            r.on(Selectors.matchAll(), x -> {
                System.out.println(x + " --> " +
                        r.getConsumerRegistry().select(x.getKey()).size());
            });
        }
        r.on(T(Exception.class), t -> {
            Throwable e = (Throwable)t.getData();
            if (Parameters.DEBUG) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            else {
                System.err.println(e);
            }

        });

    }



    public void synch() {
        r.getDispatcher().awaitAndShutdown();
    }

    public void shutdown() {
        r.getDispatcher().shutdown();
    }

    /** new Eventer with a dispatcher mode that runs in the same thread */
    public static Eventer newSynchronous() {
        return new Eventer(Reactors.reactor().env(new Environment()).synchronousDispatcher().firstEventRouting().get());
    }


    /*public static Eventer newWorkQueue() {
        return new Eventer(Reactors.reactor(new Environment(), Environment.WORK_QUEUE));
    }*/

    public <X extends Event<?>> Registration on(Selector s, Consumer<X> c) {
        return r.on(s, c);
    }
    




    public void notify(Class channel, Object arg) {
        r.notify(channel, Event.wrap(arg));
    }

    public void notify(E channel) {
        r.notify(channel);
    }
}
