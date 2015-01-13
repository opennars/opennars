/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.core;

import org.junit.Ignore;
import org.junit.Test;
import reactor.event.Event;
import reactor.event.registry.Registration;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import static reactor.event.selector.Selectors.T;

/**
 *
 * @author me
 */
public class EventTest {

    
    @Test public void testReactor() throws InterruptedException {
        
        Eventer e = Eventer.newSynchronous();
        
        AtomicBoolean b = new AtomicBoolean();
        
        e.on(T(Events.CycleEnd.class), x -> {
            //System.err.println("EVENT: " + x);
            b.set(true);
        });
        
        e.notify(Events.CycleEnd.class, Event.wrap(true));
        
        
        Thread.sleep(100);

        e.shutdown();
        
        assertTrue(b.get());
    }

    //not working yet:
    @Ignore
    @Test public void testReactorException() throws InterruptedException {

        Parameters.DEBUG = true;

        AtomicBoolean b = new AtomicBoolean();

        try {

            EventEmitter e = new EventEmitter();

            Registration on = e.on(Events.CycleEnd.class, new EventEmitter.EventObserver() {
                @Override public void event(Class event, Object[] args) {
                    throw new RuntimeException("12345");
                }
            });

            e.notify(Events.CycleEnd.class, Event.wrap(true));

            Thread.sleep(100);

            //e.shutdown();
        }
        catch (Exception e) {
            b.set(true);
        }

        assertTrue(b.get());
    }

}
