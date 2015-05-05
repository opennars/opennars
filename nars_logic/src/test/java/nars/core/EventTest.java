/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.core;

import nars.Events;
import nars.util.event.EventEmitter;
import nars.util.event.Reaction;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

/**
 * @author me
 */
public class EventTest {


//    @Test
//    public void testReactor() throws InterruptedException {
//        EventEmitter e = EventEmitter.ReactorEventEmitter.newSynchronous();
//        testEmitter(e);
//    }
//    @Test
//    public void testReactorException() throws InterruptedException {
//        EventEmitter e = EventEmitter.ReactorEventEmitter.newSynchronous();
//        testException(e);
//    }

    @Test
    public void testDefault() throws InterruptedException {
        EventEmitter e = new EventEmitter.DefaultEventEmitter();
        testEmitter(e);
    }

    public void testEmitter(EventEmitter e) {


        AtomicBoolean b = new AtomicBoolean();


        e.on(Events.CycleEnd.class,new Reaction() {

            @Override
            public void event(Class event, Object[] args) {
                if (event == Events.CycleEnd.class)
                    b.set(true);
            }
        });

        e.cycle();

        e.emit(Events.CycleEnd.class);


        assertTrue(b.get());
    }


    public void testException(EventEmitter e) {
        AtomicBoolean b = new AtomicBoolean();


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

        e.cycle();

        e.emit(Events.CycleEnd.class);


        assertTrue(b.get());
    }

}
