/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nars.core;

import org.junit.Test;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import static reactor.event.selector.Selectors.T;

/**
 *
 * @author me
 */
public class EventTest {

    @Test public void testReactor() throws InterruptedException {
        Environment env = new Environment();
        Reactor r = Reactors.reactor().env(env).dispatcher(Environment.EVENT_LOOP).get();
        
        r.on(T(Events.CycleEnd.class), e -> {
            System.err.println("EVENT: " + e);
        });
        r.notify(T(Events.CycleEnd.class), Event.wrap(true));
        
        r.getDispatcher().shutdown();
        Thread.sleep(100);
    }
}
