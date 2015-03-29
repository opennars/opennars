/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.core;

import nars.Events;
import nars.event.EventEmitter;
import nars.event.Reaction;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

/**
 * @author me
 */
public class EventTest {


    @Test
    public void testReactor() throws InterruptedException {

        EventEmitter e = EventEmitter.newSynchronous();

        AtomicBoolean b = new AtomicBoolean();


        e.on(Events.CycleEnd.class,new Reaction() {

            @Override
            public void event(Class event, Object[] args) {
                if (event == Events.CycleEnd.class)
                    b.set(true);
            }
        });

        e.notify(Events.CycleEnd.class, new Object[] { true} );


        //Thread.sleep(100);

        e.shutdown();

        assertTrue(b.get());
    }

    @Test
    public void testReactorException() throws InterruptedException {

        AtomicBoolean b = new AtomicBoolean();

        EventEmitter e = new EventEmitter();

        e.on(Events.CycleEnd.class, new Reaction() {
            @Override
            public void event(Class event, Object[] args) {
                throw new RuntimeException("Exception generated for testing purposes; everything is OK");
            }
        });
        e.on(Events.ERR.class, new Reaction() {
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
