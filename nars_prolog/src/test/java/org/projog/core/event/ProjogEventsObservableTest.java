package org.projog.core.event;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ProjogEventsObservableTest {
   @Test
   public void testProjogEventsObservable() {
      ProjogEventsObservable testObject = new ProjogEventsObservable();

      DummyObserver o1 = new DummyObserver();
      DummyObserver o2 = new DummyObserver();
      DummyObserver o3 = new DummyObserver();

      ProjogEvent e1 = createEvent();
      ProjogEvent e2 = createEvent();
      ProjogEvent e3 = createEvent();

      testObject.notifyObservers(e1);

      testObject.addObserver(o1);
      testObject.addObserver(o1);
      testObject.addObserver(o2);
      testObject.addObserver(o3);

      testObject.notifyObservers(e2);

      testObject.deleteObserver(o2);

      testObject.notifyObservers(e3);

      assertEventsUpdated(o1, e2, e3);
      assertEventsUpdated(o2, e2);
      assertEventsUpdated(o3, e2, e3);
   }

   private void assertEventsUpdated(DummyObserver o, ProjogEvent... expectedEvents) {
      final List<Object> actualEvents = o.l;
      assertEquals(expectedEvents.length, actualEvents.size());
      for (int i = 0; i < expectedEvents.length; i++) {
         assertSame(expectedEvents[i], actualEvents.get(i));
      }
   }

   private ProjogEvent createEvent() {
      return new ProjogEvent(null, null, null);
   }

   private static class DummyObserver implements Observer {
      final List<Object> l = new ArrayList<>();

      @Override
      public void update(Observable o, Object arg) {
         l.add(arg);
      }
   }
}