/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.core;

import nars.io.Output;
import org.junit.Test;
import reactor.event.Event;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;
import static reactor.event.selector.Selectors.T;

/**
 * @author me
 */
public class EventTest {


    @Test
    public void testReactor() throws InterruptedException {

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

    @Test
    public void testReactorException() throws InterruptedException {

        AtomicBoolean b = new AtomicBoolean();

        EventEmitter e = new EventEmitter();

        e.on(Events.CycleEnd.class, new EventEmitter.EventObserver() {
            @Override
            public void event(Class event, Object[] args) {
                throw new RuntimeException("12345");
            }
        });
        e.on(Output.ERR.class, new EventEmitter.EventObserver() {
            @Override
            public void event(Class event, Object[] args) {
                b.set(true);
            }
        });
        e.notify(Events.CycleEnd.class);

        Thread.sleep(100);


        assertTrue(b.get());
    }

}
