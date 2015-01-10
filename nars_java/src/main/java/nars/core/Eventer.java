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
import reactor.event.dispatch.Dispatcher;
import reactor.event.dispatch.SynchronousDispatcher;
import reactor.event.registry.Registration;
import reactor.event.selector.Selector;
import reactor.function.Consumer;

/**
 *
 * @author me
 */
public class Eventer<E> {
    public final Environment env;
    public final Reactor r;

    public Eventer(Reactor r) {
        this(new Environment(), r);
    }
    
    public Eventer(Environment e, Reactor r) {
        this.r = r;
        this.env = e;
    }

    public Eventer(Environment e, Dispatcher d) {
        this(e, Reactors.reactor().env(e).dispatcher(d).get());
    }
    
    public Eventer(String dispatcher) {
        this(Reactors.reactor().env(new Environment()).dispatcher(dispatcher).get());        
    }

    /** new Eventer with a dispatcher mode that runs in the same thread */
    public static Eventer newSynchronous() {
        SynchronousDispatcher d = new SynchronousDispatcher();
        return new Eventer(new Environment(), d);
    }

    /** new Eventer with a dispatcher mode that runs in 1 separate */
    public static Eventer newWorkQueue() {
        return new Eventer(Reactors.reactor(new Environment(), Environment.WORK_QUEUE));
    }

    public Registration on(Selector s, Consumer c) {
        return r.on(s, c);
    }
    


    public void shutdown() {
        r.getDispatcher().shutdown();
    }

    public void emit(Class channel, Object arg) {        
        r.notify(channel, Event.wrap(arg));
    }    
    
}
