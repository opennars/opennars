package org.projog.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.projog.TestUtils.atom;
import static org.projog.TestUtils.integerNumber;
import static org.projog.TestUtils.list;
import static org.projog.TestUtils.structure;
import static org.projog.TestUtils.variable;
import static org.projog.core.KnowledgeBaseUtils.getProjogEventsObservable;
import static org.projog.core.term.TermUtils.createAnonymousVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.junit.Test;
import org.projog.TestUtils;
import org.projog.core.event.ProjogEvent;
import org.projog.core.event.ProjogEventType;
import org.projog.core.term.EmptyList;
import org.projog.core.term.PTerm;

public class SpyPointsTest {
   private final KnowledgeBase kb = TestUtils.createKnowledgeBase();

   @Test
   public void testGetSameSpyPointForSamePredicateKey() {
      SpyPoints testObject = new SpyPoints(kb);
      PredicateKey key = createKey("test", 2);
      assertSame(testObject.getSpyPoint(key), testObject.getSpyPoint(key));
   }

   @Test
   public void testGetSameSpyPointWhenPredicateKeysEqual() {
      SpyPoints testObject = new SpyPoints(kb);
      PredicateKey key1 = createKey("test", 2);
      PredicateKey key2 = createKey("test", 2);
      assertNotSame(key1, key2);
      assertSame(testObject.getSpyPoint(key1), testObject.getSpyPoint(key2));
   }

   @Test
   public void testGetDifferentSpyPointWhenPredicateKeysNotEqual() {
      SpyPoints testObject = new SpyPoints(kb);
      PredicateKey key1 = createKey("test1", 0);
      PredicateKey key2 = createKey("test1", 1);
      PredicateKey key3 = createKey("test1", 2);
      PredicateKey key4 = createKey("test2", 2);
      assertNotSame(testObject.getSpyPoint(key1), testObject.getSpyPoint(key2));
      assertNotSame(testObject.getSpyPoint(key2), testObject.getSpyPoint(key3));
      assertNotSame(testObject.getSpyPoint(key3), testObject.getSpyPoint(key4));
   }

   @Test
   public void testGetSpyPoints() {
      SpyPoints testObject = new SpyPoints(kb);
      PredicateKey[] keys = {createKey("test1", 0), createKey("test1", 2), createKey("test2", 2)};
      SpyPoints.SpyPoint[] spyPoints = new SpyPoints.SpyPoint[keys.length];

      assertTrue(testObject.getSpyPoints().isEmpty());
      for (int i = 0; i < keys.length; i++) {
         spyPoints[i] = testObject.getSpyPoint(keys[i]);
         assertEquals(i + 1, testObject.getSpyPoints().size());
      }

      for (int i = 0; i < keys.length; i++) {
         assertSame(spyPoints[i], testObject.getSpyPoints().get(keys[i]));
      }
   }

   @Test
   public void testSetSpyPointOnlyAltersSingleSpyPoint() {
      SpyPoints testObject = new SpyPoints(kb);
      PredicateKey key1 = createKey("test1", 2);
      PredicateKey key2 = createKey("test2", 2);
      SpyPoints.SpyPoint sp1 = testObject.getSpyPoint(key1);
      SpyPoints.SpyPoint sp2 = testObject.getSpyPoint(key2);

      assertFalse(sp1.isSet());
      assertFalse(sp2.isSet());

      testObject.setSpyPoint(key1, true);
      assertTrue(sp1.isSet());
      assertFalse(sp2.isSet());

      testObject.setSpyPoint(key1, true);
      assertTrue(sp1.isSet());
      assertFalse(sp2.isSet());

      testObject.setSpyPoint(key1, false);
      assertFalse(sp1.isSet());
      assertFalse(sp2.isSet());
   }

   @Test
   public void testSetSpyPointWorksEvenBeforeGetSpyPointCalled() {
      SpyPoints testObject = new SpyPoints(kb);
      PredicateKey key = createKey("test", 2);
      testObject.setSpyPoint(key, true);
      assertTrue(testObject.getSpyPoint(key).isSet());
   }

   @Test
   public void testSpyPointAffectedBySetTraceEnabledCallAfterItWasCreated() {
      SpyPoints testObject = new SpyPoints(kb);
      SpyPoints.SpyPoint sp = testObject.getSpyPoint(createKey("test", 1));
      assertFalse(sp.isEnabled());
      testObject.setTraceEnabled(true);
      assertTrue(sp.isEnabled());
      testObject.setTraceEnabled(false);
      assertFalse(sp.isEnabled());
   }

   @Test
   public void testSpyPointAffectedBySetTraceEnabledCallBeforeItWasCreated() {
      SpyPoints testObject = new SpyPoints(kb);
      testObject.setTraceEnabled(true);
      SpyPoints.SpyPoint sp = testObject.getSpyPoint(createKey("test", 1));
      assertTrue(sp.isEnabled());
      testObject.setTraceEnabled(false);
      assertFalse(sp.isEnabled());
   }

   @Test
   public void testSetTraceEnabledIndependantOfSetSpyPoint() {
      SpyPoints testObject = new SpyPoints(kb);
      PredicateKey key = createKey("test", 2);

      SpyPoints.SpyPoint sp = testObject.getSpyPoint(key);
      assertTrue(sp.isSet() == false && sp.isEnabled() == false);

      testObject.setTraceEnabled(true);
      assertTrue(sp.isSet() == false && sp.isEnabled() == true);

      testObject.setTraceEnabled(false);
      assertTrue(sp.isSet() == false && sp.isEnabled() == false);

      testObject.setSpyPoint(key, true);
      assertTrue(sp.isSet() == true && sp.isEnabled() == true);

      testObject.setTraceEnabled(true);
      assertTrue(sp.isSet() == true && sp.isEnabled() == true);

      testObject.setTraceEnabled(false);
      assertTrue(sp.isSet() == true && sp.isEnabled() == true);

      testObject.setTraceEnabled(true);
      assertTrue(sp.isSet() == true && sp.isEnabled() == true);

      testObject.setSpyPoint(key, false);
      assertTrue(sp.isSet() == false && sp.isEnabled() == true);
   }

   @Test
   public void testSpyPointUpdatesObserver() {
      // add an observer to the KnowledgeBase's ProjogEventsObservable
      // so we can keep track of ProjogEvent objects created by the SpyPoint
      final List<Object> events = new ArrayList<>();
      Observer observer = new Observer() {
         @Override
         public void update(Observable o, Object arg) {
            events.add(arg);
         }
      };
      getProjogEventsObservable(kb).addObserver(observer);

      SpyPoints testObject = new SpyPoints(kb);
      PredicateKey key = createKey("test", 2);
      SpyPoints.SpyPoint sp = testObject.getSpyPoint(key);

      // make a number of log calls to the spy point - 
      // the observer should not be updated with any of them as the spy point is not set
      assertFalse(sp.isSet());
      sp.logCall(this, new PTerm[] {atom("a")});
      sp.logExit(this, new PTerm[] {atom("b")});
      sp.logFail(this, new PTerm[] {atom("c")});
      sp.logRedo(this, new PTerm[] {atom("d")});
      assertTrue(events.isEmpty());

      // set the spy point and then make a number of log calls to the spy point - 
      // the observer should now be updated with each call in the order they are made
      testObject.setSpyPoint(key, true);
      sp.logCall(this, new PTerm[] {atom("z")});
      sp.logExit(this, new PTerm[] {list(atom("a"), variable("X"))});
      sp.logFail(this, new PTerm[] {structure("c", EmptyList.EMPTY_LIST, atom("z"), integerNumber(1))});
      sp.logRedo(this, new PTerm[] {createAnonymousVariable()});
      assertEquals(4, events.size());
      assertProjogEvent(events.get(0), ProjogEventType.CALL, "test( z )");
      assertProjogEvent(events.get(1), ProjogEventType.EXIT, "test( [a,X] )");
      assertProjogEvent(events.get(2), ProjogEventType.FAIL, "test( c([], z, 1) )");
      assertProjogEvent(events.get(3), ProjogEventType.REDO, "test( _ )");
   }

   private void assertProjogEvent(Object o, ProjogEventType t, String message) {
      assertSame(ProjogEvent.class, o.getClass());
      ProjogEvent e = (ProjogEvent) o;
      assertSame(t, e.getType());
      assertEquals(message, e.getMessage());
      assertSame(this, e.getSource());
   }

   private PredicateKey createKey(String name, int numArgs) {
      PTerm arity = structure("/", atom(name), integerNumber(numArgs));
      return PredicateKey.createFromNameAndArity(arity);
   }
}